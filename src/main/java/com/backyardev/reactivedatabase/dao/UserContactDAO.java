package com.backyardev.reactivedatabase.dao;

import com.backyardev.reactivedatabase.exception.ReactiveAppException;
import com.backyardev.reactivedatabase.model.UserContact;
import com.backyardev.reactivedatabase.repository.UserContactRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class UserContactDAO {

    @Autowired
    private UserContactRepository repository;

    public Flux<UserContact> findByUserId(Integer userId) throws ReactiveAppException {
        if (userId == null)
            throw new ReactiveAppException("userId can not be null!!!");
        return repository.findByUserId(userId)
                .map(userContact -> {
                    log.info("Fetched user {} for the userId: {}", userContact.getContactType(), userId);
                    return userContact;
                })
                .doOnError(ex -> log.error("Error occurred while fetching user contacts with userId={} - {}",
                        userId, ex.getMessage(), ex)
                );
    }

    public Mono<UserContact> findByContactTypeAndUserId(String contactType, Integer userId) {
        return repository.findByContactTypeAndUserId(contactType, userId);
    }

    public Flux<UserContact> getUserContacts() {
        return repository.findAll()
                .doOnComplete(() -> log.info("Fetched all the user contacts!"))
                .doOnError(ex -> log.error("Error occurred while fetching all the user contacts- {}",
                        ex.getMessage(), ex));
    }

    public Mono<UserContact> upsert(UserContact userContact) {
        return repository.findByContactTypeAndUserId(userContact.getContactType(), userContact.getUserId())
                .flatMap(saved -> repository.save(saved.updateContactValue(userContact)))
                .switchIfEmpty(repository.save(userContact))
                .doOnSuccess(upserted -> log.info("User contact upserted for the entry with id: {}", upserted.getId()))
                .doOnError(ex -> log.error("Error occurred while upserting user contact {}", ex.getMessage(), ex));
    }

    public Mono<Void> delete(UserContact userContact) {
        return repository.delete(userContact);
    }
}
