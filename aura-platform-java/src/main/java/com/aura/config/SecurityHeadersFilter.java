package com.aura.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (response instanceof HttpServletResponse httpRes) {
            httpRes.setHeader("X-Content-Type-Options", "nosniff");
            httpRes.setHeader("X-Frame-Options", "SAMEORIGIN");
            httpRes.setHeader("X-XSS-Protection", "1; mode=block");
            httpRes.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
            // Allow Google OAuth scripts, fonts, and inline styles used by glassmorphism UI
            httpRes.setHeader("Content-Security-Policy",
                    "default-src 'self' * data: blob: 'unsafe-inline' 'unsafe-eval'; " +
                    "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdn.jsdelivr.net https://accounts.google.com https://apis.google.com https://www.gstatic.com; " +
                    "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://accounts.google.com https://cdn.jsdelivr.net; " +
                    "font-src 'self' https://fonts.gstatic.com data:; " +
                    "connect-src 'self' * ws: wss:; " +
                    "frame-src 'self' https://accounts.google.com;");
        }
        chain.doFilter(request, response);
    }
}
