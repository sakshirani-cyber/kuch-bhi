package com.preeti.authenticationdemo.validation;

/**
 * Single source of truth for every regex used across the DTOs.
 * Previously the same "rule" (e.g. password strength) was duplicated
 * as separate regex literals in multiple files, which meant a future
 * change had to be made in several places and could easily drift out
 * of sync. Now every DTO references these constants instead.
 */
public final class ValidationPatterns {

    private ValidationPatterns() {
        // constants holder only, never instantiated
    }

    public static final String USERNAME_REGEX = "^[a-zA-Z0-9._]{3,20}$";

    public static final String PASSWORD_REGEX =
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,13}$";

    public static final String PHONE_REGEX = "^[0-9]{10}$";

    // Anchored (^...$) on purpose: this rejects any leading/trailing
    // whitespace around an otherwise valid-looking email, which the
    // default @Email constraint alone can be too lenient about.
    public static final String EMAIL_REGEX =
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

}
