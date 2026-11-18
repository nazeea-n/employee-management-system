package com.vei.ems.service;

import com.vei.ems.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PasswordPolicyTest {

    @Test
    void acceptsStrongPassword() {
        PasswordPolicy.validate("Admin@123");
        PasswordPolicy.validate("abcdefg1");
    }

    @Test
    void rejectsShortPassword() {
        assertThrows(BadRequestException.class, () -> PasswordPolicy.validate("ab1"));
    }

    @Test
    void rejectsPasswordWithoutDigit() {
        assertThrows(BadRequestException.class, () -> PasswordPolicy.validate("onlyletters"));
    }

    @Test
    void rejectsPasswordWithoutLetter() {
        assertThrows(BadRequestException.class, () -> PasswordPolicy.validate("12345678"));
    }

    @Test
    void rejectsNull() {
        assertThrows(BadRequestException.class, () -> PasswordPolicy.validate(null));
    }
}
