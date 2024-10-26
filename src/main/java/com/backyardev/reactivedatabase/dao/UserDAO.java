package com.backyardev.reactivedatabase.dao;

import com.backyardev.reactivedatabase.model.User;
import com.backyardev.reactivedatabase.model.UserContact;
import com.backyardev.reactivedatabase.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class UserDAO {

    @Autowired
    private UserRepository repository;

    @Autowired
    private UserContactDAO userContactDAO;

    public Mono<User> findByUsername(String username) {
        return repository.findByUsername(username)
                .doOnSuccess(fetched -> log.info("Fetched user for the username: {}", username))
                .doOnError(ex -> log.error("Error occurred while fetching user with username={} - {}",
                        username, ex.getMessage()));
    }

    @Transactional
    public Mono<User> findByUsernameAndUpsert(String username, Integer organizationId, UserContact userContact) {
        return repository.findByUsernameAndOrganizationId(username, organizationId)
                .doOnSuccess(fetched -> {
                    log.info("Fetched user for the username: {}", username);
                    var userId = fetched.getId();
                    userContact.setUserId(userId);
                    var contactType = userContact.getContactType();
                    userContactDAO.findByContactTypeAndUserId(contactType, userId)
                            .map(found -> userContactDAO.upsert(found.updateContactValue(userContact))
                                    .doOnSuccess(updated -> log.info("{} updated for {}", updated.getContactType(), username))
                                    .doOnError(ex -> log.error("Error occurred while upserting {} for {}", contactType, username))
                                    .toFuture().join()
                            )
                            .switchIfEmpty(Mono.just(userContactDAO.upsert(userContact).toFuture().join()))
                            .doOnSuccess(updated -> log.info("{} updated for {}", updated.getContactType(), username))
                            .doOnError(ex -> log.error("Error occurred while trying to find {} for {}", contactType, username));
                })
                .doOnError(ex -> log.error("Error occurred while fetching user with username={} - {}",
                        username, ex.getMessage()));
    }

    public Flux<User> getUsers() {
        return repository.findAll()
                .doOnComplete(() -> log.info("Fetched all the users!"))
                .doOnError(ex -> log.error("Error occurred while fetching all the users - {}",
                        ex.getMessage(), ex));
    }

    public Flux<User> findByOrgId(Integer organizationId) {
        return repository.findByOrganizationId(organizationId)
                .map(user -> {
                    log.info("Fetched user {} for the orgId: {}", user.getUsername(), organizationId);
                    return user;
                })
                .doOnError(ex -> log.error("Error occurred while fetching organization contacts with userId={} - {}",
                        organizationId, ex.getMessage(), ex));
    }
}
