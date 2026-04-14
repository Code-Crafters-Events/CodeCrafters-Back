package com.code.crafters.service;

import com.code.crafters.dto.request.LocationRequestDTO;
import com.code.crafters.entity.Location;

public interface LocationService {
    Location create(LocationRequestDTO dto);
}
