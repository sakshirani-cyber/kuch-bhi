package com.authentication.AuthProject.entity;


import com.authentication.AuthProject.enums.Gender;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false)
    private LocalDate dob;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false, unique = true, length = 100 )
    private String email;

    @Column(nullable = false, unique = true, length = 15)
    private String phoneNumber;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;


    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        age = Period.between(dob, LocalDate.now()).getYears();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
        age = Period.between(dob, LocalDate.now()).getYears();
    }
}
