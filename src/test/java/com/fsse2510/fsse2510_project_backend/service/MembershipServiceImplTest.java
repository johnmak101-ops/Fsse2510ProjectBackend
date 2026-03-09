package com.fsse2510.fsse2510_project_backend.service;

import com.fsse2510.fsse2510_project_backend.data.membership.entity.MembershipConfigEntity;
import com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;
import com.fsse2510.fsse2510_project_backend.repository.MembershipConfigRepository;
import com.fsse2510.fsse2510_project_backend.service.impl.MembershipServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembershipServiceImplTest {

        @Mock
        private MembershipConfigRepository configRepository;

        @InjectMocks
        private MembershipServiceImpl membershipService;

        @BeforeEach
        void setupConfig() {
                // Mock Configs
                lenient().when(configRepository.findById(MembershipLevel.SILVER))
                                .thenReturn(Optional.of(new MembershipConfigEntity(MembershipLevel.SILVER,
                                                new BigDecimal("5000"),
                                                new BigDecimal("0.03"), 90)));
                lenient().when(configRepository.findById(MembershipLevel.GOLD))
                                .thenReturn(Optional.of(new MembershipConfigEntity(MembershipLevel.GOLD,
                                                new BigDecimal("10000"),
                                                new BigDecimal("0.05"), 90)));
                lenient().when(configRepository.findById(MembershipLevel.DIAMOND))
                                .thenReturn(Optional.of(new MembershipConfigEntity(MembershipLevel.DIAMOND,
                                                new BigDecimal("20000"),
                                                new BigDecimal("0.07"), 90)));
        }

        @Test
        void testDowngradeLogic() {
                // User: Silver, Expired yesterday, Spent 0 (Target 5000), Already in Grace
                // Period -> Should Downgrade
                UserEntity user = UserEntity.builder()
                                .level(MembershipLevel.SILVER)
                                .cycleEndDate(LocalDate.now().minusDays(1))
                                .accumulatedSpending(BigDecimal.ZERO)
                                .isInGracePeriod(true) // Already used grace
                                .points(new BigDecimal("100"))
                                .build();

                membershipService.checkStatusAndAutoUpdate(user);

                // Assertions
                assertEquals(MembershipLevel.BRONZE, user.getLevel()); // Downgrade success
                assertEquals(new BigDecimal("100"), user.getPoints()); // [Fix] Retain points after downgrade (100)
                assertFalse(user.getIsInGracePeriod());
        }

        @Test
        void testUpgradeLogic() {
                // User: Silver, Spent 4900. Buys 200 more. -> Should Upgrade to Gold (Target
                // 10000? No wait, Silver->Gold needs 10000)
                // Let's say accumulated 9900, buys 200 -> 10100 -> Upgrade Gold
                UserEntity user = UserEntity.builder()
                                .uid(1)
                                .level(MembershipLevel.SILVER)
                                .accumulatedSpending(new BigDecimal("9900"))
                                .cycleSpending(new BigDecimal("9900"))
                                .fullName("Test User")
                                .phoneNumber("12345678") // Info complete
                                .build();

                membershipService.accumulateAndCheckUpgrade(user, new BigDecimal("200"));

                assertEquals(MembershipLevel.GOLD, user.getLevel());
                assertEquals(0, new BigDecimal("10100.00").compareTo(user.getAccumulatedSpending()));
                assertNotNull(user.getCycleEndDate());
        }

        @Test
        void testEarnPointsWithGracePenalty() {
                // User: Gold but in Grace Period -> Should use Silver Rate (0.03) instead of
                // Gold (0.05)
                UserEntity user = UserEntity.builder()
                                .level(MembershipLevel.GOLD)
                                .isInGracePeriod(true)
                                .build();

                BigDecimal points = membershipService.calculateEarnedPoints(user, new BigDecimal("100.00"));

                // 100 * 0.03 (Silver Rate) = 3.00, then * 10 multiplier = 30.00
                assertEquals(0, new BigDecimal("30.00").compareTo(points));
        }
}