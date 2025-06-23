package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.Student;
import org.bedrock.teateach.beans.Teacher;
import org.bedrock.teateach.beans.User;
import org.bedrock.teateach.dto.AuthRequest;
import org.bedrock.teateach.dto.AuthResponse;
import org.bedrock.teateach.dto.PasswordResetRequest;
import org.bedrock.teateach.dto.RegisterRequest;
import org.bedrock.teateach.mappers.StudentMapper;
import org.bedrock.teateach.mappers.TeacherMapper;
import org.bedrock.teateach.mappers.UserMapper;
import org.bedrock.teateach.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private StudentMapper studentMapper;

    @Mock
    private TeacherMapper teacherMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest studentRegisterRequest;
    private RegisterRequest teacherRegisterRequest;
    private User mockUser;

    @BeforeEach
    public void setup() {
        // Setup student registration request
        studentRegisterRequest = new RegisterRequest();
        studentRegisterRequest.setUsername("student1");
        studentRegisterRequest.setPassword("password");
        studentRegisterRequest.setEmail("student@example.com");
        studentRegisterRequest.setUserType("STUDENT");
        studentRegisterRequest.setName("Student Name");
        studentRegisterRequest.setStudentId("S12345");
        studentRegisterRequest.setMajor("Computer Science");

        // Setup teacher registration request
        teacherRegisterRequest = new RegisterRequest();
        teacherRegisterRequest.setUsername("teacher1");
        teacherRegisterRequest.setPassword("password");
        teacherRegisterRequest.setEmail("teacher@example.com");
        teacherRegisterRequest.setUserType("TEACHER");
        teacherRegisterRequest.setName("Teacher Name");
        teacherRegisterRequest.setTeacherId("T12345");
        teacherRegisterRequest.setDepartment("Computer Science");

        // Setup mock user
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("existinguser");
        mockUser.setPassword("encodedPassword");
        mockUser.setEmail("existing@example.com");
        mockUser.setUserType("STUDENT");
        mockUser.setReferenceId("S54321");
        mockUser.setActive(true);
    }

    @Test
    public void testRegisterStudent() {
        // Given
        when(userMapper.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userMapper.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(jwtService.generateToken(anyString(), anyString(), anyString())).thenReturn("jwt-token");

        // When
        AuthResponse response = authService.register(studentRegisterRequest);

        // Then
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("STUDENT", response.getUserType());
        assertEquals("student1", response.getUsername());

        // Verify student was created
        ArgumentCaptor<Student> studentCaptor = ArgumentCaptor.forClass(Student.class);
        verify(studentMapper).insert(studentCaptor.capture());
        assertEquals("S12345", studentCaptor.getValue().getStudentId());
        assertEquals("Student Name", studentCaptor.getValue().getName());

        // Verify user was created
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(userCaptor.capture());
        assertEquals("student1", userCaptor.getValue().getUsername());
        assertEquals("encodedPassword", userCaptor.getValue().getPassword());
        assertEquals("STUDENT", userCaptor.getValue().getUserType());
    }

    @Test
    public void testRegisterTeacher() {
        // Given
        when(userMapper.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userMapper.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(jwtService.generateToken(anyString(), anyString(), anyString())).thenReturn("jwt-token");

        // When
        AuthResponse response = authService.register(teacherRegisterRequest);

        // Then
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("TEACHER", response.getUserType());
        assertEquals("teacher1", response.getUsername());

        // Verify teacher was created
        ArgumentCaptor<Teacher> teacherCaptor = ArgumentCaptor.forClass(Teacher.class);
        verify(teacherMapper).insert(teacherCaptor.capture());
        assertEquals("T12345", teacherCaptor.getValue().getTeacherId());
        assertEquals("Teacher Name", teacherCaptor.getValue().getName());

        // Verify user was created
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(userCaptor.capture());
        assertEquals("teacher1", userCaptor.getValue().getUsername());
        assertEquals("encodedPassword", userCaptor.getValue().getPassword());
        assertEquals("TEACHER", userCaptor.getValue().getUserType());
    }

    @Test
    public void testRegisterWithExistingUsername() {
        // Given
        when(userMapper.findByUsername("student1")).thenReturn(Optional.of(mockUser));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> authService.register(studentRegisterRequest));
        verify(studentMapper, never()).insert(any());
        verify(userMapper, never()).insert(any());
    }

    @Test
    public void testLogin() {
        // Given
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("existinguser");
        authRequest.setPassword("password");

        when(userMapper.findByUsername("existinguser")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken("existinguser", "STUDENT", "S54321")).thenReturn("jwt-token");

        // When
        AuthResponse response = authService.login(authRequest);

        // Then
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("STUDENT", response.getUserType());
        assertEquals("existinguser", response.getUsername());
        assertEquals("S54321", response.getReferenceId());
    }

    @Test
    public void testLoginWithInvalidCredentials() {
        // Given
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("existinguser");
        authRequest.setPassword("wrongpassword");

        when(userMapper.findByUsername("existinguser")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        // When & Then
        assertThrows(BadCredentialsException.class, () -> authService.login(authRequest));
    }

    @Test
    public void testRequestPasswordReset() {
        // Given
        PasswordResetRequest request = new PasswordResetRequest();
        request.setEmail("existing@example.com");

        when(userMapper.findByEmail("existing@example.com")).thenReturn(Optional.of(mockUser));

        // When
        authService.requestPasswordReset(request);

        // Then
        verify(userMapper).updateResetToken(eq("existing@example.com"), anyString(), any(LocalDateTime.class));
        verify(emailService).sendPasswordResetEmail(eq("existing@example.com"), anyString());
    }
}
