package com.preeti.authenticationdemo.repository;

import com.preeti.authenticationdemo.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    // Written manually with @Query instead of relying on Spring Data's
    // method-name-derived query generation (e.g. findByUsername(...)).

    @Query("{ 'username' : ?0 }")
    Optional<User> findByUsernameManual(String username);

    @Query("{ 'email' : ?0 }")
    Optional<User> findByEmailManual(String email);

    @Query("{ 'phoneNumber' : ?0 }")
    Optional<User> findByPhoneNumberManual(String phoneNumber);

}
