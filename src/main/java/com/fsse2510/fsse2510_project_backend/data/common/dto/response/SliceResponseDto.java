package com.fsse2510.fsse2510_project_backend.data.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SliceResponseDto<T> {
    private List<T> content;
    private boolean hasNext;

    public static <T> SliceResponseDto<T> of(Slice<T> slice) {
        return SliceResponseDto.<T>builder()
                .content(slice.getContent())
                .hasNext(slice.hasNext())
                .build();
    }

    public static <T> SliceResponseDto<T> of(List<T> content, boolean hasNext) {
        return SliceResponseDto.<T>builder()
                .content(content)
                .hasNext(hasNext)
                .build();
    }
}
