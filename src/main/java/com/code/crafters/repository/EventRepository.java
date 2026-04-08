package com.code.crafters.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.code.crafters.entity.Event;
import com.code.crafters.entity.enums.EventCategory;
import com.code.crafters.entity.enums.EventType;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByAuthorId(Long authorId);

    List<Event> findByCategory(EventCategory category);

    List<Event> findByType(EventType type);
}
