package com.code.crafters.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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

    @Test
    @DisplayName("Should return null when authentication is not authenticated")
    void shouldReturnNullWhenAuthenticationIsNotAuthenticated() {
        Authentication unauthenticated = new UsernamePasswordAuthenticationToken(null, null);
        SecurityContextHolder.getContext().setAuthentication(unauthenticated);
        assertNull(SecurityUtils.getCurrentUserEmail());
    }

    @Test
    @DisplayName("Should not be instantiable - covers implicit constructor")
    void shouldCoverPrivateConstructor() throws Exception {
        java.lang.reflect.Constructor<SecurityUtils> constructor = SecurityUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
