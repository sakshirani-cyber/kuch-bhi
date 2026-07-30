package com.authentication.AuthProject.repository;

import com.authentication.AuthProject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u.email FROM User u WHERE u.id = :id")
    Optional<String> findEmailById(@Param("id") Long id);
    // made these field into one entity
//
//    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumberHash(String phoneNumberHash);

}

