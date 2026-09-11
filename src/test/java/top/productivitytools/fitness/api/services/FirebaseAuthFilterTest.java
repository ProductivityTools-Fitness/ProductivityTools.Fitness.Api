package top.productivitytools.fitness.api.services;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.ObjectMapper;
import top.productivitytools.fitness.api.entities.FitnessUser;
import top.productivitytools.fitness.api.repositories.FitnessUserRepository;
import top.productivitytools.fitness.api.security.FirebaseAuthFilter;
import top.productivitytools.fitness.api.security.UserContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FirebaseAuthFilterTest {

    @Mock
    private FitnessUserRepository userRepository;

    @Mock
    private FilterChain filterChain;

    private FirebaseAuthFilter filter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        filter = new FirebaseAuthFilter(userRepository, objectMapper);
    }

    @Test
    void doFilter_WithValidFirebaseBearerToken_ResolvesAndSetsUserInContext() throws ServletException, IOException {
        String headerJson = "{\"alg\":\"RS256\",\"typ\":\"JWT\"}";
        long futureExp = Instant.now().getEpochSecond() + 3600;
        String payloadJson = String.format("{\"email\":\"john.doe@example.com\",\"name\":\"John Doe\",\"sub\":\"firebaseUid123\",\"exp\":%d}", futureExp);
        String dummySig = "signature";

        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(headerJson.getBytes(StandardCharsets.UTF_8))
                + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8))
                + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(dummySig.getBytes(StandardCharsets.UTF_8));

        FitnessUser user = new FitnessUser();
        user.setId(42L);
        user.setEmail("john.doe@example.com");
        user.setUsername("John Doe");

        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/workout/list");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        AtomicReference<FitnessUser> capturedUserDuringFilter = new AtomicReference<>();
        doAnswer(invocation -> {
            capturedUserDuringFilter.set(UserContext.getCurrentUser());
            return null;
        }).when(filterChain).doFilter(any(), any());

        filter.doFilter(request, response, filterChain);

        assertNotNull(capturedUserDuringFilter.get());
        assertEquals("john.doe@example.com", capturedUserDuringFilter.get().getEmail());
        assertEquals(42L, capturedUserDuringFilter.get().getId());
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());

        // Ensure UserContext is cleared after request finishes
        assertNull(UserContext.getCurrentUser());
    }

    @Test
    void doFilter_WithoutAuthorizationHeader_Returns401AndStopsChain() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/workout/list");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertTrue(response.getContentAsString().contains("Missing or invalid Authorization header"));
        verify(filterChain, never()).doFilter(any(), any());
        assertNull(UserContext.getCurrentUser());
    }

    @Test
    void doFilter_WithExpiredToken_Returns401AndStopsChain() throws ServletException, IOException {
        String headerJson = "{\"alg\":\"RS256\",\"typ\":\"JWT\"}";
        long pastExp = Instant.now().getEpochSecond() - 3600;
        String payloadJson = String.format("{\"email\":\"john.doe@example.com\",\"name\":\"John Doe\",\"sub\":\"firebaseUid123\",\"exp\":%d}", pastExp);
        String dummySig = "signature";

        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(headerJson.getBytes(StandardCharsets.UTF_8))
                + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8))
                + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(dummySig.getBytes(StandardCharsets.UTF_8));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/workout/list");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertTrue(response.getContentAsString().contains("Invalid or expired authentication token"));
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilter_WithOptionsRequest_SkipsFilterAndContinuesChain() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("OPTIONS");
        request.setServletPath("/api/workout/list");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }

    @Test
    void doFilter_WithDebugEndpoint_SkipsFilterAndContinuesChain() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setServletPath("/api/debug/hello");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    }
}
