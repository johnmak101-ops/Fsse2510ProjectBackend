package com.fsse2510.fsse2510_project_backend.data.user.domainObject.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FirebaseUserData {
    private String firebaseUid;
    private String email;
}
