package com.plantogether.common.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Acceptance test for Story 1.2 — AC3:
 * SecurityConstants.DEVICE_ID_HEADER must equal "X-Device-Id".
 * No service should hardcode the header name string directly.
 */
class SecurityConstantsTest {

    @Test
    void deviceIdHeader_hasCorrectValue() {
        assertThat(SecurityConstants.DEVICE_ID_HEADER).isEqualTo("X-Device-Id");
    }

    @Test
    void deviceIdHeader_isNotBlank() {
        assertThat(SecurityConstants.DEVICE_ID_HEADER).isNotBlank();
    }
}
