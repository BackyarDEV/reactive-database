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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

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

    @Autowired
    R2dbcEntityTemplate template;

    public Mono<Organization> getOrgIdFromOrgName(String orgName) {
        return template
                .select(Organization.class)
                .from(Organization.SCHEMA.concat(".").concat(Organization.TABLE_NAME))
                .matching(query(where("org_name").is(orgName)).columns("id"))
                .one();
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
                .map(userContact -> {
                    log.info("Fetched {} for user: {}", userContact.getContactType(), username);
                    return mapper.convertValue(userContact, JsonNode.class);
                })
                .collectList()
                .map(contacts::addAll)
                .toFuture()
                .join();

    }

    private CompletableFuture<User> getUserFuture(String username) {
        return userDAO.findByUsername(username)
                .toFuture();
    }

    private CompletableFuture<User> getUserContactUpsertFuture(String username, Integer organizationId, UserContact userContact) {
        return userDAO.findByUsernameAndUpsert(username, organizationId, userContact)
                .toFuture();
    }

    private CompletableFuture<List<UserContact>> getUserContactsFuture(Integer userId) throws ReactiveAppException {
        return userContactDAO.findByUserId(userId)
                .collectList()
                .toFuture().completeOnTimeout(null, 10, TimeUnit.SECONDS);
    }

    public Mono<JsonNode> getOrganizationWithUsersFromOrgName(String orgName) {

        var organizationFuture = getOrganizationFuture(orgName);
        var organization = organizationFuture.join();

        if (organizationFuture.isDone() && organization != null) {
            Integer orgId = organization.getId();
            log.info("Fetching users for the orgId: {}", orgId);
            var usersFuture = getOrgUsersFuture(orgId);
            var users = usersFuture.join();
            if (usersFuture.isDone() && users != null) {
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
                .toFuture().completeOnTimeout(null, 10, TimeUnit.SECONDS);
    }

    private CompletableFuture<List<User>> getOrgUsersFuture(Integer orgId) {
        return userDAO.findByOrgId(orgId)
                .collectList()
                .toFuture().completeOnTimeout(null, 30, TimeUnit.SECONDS);
    }

    private Mono<JsonNode> timedOutResponse(String tableName, String element, String query) {
        var responseNode = JsonNodeFactory.instance.objectNode();
        responseNode.put("errorMessage", String.format("Timed out while trying to query the table: %s for ", tableName));
        responseNode.put("element", element);
        responseNode.put("query", query);
        return Mono.just(responseNode);
    }

    public Mono<Void> addUserContactsFromTransaction(String auth, JsonNode requestNode) {
        var orgName = getOrgNameFromToken(auth);
        var contactType = requestNode.at("/user/contact/type").asText();
        var contactValue = requestNode.at("/user/contact/value").asText();
        var username = requestNode.at("/user/name").asText();
        var orgId = requestNode.at("/user/orgId").asInt();

        DatabaseClient db = template.getDatabaseClient();
        var transactionalOperator = TransactionalOperator
                .create(new R2dbcTransactionManager(db.getConnectionFactory()));

        assert orgName != null;
        template.select(Organization.class)
                .from(Organization.SCHEMA.concat(".").concat(Organization.TABLE_NAME))
                .matching(query(where("org_name").is(orgName)))
                .one()
                .map(org -> template.select(User.class)
                        .from(User.SCHEMA.concat(".").concat(User.TABLE_NAME))
                        .matching(query(where("org_id").is(org.getId())))
                        .one()
                        .map(user -> template.select(UserContact.class)
                                .from(UserContact.SCHEMA.concat(".").concat(UserContact.TABLE_NAME))
                                .matching(query(where("user_id").is(user.getId())))
                                .one()
                                .then()
                                .as(transactionalOperator::transactional)
                        )
                );

        return db.sql("SELECT * FROM ")
                .fetch().rowsUpdated()
                .then(db.sql("")
                        .then())
                .as(transactionalOperator::transactional);
    }

    @Transactional
    public Mono<JsonNode> addUserContacts(String auth, JsonNode requestNode) throws ReactiveAppException {

        Mono<JsonNode> response = Mono.just(JsonNodeFactory.instance.objectNode());
        var orgName = getOrgNameFromToken(auth);
        var contactType = requestNode.at("/user/contact/type").asText();
        var contactValue = requestNode.at("/user/contact/value").asText();
        var username = requestNode.at("/user/name").asText();
        var organizationId = requestNode.at("/user/orgId").asInt();
        var userContact = new UserContact();
        userContact.setContactType(contactType);
        userContact.setContactValue(contactValue);

        var orgFuture = getOrganizationFuture(orgName);
        var org = orgFuture.join();
        if (orgFuture.isDone() && org != null &&  org.getId().equals(organizationId)) {
            var userFuture = getUserFuture(username);
            var user = userFuture.join();
            if (userFuture.isDone() && user != null) {
                Integer userId = user.getId();
                userContact.setUserId(userId);
                var saveTask = userContactDAO.upsert(userContact).toFuture();
                var savedContact = saveTask.join();
                if (saveTask.isDone())
                    response = userContactAddedResponse(userContact);
                else errorResponseNode(String.format("Error occurred while upserting %s for user: %s", contactType, username));
            } else errorResponseNode(String.format("Error occurred while upserting %s for user: %s", contactType, username));
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
