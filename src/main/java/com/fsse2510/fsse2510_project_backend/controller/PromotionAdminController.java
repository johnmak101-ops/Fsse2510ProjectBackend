package com.fsse2510.fsse2510_project_backend.controller;

import com.fsse2510.fsse2510_project_backend.data.promotion.dto.request.CreatePromotionRequestDto;
import com.fsse2510.fsse2510_project_backend.data.promotion.dto.request.UpdatePromotionRequestDto;
import com.fsse2510.fsse2510_project_backend.data.promotion.dto.response.PromotionResponseDto;
import com.fsse2510.fsse2510_project_backend.mapper.promotion.PromotionDataMapper;
import com.fsse2510.fsse2510_project_backend.mapper.promotion.PromotionDtoMapper;
import com.fsse2510.fsse2510_project_backend.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/promotions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PromotionAdminController {

    private final PromotionService promotionService;
    private final PromotionDataMapper promotionDataMapper;
    private final PromotionDtoMapper promotionDtoMapper;

    @PostMapping
    public PromotionResponseDto createPromotion(@RequestBody @Valid CreatePromotionRequestDto requestDto) {
        var requestData = promotionDataMapper.toRequestData(requestDto);
        var result = promotionService.createPromotion(requestData);
        return promotionDtoMapper.toResponseDto(result);
    }

    @PatchMapping("/{promoId}/assign/{pid}")
    public void assignPromotionToProduct(@PathVariable Integer promoId, @PathVariable Integer pid) {
        promotionService.assignPromotionToProduct(promoId, pid);
    }

    @GetMapping
    public List<PromotionResponseDto> getAllPromotions() {
        return promotionService.getAllPromotions().stream()
                .map(promotionDtoMapper::toResponseDto)
                .toList();
    }

    @GetMapping("/{id}")
    public PromotionResponseDto getPromotionById(@PathVariable Integer id) {
        var result = promotionService.getPromotionById(id);
        return promotionDtoMapper.toResponseDto(result);
    }

    @PutMapping("/{id}")
    public PromotionResponseDto updatePromotion(@PathVariable Integer id,
                                                @RequestBody @Valid UpdatePromotionRequestDto requestDto) {
        var requestData = promotionDataMapper.toUpdateRequestData(requestDto);
        var result = promotionService.updatePromotion(id, requestData);
        return promotionDtoMapper.toResponseDto(result);
    }

    @DeleteMapping("/{id}")
    public void deletePromotion(@PathVariable Integer id) {
        promotionService.deletePromotion(id);
    }
}