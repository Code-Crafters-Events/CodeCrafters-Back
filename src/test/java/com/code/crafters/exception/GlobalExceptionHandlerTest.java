package com.code.crafters.exception;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings("null")
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new TestExceptionController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void shouldHandleResourceNotFound() throws Exception {
        mockMvc.perform(get("/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Recurso no encontrado"));
    }

    @Test
    void shouldHandleResourceAlreadyExists() throws Exception {
        mockMvc.perform(get("/conflict"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Recurso ya existe"));
    }

    @Test
    void shouldHandleForbiddenOperation() throws Exception {
        mockMvc.perform(get("/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Operacion prohibida"));
    }

    @Test
    void shouldHandleSecurityException() throws Exception {
        mockMvc.perform(get("/unauthorized"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Credenciales incorrectas"));
    }

    @Test
    void shouldHandleGenericException() throws Exception {
        mockMvc.perform(get("/error"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Error en el servidor")));
    }

    @RestController
    static class TestExceptionController {

        @GetMapping("/not-found")
        ResponseEntity<Void> notFound() {
            throw new ResourceNotFoundException("Recurso no encontrado");
        }

        @GetMapping("/conflict")
        ResponseEntity<Void> conflict() {
            throw new ResourceAlreadyExistsException("Recurso ya existe");
        }

        @GetMapping("/forbidden")
        ResponseEntity<Void> forbidden() {
            throw new ForbiddenOperationException("Operacion prohibida");
        }

        @GetMapping("/unauthorized")
        ResponseEntity<Void> unauthorized() {
            throw new SecurityException("Credenciales incorrectas");
        }

        @GetMapping("/error")
        ResponseEntity<Void> error() {
            throw new RuntimeException("Boom");
        }

    }

}
