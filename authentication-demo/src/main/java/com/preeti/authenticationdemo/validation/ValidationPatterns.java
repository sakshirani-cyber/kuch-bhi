package com.preeti.authenticationdemo.validation;

public final class ValidationPatterns {

    private ValidationPatterns() {
    }

    public static final String USERNAME_REGEX = "^[a-zA-Z0-9._]{3,20}$";

    public static final String PASSWORD_REGEX =
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,13}$";

    public static final String PHONE_REGEX = "^[0-9]{10}$";

    public static final String EMAIL_REGEX =
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

}
