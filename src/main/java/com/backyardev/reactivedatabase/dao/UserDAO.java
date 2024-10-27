package com.backyardev.reactivedatabase.dao;

import com.backyardev.reactivedatabase.model.User;
import com.backyardev.reactivedatabase.repository.UserRepository;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UserDAO {

    @Autowired
    private UserRepository repository;

    @Autowired
    private UserContactDAO userContactDAO;

    public Uni<User> findByUsername(String username) {
        return repository.findByUsername(username)
                .map(fetched -> {
                    log.info("Fetched user for the username: {}", username);
                    return fetched;
                });
    }

    public Uni<List<User>> getUsers() {
        return repository.findAll().map(users -> users);
    }

    public Uni<List<User>> findByOrgId(Integer organizationId) {
        return repository.findByOrganizationId(organizationId).map(user -> user);
    }
}
