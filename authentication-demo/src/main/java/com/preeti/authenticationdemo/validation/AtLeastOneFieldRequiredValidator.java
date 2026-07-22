package com.preeti.authenticationdemo.validation;

import com.preeti.authenticationdemo.dto.UpdateRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AtLeastOneFieldRequiredValidator implements ConstraintValidator<AtLeastOneFieldRequired, UpdateRequest> {

    @Override
    public boolean isValid(UpdateRequest request, ConstraintValidatorContext context) {

        if (request == null) {
            return true;
        }

        return isPresent(request.getNewUsername())
                || isPresent(request.getNewPassword())
                || isPresent(request.getNewEmail())
                || isPresent(request.getNewPhoneNumber());
    }

    private boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }

}
