package com.authentication.AuthProject.repository;

import com.authentication.AuthProject.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String email);
//
//    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsByEmail(@NotBlank(message = "Email is required") String email);

    boolean existsByPhoneNumber(@NotBlank(message = "Phone number is required") String phoneNumber);

}

