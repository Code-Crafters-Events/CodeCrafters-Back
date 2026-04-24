package com.code.crafters.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.code.crafters.entity.Event;
import com.code.crafters.entity.User;
import com.code.crafters.exception.ForbiddenOperationException;
import com.code.crafters.exception.ResourceNotFoundException;
import com.code.crafters.repository.EventRepository;
import com.code.crafters.repository.UserRepository;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
@DisplayName("ImageServiceImpl Unit Tests")
class ImageServiceImplTest {

        @Mock
        private EventRepository eventRepository;

        @Mock
        private UserRepository userRepository;

        @InjectMocks
        private ImageServiceImpl imageService;

        @TempDir
        Path tempDir;

        private User author;
        private Event event;
        private User user;

        @BeforeEach
        void setUp() {
                ReflectionTestUtils.setField(imageService, "uploadDir", tempDir.toString());
                ReflectionTestUtils.setField(imageService, "baseUrl", "http://localhost:8080");
                author = new User();
                author.setId(1L);
                event = new Event();
                event.setId(10L);
                event.setAuthor(author);
                user = new User();
                user.setId(2L);
        }

        @Test
        @DisplayName("Should upload event image successfully")
        void shouldUploadEventImageSuccessfully() {
                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "event.png",
                                "image/png",
                                "content".getBytes());

                when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
                when(eventRepository.save(event)).thenReturn(event);
                String result = imageService.uploadEventImage(10L, file, 1L);
                assertTrue(result.startsWith("http://localhost:8080/uploads/events/"));
                verify(eventRepository).save(event);
        }

        @Test
        @DisplayName("Should reject event image upload when event does not exist")
        void shouldRejectEventImageUploadWhenEventNotFound() {
                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "event.png",
                                "image/png",
                                "content".getBytes());
                when(eventRepository.findById(10L)).thenReturn(Optional.empty());
                assertThrows(ResourceNotFoundException.class,
                                () -> imageService.uploadEventImage(10L, file, 1L));
        }

        @Test
        @DisplayName("Should reject event image upload when user is not author")
        void shouldRejectEventImageUploadWhenUserIsNotAuthor() {
                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "event.png",
                                "image/png",
                                "content".getBytes());
                when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
                assertThrows(ForbiddenOperationException.class,
                                () -> imageService.uploadEventImage(10L, file, 999L));
        }

        @Test
        @DisplayName("Should upload profile image successfully")
        void shouldUploadProfileImageSuccessfully() {
                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "avatar.jpg",
                                "image/jpeg",
                                "avatar-content".getBytes());
                when(userRepository.findById(2L)).thenReturn(Optional.of(user));
                when(userRepository.save(user)).thenReturn(user);
                String result = imageService.uploadProfileImage(2L, file);
                assertTrue(result.startsWith("http://localhost:8080/uploads/avatars/"));
                verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should reject profile image upload when user not found")
        void shouldRejectProfileImageUploadWhenUserNotFound() {
                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "avatar.jpg",
                                "image/jpeg",
                                "avatar-content".getBytes());
                when(userRepository.findById(2L)).thenReturn(Optional.empty());
                assertThrows(ResourceNotFoundException.class,
                                () -> imageService.uploadProfileImage(2L, file));
        }

        @Test
        @DisplayName("Should reject empty file")
        void shouldRejectEmptyFile() {
                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "empty.png",
                                "image/png",
                                new byte[0]);
                when(userRepository.findById(2L)).thenReturn(Optional.of(user));
                assertThrows(IllegalArgumentException.class,
                                () -> imageService.uploadProfileImage(2L, file));
        }

        @Test
        @DisplayName("Should reject unsupported content type")
        void shouldRejectUnsupportedContentType() {
                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "document.pdf",
                                "application/pdf",
                                "pdf".getBytes());
                when(userRepository.findById(2L)).thenReturn(Optional.of(user));
                assertThrows(IllegalArgumentException.class,
                                () -> imageService.uploadProfileImage(2L, file));
        }

        @Test
        @DisplayName("Should reject file larger than 5MB")
        void shouldRejectFileLargerThan5Mb() {
                byte[] content = new byte[(5 * 1024 * 1024) + 1];
                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "big.png",
                                "image/png",
                                content);
                when(userRepository.findById(2L)).thenReturn(Optional.of(user));
                assertThrows(IllegalArgumentException.class,
                                () -> imageService.uploadProfileImage(2L, file));
        }

        @Test
        @DisplayName("Should delete local image successfully")
        void shouldDeleteLocalImageSuccessfully() throws IOException {
                Path uploadsDir = tempDir.resolve("avatars");
                Files.createDirectories(uploadsDir);
                Path image = uploadsDir.resolve("test.png");
                Files.write(image, "data".getBytes());
                String imageUrl = "http://localhost:8080/uploads/avatars/test.png";
                imageService.deleteImage(imageUrl);
                assertFalse(Files.exists(image));
        }

        @Test
        @DisplayName("Should ignore delete when image url is null")
        void shouldIgnoreDeleteWhenImageUrlIsNull() {
                assertDoesNotThrow(() -> imageService.deleteImage(null));
        }

        @Test
        @DisplayName("Should ignore delete when image url does not belong to local uploads")
        void shouldIgnoreDeleteWhenImageUrlIsExternal() {
                assertDoesNotThrow(() -> imageService.deleteImage("https://example.com/image.png"));
        }

        @Test
        @DisplayName("Should ignore delete when local file does not exist")
        void shouldIgnoreDeleteWhenLocalFileDoesNotExist() {
                String imageUrl = "http://localhost:8080/uploads/avatars/missing.png";
                assertDoesNotThrow(() -> imageService.deleteImage(imageUrl));
        }

        @Test
        @DisplayName("Should accept webp profile image")
        void shouldAcceptWebpProfileImage() {
                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "avatar.webp",
                                "image/webp",
                                "webp-content".getBytes());
                when(userRepository.findById(2L)).thenReturn(Optional.of(user));
                when(userRepository.save(user)).thenReturn(user);
                String result = imageService.uploadProfileImage(2L, file);
                assertTrue(result.contains("/uploads/avatars/"));
        }

        @Test
        @DisplayName("Should accept gif event image")
        void shouldAcceptGifEventImage() {
                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "event.gif",
                                "image/gif",
                                "gif-content".getBytes());
                when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
                when(eventRepository.save(event)).thenReturn(event);
                String result = imageService.uploadEventImage(10L, file, 1L);
                assertTrue(result.contains("/uploads/events/"));
        }

        @Test
        @DisplayName("Should replace previous profile image")
        void shouldReplacePreviousProfileImage() throws IOException {
                Path avatarsDir = tempDir.resolve("avatars");
                Files.createDirectories(avatarsDir);
                Path oldImage = avatarsDir.resolve("old.png");
                Files.write(oldImage, "old".getBytes());
                user.setProfileImage("http://localhost:8080/uploads/avatars/old.png");
                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "avatar.jpg",
                                "image/jpeg",
                                "new-content".getBytes());
                when(userRepository.findById(2L)).thenReturn(Optional.of(user));
                when(userRepository.save(user)).thenReturn(user);
                String result = imageService.uploadProfileImage(2L, file);
                assertTrue(result.contains("/uploads/avatars/"));
                assertFalse(Files.exists(oldImage));
        }

        @Test
        @DisplayName("Should use default jpg extension when filename has no extension")
        void shouldUseDefaultJpgExtensionWhenFilenameHasNoExtension() {
                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "avatar",
                                "image/jpeg",
                                "avatar-content".getBytes());
                when(userRepository.findById(2L)).thenReturn(Optional.of(user));
                when(userRepository.save(user)).thenReturn(user);
                String result = imageService.uploadProfileImage(2L, file);
                assertTrue(result.endsWith(".jpg"));
        }

        @Test
        @DisplayName("Should replace previous event image")
        void shouldReplacePreviousEventImage() throws IOException {
                Path eventsDir = tempDir.resolve("events");
                Files.createDirectories(eventsDir);
                Path oldImage = eventsDir.resolve("old.png");
                Files.write(oldImage, "old".getBytes());
                event.setImageUrl("http://localhost:8080/uploads/events/old.png");
                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "event.png",
                                "image/png",
                                "new-content".getBytes());
                when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
                when(eventRepository.save(event)).thenReturn(event);
                String result = imageService.uploadEventImage(10L, file, 1L);
                assertTrue(result.contains("/uploads/events/"));
                assertFalse(Files.exists(oldImage));
        }

        @Test
        @DisplayName("Should ignore delete when image url belongs to another base url")
        void shouldIgnoreDeleteWhenImageUrlBelongsToAnotherBaseUrl() {
                assertDoesNotThrow(() -> imageService.deleteImage("http://another-host/uploads/avatars/test.png"));
        }

        @Test
        @DisplayName("Should accept webp event image")
        void shouldAcceptWebpEventImage() {
                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "event.webp",
                                "image/webp",
                                "content".getBytes());
                when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
                when(eventRepository.save(event)).thenReturn(event);
                String result = imageService.uploadEventImage(10L, file, 1L);
                assertTrue(result.contains("/uploads/events/"));
        }

        @Test
        @DisplayName("Should replace previous event image when uploading a new one")
        void shouldReplacePreviousEventImageWhenUploadingNewOne() throws IOException {
                Path eventsDir = tempDir.resolve("events");
                Files.createDirectories(eventsDir);
                Path oldImage = eventsDir.resolve("old.png");
                Files.write(oldImage, "old-content".getBytes());
                event.setImageUrl("http://localhost:8080/uploads/events/old.png");
                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "new.png",
                                "image/png",
                                "new-content".getBytes());
                when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
                when(eventRepository.save(event)).thenReturn(event);
                String result = imageService.uploadEventImage(10L, file, 1L);
                assertTrue(result.contains("/uploads/events/"));
                assertFalse(Files.exists(oldImage));
        }

        @Test
        @DisplayName("Should log error when file cannot be deleted (IOException)")
        void shouldLogErrorWhenFileCannotBeDeleted() throws IOException {
                Path uploadsDir = tempDir.resolve("avatars");
                Files.createDirectories(uploadsDir);
                Path fakeFile = uploadsDir.resolve("locked");
                Files.createDirectories(fakeFile);
                Files.write(fakeFile.resolve("child.txt"), "data".getBytes());
                String imageUrl = "http://localhost:8080/uploads/avatars/locked";
                assertDoesNotThrow(() -> imageService.deleteImage(imageUrl));
        }

        @Test
        @DisplayName("Should throw RuntimeException when IOException occurs saving file")
        void shouldThrowRuntimeExceptionWhenIOExceptionOccursSavingFile() throws IOException {
                Path eventsPath = tempDir.resolve("events");
                Files.write(eventsPath, "bloqueo".getBytes());

                MockMultipartFile file = new MockMultipartFile(
                                "file", "test.png", "image/png", "content".getBytes());

                when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
                assertThrows(RuntimeException.class,
                                () -> imageService.uploadEventImage(10L, file, 1L));
        }

        @Test
        @DisplayName("Should reject null file (covers file == null branch)")
        void shouldRejectNullFile() {
                when(userRepository.findById(2L)).thenReturn(Optional.of(user));
                assertThrows(IllegalArgumentException.class,
                                () -> imageService.uploadProfileImage(2L, null));
        }

        @Test
        @DisplayName("Should use default jpg extension when getOriginalFilename returns null")
        void shouldUseDefaultJpgExtensionWhenOriginalFilenameIsNull() throws Exception {
                MockMultipartFile file = new MockMultipartFile(
                                "file", "dummy.jpg", "image/jpeg", "content".getBytes());
                MockMultipartFile spyFile = org.mockito.Mockito.spy(file);
                org.mockito.Mockito.when(spyFile.getOriginalFilename()).thenReturn(null);
                when(userRepository.findById(2L)).thenReturn(Optional.of(user));
                when(userRepository.save(user)).thenReturn(user);
                String result = imageService.uploadProfileImage(2L, spyFile);
                assertTrue(result.endsWith(".jpg"));
        }

}
