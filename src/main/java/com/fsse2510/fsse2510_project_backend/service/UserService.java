package com.fsse2510.fsse2510_project_backend.service;

import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.FirebaseUserData;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.request.UpdateUserProfileRequestData;
import com.fsse2510.fsse2510_project_backend.data.user.domainObject.response.UserData;
import com.fsse2510.fsse2510_project_backend.data.user.entity.UserEntity;

public interface UserService {

    UserData getOrCreateUser(FirebaseUserData firebaseUserData);

    UserData updateUserProfile(FirebaseUserData firebaseUserData, UpdateUserProfileRequestData requestData);

    void saveUser(UserEntity user);

    UserEntity findEntityByFirebaseUid(String firebaseUid);

    UserEntity findEntityByIdWithLock(Integer uid);
}
