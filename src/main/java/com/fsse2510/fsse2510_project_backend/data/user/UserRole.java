package com.fsse2510.fsse2510_project_backend.data.user;

/**
 * Represents the application-level role of a user.
 * Roles are determined at runtime based on the {@code app.admin.emails}
 * configuration.
 */
public enum UserRole {
    /** Standard authenticated user with no elevated privileges. */
    USER,
    /** Administrator with access to the admin dashboard and management APIs. */
    ADMIN
}
