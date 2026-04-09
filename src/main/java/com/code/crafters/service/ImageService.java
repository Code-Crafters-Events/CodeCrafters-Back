package com.code.crafters.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    String uploadEventImage(Long eventId, MultipartFile file, Long userId);

    String uploadProfileImage(Long userId, MultipartFile file);

    void deleteImage(String imageUrl);

}
