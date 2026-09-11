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

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class FirebaseAuthFilter extends OncePerRequestFilter {

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
                sendUnauthorizedError(response, "Missing or invalid Authorization header");
                return;
            }

            String token = authHeader.substring(7).trim();
            FitnessUser user = resolveUserFromToken(token);
            if (user == null) {
                sendUnauthorizedError(response, "Invalid or expired authentication token");
                return;
            }

            UserContext.setCurrentUser(user);
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }

    private void sendUnauthorizedError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(String.format("{\"error\":\"Unauthorized\",\"message\":\"%s\"}", message));
    }

    private FitnessUser resolveUserFromToken(String token) {
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
                    return getOrCreateUser(email, name);
                }
            } catch (Exception e) {
                log.warn("Failed to decode Bearer token as JWT: {}", e.getMessage());
            }
        }

        // Support dev/testing fallback: Bearer <email> or Bearer <userId>
        if (token.contains("@")) {
            return getOrCreateUser(token, null);
        }
        try {
            Long id = Long.parseLong(token);
            return userRepository.findById(id).orElse(null);
        } catch (NumberFormatException ignored) {
        }

        return null;
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
}
