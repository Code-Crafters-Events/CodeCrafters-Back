package com.code.crafters.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.code.crafters.dto.request.EventFilterDTO;
import com.code.crafters.dto.request.EventRequestDTO;
import com.code.crafters.dto.response.EventResponseDTO;
import com.code.crafters.dto.response.PageResponseDTO;
import com.code.crafters.entity.Event;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("EventServiceImpl Unit Tests")
@SuppressWarnings({ "null", "unchecked" })
class EventServiceImplTest {

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

    private Event testEvent;
    private User testUser;
    private EventRequestDTO eventRequestDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Juan");
        testUser.setEmail("juan@example.com");

        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setTitle("Masterclass Java");
        testEvent.setDescription("Una masterclass completa sobre Java");
        testEvent.setType(EventType.MASTERCLASS);
        testEvent.setDate(LocalDate.now().plusDays(10));
        testEvent.setTime(LocalTime.of(14, 0));
        testEvent.setMaxAttendees(50);
        testEvent.setCategory(EventCategory.PRESENCIAL);
        testEvent.setPrice(BigDecimal.valueOf(29.99));
        testEvent.setAuthor(testUser);

        eventRequestDTO = new EventRequestDTO(
                "Masterclass Java",
                "Una masterclass completa sobre Java",
                EventType.MASTERCLASS,
                LocalDate.now().plusDays(10),
                LocalTime.of(14, 0),
                50,
                null,
                EventCategory.PRESENCIAL,
                BigDecimal.valueOf(29.99),
                null);
    }

    @Test
    @DisplayName("Should create event successfully")
    void testCreateEventSuccess() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(eventMapper.toEntity(eventRequestDTO)).thenReturn(testEvent);
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
        when(eventMapper.toResponse(testEvent)).thenReturn(createEventResponse());

        EventResponseDTO result = eventService.createEvent(eventRequestDTO, 1L);

        assertNotNull(result);
        assertEquals("Masterclass Java", result.title());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    @DisplayName("Should throw exception when user not found during creation")
    void testCreateEventUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventService.createEvent(eventRequestDTO, 999L));
    }

    @Test
    @DisplayName("Should get event by id successfully")
    void testGetEventByIdSuccess() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventMapper.toResponse(testEvent)).thenReturn(createEventResponse());

        EventResponseDTO result = eventService.getEventById(1L);

        assertNotNull(result);
        assertEquals("Masterclass Java", result.title());
    }

    @Test
    @DisplayName("Should get all events with pagination")
    void testGetAllEventsSuccess() {
        Page<Event> page = new PageImpl<>(List.of(testEvent));
        PageResponseDTO<EventResponseDTO> pageResponse = new PageResponseDTO<>(
                List.of(createEventResponse()),
                0, 15, 1, 1, true);
        when(eventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(pageMapper.toEventPageResponse(page)).thenReturn(pageResponse);
        PageResponseDTO<EventResponseDTO> result = eventService.getAllEvents(0, 15);
        assertNotNull(result);
        assertEquals(1, result.content().size());
    }

    @Test
    @DisplayName("Should update event successfully")
    void testUpdateEventSuccess() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
        when(eventMapper.toResponse(testEvent)).thenReturn(createEventResponse());
        EventResponseDTO result = eventService.updateEvent(1L, eventRequestDTO, 1L);
        assertNotNull(result);
        verify(eventMapper, times(1)).updateEntity(eq(eventRequestDTO), eq(testEvent));
    }

    @Test
    @DisplayName("Should throw exception when user not owner of event")
    void testUpdateEventNotOwner() {
        User otherUser = new User();
        otherUser.setId(999L);
        testEvent.setAuthor(otherUser);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        assertThrows(ForbiddenOperationException.class, () -> eventService.updateEvent(1L, eventRequestDTO, 1L));
    }

    @Test
    @DisplayName("Should delete event successfully")
    void testDeleteEventSuccess() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(ticketRepository.findByEventId(1L)).thenReturn(List.of());
        assertDoesNotThrow(() -> eventService.deleteEvent(1L, 1L));
        verify(eventRepository, times(1)).delete(testEvent);
    }

    @Test
    @DisplayName("Should search events with filters")
    void testSearchEventsSuccess() {
        EventFilterDTO filter = new EventFilterDTO(
                "Masterclass", null, EventCategory.PRESENCIAL,
                null, null, null, null, false);

        Page<Event> page = new PageImpl<>(List.of(testEvent));
        PageResponseDTO<EventResponseDTO> pageResponse = new PageResponseDTO<>(
                List.of(createEventResponse()),
                0, 15, 1, 1, true);

        when(eventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(pageMapper.toEventPageResponse(page)).thenReturn(pageResponse);
        PageResponseDTO<EventResponseDTO> result = eventService.searchEvents(filter, 0, 15);
        assertNotNull(result);
        assertEquals(1, result.content().size());
    }

    private EventResponseDTO createEventResponse() {
        return new EventResponseDTO(
                1L, "Masterclass Java", "Desc", EventType.MASTERCLASS,
                LocalDate.now().plusDays(10), LocalTime.of(14, 0), 50, 0,
                null, EventCategory.PRESENCIAL, BigDecimal.valueOf(29.99),
                null, 1L, "Juan", null);
    }

    @Test
    @DisplayName("Should get events by user successfully")
    void testGetEventsByUserSuccess() {
        Page<Event> page = new PageImpl<>(List.of(testEvent));
        PageResponseDTO<EventResponseDTO> pageResponse = new PageResponseDTO<>(
                List.of(createEventResponse()),
                0, 15, 1, 1, true);
        when(eventRepository.findByAuthorId(eq(1L), any(Pageable.class))).thenReturn(page);
        when(pageMapper.toEventPageResponse(page)).thenReturn(pageResponse);
        PageResponseDTO<EventResponseDTO> result = eventService.getEventsByUser(1L, 0, 15);
        assertNotNull(result);
        assertEquals(1, result.content().size());
        verify(eventRepository, times(1)).findByAuthorId(eq(1L), any(Pageable.class));
    }

    @Test
    void shouldFailUpdateEventWhenNotOwner() {
        User other = new User();
        other.setId(2L);
        testEvent.setAuthor(other);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        assertThrows(ForbiddenOperationException.class,
                () -> eventService.updateEvent(1L, eventRequestDTO, 1L));
    }

    @Test
    void shouldDeleteEventWithoutTickets() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(ticketRepository.findByEventId(1L)).thenReturn(List.of());

        eventService.deleteEvent(1L, 1L);

        verify(emailService).sendBulkCancellationEmail(List.of(), testEvent.getTitle(), testEvent.getPrice());
        verify(eventRepository).delete(testEvent);
    }

}