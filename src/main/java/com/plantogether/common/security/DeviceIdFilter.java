package com.plantogether.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

public class DeviceIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String deviceIdHeader = request.getHeader(SecurityConstants.DEVICE_ID_HEADER);
        if (deviceIdHeader != null && !deviceIdHeader.isBlank()) {
            try {
                UUID deviceId = UUID.fromString(deviceIdHeader.trim());
                var auth = new UsernamePasswordAuthenticationToken(
                        deviceId.toString(), null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (IllegalArgumentException ignored) {
                // Invalid UUID format — leave SecurityContext unauthenticated
            }
        }
        filterChain.doFilter(request, response);
    }
}
