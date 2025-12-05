//package org.example.bloodrequestservice;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//
//    private final JwtUtil jwtUtil;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//
//        // Получаем токен из заголовка Authorization или из query параметра token
//        String token = extractToken(request);
//
//        log.debug("Request URI: {}, Token: {}", request.getRequestURI(), token != null ? "present" : "absent");
//
//        if (token != null && jwtUtil.validateToken(token)) {
//            try {
//                // Извлекаем информацию из токена
//                String email = jwtUtil.extractEmail(token);
//                String role = jwtUtil.extractRole(token);
//                Long userId = jwtUtil.extractUserId(token);
//
//                log.debug("Authenticated user - Email: {}, Role: {}, UserId: {}", email, role, userId);
//
//                // Добавляем параметры в атрибуты запроса для использования в контроллерах
//                request.setAttribute("userEmail", email);
//                request.setAttribute("userRole", role);
//                request.setAttribute("userId", userId);
//
//            } catch (Exception e) {
//                log.warn("Failed to extract JWT claims: {}", e.getMessage());
//            }
//        } else if (token != null) {
//            log.warn("Invalid JWT token provided");
//        }
//
//        filterChain.doFilter(request, response);
//    }
//
//    private String extractToken(HttpServletRequest request) {
//        // Проверяем заголовок Authorization
//        String authHeader = request.getHeader("Authorization");
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            return authHeader.substring(7);
//        }
//
//        // Проверяем query параметр token
//        String tokenParam = request.getParameter("token");
//        if (tokenParam != null && !tokenParam.trim().isEmpty()) {
//            return tokenParam;
//        }
//
//        // Проверяем query параметр authorization (для API Gateway)
//        String authParam = request.getParameter("authorization");
//        if (authParam != null && authParam.startsWith("Bearer ")) {
//            return authParam.substring(7);
//        }
//
//        return null;
//    }
//
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        // Исключаем публичные эндпоинты из фильтрации
//        String path = request.getServletPath();
//
//        // Публичные API эндпоинты
//        if (path.startsWith("/api/")) {
//            return true; // Пропускаем API эндпоинты
//        }
//
//        // Публичные статические ресурсы (если есть)
//        if (path.startsWith("/css/") || path.startsWith("/js/") ||
//                path.startsWith("/images/") || path.startsWith("/webjars/")) {
//            return true;
//        }
//
//        // Публичные страницы (если нужны)
//        if (path.equals("/") || path.equals("/login") || path.equals("/public")) {
//            return true;
//        }
//
//        return false;
//    }
//}


package org.example.bloodrequestservice;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.debug("=== JWT FILTER START: {} ===", request.getRequestURI());

        // Пробуем разные способы получения токена
        String token = extractTokenFromRequest(request);
        log.debug("Extracted token: {}", token != null ? "present" : "null");

        if (token != null && jwtUtil.validateToken(token)) {
            try {
                String email = jwtUtil.extractEmail(token);
                String role = jwtUtil.extractRole(token);
                Long userId = jwtUtil.extractUserId(token);

                log.debug("Authenticated user - Email: {}, Role: {}, UserId: {}", email, role, userId);

                // Устанавливаем атрибуты
                request.setAttribute("userEmail", email);
                request.setAttribute("userRole", role);
                request.setAttribute("userId", userId);
                request.setAttribute("jwtToken", token);

            } catch (Exception e) {
                log.warn("Failed to process JWT token: {}", e.getMessage());
            }
        } else {
            log.debug("No valid token found");
        }

        filterChain.doFilter(request, response);

        log.debug("=== JWT FILTER END ===");
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        // 1. Из заголовка Authorization
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.debug("Found token in Authorization header");
            return token;
        }

        // 2. Из query параметра token
        String tokenParam = request.getParameter("token");
        if (tokenParam != null && !tokenParam.trim().isEmpty()) {
            log.debug("Found token in query parameter");
            return tokenParam;
        }

        // 3. Из cookie
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if ("token".equals(cookie.getName()) || "jwt".equals(cookie.getName())) {
                    log.debug("Found token in cookie");
                    return cookie.getValue();
                }
            }
        }

        // 4. Из заголовка X-Auth-Token
        String xAuthToken = request.getHeader("X-Auth-Token");
        if (xAuthToken != null && !xAuthToken.trim().isEmpty()) {
            log.debug("Found token in X-Auth-Token header");
            return xAuthToken;
        }

        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        // Пропускаем публичные endpoints
        boolean isPublic = path.startsWith("/api/") ||
                path.startsWith("/debug/") ||
                path.startsWith("/health") ||
                path.startsWith("/test/");

        if (isPublic) {
            log.debug("Skipping JWT filter for public path: {}", path);
        }

        return isPublic;
    }
}