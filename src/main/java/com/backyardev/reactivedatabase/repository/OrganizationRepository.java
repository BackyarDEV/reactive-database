package com.backyardev.reactivedatabase.repository;

import com.backyardev.reactivedatabase.exception.ReactiveAppException;
import com.backyardev.reactivedatabase.model.Organization;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import org.hibernate.reactive.mutiny.Mutiny;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrganizationRepository {

    private final Mutiny.SessionFactory sessionFactory;

    public Uni<List<Organization>> findAll() {
        var cb = this.sessionFactory.getCriteriaBuilder();
        var cq = cb.createQuery(Organization.class);
        var root = cq.from(Organization.class);
        return this.sessionFactory.withSession(session -> session.createQuery(cq).getResultList());
    }

    public Uni<Organization> findByOrgName(String orgName) {
        var cb = this.sessionFactory.getCriteriaBuilder();
        var cq = cb.createQuery(Organization.class);
        var root = cq.from(Organization.class);


        if (orgName != null) {
            cq.where(cb.equal(root.get("orgName"), orgName));
        }

        return this.sessionFactory.withSession(session -> session
                        .createQuery(cq).getSingleResult()
                        .call(org -> Mutiny.fetch(org.getUsers()))
                )
                .onItem().ifNull()
                .failWith(() -> new ReactiveAppException(String.format("Error occurred while fetching Organization with orgName: %s", orgName)));

    }
}
