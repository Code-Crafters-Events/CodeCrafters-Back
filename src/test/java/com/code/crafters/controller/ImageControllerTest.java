package com.code.crafters.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.code.crafters.exception.GlobalExceptionHandler;
import com.code.crafters.service.ImageService;

@DisplayName("ImageController Tests")
class ImageControllerTest {

    private MockMvc mockMvc;
    private ImageService imageService;

    @BeforeEach
    void setUp() {
        imageService = org.mockito.Mockito.mock(ImageService.class);
        ImageController controller = new ImageController(imageService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldUploadEventImageSuccessfully() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "event.png", "image/png", "content".getBytes());

        when(imageService.uploadEventImage(eq(10L), any(), eq(1L)))
                .thenReturn("http://localhost:8080/uploads/events/test.png");

        mockMvc.perform(multipart("/api/v1/images/events/{eventId}", 10L)
                        .file(file)
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl")
                        .value("http://localhost:8080/uploads/events/test.png"));
    }

    @Test
    void shouldUploadProfileImageSuccessfully() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", "content".getBytes());

        when(imageService.uploadProfileImage(eq(2L), any()))
                .thenReturn("http://localhost:8080/uploads/avatars/test.png");

        mockMvc.perform(multipart("/api/v1/images/users/{userId}", 2L)
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl")
                        .value("http://localhost:8080/uploads/avatars/test.png"));
    }
}
