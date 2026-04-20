package com.code.crafters.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "El primer apellido es obligatorio")
    @Column(nullable = false, name = "first_name")
    private String firstName;

    @Column(name = "second_name")
    private String secondName;

    @Size(min = 3, max = 20, message = "El alias debe tener entre 3 y 20 caracteres")
    @Column(unique = true)
    private String alias;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;
    
    private String profileImage;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<Event> events;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Ticket> tickets;

    @PrePersist
    public void onPrePersist() {
        normalizeData();
    }

    @PreUpdate
    public void onPreUpdate() {
        normalizeData();
    }

    private void normalizeData() {
        if (this.email != null) {
            this.email = this.email.toLowerCase().trim();
        }
        if (this.alias != null) {
            this.alias = this.alias.trim();
        }
    }
}