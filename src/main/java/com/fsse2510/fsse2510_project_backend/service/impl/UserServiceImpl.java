package com.fsse2510.fsse2510_project_backend.service.impl;

import com.fsse2510.fsse2510_project_backend.data.membership.entity.MembershipConfigEntity;
import com.fsse2510.fsse2510_project_backend.data.user.UserRole;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.FirebaseUserData;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.UpdateUserProfileRequestData;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.response.UserData;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;
import com.fsse2510.fsse2510_project_backend.exception.user.UserNotFoundException;
import com.fsse2510.fsse2510_project_backend.mapper.membership.MembershipDataMapper;
import com.fsse2510.fsse2510_project_backend.mapper.user.UserDataMapper;
import com.fsse2510.fsse2510_project_backend.mapper.user.UserEntityMapper;
import com.fsse2510.fsse2510_project_backend.repository.UserRepository;
import com.fsse2510.fsse2510_project_backend.service.MembershipService;
import com.fsse2510.fsse2510_project_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserEntityMapper userEntityMapper;
    private final UserDataMapper userDataMapper;
    private final MembershipService membershipService;
    private final MembershipDataMapper membershipDataMapper;

    @Value("${app.admin.emails:admin@user.com}")
    private String adminEmails;

    private UserData entityToDomain(UserEntity userEntity) {
        if (userEntity == null)
            return null;

        UserData data = userDataMapper.toDomain(userEntity);

        // Resolve role: check if user's email is in the admin list
        List<String> adminList = Arrays.asList(adminEmails.split(","));
        boolean isAdmin = userEntity.getEmail() != null && adminList.contains(userEntity.getEmail().trim());
        data.setRole(isAdmin ? UserRole.ADMIN : UserRole.USER);

        try {
            if (userEntity.getLevel() != null) {
                MembershipConfigEntity config = membershipService.getConfig(userEntity.getLevel());
                data.setMembership(membershipDataMapper.toResponseData(config));
            }
        } catch (Exception e) {
            log.warn("Failed to load membership config for level: {}", userEntity.getLevel(), e);
        }
        return data;
    }

    @Override
    @Transactional
    public UserData getOrCreateUser(FirebaseUserData firebaseUserData) {
        Optional<UserEntity> userOptional = userRepository.findByFirebaseUid(firebaseUserData.getFirebaseUid());

        if (userOptional.isPresent()) {
            return entityToDomain(userOptional.get());
        }

        return createUserHelper(firebaseUserData);
    }

    private UserData createUserHelper(FirebaseUserData firebaseUserData) {
        UserEntity newEntity = userEntityMapper.toEntity(firebaseUserData);
        newEntity.setLevel(membershipService.getDefaultLevel());
        newEntity.setAccumulatedSpending(BigDecimal.ZERO);
        newEntity.setPoints(BigDecimal.ZERO);
        newEntity.setIsInGracePeriod(false);
        newEntity.setCycleEndDate(null);

        UserEntity savedEntity = userRepository.save(newEntity);
        return entityToDomain(savedEntity);
    }

    @Override
    @Transactional
    public UserData updateUserProfile(FirebaseUserData firebaseUserData,
            UpdateUserProfileRequestData requestData) {
        UserEntity user = userRepository.findByFirebaseUid(firebaseUserData.getFirebaseUid())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + firebaseUserData.getFirebaseUid()));

        userEntityMapper.updateEntity(requestData, user);

        membershipService.accumulateAndCheckUpgrade(user, BigDecimal.ZERO);

        return entityToDomain(userRepository.save(user));
    }

    @Override
    @Transactional
    public void saveUser(UserEntity user) {
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserEntity findEntityByFirebaseUid(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + firebaseUid));
    }

    @Override
    @Transactional
    public UserEntity findEntityByIdWithLock(Integer uid) {
        return userRepository.findByIdWithLock(uid)
                .orElseThrow(() -> new UserNotFoundException("User not found: uid=" + uid));
    }

}