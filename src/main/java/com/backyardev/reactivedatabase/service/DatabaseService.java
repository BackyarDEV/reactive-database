package com.backyardev.reactivedatabase.service;

import com.backyardev.reactivedatabase.dao.OrganizationDAO;
import com.backyardev.reactivedatabase.dao.UserContactDAO;
import com.backyardev.reactivedatabase.dao.UserDAO;
import com.backyardev.reactivedatabase.exception.ReactiveAppException;
import com.backyardev.reactivedatabase.model.Organization;
import com.backyardev.reactivedatabase.model.User;
import com.backyardev.reactivedatabase.model.UserContact;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.smallrye.mutiny.Uni;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
public class DatabaseService {

    @Autowired
    private UserDAO userDAO;
    @Autowired
    private OrganizationDAO organizationDAO;
    @Autowired
    private UserContactDAO userContactDAO;
    private static final ObjectMapper mapper = new ObjectMapper();

    public Uni<Organization> getOrgIdFromOrgName(String orgName) {
        return organizationDAO.findByOrgName(orgName);
    }

    public Mono<JsonNode> getUserWithContactsFromUsername(String username) throws ReactiveAppException {
        var userFuture = getUserFuture(username);
        var user = userFuture.join();
        var response = mapper.createObjectNode();
        if (userFuture.isDone() && user != null) {
            response = mapper.convertValue(user, ObjectNode.class);
            response.set("contacts", fetchAndAddContacts(user));
        }
        return Mono.just(response);
    }

    private ArrayNode fetchAndAddContacts(User user) throws ReactiveAppException {
        var contacts = mapper.createArrayNode();
        var userId = user.getId();
        var username = user.getUsername();
        return userContactDAO.findByUserId(userId)
                .map(userContact -> mapper.convertValue(userContact, ArrayNode.class))
                .subscribeAsCompletionStage()
                .join();

    }

    private CompletableFuture<User> getUserFuture(String username) {
        return userDAO.findByUsername(username).subscribeAsCompletionStage();
    }

    private CompletableFuture<List<UserContact>> getUserContactsFuture(Integer userId) throws ReactiveAppException {
        return userContactDAO.findByUserId(userId)
                .subscribeAsCompletionStage()
                .completeOnTimeout(null, 10, TimeUnit.SECONDS);
    }

    public Mono<JsonNode> getOrganizationWithUsersFromOrgName(String orgName) {

        var organizationFuture = getOrganizationFuture(orgName);
        var organization = organizationFuture.join();

        if (organizationFuture.isDone() && organization != null) {
            Integer orgId = organization.getId();
            log.info("Fetching users for the orgId: {}", orgId);
            var users = organization.getUsers();
            if (users != null) {
                ObjectNode orgNode = JsonNodeFactory.instance.objectNode();
                orgNode.put("id", orgId);
                orgNode.put("username", organization.getOrgName());
                orgNode.set("users", mapper.convertValue(users, ArrayNode.class));
                return Mono.just(orgNode);
            } else return timedOutResponse(User.TABLE_NAME, orgId.toString(), "findByOrgId");
        } else return timedOutResponse(Organization.TABLE_NAME, orgName, "findByOrgName");
    }

    private CompletableFuture<Organization> getOrganizationFuture(String orgName) {
        return organizationDAO.findByOrgName(orgName)
                .subscribeAsCompletionStage()
                .completeOnTimeout(null, 10, TimeUnit.SECONDS);
    }

    private CompletableFuture<List<User>> getOrgUsersFuture(Integer orgId) {
        return userDAO.findByOrgId(orgId)
                .subscribeAsCompletionStage();
    }

    private Mono<JsonNode> timedOutResponse(String tableName, String element, String query) {
        var responseNode = JsonNodeFactory.instance.objectNode();
        responseNode.put("errorMessage", String.format("Timed out while trying to query the table: %s for ", tableName));
        responseNode.put("element", element);
        responseNode.put("query", query);
        return Mono.just(responseNode);
    }

    @Transactional
    public Mono<JsonNode> addUserContacts(String auth, JsonNode requestNode) throws ReactiveAppException {

        Mono<JsonNode> response = Mono.just(JsonNodeFactory.instance.objectNode());
        var orgName = getOrgNameFromToken(auth);
        var contactType = requestNode.at("/user/contact/type").asText();
        var contactValue = requestNode.at("/user/contact/value").asText();
        var username = requestNode.at("/user/name").asText();
        var organizationId = requestNode.at("/user/orgId").asInt();

        var orgFuture = getOrganizationFuture(orgName);
        var org = orgFuture.join();
        if (orgFuture.isDone() && org != null &&  org.getId().equals(organizationId)) {
            var users = org.getUsers();
            log.info("Fetched {} users for organization.", users.size());
            var filteredUsers = users.stream().filter(u -> u.getUsername().equals(username)).toList();
            User user;
            if (filteredUsers.size() == 1) {
                user = filteredUsers.getFirst();
                log.info("Found user with username: {}", username);
                var contacts = user.getContacts();
                log.info("Fetched {} contacts for user with username: {}", contacts.size(), user.getUsername());
                var filteredContacts = contacts.stream()
                        .filter(uc -> uc.getContactType().equals(contactType)).toList();
                UserContact userContact;
                if (filteredContacts.isEmpty())
                    userContact = new UserContact(null, contactType, contactValue, user);
                else
                    userContact = filteredContacts.getFirst();
                var saveTask = userContactDAO.upsert(userContact)
                        .subscribeAsCompletionStage();
                var savedContact = saveTask.join();
                if (saveTask.isDone())
                    response = userContactAddedResponse(userContact);
                else errorResponseNode(String.format("Error occurred while upserting %s for user: %s", contactType, username));
            } else errorResponseNode(String.format("No User found by the name: %s", username));
        } else errorResponseNode("Error occurred while fetching Organization with orgName: " + orgName);
        return response;
    }

    private static Mono<JsonNode> userContactAddedResponse(UserContact savedContact) {
        var responseNode = JsonNodeFactory.instance.objectNode();
        responseNode.put("status", "SUCCESS");
        responseNode.put("message", "User Contact added successfully!");
        responseNode.set("details", mapper.convertValue(savedContact, JsonNode.class));
        return Mono.just(responseNode);
    }

    private String getOrgNameFromToken(String auth) {
        if ("INTERNAL_ID".equals(auth)) {
            return "SpaceJam";
        }
        return null;
    }

    private void errorResponseNode(String message) throws ReactiveAppException {
        throw new ReactiveAppException(HttpStatus.BAD_REQUEST, message);
    }
}
