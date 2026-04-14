package com.code.crafters.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.code.crafters.dto.request.LocationRequestDTO;
import com.code.crafters.dto.response.LocationResponseDTO;
import com.code.crafters.mapper.LocationMapper;
import com.code.crafters.service.LocationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class LocationController {
private final LocationService locationService;
    private final LocationMapper locationMapper;

    @PostMapping
    public ResponseEntity<LocationResponseDTO> create(@Valid @RequestBody LocationRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(locationMapper.toResponse(locationService.create(dto)));
    }
}
