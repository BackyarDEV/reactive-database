package com.backyardev.reactivedatabase.repository;

import com.backyardev.reactivedatabase.exception.ReactiveAppException;
import com.backyardev.reactivedatabase.model.UserContact;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.reactive.mutiny.Mutiny;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserContactRepository {

    private final Mutiny.SessionFactory sessionFactory;

    public Uni<List<UserContact>> findAll() {
        var cb = this.sessionFactory.getCriteriaBuilder();
        var cq = cb.createQuery(UserContact.class);
        var root = cq.from(UserContact.class);
        return this.sessionFactory.withSession(session -> session.createQuery(cq).getResultList());
    }

    public Uni<List<UserContact>> findByUserId(Integer userId) {

        var cb = this.sessionFactory.getCriteriaBuilder();
        var cq = cb.createQuery(UserContact.class);
        var root = cq.from(UserContact.class);

        if (userId != null) {
            cq.where(cb.equal(root.get("user").get("id"), userId));
        }

        return this.sessionFactory.withSession(session -> session.createQuery(cq).getResultList())
                .onItem().ifNull()
                .failWith(() -> new ReactiveAppException(String.format("Error occurred while fetching User Contacts with userId: %s", userId)));
    }

    public Uni<UserContact> findByContactTypeAndUserId(String contactType, Integer userId) {
        var cb = this.sessionFactory.getCriteriaBuilder();
        var cq = cb.createQuery(UserContact.class);
        var root = cq.from(UserContact.class);

        if (contactType != null && userId != null) {
            cq.where(cb.and(
                            cb.equal(root.get("contactType"), contactType),
                            cb.equal(root.get("user").get("id"), userId)
                    )
            );
        }

        return this.sessionFactory.withSession(session -> session.createQuery(cq).getSingleResult())
                .onItem().ifNull()
                .failWith(() -> new ReactiveAppException(
                        String.format(
                                """
                                Error occurred while fetching User Contacts with contactType: %s and userId: %s
                                """,
                                contactType, userId))
                );
    }

    public Uni<Integer> delete(UserContact userContact) {
        var cb = this.sessionFactory.getCriteriaBuilder();
        var delete = cb.createCriteriaDelete(UserContact.class);
        var root = delete.from(UserContact.class);
        delete.where(cb.equal(root.get("id"), userContact.getId()));
        return this.sessionFactory.withTransaction((session, tx) ->
                session.createQuery(delete).executeUpdate()
        );
    }

    public Uni<UserContact> upsert(UserContact userContact) {
        if (userContact.getId() == null) {
            log.debug("UserContact has null id! Persisting new user contact.");
            return this.sessionFactory.withSession(session ->
                    session.persist(userContact)
                            .chain(session::flush)
                            .replaceWith(userContact)
            );
        } else {
            log.debug("UserContact has valid id! Merging existing user contact.");
            return this.sessionFactory.withSession(session -> session.merge(userContact).onItem().call(session::flush));
        }
    }

}
