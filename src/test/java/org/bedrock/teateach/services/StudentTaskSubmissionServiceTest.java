// src/test/java/org/bedrock/teateach/services/StudentTaskSubmissionServiceTest.java
package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.StudentTaskSubmission;
import org.bedrock.teateach.mappers.StudentTaskSubmissionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentTaskSubmissionServiceTest {

    @Mock
    private StudentTaskSubmissionMapper submissionMapper;

    @InjectMocks
    private StudentTaskSubmissionService studentTaskSubmissionService;

    private StudentTaskSubmission testSubmission;

    @BeforeEach
    void setUp() {
        testSubmission = new StudentTaskSubmission();
        testSubmission.setId(1L);
        testSubmission.setStudentId(101L);
        testSubmission.setTaskId(201L);
        testSubmission.setSubmissionContent("Test submission content");
        testSubmission.setSubmissionTime(LocalDateTime.now());
        testSubmission.setCompletionStatus(1); // Submitted
//        testSubmission.setCreatedAt(LocalDateTime.now());
//        testSubmission.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateSubmission() {
        // Correct for void insert method
        doNothing().when(submissionMapper).insert(any(StudentTaskSubmission.class));

        StudentTaskSubmission createdSubmission = studentTaskSubmissionService.createSubmission(testSubmission);

        assertNotNull(createdSubmission);
        assertEquals(testSubmission.getId(), createdSubmission.getId());
        verify(submissionMapper, times(1)).insert(testSubmission);
    }

    @Test
    void testUpdateSubmission() {
        // Corrected for void update method
        doNothing().when(submissionMapper).update(any(StudentTaskSubmission.class));

        StudentTaskSubmission updatedSubmission = studentTaskSubmissionService.updateSubmission(testSubmission);

        assertNotNull(updatedSubmission);
        assertEquals(testSubmission.getId(), updatedSubmission.getId());
        verify(submissionMapper, times(1)).update(testSubmission);
    }

    @Test
    void testDeleteSubmission() {
        doNothing().when(submissionMapper).delete(anyLong());

        studentTaskSubmissionService.deleteSubmission(1L);

        verify(submissionMapper, times(1)).delete(1L);
    }

    @Test
    void testGetSubmissionByIdFound() {
        when(submissionMapper.findById(1L)).thenReturn(testSubmission);

        Optional<StudentTaskSubmission> foundSubmission = studentTaskSubmissionService.getSubmissionById(1L);

        assertTrue(foundSubmission.isPresent());
        assertEquals(testSubmission.getId(), foundSubmission.get().getId());
        verify(submissionMapper, times(1)).findById(1L);
    }

    @Test
    void testGetSubmissionByIdNotFound() {
        when(submissionMapper.findById(anyLong())).thenReturn(null);

        Optional<StudentTaskSubmission> foundSubmission = studentTaskSubmissionService.getSubmissionById(999L);

        assertFalse(foundSubmission.isPresent());
        verify(submissionMapper, times(1)).findById(999L);
    }

    @Test
    void testGetSubmissionsByStudentId() {
        List<StudentTaskSubmission> submissions = Arrays.asList(testSubmission, new StudentTaskSubmission());
        when(submissionMapper.findByStudentId(101L)).thenReturn(submissions);

        List<StudentTaskSubmission> result = studentTaskSubmissionService.getSubmissionsByStudentId(101L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        verify(submissionMapper, times(1)).findByStudentId(101L);
    }

    @Test
    void testGetSubmissionsByTaskId() {
        List<StudentTaskSubmission> submissions = Arrays.asList(testSubmission);
        when(submissionMapper.findByTaskId(201L)).thenReturn(submissions);

        List<StudentTaskSubmission> result = studentTaskSubmissionService.getSubmissionsByTaskId(201L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(submissionMapper, times(1)).findByTaskId(201L);
    }
}