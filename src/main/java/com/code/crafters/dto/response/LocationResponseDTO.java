package com.code.crafters.dto.response;

public record LocationResponseDTO(
        Long id,
        String venue,
        String address,
        String city,
        String province,
        String country,
        String zipCode,
        Double latitude,
        Double longitude) {

}
