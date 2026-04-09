package com.code.crafters.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.code.crafters.entity.Event;
import com.code.crafters.entity.User;
import com.code.crafters.exception.ForbiddenOperationException;
import com.code.crafters.exception.ResourceNotFoundException;
import com.code.crafters.repository.EventRepository;
import com.code.crafters.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ImageServiceImpl implements ImageService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:5173}")
    private String baseUrl;

    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp", "image/gif");
    private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024;

    @Override
    public String uploadEventImage(Long eventId, MultipartFile file, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado: " + eventId));
        if (!event.getAuthor().getId().equals(userId))
            throw new ForbiddenOperationException("No tienes permiso para editar este evento");

        String url = saveFile(file, "events");
        if (event.getImageUrl() != null)
            deleteImage(event.getImageUrl());

        event.setImageUrl(url);
        eventRepository.save(event);
        return url;
    }

    @Override
    public String uploadProfileImage(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + userId));

        String url = saveFile(file, "avatars");

        if (user.getProfileImage() != null)
            deleteImage(user.getProfileImage());

        user.setProfileImage(url);
        userRepository.save(user);
        return url;
    }

    @Override
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith(baseUrl + "/uploads/"))
            return;
        String relativePath = imageUrl.replace(baseUrl + "/uploads/", "");
        Path path = Paths.get(uploadDir, relativePath);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            System.err.println("No se pudo eliminar la imagen: " + path);
        }
    }

    private String saveFile(MultipartFile file, String subfolder) {
        validateFile(file);
        try {
            Path dir = Paths.get(uploadDir, subfolder);
            Files.createDirectories(dir);

            String extension = getExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + "." + extension;
            Path destination = dir.resolve(filename);

            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            return baseUrl + "/uploads/" + subfolder + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la imagen: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("El archivo está vacío");
        if (!ALLOWED_TYPES.contains(file.getContentType()))
            throw new IllegalArgumentException("Tipo de archivo no permitido: " + file.getContentType());
        if (file.getSize() > MAX_SIZE_BYTES)
            throw new IllegalArgumentException("El archivo supera el tamaño máximo de 5MB");
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains("."))
            return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
