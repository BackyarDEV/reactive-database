package com.backyardev.reactivedatabase.repository;

import com.backyardev.reactivedatabase.model.Organization;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface OrganizationRepository extends ReactiveCrudRepository<Organization, Integer> {

    Mono<Organization> findByOrgName(String orgName);
}
