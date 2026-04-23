package com.code.crafters.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

@DisplayName("SecurityUtils Unit Tests")
class SecurityUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should return null when no authentication exists")
    void shouldReturnNullWhenNoAuthentication() {
        SecurityContextHolder.clearContext();

        assertNull(SecurityUtils.getCurrentUserEmail());
    }

    @Test
    @DisplayName("Should return username when principal is UserDetails")
    void shouldReturnUsernameFromUserDetails() {
        User principal = new User("juan@example.com", "secret", Collections.emptyList());
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null,
                principal.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);

        assertEquals("juan@example.com", SecurityUtils.getCurrentUserEmail());
    }

    @Test
    @DisplayName("Should return principal toString when principal is not UserDetails")
    void shouldReturnPrincipalStringWhenNotUserDetails() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("plain-user", null,
                Collections.emptyList());

        SecurityContextHolder.getContext().setAuthentication(auth);

        assertEquals("plain-user", SecurityUtils.getCurrentUserEmail());
    }
}
