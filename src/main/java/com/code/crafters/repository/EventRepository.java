package com.code.crafters.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.code.crafters.entity.Event;
import com.code.crafters.entity.enums.EventCategory;
import com.code.crafters.entity.enums.EventType;

public interface EventRepository extends JpaRepository<Event, Long> {    
    Page<Event> findByAuthorId(Long authorId, Pageable pageable);

    Page<Event> findByCategory(EventCategory category, Pageable pageable);

    Page<Event> findByType(EventType type, Pageable pageable);
}
