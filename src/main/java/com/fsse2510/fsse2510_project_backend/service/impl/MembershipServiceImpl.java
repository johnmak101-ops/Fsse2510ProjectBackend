package com.fsse2510.fsse2510_project_backend.service.impl;

import com.fsse2510.fsse2510_project_backend.data.membership.domainObject.request.UpdateMembershipConfigRequestData;
import com.fsse2510.fsse2510_project_backend.data.membership.domainObject.response.MembershipConfigResponseData;
import com.fsse2510.fsse2510_project_backend.data.membership.entity.MembershipConfigEntity;
import com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;
import com.fsse2510.fsse2510_project_backend.mapper.membership.MembershipConfigDataMapper;
import com.fsse2510.fsse2510_project_backend.mapper.membership.MembershipConfigEntityMapper;
import com.fsse2510.fsse2510_project_backend.repository.MembershipConfigRepository;
import com.fsse2510.fsse2510_project_backend.service.MembershipService;
import com.fsse2510.fsse2510_project_backend.util.BusinessConstants;

import static com.fsse2510.fsse2510_project_backend.util.BusinessConstants.MONEY_ROUNDING;
import jakarta.annotation.PostConstruct;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class MembershipServiceImpl implements MembershipService {

    private static final Logger logger = LoggerFactory.getLogger(MembershipServiceImpl.class);

    private final MembershipConfigRepository configRepository;
    private final MembershipConfigDataMapper configDataMapper;
    private final MembershipConfigEntityMapper configEntityMapper;

    private static class CacheEntry {
        MembershipConfigEntity config;
        long timestamp;

        CacheEntry(MembershipConfigEntity config) {
            this.config = config;
            this.timestamp = System.currentTimeMillis();
        }
    }

    // Thread-Safety, prevent ConcurrentModificationException
    // Read-Heavy & Hot Path
    private final Map<MembershipLevel, CacheEntry> configCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 10 * 60 * 1000; // 10 minutes cache TTL

    // Load setting in memory if db is not set
    // Cache Warming
    // Optimization: Fetch all valid configs once to warm cache and check for
    // seeding
    @PostConstruct
    public void initConfig() {

        List<MembershipConfigEntity> existingConfigs = configRepository.findAll();

        if (existingConfigs.isEmpty()) {
            // [Seeding] Only runs if DB is empty
            MembershipConfigEntity noMember = new MembershipConfigEntity(MembershipLevel.NO_MEMBERSHIP, BigDecimal.ZERO,
                    BigDecimal.ZERO, 0);
            MembershipConfigEntity bronze = new MembershipConfigEntity(MembershipLevel.BRONZE, new BigDecimal("500"),
                    new BigDecimal("0.01"), 90);
            MembershipConfigEntity silver = new MembershipConfigEntity(MembershipLevel.SILVER, new BigDecimal("5000"),
                    new BigDecimal("0.03"), 90);
            MembershipConfigEntity gold = new MembershipConfigEntity(MembershipLevel.GOLD, new BigDecimal("10000"),
                    new BigDecimal("0.05"), 90);
            MembershipConfigEntity diamond = new MembershipConfigEntity(MembershipLevel.DIAMOND,
                    new BigDecimal("50000"),
                    new BigDecimal("0.08"), 90);

            // Save and Cache
            configRepository.saveAll(List.of(noMember, bronze, silver, gold, diamond))
                    .forEach(config -> configCache.put(config.getLevel(), new CacheEntry(config)));
        } else {
            // Just Cache
            existingConfigs.forEach(config -> configCache.put(config.getLevel(), new CacheEntry(config)));
        }
    }

    @Override
    public MembershipLevel getDefaultLevel() {
        return MembershipLevel.NO_MEMBERSHIP;
    }

    @Override
    public MembershipConfigEntity getConfig(MembershipLevel level) {
        CacheEntry entry = configCache.get(level);
        if (entry != null && (System.currentTimeMillis() - entry.timestamp) < CACHE_TTL_MS) {
            return entry.config;
        }

        MembershipConfigEntity config = configRepository.findById(level)
                .orElseThrow(() -> new RuntimeException("Config missing for level: " + level));
        configCache.put(level, new CacheEntry(config));
        return config;
    }

    // === 1. State Machine Logic ===
    // Note: This method only modifies the user object state; it does not execute
    // Save.
    @Override
    public void checkStatusAndAutoUpdate(UserEntity user) {
        // If No Member, return directly (unless you want to handle default logic here)
        if (user.getLevel() == MembershipLevel.NO_MEMBERSHIP) {
            return;
        }

        // Defensive: Set cycleEndDate for legacy data
        if (user.getCycleEndDate() == null) {
            user.setCycleEndDate(LocalDate.now().plusYears(1));
            return;
        }

        LocalDate now = LocalDate.now();
        LocalDate endDate = user.getCycleEndDate();

        if (!now.isAfter(endDate)) {
            return; // Not expired
        }

        // --- Expiry Handling ---
        MembershipConfigEntity config = getConfig(user.getLevel());
        boolean targetMet = user.getAccumulatedSpending().compareTo(config.getMinSpend()) >= 0;

        if (targetMet) {
            logger.info("User {} Renewed {}", user.getUid(), user.getLevel());
            renewMembership(user);
        } else if (!user.getIsInGracePeriod()) {
            logger.info("User {} Entered Grace Period", user.getUid());
            enterGracePeriod(user, config.getGracePeriodDays());
        } else {
            downgradeMembership(user);
        }
    }

    private void renewMembership(UserEntity user) {
        user.setCycleEndDate(LocalDate.now().plusYears(1));
        user.setCycleSpending(BigDecimal.ZERO); // [Fix] Reset annual accumulated spending
        user.setPoints(BigDecimal.ZERO); // Reset annual points
        user.setIsInGracePeriod(false);
    }

    private void enterGracePeriod(UserEntity user, Integer days) {
        user.setCycleEndDate(LocalDate.now().plusDays(days));
        user.setIsInGracePeriod(true);
    }

    private void downgradeMembership(UserEntity user) {
        MembershipLevel currentLevel = user.getLevel();
        MembershipLevel newLevel = currentLevel.getPrevious();

        logger.info("User {} Downgraded from {} to {}", user.getUid(), currentLevel, newLevel);

        user.setLevel(newLevel);
        // user.setAccumulatedSpending(BigDecimal.ZERO); // Removed: Keep as lifetime
        // accumulated spending
        user.setCycleSpending(BigDecimal.ZERO); // [Fix] Reset annual spending on downgrade
        user.setIsInGracePeriod(false);

        // If downgraded to No Member, no Date is needed; otherwise set to one year
        if (newLevel == MembershipLevel.NO_MEMBERSHIP) {
            user.setCycleEndDate(null);
        } else {
            user.setCycleEndDate(LocalDate.now().plusYears(1));
        }
    }

    // === 2. Points Calculation ===
    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateEarnedPoints(UserEntity user, BigDecimal paidAmount) {
        MembershipLevel effectiveLevel = user.getLevel();

        // Penalty Mechanism: Use the previous level's Rate during Grace Period
        if (user.getIsInGracePeriod() && effectiveLevel.getRank() > 0) {
            effectiveLevel = effectiveLevel.getPrevious();
        }

        BigDecimal rate = getConfig(effectiveLevel).getPointRate();

        // Prevent Bronze from getting 0 points during Grace in previous version
        // Period (NO_MEMBERSHIP rate is 0)
        // If Rate is 0 but user still has a level (Grace Period), provide a Floor Rate.
        if (rate.compareTo(BigDecimal.ZERO) == 0 && user.getLevel().getRank() > 0) {
            rate = BusinessConstants.GRACE_PERIOD_FLOOR_RATE;
        }

        BigDecimal pointsValue = paidAmount.multiply(rate);
        return pointsValue
                .multiply(BigDecimal
                        .valueOf(BusinessConstants.POINTS_TO_DOLLAR_RATE))
                .setScale(BusinessConstants.MONEY_SCALE,
                        MONEY_ROUNDING);
    }

    // === 3. Upgrade Check ===
    // This method only modifies the user object; it does not execute Save.
    @Override
    public void accumulateAndCheckUpgrade(UserEntity user, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Ignored non-positive spending amount: {} for User: {}", amount, user.getUid());
            return;
        }

        BigDecimal currentAccumulated = user.getAccumulatedSpending() == null ? BigDecimal.ZERO
                : user.getAccumulatedSpending();
        BigDecimal currentCycle = user.getCycleSpending() == null ? BigDecimal.ZERO : user.getCycleSpending();

        user.setAccumulatedSpending(currentAccumulated.add(amount));
        user.setCycleSpending(currentCycle.add(amount));

        // Use cycleSpending (annual total) to determine level, solving the multi-tier
        // jump issue with large transactions in previous version
        while (!user.getLevel().isMaxLevel()) {
            MembershipLevel nextLevel = user.getLevel().getNext();
            MembershipConfigEntity nextConfig = getConfig(nextLevel);

            if (user.getCycleSpending().compareTo(nextConfig.getMinSpend()) >= 0) {
                if (user.isInfoComplete()) {
                    logger.info("User {} Upgraded from {} to {}", user.getUid(), user.getLevel(), nextLevel);
                    user.setLevel(nextLevel);
                    user.setCycleEndDate(LocalDate.now().plusYears(1));
                    user.setIsInGracePeriod(false);
                } else {
                    logger.info("User {} reached threshold for {} but profile incomplete", user.getUid(), nextLevel);
                    break;
                }
            } else {
                break;
            }
        }

        // Do not reset accumulatedSpending after upgrade, retain progress for grace
        // period or further jumps.
    }

    // === 4. Admin Config ===
    @Override
    @Transactional(readOnly = true)
    public List<MembershipConfigResponseData> getAllConfigs() {
        return configRepository.findAll().stream().map(configDataMapper::toResponseData).toList();
    }

    @Override
    @Transactional
    public MembershipConfigResponseData updateConfig(MembershipLevel level,
            UpdateMembershipConfigRequestData requestData) {
        MembershipConfigEntity config = configRepository.findById(level)
                .orElseThrow(() -> new IllegalArgumentException("Config not found: " + level));
        configEntityMapper.updateEntity(requestData, config);
        MembershipConfigEntity savedConfig = configRepository.save(config);
        configCache.put(level, new CacheEntry(savedConfig)); // [Fix] Update Cache to avoid config inconsistency
        return configDataMapper.toResponseData(savedConfig);
    }
}