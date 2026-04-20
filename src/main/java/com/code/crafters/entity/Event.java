package com.code.crafters.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.hibernate.validator.constraints.URL;

import com.code.crafters.entity.enums.EventCategory;
import com.code.crafters.entity.enums.EventType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 5, max = 100, message = "El título debe tener entre 5 y 100 carácteres")
    private String title;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 5, max = 255, message = "La descripción debe tener entre 5 y 255 carácteres")
    private String description;

    @NotNull(message = "El tipo de evento es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type;

    @NotNull(message = "La fecha es obligatoria")
    @Future(message = "La fecha debe ser en el futuro")
    private LocalDate date;

    @NotNull(message = "La hora es obligatoria")
    private LocalTime time;

    @NotNull(message = "El número máximo de asistentes es obligatorio")
    @Min(value = 1, message = "Debe haber al menos 1 plaza")
    private Integer maxAttendees;

    @ManyToOne
    @JoinColumn(name = "location_id", nullable = true)
    private Location location;

    @NotNull(message = "La categoría es obligatoria")
    @Enumerated(EnumType.STRING)
    private EventCategory category;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
    private BigDecimal price;

    @URL(message = "La URL de la imagen no es válida")
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User author;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<Ticket> tickets;

}
