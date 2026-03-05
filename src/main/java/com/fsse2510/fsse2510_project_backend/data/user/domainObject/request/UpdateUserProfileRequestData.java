package com.fsse2510.fsse2510_project_backend.data.user.domainObject.request;

import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserProfileRequestData {
    private String fullName;
    private String phoneNumber;
    private String address;
    private LocalDate birthday;
}
