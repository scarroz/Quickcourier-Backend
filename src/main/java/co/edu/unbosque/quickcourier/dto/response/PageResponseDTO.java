package co.edu.unbosque.quickcourier.dto.response;

import java.util.List;

public record PageResponseDTO<T>(
        List<T> content,
        Integer page,
        Integer size,
        Long totalElements,
        Integer totalPages,
        Boolean first,
        Boolean last
) {
}