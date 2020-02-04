package ru.alamics.sso.registration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageDto {
    private long number;
    private long size;
    private long numberOfElements;
    private long totalElements;
    private long totalPages;
    private boolean hasPrevious;
    private boolean hasNext;
}
