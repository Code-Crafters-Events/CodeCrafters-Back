package com.code.crafters.specification;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.code.crafters.dto.request.EventFilterDTO;
import com.code.crafters.entity.Event;

import jakarta.persistence.criteria.Predicate;

public class EventSpecification {
    private EventSpecification() {
    }

    public static Specification<Event> withFilters(EventFilterDTO filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            LocalTime now = LocalTime.now();
            LocalDate today = LocalDate.now();

            if (filter.title() != null && !filter.title().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("title")),
                        "%" + filter.title().toLowerCase() + "%"));
            }

            if (filter.authorAlias() != null && !filter.authorAlias().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("author").get("alias")),
                        "%" + filter.authorAlias().toLowerCase() + "%"));
            }

            if (filter.category() != null) {
                predicates.add(cb.equal(root.get("category"), filter.category()));
            }

            if (filter.dateFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), filter.dateFrom()));
            }

            if (filter.dateTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), filter.dateTo()));
            }

            if (filter.priceMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.priceMin()));
            }

            if (filter.priceMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.priceMax()));
            }

            if (filter.showPast() != null && filter.showPast()) {
                predicates.add(cb.or(
                        cb.lessThan(root.get("date"), today),
                        cb.and(
                                cb.equal(root.get("date"), today),
                                cb.lessThan(root.get("time"), now))));
            } else {
                predicates.add(cb.or(
                        cb.greaterThan(root.get("date"), today),
                        cb.and(
                                cb.equal(root.get("date"), today),
                                cb.greaterThanOrEqualTo(root.get("time"), now))));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
