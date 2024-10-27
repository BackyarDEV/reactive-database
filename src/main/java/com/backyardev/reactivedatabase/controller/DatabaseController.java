package com.backyardev.reactivedatabase.controller;

import com.backyardev.reactivedatabase.dao.OrganizationDAO;
import com.backyardev.reactivedatabase.dao.UserContactDAO;
import com.backyardev.reactivedatabase.dao.UserDAO;
import com.backyardev.reactivedatabase.exception.ReactiveAppException;
import com.backyardev.reactivedatabase.model.Organization;
import com.backyardev.reactivedatabase.model.User;
import com.backyardev.reactivedatabase.model.UserContact;
import com.backyardev.reactivedatabase.service.DatabaseService;
import com.fasterxml.jackson.databind.JsonNode;
import io.smallrye.mutiny.Uni;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/db-api")
public class DatabaseController {


    @Autowired
    private UserDAO userDAO;

    @Autowired
    private OrganizationDAO organizationDAO;

    @Autowired
    private UserContactDAO userContactDAO;

    @Autowired
    private DatabaseService databaseService;

    @GetMapping(path = "/users")
    public Uni<List<User>> getUsers() {
        return userDAO.getUsers();
    }

    @GetMapping(path = "/users/{username}")
    public Uni<User> getUserByName(@PathVariable(value = "username") String username) {
        return userDAO.findByUsername(username);
    }

    @GetMapping(path = "/organizations")
    public Uni<List<Organization>> getOrganizations() {
        return organizationDAO.getOrganizations();
    }

    @GetMapping(path = "/organizations/{orgName}")
    public Uni<Organization> getOrganizationByName(@PathVariable(value = "orgName") String orgName) {
        return databaseService.getOrgIdFromOrgName(orgName);
    }

    @GetMapping(path = "/user-contacts")
    public Uni<List<UserContact>> getUserContacts() {
        return userContactDAO.getUserContacts();
    }

    @GetMapping(path = "/user-contacts/{userId}")
    public Uni<List<UserContact>> getUserContactsByName(@PathVariable(value = "userId") Integer userId) throws ReactiveAppException {
        return userContactDAO.findByUserId(userId);
    }

    @GetMapping(path = "/users-with-contacts/{username}")
    public Mono<JsonNode> getUserWithContactsFromUsername(@PathVariable(value = "username") String username) throws ReactiveAppException {
        return databaseService.getUserWithContactsFromUsername(username);
    }

    @GetMapping(path = "/org-with-users/{orgName}")
    public Mono<JsonNode> getOrganizationWithUsersFromOrgName(@PathVariable(value = "orgName") String orgName) {
        return databaseService.getOrganizationWithUsersFromOrgName(orgName);
    }

    @PostMapping(path = "/user-contacts",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<JsonNode> addUserContacts(
            @RequestHeader("Authorization") String auth,
            @RequestBody JsonNode requestNode
    ) throws ReactiveAppException {
        return databaseService.addUserContacts(auth, requestNode);
    }
}
