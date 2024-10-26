package com.backyardev.reactivedatabase.repository;

import com.backyardev.reactivedatabase.model.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, Integer> {

    Mono<User> findByUsername(String username);

    Flux<User> findByOrganizationId(Integer organizationId);

    Mono<User> findByUsernameAndOrganizationId(String username, Integer organizationId);

}
