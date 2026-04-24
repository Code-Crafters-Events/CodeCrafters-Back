package com.code.crafters.security;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthFilter - Tests Adicionales para Cobertura Completa")
class JwtAuthFilterAdditionalTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should skip authentication when extractEmail returns null")
    void shouldSkipAuthenticationWhenExtractEmailReturnsNull() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isValid("valid-token")).thenReturn(true);
        when(jwtService.extractEmail("valid-token")).thenReturn(null); // ← NULL

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService).isValid("valid-token");
        verify(jwtService).extractEmail("valid-token");
        verify(userDetailsService, never()).loadUserByUsername(org.mockito.ArgumentMatchers.anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should skip authentication when authentication already exists in context")
    void shouldSkipAuthenticationWhenAuthenticationAlreadyExists() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        User principal = new User("juan@example.com", "password", Collections.emptyList());

        when(jwtService.isValid("valid-token")).thenReturn(true);
        when(jwtService.extractEmail("valid-token")).thenReturn("juan@example.com");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList())
        );

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService).isValid("valid-token");
        verify(jwtService).extractEmail("valid-token");
        verify(userDetailsService, never()).loadUserByUsername(org.mockito.ArgumentMatchers.anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should skip authentication when loadUserByUsername returns null")
    void shouldSkipAuthenticationWhenLoadUserByUsernameReturnsNull() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isValid("valid-token")).thenReturn(true);
        when(jwtService.extractEmail("valid-token")).thenReturn("juan@example.com");
        when(userDetailsService.loadUserByUsername("juan@example.com")).thenReturn(null); // ← NULL

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService).isValid("valid-token");
        verify(jwtService).extractEmail("valid-token");
        verify(userDetailsService).loadUserByUsername("juan@example.com");
        
        org.junit.jupiter.api.Assertions.assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );
        verify(filterChain).doFilter(request, response);
    }
}