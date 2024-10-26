package com.backyardev.reactivedatabase.dao;

import com.backyardev.reactivedatabase.model.Organization;
import com.backyardev.reactivedatabase.repository.OrganizationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class OrganizationDAO {

    @Autowired
    private OrganizationRepository repository;

    public Mono<Organization> findByOrgName(String orgName) {
        return repository.findByOrgName(orgName)
                .doOnSuccess( organization -> log.info("Fetched Organization for the orgName={}", organization.getOrgName()))
                .doOnError(ex -> log.error("Error occurred while fetching organization with orgName={} - {}",
                        orgName, ex.getMessage()));
    }

    public Flux<Organization> getOrganizations() {
        return repository.findAll()
                .doOnComplete(() -> log.info("Fetched all Organizations"))
                .doOnError(ex -> log.error("Error occurred while fetching all organizations"));
    }
}
