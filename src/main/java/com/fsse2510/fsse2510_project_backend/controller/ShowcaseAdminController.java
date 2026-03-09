package com.fsse2510.fsse2510_project_backend.controller;

import com.fsse2510.fsse2510_project_backend.data.showcase.domainObject.ShowcaseCollectionAdminData;
import com.fsse2510.fsse2510_project_backend.data.showcase.dto.request.CreateShowcaseCollectionRequestDto;
import com.fsse2510.fsse2510_project_backend.data.showcase.dto.request.UpdateShowcaseCollectionRequestDto;
import com.fsse2510.fsse2510_project_backend.data.showcase.dto.response.ShowcaseCollectionAdminResponseDto;
import com.fsse2510.fsse2510_project_backend.mapper.showcase.ShowcaseDataMapper;
import com.fsse2510.fsse2510_project_backend.mapper.showcase.ShowcaseDtoMapper;
import com.fsse2510.fsse2510_project_backend.service.ShowcaseAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/showcase/collections")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ShowcaseAdminController {

    private final ShowcaseAdminService showcaseAdminService;
    private final ShowcaseDataMapper showcaseDataMapper;
    private final ShowcaseDtoMapper showcaseDtoMapper;

    @GetMapping
    public List<ShowcaseCollectionAdminResponseDto> getAll() {
        return showcaseAdminService.getAll().stream()
                .map(showcaseDtoMapper::toResponseDto)
                .toList();
    }

    @PostMapping
    public ShowcaseCollectionAdminResponseDto create(
            @RequestBody @Valid CreateShowcaseCollectionRequestDto requestDto) {
        ShowcaseCollectionAdminData data = showcaseDataMapper.toAdminData(requestDto);
        ShowcaseCollectionAdminData result = showcaseAdminService.create(data);
        return showcaseDtoMapper.toResponseDto(result);
    }

    @PutMapping("/{id}")
    public ShowcaseCollectionAdminResponseDto update(
            @PathVariable Integer id,
            @RequestBody @Valid UpdateShowcaseCollectionRequestDto requestDto) {
        ShowcaseCollectionAdminData data = showcaseDataMapper.toAdminData(requestDto);
        ShowcaseCollectionAdminData result = showcaseAdminService.update(id, data);
        return showcaseDtoMapper.toResponseDto(result);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        showcaseAdminService.delete(id);
    }
}
