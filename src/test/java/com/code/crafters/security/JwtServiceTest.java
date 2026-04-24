package com.code.crafters.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Tests")
class JwtServiceTest {

    private static final String SECRET = "mi-clave-secreta-de-prueba-32bytes!!";

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private JwtService jwtService;

    @Test
    @DisplayName("isValid returns true for a valid token")
    void isValid_shouldReturnTrue_whenTokenIsValid() {
        when(jwtProperties.getSecret()).thenReturn(SECRET);
        when(jwtProperties.getExpiration()).thenReturn(3600000L);
        String token = jwtService.generateToken(1L, "juan@example.com");
        assertTrue(jwtService.isValid(token));
    }

    @Test
    @DisplayName("isValid returns false when token is signed with a different key (JwtException)")
    void isValid_shouldReturnFalse_whenTokenSignedWithDifferentKey() {
        JwtProperties otherProps = mock(JwtProperties.class);
        when(otherProps.getSecret()).thenReturn("otra-clave-completamente-distinta-xyz!!");
        when(otherProps.getExpiration()).thenReturn(3600000L);
        when(jwtProperties.getSecret()).thenReturn(SECRET);
        JwtService otherService = new JwtService(otherProps);
        String tokenWithWrongKey = otherService.generateToken(99L, "fake@example.com");
        assertFalse(jwtService.isValid(tokenWithWrongKey));
    }

    @Test
    @DisplayName("isValid returns false when token is expired (JwtException)")
    void isValid_shouldReturnFalse_whenTokenIsExpired() {
        when(jwtProperties.getSecret()).thenReturn(SECRET);
        when(jwtProperties.getExpiration()).thenReturn(-1000L);
        String expiredToken = jwtService.generateToken(1L, "test@example.com");
        assertFalse(jwtService.isValid(expiredToken));
    }

    @Test
    @DisplayName("isValid returns false when token is malformed (JwtException)")
    void isValid_shouldReturnFalse_whenTokenIsMalformed() {
        when(jwtProperties.getSecret()).thenReturn(SECRET);
        assertFalse(jwtService.isValid("esto.no.es.un.jwt.valido"));
    }

    @Test
    @DisplayName("isValid returns false when token is null (IllegalArgumentException)")
    void isValid_shouldReturnFalse_whenTokenIsNull() {
        when(jwtProperties.getSecret()).thenReturn(SECRET);
        assertFalse(jwtService.isValid(null));
    }

    @Test
    @DisplayName("isValid returns false when token is empty string (IllegalArgumentException)")
    void isValid_shouldReturnFalse_whenTokenIsEmpty() {
        when(jwtProperties.getSecret()).thenReturn(SECRET);
        assertFalse(jwtService.isValid(""));
    }
}