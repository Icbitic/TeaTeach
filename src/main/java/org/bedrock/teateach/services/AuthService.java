package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.Student;
import org.bedrock.teateach.beans.Teacher;
import org.bedrock.teateach.beans.User;
import org.bedrock.teateach.dto.*;
import org.bedrock.teateach.mappers.StudentMapper;
import org.bedrock.teateach.mappers.TeacherMapper;
import org.bedrock.teateach.mappers.UserMapper;
import org.bedrock.teateach.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    public AuthService(UserMapper userMapper, StudentMapper studentMapper, TeacherMapper teacherMapper,
                      PasswordEncoder passwordEncoder, JwtService jwtService, EmailService emailService) {
        this.userMapper = userMapper;
        this.studentMapper = studentMapper;
        this.teacherMapper = teacherMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userMapper.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email already exists
        if (userMapper.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        String referenceId;

        // Create student or teacher record
        if ("STUDENT".equals(request.getUserType())) {
            Student student = new Student();
            student.setStudentId(request.getStudentId());
            student.setName(request.getName());
            student.setEmail(request.getEmail());
            student.setMajor(request.getMajor());
            student.setDateOfBirth(LocalDate.now()); // This should be set properly in a real application

            studentMapper.insert(student);
            referenceId = student.getStudentId();
        } else if ("TEACHER".equals(request.getUserType())) {
            Teacher teacher = new Teacher();
            teacher.setTeacherId(request.getTeacherId());
            teacher.setName(request.getName());
            teacher.setEmail(request.getEmail());
            teacher.setDepartment(request.getDepartment());
            teacher.setDateOfBirth(LocalDate.now()); // This should be set properly in a real application

            teacherMapper.insert(teacher);
            referenceId = teacher.getTeacherId();
        } else {
            throw new IllegalArgumentException("Invalid user type");
        }

        // Create user record
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setUserType(request.getUserType());
        user.setReferenceId(referenceId);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);

        // Generate JWT
        String token = jwtService.generateToken(user.getUsername(), user.getUserType(), user.getReferenceId());

        // Return response
        return AuthResponse.builder()
                .token(token)
                .userType(user.getUserType())
                .username(user.getUsername())
                .referenceId(user.getReferenceId())
                .build();
    }

    public AuthResponse login(AuthRequest request) {
        // Find user by username
        User user = userMapper.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        // Check if password matches
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        // Generate JWT
        String token = jwtService.generateToken(user.getUsername(), user.getUserType(), user.getReferenceId());

        // Return response
        return AuthResponse.builder()
                .token(token)
                .userType(user.getUserType())
                .username(user.getUsername())
                .referenceId(user.getReferenceId())
                .build();
    }

    @Transactional
    public void requestPasswordReset(PasswordResetRequest request) {
        try {
            // Validate email
            if (request.getEmail() == null || request.getEmail().isEmpty()) {
                throw new IllegalArgumentException("Email is required");
            }

            // Find user by email
            User user = userMapper.findByEmail(request.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("Email not found"));

            // Generate reset token
            String resetToken = UUID.randomUUID().toString();
            LocalDateTime resetTokenExpiry = LocalDateTime.now().plusHours(24);

            // Update user with reset token
            userMapper.updateResetToken(user.getEmail(), resetToken, resetTokenExpiry);

            // Send email with a reset token
            emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
        } catch (Exception e) {
            // Log the exception for debugging purposes
            e.printStackTrace();
            throw e; // Re-throw to be handled by the controller
        }
    }

    @Transactional
    public void resetPassword(PasswordUpdateRequest request) {
        // Find user by reset token
        User user = userMapper.findByResetToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));

        // Check if token is expired
        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token has expired");
        }

        // Update password
        userMapper.updatePassword(user.getId(), passwordEncoder.encode(request.getPassword()));
    }
}
