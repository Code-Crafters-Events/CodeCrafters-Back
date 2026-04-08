package com.code.crafters.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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
    private String title;
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type;
    private LocalDate date;
    private LocalTime time;
    private Integer maxAttendees;
    @ManyToOne
    @JoinColumn(name = "location_id", nullable = true)
    private Location location;
    @Enumerated(EnumType.STRING)
    private EventCategory category;
    private BigDecimal price;
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User author;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<Ticket> tickets;

}
