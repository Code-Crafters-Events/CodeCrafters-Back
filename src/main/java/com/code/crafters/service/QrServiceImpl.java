package com.code.crafters.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

@Service
public class QrServiceImpl implements QrService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    public String generateTicketQr(Long ticketId, String verificationCode) {
        try {
            String qrContent = baseUrl + "/api/v1/tickets/verify/" + verificationCode;
            Path dir = Paths.get(uploadDir, "qr");
            Files.createDirectories(dir);
            String filename = UUID.randomUUID() + ".png";
            Path destination = dir.resolve(filename);
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, 300, 300);
            MatrixToImageWriter.writeToPath(matrix, "PNG", destination);
            return baseUrl + "/uploads/qr/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el QR: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el QR: " + e.getMessage());
        }
    }
}