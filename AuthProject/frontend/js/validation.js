const PHONE_PATTERN = /^[6-9]\d{9}$/;

const SIGNUP_PASSWORD_PATTERN =
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#])[A-Za-z\d@$!%*?&#]{8,20}$/;

function todayIsoDate() {
    return new Date().toISOString().slice(0, 10);
}

function validateIndianPhone(phone) {
    if (!phone) {
        return 'Phone number is required';
    }
    if (!PHONE_PATTERN.test(phone)) {
        return 'Phone number must be a valid 10-digit Indian mobile number';
    }
    return null;
}

function validateSignupForm(fields) {
    const errors = [];

    if (!fields.firstName?.trim()) {
        errors.push('First name is required');
    } else if (fields.firstName.length > 50) {
        errors.push('First name cannot exceed 50 characters');
    }

    if (fields.lastName && fields.lastName.length > 50) {
        errors.push('Last name cannot exceed 50 characters');
    }

    if (!fields.dob) {
        errors.push('Date of birth is required');
    } else if (fields.dob >= todayIsoDate()) {
        errors.push('Date of birth must be in the past');
    }

    if (!fields.gender) {
        errors.push('Gender is required');
    }

    if (!fields.email?.trim()) {
        errors.push('Email is required');
    }

    const phoneError = validateIndianPhone(fields.phoneNumber?.trim());
    if (phoneError) {
        errors.push(phoneError);
    }

    if (!fields.password) {
        errors.push('Password is required');
    } else if (fields.password.length < 8 || fields.password.length > 20) {
        errors.push('Password must be between 8 and 20 characters');
    } else if (!SIGNUP_PASSWORD_PATTERN.test(fields.password)) {
        errors.push(
            'Password must contain uppercase, lowercase, number, and special character (@$!%*?&#)'
        );
    }

    return errors.length ? errors.join(' ') : null;
}

function validateUpdateProfileForm(fields) {
    const errors = [];

    if (!fields.firstName?.trim()) {
        errors.push('First name is required');
    } else if (fields.firstName.length > 50) {
        errors.push('First name cannot exceed 50 characters');
    }

    if (fields.lastName && fields.lastName.length > 50) {
        errors.push('Last name cannot exceed 50 characters');
    }

    if (!fields.dob) {
        errors.push('Date of birth is required');
    } else if (fields.dob >= todayIsoDate()) {
        errors.push('Date of birth must be in the past');
    }

    if (!fields.gender) {
        errors.push('Gender is required');
    }

    const phoneError = validateIndianPhone(fields.phoneNumber?.trim());
    if (phoneError) {
        errors.push(phoneError);
    }

    return errors.length ? errors.join(' ') : null;
}

function validateChangePasswordForm(current, newPwd, confirm) {
    const errors = [];

    if (!current) {
        errors.push('Current password is required');
    }
    if (!newPwd) {
        errors.push('New password is required');
    } else if (newPwd.length < 8 || newPwd.length > 20) {
        errors.push('New password must be between 8 and 20 characters');
    }
    if (!confirm) {
        errors.push('Confirm password is required');
    }
    if (newPwd && confirm && newPwd !== confirm) {
        errors.push('New password and confirm password do not match');
    }
    if (current && newPwd && current === newPwd) {
        errors.push('New password must be different from current password');
    }

    return errors.length ? errors.join(' ') : null;
}
