package com.system.auth_service.repository;

import com.system.auth_service.model.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository {

    private final MongoTemplate usersMongoTemplate;

    public UserRepository(@Qualifier("usersMongoTemplate") MongoTemplate usersMongoTemplate) {
        this.usersMongoTemplate = usersMongoTemplate;
    }

    public Optional<User> findByPhoneNumber(String phoneNumber) {
        Query query = Query.query(Criteria.where("phoneNumber").is(phoneNumber));
        return Optional.ofNullable(usersMongoTemplate.findOne(query, User.class));
    }

    public boolean existsByPhoneNumber(String phoneNumber) {
        Query query = Query.query(Criteria.where("phoneNumber").is(phoneNumber));
        return usersMongoTemplate.exists(query, User.class);
    }

    public User save(User user) {
        return usersMongoTemplate.save(user);
    }

    public Optional<User> findById(String id) {
        return Optional.ofNullable(usersMongoTemplate.findById(id, User.class));
    }
}
