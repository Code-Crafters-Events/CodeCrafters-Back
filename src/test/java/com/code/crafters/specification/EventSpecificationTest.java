package com.code.crafters.specification;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import com.code.crafters.dto.request.EventFilterDTO;
import com.code.crafters.entity.Event;
import com.code.crafters.entity.enums.EventCategory;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@SuppressWarnings({ "rawtypes", "unchecked", "null" })
@DisplayName("EventSpecification Tests")
class EventSpecificationTest {

    @Test
    void shouldBuildSpecificationWithAllFilters() {
        EventFilterDTO filter = new EventFilterDTO(
                "java",
                "juanp",
                EventCategory.PRESENCIAL,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(10),
                BigDecimal.ZERO,
                BigDecimal.valueOf(100),
                false);

        Specification<Event> specification = EventSpecification.withFilters(filter);

        Root<Event> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Predicate predicate = mock(Predicate.class);

        Path titlePath = mock(Path.class);
        Path authorPath = mock(Path.class);
        Path aliasPath = mock(Path.class);
        Path categoryPath = mock(Path.class);
        Path datePath = mock(Path.class);
        Path pricePath = mock(Path.class);
        Path timePath = mock(Path.class);

        Expression<String> loweredTitle = mock(Expression.class);
        Expression<String> loweredAlias = mock(Expression.class);

        when(root.get("title")).thenReturn(titlePath);
        when(root.get("author")).thenReturn(authorPath);
        when(authorPath.get("alias")).thenReturn(aliasPath);
        when(root.get("category")).thenReturn(categoryPath);
        when(root.get("date")).thenReturn(datePath);
        when(root.get("price")).thenReturn(pricePath);
        when(root.get("time")).thenReturn(timePath);

        when(cb.lower(titlePath)).thenReturn(loweredTitle);
        when(cb.lower(aliasPath)).thenReturn(loweredAlias);

        when(cb.like(loweredTitle, "%java%")).thenReturn(predicate);
        when(cb.like(loweredAlias, "%juanp%")).thenReturn(predicate);
        when(cb.equal(categoryPath, EventCategory.PRESENCIAL)).thenReturn(predicate);
        when(cb.greaterThanOrEqualTo(datePath, filter.dateFrom())).thenReturn(predicate);
        when(cb.lessThanOrEqualTo(datePath, filter.dateTo())).thenReturn(predicate);
        when(cb.greaterThanOrEqualTo(pricePath, filter.priceMin())).thenReturn(predicate);
        when(cb.lessThanOrEqualTo(pricePath, filter.priceMax())).thenReturn(predicate);
        when(cb.greaterThan(datePath, LocalDate.now())).thenReturn(predicate);
        when(cb.equal(datePath, LocalDate.now())).thenReturn(predicate);
        when(cb.greaterThanOrEqualTo(timePath, LocalTime.now())).thenReturn(predicate);
        when(cb.and(any(Predicate.class), any(Predicate.class))).thenReturn(predicate);
        when(cb.or(any(Predicate.class), any(Predicate.class))).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        Predicate result = specification.toPredicate(root, query, cb);

        assertNotNull(result);
    }

    @Test
    void shouldBuildSpecificationForPastEvents() {
        EventFilterDTO filter = new EventFilterDTO(
                null, null, null, null, null, null, null, true);

        Specification<Event> specification = EventSpecification.withFilters(filter);

        Root<Event> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Predicate predicate = mock(Predicate.class);

        Path datePath = mock(Path.class);
        Path timePath = mock(Path.class);

        when(root.get("date")).thenReturn(datePath);
        when(root.get("time")).thenReturn(timePath);

        when(cb.lessThan(datePath, LocalDate.now())).thenReturn(predicate);
        when(cb.equal(datePath, LocalDate.now())).thenReturn(predicate);
        when(cb.lessThan(timePath, LocalTime.now())).thenReturn(predicate);
        when(cb.and(any(Predicate.class), any(Predicate.class))).thenReturn(predicate);
        when(cb.or(any(Predicate.class), any(Predicate.class))).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        Predicate result = specification.toPredicate(root, query, cb);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Should skip title and authorAlias filters when they are null or blank")
    void shouldSkipFiltersWhenNullOrBlank() {
        EventFilterDTO nullFilter = new EventFilterDTO(
                null, null, null, null, null, null, null, false);
        EventFilterDTO blankFilter = new EventFilterDTO(
                "   ", "", null, null, null, null, null, false);

        Root<Event> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Predicate predicate = mock(Predicate.class);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);
        Specification<Event> specNull = EventSpecification.withFilters(nullFilter);
        specNull.toPredicate(root, query, cb);
        Specification<Event> specBlank = EventSpecification.withFilters(blankFilter);
        specBlank.toPredicate(root, query, cb);
        verify(root, never()).get("title");
        verify(root, never()).get("author");
    }
}
