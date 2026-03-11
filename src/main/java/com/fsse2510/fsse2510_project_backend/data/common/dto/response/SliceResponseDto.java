package com.fsse2510.fsse2510_project_backend.data.common.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class SliceResponseDto<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
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
