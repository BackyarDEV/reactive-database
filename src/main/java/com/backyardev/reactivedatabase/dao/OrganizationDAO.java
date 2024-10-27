package com.backyardev.reactivedatabase.dao;

import com.backyardev.reactivedatabase.model.Organization;
import com.backyardev.reactivedatabase.repository.OrganizationRepository;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class OrganizationDAO {

    @Autowired
    private OrganizationRepository repository;

    public Uni<Organization> findByOrgName(String orgName) {
        return repository.findByOrgName(orgName);
    }

    public Uni<List<Organization>> getOrganizations() {
        return repository.findAll();
    }
}
