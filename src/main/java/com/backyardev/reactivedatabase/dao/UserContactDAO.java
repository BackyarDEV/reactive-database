package com.backyardev.reactivedatabase.dao;

import com.backyardev.reactivedatabase.exception.ReactiveAppException;
import com.backyardev.reactivedatabase.model.UserContact;
import com.backyardev.reactivedatabase.repository.UserContactRepository;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UserContactDAO {

    @Autowired
    private UserContactRepository repository;

    public Uni<List<UserContact>> findByUserId(Integer userId) throws ReactiveAppException {
        if (userId == null)
            throw new ReactiveAppException("userId can not be null!!!");
        return repository.findByUserId(userId);
    }

    public Uni<UserContact> findByContactTypeAndUserId(String contactType, Integer userId) {
        return repository.findByContactTypeAndUserId(contactType, userId);
    }

    public Uni<List<UserContact>> getUserContacts() {
        return repository.findAll();
    }

    public Uni<UserContact> upsert(UserContact userContact) {
        return repository.upsert(userContact);
    }

    public Uni<Integer> delete(UserContact userContact) {
        return repository.delete(userContact);
    }
}
