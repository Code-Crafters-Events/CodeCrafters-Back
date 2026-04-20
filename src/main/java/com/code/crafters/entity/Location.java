package com.code.crafters.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "locations")
@Data
@NoArgsConstructor
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del lugar es obligatorio")
    private String venue;

    @NotBlank(message = "La dirección es obligatoria")
    private String address;

    @NotBlank(message = "La ciudad es obligatoria")
    private String city;

    private String province;

    @NotBlank(message = "El país es obligatorio")
    private String country;

    @Pattern(regexp = "^[0-9]{5}$", message = "El código postal debe tener 5 dígitos")
    private String zipCode;

    @Min(-90) @Max(90)
    private Double latitude;

    @Min(-180) @Max(180)
    private Double longitude;
}
