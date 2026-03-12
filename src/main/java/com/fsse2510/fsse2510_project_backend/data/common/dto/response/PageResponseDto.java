package com.fsse2510.fsse2510_project_backend.data.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Stable pagination response DTO that matches the frontend's PaginatedResponse interface.
 * Provides a flat structure for metadata like totalElements and totalPages.
 *
 * @param <T> The type of content items.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponseDto<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int number;
    private int size;
    private boolean first;
    private boolean last;
    private int numberOfElements;
    private boolean empty;

    /**
     * Creates a PageResponseDto from a Spring Data Page object.
     *
     * @param page The Spring Data Page.
     * @param <T>  The content type.
     * @return A new PageResponseDto.
     */
    public static <T> PageResponseDto<T> of(Page<T> page) {
        return PageResponseDto.<T>builder()
                .content(page.getContent())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .number(page.getNumber())
                .size(page.getSize())
                .first(page.isFirst())
                .last(page.isLast())
                .numberOfElements(page.getNumberOfElements())
                .empty(page.isEmpty())
                .build();
    }

    /**
     * Creates a PageResponseDto with mapped content.
     *
     * @param page          The original Spring Data Page.
     * @param mappedContent The transformed content list.
     * @param <T>           The new content type.
     * @return A new PageResponseDto with the mapped content.
     */
    public static <T> PageResponseDto<T> of(Page<?> page, List<T> mappedContent) {
        return PageResponseDto.<T>builder()
                .content(mappedContent)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .number(page.getNumber())
                .size(page.getSize())
                .first(page.isFirst())
                .last(page.isLast())
                .numberOfElements(page.getNumberOfElements())
                .empty(page.isEmpty())
                .build();
    }
}
