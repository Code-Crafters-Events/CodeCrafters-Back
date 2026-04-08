package com.code.crafters.mapper;

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
}
