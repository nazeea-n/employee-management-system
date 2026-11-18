package com.vei.ems.service;

import com.vei.ems.exception.BadRequestException;

/** Rules a new password must follow. */
public final class PasswordPolicy {

    private PasswordPolicy() { }

    /** At least 8 characters, with at least one letter and one digit. */
    public static void validate(String password) {
        boolean ok = password != null && password.length() >= 8
                && password.chars().anyMatch(Character::isLetter)
                && password.chars().anyMatch(Character::isDigit);
        if (!ok) {
            throw new BadRequestException("Password must be at least 8 characters with a letter and a digit.");
        }
    }
}
