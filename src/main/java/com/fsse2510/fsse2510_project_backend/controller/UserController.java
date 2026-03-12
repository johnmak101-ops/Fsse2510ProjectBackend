package com.fsse2510.fsse2510_project_backend.controller;

import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.FirebaseUserData;
import com.fsse2510.fsse2510_project_backend.data.user.dto.request.UpdateUserProfileRequestDto;
import com.fsse2510.fsse2510_project_backend.data.user.dto.response.UserResponseDto;
import com.fsse2510.fsse2510_project_backend.mapper.user.UserDataMapper;
import com.fsse2510.fsse2510_project_backend.mapper.user.UserDtoMapper;
import com.fsse2510.fsse2510_project_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
/**
 * Controller for managing user profiles.
 * <p>
 * Handles user authentication/synchronization (Firebase -> DB) and profile
 * updates.
 * </p>
 */
public class UserController {
    private final UserService userService;
    private final UserDataMapper userDataMapper;
    private final UserDtoMapper userDtoMapper;

    private FirebaseUserData getFirebaseUser(JwtAuthenticationToken token) {
        String email = (String) token.getTokenAttributes().get("email");
        return FirebaseUserData.builder()
                .firebaseUid(token.getToken().getSubject())
                .email(email != null ? email.trim() : null)
                .build();
    }

    /**
     * Retrieves the current user's profile information.
     * <p>
     * Endpoint: GET /users/me
     * If the user doesn't exist in the local DB yet (first login), they are
     * created.
     * </p>
     *
     * @param token The JWT authentication token.
     * @return UserResponseDto containing profile details.
     */
    @GetMapping("/me")
    public UserResponseDto getMe(JwtAuthenticationToken token) {
        return userDtoMapper.toResponseDto(
                userService.getOrCreateUser(getFirebaseUser(token)));
    }

    /**
     * Updates the user's profile information.
     * <p>
     * Endpoint: PATCH /users/profile
     * </p>
     *
     * @param token      The JWT authentication token.
     * @param requestDto DTO containing fields to update (firstName, lastName,
     *                   etc.).
     * @return UserResponseDto containing updated profile details.
     */
    @PatchMapping("/profile")
    public UserResponseDto updateProfile(JwtAuthenticationToken token,
            @Valid @RequestBody UpdateUserProfileRequestDto requestDto) {
        var requestData = userDataMapper.toUpdateData(requestDto);
        return userDtoMapper.toResponseDto(
                userService.updateUserProfile(getFirebaseUser(token), requestData));
    }
}
