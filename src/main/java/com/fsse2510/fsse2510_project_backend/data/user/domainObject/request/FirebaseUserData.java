package com.fsse2510.fsse2510_project_backend.data.user.domainObject.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FirebaseUserData {
    private String firebaseUid;
    private String email;
}
