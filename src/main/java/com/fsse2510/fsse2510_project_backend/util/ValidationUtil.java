package com.fsse2510.fsse2510_project_backend.util;

public class ValidationUtil {

    /**
     * Checks if the user's profile information is complete enough for membership
     * upgrades.
     * Required fields: Full Name (no numbers), Phone Number (min 8 chars).
     */
    public static boolean isProfileComplete(String fullName, String phoneNumber) {
        if (fullName == null || fullName.isBlank() || phoneNumber == null || phoneNumber.isBlank()) {
            return false;
        }

        boolean isNameValid = fullName.matches("^[\\p{L}\\s]+$");

        boolean isPhoneValid = phoneNumber.trim().length() >= 8;

        return isNameValid && isPhoneValid;
    }
}
