package com.fsse2510.fsse2510_project_backend.service;

import com.fsse2510.fsse2510_project_backend.data.membership.domainObject.response.MembershipResponseData;
import com.fsse2510.fsse2510_project_backend.data.membership.entity.MembershipConfigEntity;
import com.fsse2510.fsse2510_project_backend.data.membership.membershipLevel.MembershipLevel;
import com.fsse2510.fsse2510_project_backend.data.user.UserRole;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.FirebaseUserData;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.UpdateUserProfileRequestData;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.response.UserData;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;
import com.fsse2510.fsse2510_project_backend.mapper.membership.MembershipDataMapper;
import com.fsse2510.fsse2510_project_backend.mapper.user.UserDataMapper;
import com.fsse2510.fsse2510_project_backend.mapper.user.UserEntityMapper;
import com.fsse2510.fsse2510_project_backend.repository.UserRepository;
import com.fsse2510.fsse2510_project_backend.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserEntityMapper userEntityMapper;
    @Mock
    private UserDataMapper userDataMapper;
    @Mock
    private MembershipService membershipService;
    @Mock
    private MembershipDataMapper membershipDataMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        // Inject admin email list so role-detection logic can run
        ReflectionTestUtils.setField(userService, "adminEmails", "admin@user.com");
    }

    @Test
    void testGetOrCreateUser_NewUser() {
        // Prepare
        FirebaseUserData firebaseUser = new FirebaseUserData("uid123", "test@test.com");
        UserEntity newEntity = new UserEntity();
        newEntity.setUid(1);
        newEntity.setEmail("test@test.com"); // Required for role detection logic

        when(userRepository.findByFirebaseUid("uid123")).thenReturn(Optional.empty());
        when(userEntityMapper.toEntity(firebaseUser)).thenReturn(newEntity);
        when(membershipService.getDefaultLevel()).thenReturn(MembershipLevel.NO_MEMBERSHIP);
        when(userRepository.save(any(UserEntity.class))).thenReturn(newEntity);

        // Mock mapping logic
        when(userDataMapper.toDomain(newEntity)).thenReturn(UserData.builder().uid(1).build());
        when(membershipService.getConfig(any())).thenReturn(new MembershipConfigEntity());
        when(membershipDataMapper.toResponseData(any())).thenReturn(new MembershipResponseData());

        // Execute
        UserData result = userService.getOrCreateUser(firebaseUser);

        // Verify
        assertNotNull(result);
        assertEquals(UserRole.USER, result.getRole()); // Non-admin email should be USER
        verify(userRepository).save(any(UserEntity.class)); // Should save new user
        assertEquals(MembershipLevel.NO_MEMBERSHIP, newEntity.getLevel()); // Check default logic
    }

    @Test
    void testUpdateUserProfile_TriggerUpgrade() {
        // Prepare
        FirebaseUserData firebaseUser = new FirebaseUserData("uid123", "test@test.com");
        UserEntity userEntity = new UserEntity();
        userEntity.setUid(1);
        userEntity.setEmail("test@test.com"); // Required for role detection logic

        when(userRepository.findByFirebaseUid("uid123")).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        // Mock mapping
        when(userDataMapper.toDomain(userEntity)).thenReturn(UserData.builder().uid(1).build());
        when(membershipService.getConfig(any())).thenReturn(new MembershipConfigEntity());

        // Execute
        userService.updateUserProfile(firebaseUser, UpdateUserProfileRequestData.builder().fullName("John").build());

        // Verify
        // Most importantly, check if membershipService.accumulateAndCheckUpgrade is
        // called
        verify(membershipService).accumulateAndCheckUpgrade(eq(userEntity), eq(BigDecimal.ZERO));
        verify(userRepository).save(userEntity);
    }

    @Test
    void testGetOrCreateUser_AdminUser_ShouldHaveAdminRole() {
        // Prepare
        FirebaseUserData firebaseUser = new FirebaseUserData("adminUid", "admin@user.com");
        UserEntity adminEntity = new UserEntity();
        adminEntity.setUid(99);
        adminEntity.setEmail("admin@user.com");

        when(userRepository.findByFirebaseUid("adminUid")).thenReturn(Optional.empty());
        when(userEntityMapper.toEntity(firebaseUser)).thenReturn(adminEntity);
        when(membershipService.getDefaultLevel()).thenReturn(MembershipLevel.NO_MEMBERSHIP);
        when(userRepository.save(any(UserEntity.class))).thenReturn(adminEntity);
        when(userDataMapper.toDomain(adminEntity))
                .thenReturn(UserData.builder().uid(99).email("admin@user.com").build());
        when(membershipService.getConfig(any())).thenReturn(new MembershipConfigEntity());
        when(membershipDataMapper.toResponseData(any())).thenReturn(new MembershipResponseData());

        // Execute
        UserData result = userService.getOrCreateUser(firebaseUser);

        // Verify
        assertNotNull(result);
        assertEquals(UserRole.ADMIN, result.getRole()); // admin@user.com should get ADMIN role
    }
}