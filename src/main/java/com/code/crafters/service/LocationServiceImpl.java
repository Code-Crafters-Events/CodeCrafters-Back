package com.code.crafters.service;

import org.springframework.stereotype.Service;

import com.code.crafters.dto.request.LocationRequestDTO;
import com.code.crafters.entity.Location;
import com.code.crafters.mapper.LocationMapper;
import com.code.crafters.repository.LocationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class LocationServiceImpl implements LocationService {
private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    @Override
    public Location create(LocationRequestDTO dto) {
        return locationRepository.save(locationMapper.toEntity(dto));
    }
}
