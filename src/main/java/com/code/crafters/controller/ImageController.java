package com.code.crafters.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.code.crafters.service.ImageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {
    private final ImageService imageService;

    @PostMapping("/events/{eventId}")
    public ResponseEntity<Map<String, String>> uploadEventImage(
            @PathVariable Long eventId,
            @RequestParam Long userId,
            @RequestParam("file") MultipartFile file) {
        String url = imageService.uploadEventImage(eventId, file, userId);
        return ResponseEntity.ok(Map.of("imageUrl", url));
    }

    @PostMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> uploadProfileImage(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) {
        String url = imageService.uploadProfileImage(userId, file);
        return ResponseEntity.ok(Map.of("imageUrl", url));
    }
}
