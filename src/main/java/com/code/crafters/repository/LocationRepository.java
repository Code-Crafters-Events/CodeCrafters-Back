package com.code.crafters.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.code.crafters.entity.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByVenueAndCity(String venue, String city);
}
