package com.backyardev.reactivedatabase.repository;

import com.backyardev.reactivedatabase.exception.ReactiveAppException;
import com.backyardev.reactivedatabase.model.User;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import org.hibernate.reactive.mutiny.Mutiny;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserRepository {

    private final Mutiny.SessionFactory sessionFactory;

    public Uni<User> findByUsername(String username) {
        var cb = this.sessionFactory.getCriteriaBuilder();
        var cq = cb.createQuery(User.class);
        var root = cq.from(User.class);

        if (username != null) {
            cq.where(cb.equal(root.get("username"), username));
        }

        return this.sessionFactory.withSession(session -> session
                        .createQuery(cq).getSingleResult()
                        .call(user -> Mutiny.fetch(user.getContacts()))
                )
                .onItem().ifNull()
                .failWith(() -> new ReactiveAppException(String.format("Error occurred while fetching User with username: %s", username)));
    }

    public Uni<List<User>> findByOrganizationId(Integer organizationId) {

        var cb = this.sessionFactory.getCriteriaBuilder();
        var cq = cb.createQuery(User.class);
        var root = cq.from(User.class);

        if (organizationId != null) {
            cq.where(cb.equal(root.get("organization").get("id"), organizationId));
        }

        return this.sessionFactory.withSession(session -> session.createQuery(cq).getResultList())
                .onItem()
                .ifNull()
                .failWith(() -> new ReactiveAppException(String.format("Error occurred while fetching Users with orgId: %s", organizationId)));
    }

    public Uni<User> findByUsernameAndOrganizationId(String username, Integer organizationId) {
        var cb = this.sessionFactory.getCriteriaBuilder();
        var cq = cb.createQuery(User.class);
        var root = cq.from(User.class);

        if (organizationId != null && username != null) {
            cq.where(cb.and(
                    cb.equal(root.get("username"), username),
                    cb.equal(root.get("organization").get("id"), organizationId)
                    )
            );
        }

        return this.sessionFactory.withSession(session -> session.createQuery(cq).getSingleResult())
                .onItem().ifNull()
                .failWith(() -> new ReactiveAppException(String.format("Error occurred while fetching Users with orgId: %s", organizationId)));
    }

    public Uni<List<User>> findAll() {
        var cb = this.sessionFactory.getCriteriaBuilder();
        var cq = cb.createQuery(User.class);
        var root = cq.from(User.class);
        return this.sessionFactory.withSession(session -> session.createQuery(cq).getResultList());
    }
}
