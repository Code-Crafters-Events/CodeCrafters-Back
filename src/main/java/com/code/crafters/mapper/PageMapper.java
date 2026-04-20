/*package com.code.crafters.mapper;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

import com.code.crafters.dto.response.PageResponseDTO;

public interface PageMapper {
default <T, R> PageResponseDTO<R> toPageResponse(Page<T> page, Function<T, R> mapper) {
        List<R> content = page.getContent()
                .stream()
                .map(mapper)
                .toList();
        return new PageResponseDTO<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}*/

package com.code.crafters.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import com.code.crafters.dto.response.EventResponseDTO;
import com.code.crafters.dto.response.PageResponseDTO;
import com.code.crafters.dto.response.TicketResponseDTO;
import com.code.crafters.entity.Event;
import com.code.crafters.entity.Ticket;

@Mapper(componentModel = "spring", uses = { EventMapper.class, TicketMapper.class })
public interface PageMapper {

   @Mapping(target = "page", source = "number")
    PageResponseDTO<EventResponseDTO> toEventPageResponse(Page<Event> page);

    @Mapping(target = "page", source = "number")
    PageResponseDTO<TicketResponseDTO> toTicketPageResponse(Page<Ticket> page);
}
