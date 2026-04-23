package com.code.crafters.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.code.crafters.dto.request.EventRequestDTO;
import com.code.crafters.dto.response.EventResponseDTO;
import com.code.crafters.entity.Event;
import com.code.crafters.entity.Location;
import com.code.crafters.entity.Ticket;
import com.code.crafters.entity.User;
import com.code.crafters.entity.enums.EventCategory;
import com.code.crafters.entity.enums.EventType;
import com.code.crafters.exception.ForbiddenOperationException;
import com.code.crafters.exception.ResourceNotFoundException;
import com.code.crafters.mapper.EventMapper;
import com.code.crafters.mapper.PageMapper;
import com.code.crafters.repository.EventRepository;
import com.code.crafters.repository.LocationRepository;
import com.code.crafters.repository.TicketRepository;
import com.code.crafters.repository.UserRepository;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
@DisplayName("EventServiceImpl Additional Tests")
class EventServiceImplAdditionalTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private PageMapper pageMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    private User author;
    private Event event;
    private EventRequestDTO dto;

    @BeforeEach
    void setUp() {
        author = new User();
        author.setId(1L);

        event = new Event();
        event.setId(10L);
        event.setAuthor(author);

        dto = new EventRequestDTO(
                "Nuevo evento",
                "Descripcion valida",
                EventType.TALLER,
                LocalDate.now().plusDays(5),
                LocalTime.of(18, 0),
                20,
                2L,
                EventCategory.PRESENCIAL,
                BigDecimal.TEN,
                null);
    }

    @Test
    void shouldCreateEventWithLocation() {
        Location location = new Location();
        EventResponseDTO response = new EventResponseDTO(
                10L, "Nuevo evento", "Descripcion valida", EventType.TALLER,
                dto.date(), dto.time(), 20, 0, null,
                EventCategory.PRESENCIAL, BigDecimal.TEN, null, 1L, "Juan", "juanp");

        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(eventMapper.toEntity(dto)).thenReturn(event);
        when(locationRepository.findById(2L)).thenReturn(Optional.of(location));
        when(eventRepository.save(event)).thenReturn(event);
        when(eventMapper.toResponse(event)).thenReturn(response);

        eventService.createEvent(dto, 1L);

        verify(locationRepository).findById(2L);
        verify(eventRepository).save(event);
    }

    @Test
    void shouldFailCreateEventWhenLocationNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(eventMapper.toEntity(dto)).thenReturn(event);
        when(locationRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventService.createEvent(dto, 1L));
    }

    @Test
    void shouldFailGetEventByIdWhenNotFound() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventService.getEventById(99L));
    }

    @Test
    void shouldUpdateEventWithLocation() {
        Location location = new Location();
        EventResponseDTO response = new EventResponseDTO(
                10L, "Nuevo evento", "Descripcion valida", EventType.TALLER,
                dto.date(), dto.time(), 20, 0, null,
                EventCategory.PRESENCIAL, BigDecimal.TEN, null, 1L, "Juan", "juanp");

        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(locationRepository.findById(2L)).thenReturn(Optional.of(location));
        when(eventRepository.save(event)).thenReturn(event);
        when(eventMapper.toResponse(event)).thenReturn(response);

        eventService.updateEvent(10L, dto, 1L);

        verify(eventMapper).updateEntity(dto, event);
        verify(locationRepository).findById(2L);
        verify(eventRepository).save(event);
    }

    @Test
    void shouldFailUpdateEventWhenLocationNotFound() {
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(locationRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventService.updateEvent(10L, dto, 1L));
    }

    @Test
    void shouldFailDeleteEventWhenNotOwner() {
        User other = new User();
        other.setId(2L);
        event.setAuthor(other);

        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

        assertThrows(ForbiddenOperationException.class, () -> eventService.deleteEvent(10L, 1L));
    }

    @Test
    void shouldDeleteEventAndSendBulkEmail() {
        Ticket ticket = new Ticket();

        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(ticketRepository.findByEventId(10L)).thenReturn(List.of(ticket));

        assertDoesNotThrow(() -> eventService.deleteEvent(10L, 1L));

        verify(emailService).sendBulkCancellationEmail(List.of(ticket), event.getTitle(), event.getPrice());
        verify(eventRepository).delete(event);
    }

    @Test
    void shouldCreateEventWithoutLocation() {
        EventRequestDTO dtoWithoutLocation = new EventRequestDTO(
                "Nuevo evento",
                "Descripcion valida",
                EventType.TALLER,
                LocalDate.now().plusDays(5),
                LocalTime.of(18, 0),
                20,
                null,
                EventCategory.PRESENCIAL,
                BigDecimal.TEN,
                null);

        EventResponseDTO response = new EventResponseDTO(
                10L, "Nuevo evento", "Descripcion valida", EventType.TALLER,
                dtoWithoutLocation.date(), dtoWithoutLocation.time(), 20, 0, null,
                EventCategory.PRESENCIAL, BigDecimal.TEN, null, 1L, "Juan", "juanp");

        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(eventMapper.toEntity(dtoWithoutLocation)).thenReturn(event);
        when(eventRepository.save(event)).thenReturn(event);
        when(eventMapper.toResponse(event)).thenReturn(response);

        eventService.createEvent(dtoWithoutLocation, 1L);

        verify(eventRepository).save(event);
        verify(locationRepository, org.mockito.Mockito.never()).findById(any());
    }

    @Test
    void shouldFailUpdateEventWhenNotOwner() {
        User other = new User();
        other.setId(2L);
        event.setAuthor(other);

        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

        assertThrows(ForbiddenOperationException.class, () -> eventService.updateEvent(10L, dto, 1L));
    }

    @Test
    void shouldFailDeleteEventWhenEventNotFound() {
        when(eventRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventService.deleteEvent(10L, 1L));
    }

    @Test
    void shouldGetEventByIdSuccessfully() {
        EventResponseDTO response = new EventResponseDTO(
                10L, "Nuevo evento", "Descripcion valida", EventType.TALLER,
                dto.date(), dto.time(), 20, 0, null,
                EventCategory.PRESENCIAL, BigDecimal.TEN, null, 1L, "Juan", "juanp");

        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(eventMapper.toResponse(event)).thenReturn(response);

        EventResponseDTO result = eventService.getEventById(10L);

        assertNotNull(result);
        assertEquals(10L, result.id());
    }

    @Test
    void shouldFailGetEventByIdWhenMapperIsNotReachedBecauseEventMissing() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventService.getEventById(999L));
    }

    @Test
    void shouldFailUpdateEventWhenEventNotFound() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventService.updateEvent(999L, dto, 1L));
    }

    @Test
    void shouldDeleteEventWithoutTickets() {
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(ticketRepository.findByEventId(10L)).thenReturn(List.of());

        assertDoesNotThrow(() -> eventService.deleteEvent(10L, 1L));

        verify(emailService).sendBulkCancellationEmail(List.of(), event.getTitle(), event.getPrice());
        verify(eventRepository).delete(event);
    }

    
}
