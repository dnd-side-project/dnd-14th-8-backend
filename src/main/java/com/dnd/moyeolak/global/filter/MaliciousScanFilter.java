package com.dnd.moyeolak.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MaliciousScanFilter extends OncePerRequestFilter {

    private static final List<String> SCAN_PREFIXES = List.of(
            "/jmx-console", "/invoker", "/web-console",
            "/manager", "/admin", "/phpmyadmin",
            "/.env", "/wp-admin", "/wp-login"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();
        boolean isScan = SCAN_PREFIXES.stream().anyMatch(path::startsWith);
        if (isScan) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        chain.doFilter(request, response);
    }
}
