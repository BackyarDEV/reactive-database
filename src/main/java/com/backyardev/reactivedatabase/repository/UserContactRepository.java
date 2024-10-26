package com.backyardev.reactivedatabase.repository;

import com.backyardev.reactivedatabase.model.UserContact;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserContactRepository extends ReactiveCrudRepository<UserContact, Integer> {

    Flux<UserContact> findByUserId(Integer userId);

    Mono<UserContact> findByContactTypeAndUserId(String contactType, Integer userId);
}
