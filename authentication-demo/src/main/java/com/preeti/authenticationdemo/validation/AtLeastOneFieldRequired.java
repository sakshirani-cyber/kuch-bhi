package com.preeti.authenticationdemo.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class-level constraint for UpdateRequest: at least one of
 * newUsername / newPassword / newEmail / newPhoneNumber must be
 * provided. Prevents a "verify password only" submission from
 * reaching the database as an empty update.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AtLeastOneFieldRequiredValidator.class)
public @interface AtLeastOneFieldRequired {

    String message() default "Provide at least one new value to update: username, password, email, or phone number";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
