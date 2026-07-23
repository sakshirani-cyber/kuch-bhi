package com.example.myProject.repository;

import com.example.myProject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Transactional
    @Query(value = "SELECT * FROM users WHERE email = :email", nativeQuery = true)
    Optional<User> findByEmail(@Param("email") String email);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM users WHERE email = :email)", nativeQuery = true)
    boolean existsByEmail(@Param("email") String email);

    @Query(value = "SELECT * FROM users WHERE user_id = :id", nativeQuery = true)
    Optional<User> findUserById(@Param("id") Long id);

    @Modifying
    @Query(value = """
            INSERT INTO users (username, email, password, contact_number, date_of_birth, age)
            VALUES (:username, :email, :password, :contactNumber, :dateOfBirth, :age)
            """, nativeQuery = true)
    int insertUser(@Param("username") String username,
                   @Param("email") String email,
                   @Param("password") String password,
                   @Param("contactNumber") String contactNumber,
                   @Param("dateOfBirth") LocalDate dateOfBirth,
                   @Param("age") Integer age);

    @Modifying
    @Query(value = "UPDATE users SET username = :username WHERE user_id = :id", nativeQuery = true)
    int updateUsernameById(@Param("id") Long id, @Param("username") String username);

    @Modifying
    @Query(value = "UPDATE users SET password = :password WHERE user_id = :id", nativeQuery = true)
    int updatePasswordById(@Param("id") Long id, @Param("password") String password);
}
