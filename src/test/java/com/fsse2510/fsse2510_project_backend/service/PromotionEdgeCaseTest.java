package com.fsse2510.fsse2510_project_backend.service;

import com.fsse2510.fsse2510_project_backend.data.product.entity.CategoryEntity;
import com.fsse2510.fsse2510_project_backend.data.product.domainObject.response.ProductResponseData;
import com.fsse2510.fsse2510_project_backend.data.product.entity.ProductEntity;
import com.fsse2510.fsse2510_project_backend.data.common.constant.DiscountType;
import com.fsse2510.fsse2510_project_backend.data.promotion.entity.PromotionEntity;
import com.fsse2510.fsse2510_project_backend.data.promotion.promotionType.PromotionType;
import com.fsse2510.fsse2510_project_backend.repository.ProductRepository;
import com.fsse2510.fsse2510_project_backend.repository.PromotionRepository;
import com.fsse2510.fsse2510_project_backend.service.PromotionApplicabilityService;
import com.fsse2510.fsse2510_project_backend.service.impl.ProductPromotionEnricherServiceImpl;
import com.fsse2510.fsse2510_project_backend.service.impl.PromotionCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PromotionEdgeCaseTest {

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PromotionApplicabilityService promotionApplicabilityService;

    @Mock
    private PromotionCalculator promotionCalculator;

    @InjectMocks
    private ProductPromotionEnricherServiceImpl enricherService;

    private PromotionEntity activePromo;
    private ProductEntity product;
    private ProductResponseData productDto;

    @BeforeEach
    void setUp() {
        activePromo = new PromotionEntity();
        activePromo.setId(1);
        activePromo.setType(PromotionType.STOREWIDE_SALE);
        activePromo.setDiscountType(DiscountType.PERCENTAGE);
        activePromo.setDiscountValue(new BigDecimal("10"));
        activePromo.setStartDate(LocalDateTime.now().minusDays(1));
        activePromo.setEndDate(LocalDateTime.now().plusDays(1));
        activePromo.setTargetCategories(Set.of());

        product = new ProductEntity();
        product.setPid(1);
        product.setPrice(new BigDecimal("100"));
        CategoryEntity category = new CategoryEntity();
        category.setName("Electronics");
        product.setCategory(category);

        productDto = new ProductResponseData();
        productDto.setPid(1);
        productDto.setPrice(new BigDecimal("100"));
    }

    @Test
    void testEnrich_WithZeroPrice() {
        productDto.setPrice(BigDecimal.ZERO);
        product.setPrice(BigDecimal.ZERO);

        when(promotionRepository.findActivePromotionsWithTargets(any())).thenReturn(List.of(activePromo));

        ProductResponseData result = enricherService.enrichWithPromotions(productDto);

        assertNotNull(result);
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getPrice()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getDiscountAmount()));
    }

    @Test
    void testEnrich_WithEmptyActivePromotions() {
        when(promotionRepository.findActivePromotionsWithTargets(any())).thenReturn(new ArrayList<>());

        ProductResponseData result = enricherService.enrichWithPromotions(productDto);

        assertNotNull(result);
        assertEquals(0, new BigDecimal("100").compareTo(result.getPrice()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getDiscountAmount()));
    }

    @Test
    void testEnrich_WithNullProduct() {
        ProductResponseData result = enricherService.enrichWithPromotions((ProductResponseData) null);
        assertNull(result);
    }

    @Test
    void testPreNormalizedStringMatching() {
        // Setup promotion with unnormalized category name
        activePromo.setType(PromotionType.SPECIFIC_CATEGORY_DISCOUNT);
        activePromo.setTargetCategories(Set.of("  ELECTRONICS  "));

        when(promotionRepository.findActivePromotionsWithTargets(any())).thenReturn(List.of(activePromo));
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(promotionApplicabilityService.isApplicable(any(), any(), any(Boolean.class))).thenReturn(true);
        lenient().when(promotionCalculator.calculateDiscountAmount(any(), any())).thenReturn(new BigDecimal("10"));
        when(promotionCalculator.calculatePromotionalPrice(any(), any())).thenReturn(new BigDecimal("90"));

        ProductResponseData result = enricherService.enrichWithPromotions(productDto);

        assertNotNull(result);
        assertTrue(result.getIsSale());
        assertEquals(0, new BigDecimal("10").compareTo(result.getDiscountAmount()));
    }

    @Test
    void testTyingDiscount_PrefersMembership() {
        PromotionEntity standardPromo = new PromotionEntity();
        standardPromo.setId(1);
        standardPromo.setType(PromotionType.STOREWIDE_SALE);
        standardPromo.setDiscountType(DiscountType.PERCENTAGE);
        standardPromo.setDiscountValue(new BigDecimal("20"));

        PromotionEntity membershipPromo = new PromotionEntity();
        membershipPromo.setId(2);
        membershipPromo.setType(PromotionType.MEMBERSHIP_DISCOUNT);
        membershipPromo.setDiscountType(DiscountType.PERCENTAGE);
        membershipPromo.setDiscountValue(new BigDecimal("20"));

        when(promotionRepository.findActivePromotionsWithTargets(any()))
                .thenReturn(List.of(standardPromo, membershipPromo));
        when(productRepository.findById(1)).thenReturn(Optional.of(product));
        when(promotionApplicabilityService.isApplicable(any(), any(), any(Boolean.class))).thenReturn(true);

        // Both give $20 discount
        when(promotionCalculator.calculateDiscountAmount(any(), any())).thenReturn(new BigDecimal("20"));
        when(promotionCalculator.calculatePromotionalPrice(any(), any())).thenReturn(new BigDecimal("80"));

        // Mock badge text for both
        lenient().when(promotionCalculator.generateBadgeText(standardPromo)).thenReturn("SALE");
        lenient().when(promotionCalculator.generateBadgeText(membershipPromo)).thenReturn("GOLD+ EXCLUSIVE");

        ProductResponseData result = enricherService.enrichWithPromotions(productDto);

        assertNotNull(result);
        // Should pick membership promo due to tie-breaker logic
        assertEquals("GOLD+ EXCLUSIVE", result.getPromotionBadgeText());
    }
}
