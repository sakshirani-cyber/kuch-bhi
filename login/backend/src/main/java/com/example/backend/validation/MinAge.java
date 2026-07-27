package com.example.backend.validation;
// custom annotation @MinAge

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

// @interface defines custom validation
@Documented
@Constraint(validatedBy = MinAgeValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface MinAge {

	int value() default 12;
	String message() default "User must be at least {value} years old.";
	// Class gives information about a class's runtime
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
