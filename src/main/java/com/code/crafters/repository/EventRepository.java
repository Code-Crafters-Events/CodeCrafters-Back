package com.code.crafters.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.code.crafters.entity.Event;
import com.code.crafters.entity.enums.EventCategory;
import com.code.crafters.entity.enums.EventType;

@SuppressWarnings("null")
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    @Override
    @EntityGraph(attributePaths = { "tickets", "author", "location" })
    Page<Event> findAll(Pageable pageable);

    @EntityGraph(attributePaths = { "tickets", "author", "location" })
    Page<Event> findByDateGreaterThanEqual(LocalDate date, Pageable pageable);

    @EntityGraph(attributePaths = { "tickets", "author", "location" })
    Page<Event> findByAuthorId(Long authorId, Pageable pageable);

    @EntityGraph(attributePaths = { "tickets", "author", "location" })
    Page<Event> findByCategory(EventCategory category, Pageable pageable);

    @EntityGraph(attributePaths = { "tickets", "author", "location" })
    Page<Event> findByType(EventType type, Pageable pageable);

    @EntityGraph(attributePaths = { "tickets", "author", "location" })
    Optional<Event> findWithDetailsById(Long id);
}