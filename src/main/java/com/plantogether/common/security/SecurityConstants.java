package com.plantogether.common.security;

public final class SecurityConstants {
    public static final String CLAIM_SUB = "sub";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_GIVEN_NAME = "given_name";
    public static final String CLAIM_FAMILY_NAME = "family_name";
    public static final String CLAIM_DISPLAY_NAME = "preferred_username";
    public static final String ROLE_PREFIX = "ROLE_";
    public static final String REALM_ACCESS = "realm_access";
    public static final String ROLES = "roles";

    private SecurityConstants() {
    }
}
