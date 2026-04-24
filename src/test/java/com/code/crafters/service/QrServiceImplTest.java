package com.code.crafters.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.zxing.qrcode.QRCodeWriter;

@SuppressWarnings("null")
@DisplayName("QrServiceImpl Unit Tests")
class QrServiceImplTest {

    private QrServiceImpl qrService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        qrService = new QrServiceImpl();
        ReflectionTestUtils.setField(qrService, "uploadDir", tempDir.toString());
        ReflectionTestUtils.setField(qrService, "baseUrl", "http://localhost:8080");
    }

    @Test
    @DisplayName("Should generate QR image and return public URL")
    void shouldGenerateQrImageAndReturnPublicUrl() throws Exception {
        String result = qrService.generateTicketQr(1L, "verification-code");

        assertTrue(result.startsWith("http://localhost:8080/uploads/qr/"));

        String fileName = result.substring(result.lastIndexOf('/') + 1);
        Path generatedFile = tempDir.resolve("qr").resolve(fileName);

        assertTrue(Files.exists(generatedFile));
        assertTrue(Files.size(generatedFile) > 0);
    }

    @Test
    @DisplayName("Should throw RuntimeException when IOException occurs")
    void shouldThrowRuntimeExceptionOnIOException() throws Exception {
        Path fileInsteadOfDir = tempDir.resolve("invalid_dir");
        Files.createFile(fileInsteadOfDir);
        ReflectionTestUtils.setField(qrService, "uploadDir", fileInsteadOfDir.toString());

        assertThrows(RuntimeException.class, () -> qrService.generateTicketQr(1L, "code"));
    }

    @Test
    @DisplayName("Should throw RuntimeException when general Exception occurs")
    void shouldThrowRuntimeExceptionOnGeneralException() {
        try (MockedStatic<QRCodeWriter> mockedWriter = Mockito.mockStatic(QRCodeWriter.class)) {
            ReflectionTestUtils.setField(qrService, "baseUrl", null);
            assertThrows(RuntimeException.class, () -> qrService.generateTicketQr(1L, "code"));
        }
    }
}
