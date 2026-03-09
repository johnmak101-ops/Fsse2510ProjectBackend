package com.fsse2510.fsse2510_project_backend.controller;

import com.fsse2510.fsse2510_project_backend.data.promotion.dto.response.PromotionResponseDto;
import com.fsse2510.fsse2510_project_backend.data.promotion.promotionType.PromotionType;
import com.fsse2510.fsse2510_project_backend.mapper.promotion.PromotionDtoMapper;
import com.fsse2510.fsse2510_project_backend.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public endpoint for active promotions — no authentication required.
 * Used by the frontend cart page to display upsell banners.
 */
@RestController
@RequestMapping("/public/promotions")
@RequiredArgsConstructor
public class PublicPromotionController {

    private final PromotionService promotionService;
    private final PromotionDtoMapper promotionDtoMapper;

    /**
     * GET /public/promotions/active?type=MIN_QUANTITY_DISCOUNT
     *
     * Returns all currently active promotions, optionally filtered by type.
     * Date filtering is done server-side (only active promotions are returned).
     */
    @GetMapping("/active")
    public List<PromotionResponseDto> getActivePromotions(
            @RequestParam(required = false) List<PromotionType> type) {
        return promotionService.getActivePublicPromotions(type).stream()
                .map(promotionDtoMapper::toResponseDto)
                .toList();
    }
}
