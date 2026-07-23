package com.preeti.authenticationdemo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * NOTE: MongoDB has no equivalent of JPA's @GeneratedValue (that
     * annotation only exists for relational databases with auto-increment
     * sequences). With Spring Data MongoDB, leaving this field null when
     * calling mongoTemplate.insert(...) is already enough — MongoDB
     * generates a unique ObjectId for it automatically. No extra
     * annotation is needed or available for this.
     */
    @Id
    private String id;

    private String username;

    private String password;

    private String email;

    private String phoneNumber;

    private LocalDate dateOfBirth;

    // Computed once at signup from dateOfBirth and stored directly,
    // instead of being derived on every read.
    private Integer age;

}
