package org.bedrock.teateach.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bedrock.teateach.config.SecurityConfig;
import org.bedrock.teateach.dto.*;
import org.bedrock.teateach.security.JwtAuthenticationFilter;
import org.bedrock.teateach.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @InjectMocks
    private AuthController authController;

    private RegisterRequest registerRequest;
    private AuthRequest authRequest;
    private AuthResponse authResponse;

    @BeforeEach
    public void setup() {
        // Initialize MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler()) // Create this class if needed
                .build();

        // Initialize ObjectMapper
        objectMapper = new ObjectMapper();

        // Setup register request
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password");
        registerRequest.setEmail("test@example.com");
        registerRequest.setUserType("STUDENT");
        registerRequest.setName("Test User");
        registerRequest.setStudentId("S12345");
        registerRequest.setMajor("Computer Science");

        // Setup auth request
        authRequest = new AuthRequest();
        authRequest.setUsername("testuser");
        authRequest.setPassword("password");

        // Setup auth response
        authResponse = AuthResponse.builder()
                .token("jwt-token")
                .userType("STUDENT")
                .username("testuser")
                .referenceId("S12345")
                .build();
    }

    @Test
    public void testRegister() throws Exception {
        // Given
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/register") // Path is relative to controller's base path
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.userType").value("STUDENT"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.referenceId").value("S12345"));
    }

    @Test
    public void testLogin() throws Exception {
        // Given
        when(authService.login(any(AuthRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login") // Path is relative to controller's base path
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.userType").value("STUDENT"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.referenceId").value("S12345"));
    }

    @Test
    public void testLoginWithInvalidCredentials() throws Exception {
        // Given
        when(authService.login(any(AuthRequest.class))).thenThrow(new BadCredentialsException("Invalid username or password"));

        // When & Then
        mockMvc.perform(post("/api/auth/login") // Path is relative to controller's base path
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testForgotPassword() throws Exception {
        // Given
        PasswordResetRequest resetRequest = new PasswordResetRequest();
        resetRequest.setEmail("test@example.com");

        doNothing().when(authService).requestPasswordReset(any(PasswordResetRequest.class));

        // When & Then
        mockMvc.perform(post("/api/auth/forgot-password") // Path is relative to controller's base path
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isOk());

        verify(authService).requestPasswordReset(any(PasswordResetRequest.class));
    }

    @Test
    public void testResetPassword() throws Exception {
        // Given
        PasswordUpdateRequest updateRequest = new PasswordUpdateRequest();
        updateRequest.setToken("reset-token");
        updateRequest.setPassword("newpassword");

        doNothing().when(authService).resetPassword(any(PasswordUpdateRequest.class));

        // When & Then
        mockMvc.perform(post("/api/auth/reset-password") // Path is relative to controller's base path
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        verify(authService).resetPassword(any(PasswordUpdateRequest.class));
    }
}
