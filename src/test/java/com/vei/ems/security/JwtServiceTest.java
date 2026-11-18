package com.vei.ems.security;

import com.vei.ems.domain.Role;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "0123456789-0123456789-0123456789-ab";

    @Test
    void validTokenReturnsUserAndRole() {
        JwtService jwt = new JwtService(SECRET, 60);
        String token = jwt.generateToken("admin", Role.ADMIN);

        Optional<JwtService.TokenInfo> info = jwt.parse(token);
        assertTrue(info.isPresent());
        assertEquals("admin", info.get().username());
        assertEquals("ADMIN", info.get().role());
    }

    @Test
    void tamperedTokenIsRejected() {
        JwtService jwt = new JwtService(SECRET, 60);
        String token = jwt.generateToken("admin", Role.ADMIN);
        String broken = token.substring(0, token.length() - 3) + "abc";
        assertFalse(jwt.parse(broken).isPresent());
    }

    @Test
    void tokenFromAnotherSecretIsRejected() {
        String token = new JwtService(SECRET, 60).generateToken("admin", Role.ADMIN);
        JwtService other = new JwtService("a-completely-different-secret-key-1234567", 60);
        assertFalse(other.parse(token).isPresent());
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService jwt = new JwtService(SECRET, -5);      // already expired when created
        assertFalse(jwt.parse(jwt.generateToken("admin", Role.ADMIN)).isPresent());
    }

    @Test
    void garbageIsRejected() {
        JwtService jwt = new JwtService(SECRET, 60);
        assertFalse(jwt.parse("not.a.token").isPresent());
        assertFalse(jwt.parse("").isPresent());
    }

    @Test
    void shortSecretIsNotAllowed() {
        assertThrows(IllegalStateException.class, () -> new JwtService("too-short", 60));
    }
}
