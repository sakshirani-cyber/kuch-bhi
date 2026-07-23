package com.example.myProject.repository;

import com.example.myProject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // JPA derived methods — login + registration only
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Native queries — fetch / update / delete
    @Query(value = "SELECT * FROM users WHERE email = :email", nativeQuery = true)
    Optional<User> findUserDetailsByEmail(@Param("email") String email);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE users SET password = :password WHERE email = :email", nativeQuery = true)
    int updatePassword(@Param("email") String email, @Param("password") String password);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE users SET username = :username WHERE email = :email", nativeQuery = true)
    int updateUsernameByEmail(@Param("email") String email, @Param("username") String username);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "DELETE FROM users WHERE email = :email", nativeQuery = true)
    int deleteUserByEmail(@Param("email") String email);
}
