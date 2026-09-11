package top.productivitytools.fitness.api.security;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import top.productivitytools.fitness.api.entities.FitnessUser;
import top.productivitytools.fitness.api.repositories.FitnessUserRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class FirebaseAuthFilter extends OncePerRequestFilter {

    public static final Set<String> ALLOWED_EMAILS = Set.of(
            "pwujczyk@gmail.com",
            "pwujczyk@google.com"
    );

    private final FitnessUserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getServletPath();
        if (path == null || path.isEmpty()) {
            path = request.getRequestURI();
        }
        return path != null && (path.startsWith("/api/debug") || path.startsWith("/error"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
                return;
            }

            String token = authHeader.substring(7).trim();
            FitnessUser user;
            try {
                user = resolveUserFromToken(token);
            } catch (AuthException e) {
                sendError(response, e.getStatusCode(), e.getMessage());
                return;
            }

            if (user == null) {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired authentication token");
                return;
            }

            UserContext.setCurrentUser(user);
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String errorName = (status == HttpServletResponse.SC_FORBIDDEN) ? "Forbidden" : "Unauthorized";
        response.getWriter().write(String.format("{\"error\":\"%s\",\"message\":\"%s\"}", errorName, message));
    }

    private FitnessUser resolveUserFromToken(String token) throws AuthException {
        if (token == null || token.isBlank()) {
            return null;
        }

        String[] parts = token.split("\\.");
        if (parts.length >= 2) {
            try {
                byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
                String payloadJson = new String(decoded, StandardCharsets.UTF_8);
                JsonNode payload = objectMapper.readTree(payloadJson);

                if (payload.hasNonNull("exp")) {
                    long exp = payload.get("exp").asLong();
                    if (exp > 0 && Instant.now().getEpochSecond() > exp) {
                        log.warn("Token is expired (exp: {})", exp);
                        return null;
                    }
                }

                String email = payload.hasNonNull("email") ? payload.get("email").asText() : null;
                String name = payload.hasNonNull("name") ? payload.get("name").asText() : null;
                String uid = payload.hasNonNull("user_id")
                        ? payload.get("user_id").asText()
                        : (payload.hasNonNull("sub") ? payload.get("sub").asText() : null);

                if (email == null && uid != null) {
                    email = uid + "@firebase.user";
                }

                if (email != null) {
                    if (!isEmailAllowed(email)) {
                        log.warn("Access denied for email: {}. Allowed emails: {}", email, ALLOWED_EMAILS);
                        throw new AuthException(HttpServletResponse.SC_FORBIDDEN,
                                "Access denied. Only authorized users " + ALLOWED_EMAILS + " are permitted.");
                    }
                    return getOrCreateUser(email, name);
                }
            } catch (AuthException e) {
                throw e;
            } catch (Exception e) {
                log.warn("Failed to decode Bearer token as JWT: {}", e.getMessage());
            }
        }

        // Support dev/testing fallback: Bearer <email> or Bearer <userId>
        if (token.contains("@")) {
            if (!isEmailAllowed(token)) {
                log.warn("Access denied for email: {}. Allowed emails: {}", token, ALLOWED_EMAILS);
                throw new AuthException(HttpServletResponse.SC_FORBIDDEN,
                        "Access denied. Only authorized users " + ALLOWED_EMAILS + " are permitted.");
            }
            return getOrCreateUser(token, null);
        }
        try {
            Long id = Long.parseLong(token);
            FitnessUser user = userRepository.findById(id).orElse(null);
            if (user != null && !isEmailAllowed(user.getEmail())) {
                log.warn("Access denied for userId {} with email {}. Allowed emails: {}", id, user.getEmail(), ALLOWED_EMAILS);
                throw new AuthException(HttpServletResponse.SC_FORBIDDEN,
                        "Access denied. Only authorized users " + ALLOWED_EMAILS + " are permitted.");
            }
            return user;
        } catch (NumberFormatException ignored) {
        }

        return null;
    }

    private boolean isEmailAllowed(String email) {
        if (email == null) {
            return false;
        }
        String trimmed = email.trim();
        return ALLOWED_EMAILS.stream().anyMatch(allowed -> allowed.equalsIgnoreCase(trimmed));
    }

    private FitnessUser getOrCreateUser(String email, String name) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    try {
                        FitnessUser newUser = new FitnessUser();
                        newUser.setEmail(email);
                        String username = (name != null && !name.isBlank()) ? name : email.split("@")[0];
                        newUser.setUsername(username);
                        newUser.setDefaultRestTimerSeconds(90);
                        return userRepository.save(newUser);
                    } catch (Exception e) {
                        return userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("Failed to get or create user: " + email, e));
                    }
                });
    }

    private static class AuthException extends Exception {
        private final int statusCode;

        public AuthException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}
