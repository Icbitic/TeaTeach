package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.GradeAnalysis;
import org.bedrock.teateach.beans.LearningTask;
import org.bedrock.teateach.beans.StudentTaskSubmission;
import org.bedrock.teateach.mappers.GradeAnalysisMapper;
import org.bedrock.teateach.mappers.LearningTaskMapper;
import org.bedrock.teateach.mappers.StudentTaskSubmissionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

    @Mock
    private StudentTaskSubmissionMapper submissionMapper;

    @Mock
    private GradeAnalysisMapper gradeAnalysisMapper;

    @Mock
    private LearningTaskMapper learningTaskMapper;

    @InjectMocks
    private GradeService gradeService;

    private StudentTaskSubmission testSubmission;
    private LearningTask testLearningTask;

    @BeforeEach
    void setUp() {
        testSubmission = new StudentTaskSubmission();
        testSubmission.setId(1L);
        testSubmission.setTaskId(10L);
        testSubmission.setStudentId(100L);
        testSubmission.setSubmissionContent("Answer text");
        testSubmission.setSubmissionTime(LocalDateTime.now());
        testSubmission.setCompletionStatus(2);

        testLearningTask = new LearningTask();
        testLearningTask.setId(10L);
        testLearningTask.setCourseId(200L);
        testLearningTask.setTaskName("Quiz 1");
    }

    @Test
    void recordSubmissionScore_shouldUpdateSubmissionAndOverallGrade_whenTaskAndSubmissionFound() {
        // Given
        Double newScore = 85.0;
        Long studentId = testSubmission.getStudentId();
        Long courseId = testLearningTask.getCourseId();

        when(submissionMapper.findById(testSubmission.getId())).thenReturn(testSubmission);
        when(learningTaskMapper.findById(testSubmission.getTaskId())).thenReturn(testLearningTask);
        doNothing().when(submissionMapper).update(any(StudentTaskSubmission.class));
        // Mock the updateStudentOverallGrade's internal calls
        when(submissionMapper.findByStudentAndCourse(studentId, courseId))
                .thenReturn(Arrays.asList(testSubmission)); // Return the now-scored submission
        when(gradeAnalysisMapper.findByStudentAndCourse(studentId, courseId)).thenReturn(null); // No existing grade analysis

        // When
        StudentTaskSubmission result = gradeService.recordSubmissionScore(testSubmission.getId(), newScore);

        // Then
        assertNotNull(result);
        assertEquals(newScore, result.getScore());
        assertEquals(3, result.getCompletionStatus()); // Graded
        verify(submissionMapper, times(1)).findById(testSubmission.getId());
        verify(submissionMapper, times(1)).update(testSubmission);
        verify(learningTaskMapper, times(1)).findById(testSubmission.getTaskId());
        verify(submissionMapper, times(1)).findByStudentAndCourse(studentId, courseId); // Called by updateStudentOverallGrade
        verify(gradeAnalysisMapper, times(1)).findByStudentAndCourse(studentId, courseId); // Called by updateStudentOverallGrade
        verify(gradeAnalysisMapper, times(1)).insert(any(GradeAnalysis.class)); // New GradeAnalysis should be inserted
    }

    @Test
    void recordSubmissionScore_shouldNotUpdateOverallGrade_whenLearningTaskNotFound() {
        // Given
        Double newScore = 85.0;
        when(submissionMapper.findById(testSubmission.getId())).thenReturn(testSubmission);
        when(learningTaskMapper.findById(testSubmission.getTaskId())).thenReturn(null); // Simulate task not found
        doNothing().when(submissionMapper).update(any(StudentTaskSubmission.class));

        // When
        StudentTaskSubmission result = gradeService.recordSubmissionScore(testSubmission.getId(), newScore);

        // Then
        assertNotNull(result);
        assertEquals(newScore, result.getScore());
        assertEquals(3, result.getCompletionStatus());
        verify(submissionMapper, times(1)).findById(testSubmission.getId());
        verify(submissionMapper, times(1)).update(testSubmission);
        verify(learningTaskMapper, times(1)).findById(testSubmission.getTaskId());
        // Verify updateStudentOverallGrade was NOT called
        verify(submissionMapper, never()).findByStudentAndCourse(anyLong(), anyLong());
        verify(gradeAnalysisMapper, never()).findByStudentAndCourse(anyLong(), anyLong());
        verify(gradeAnalysisMapper, never()).insert(any(GradeAnalysis.class));
        verify(gradeAnalysisMapper, never()).update(any(GradeAnalysis.class));
    }

    @Test
    void updateStudentOverallGrade_shouldInsertNewGradeAnalysis_whenNoneExists() {
        // Given
        Long studentId = 100L;
        Long courseId = 200L;
        StudentTaskSubmission s1 = new StudentTaskSubmission();
        s1.setScore(90.0);
        StudentTaskSubmission s2 = new StudentTaskSubmission();
        s2.setScore(80.0);
        List<StudentTaskSubmission> submissions = Arrays.asList(s1, s2);

        when(submissionMapper.findByStudentAndCourse(studentId, courseId)).thenReturn(submissions);
        when(gradeAnalysisMapper.findByStudentAndCourse(studentId, courseId)).thenReturn(null); // No existing analysis
        doNothing().when(gradeAnalysisMapper).insert(any(GradeAnalysis.class));

        // When
        gradeService.updateStudentOverallGrade(studentId, courseId);

        // Then
        // Average score (90+80)/2 = 85.0
        verify(submissionMapper, times(1)).findByStudentAndCourse(studentId, courseId);
        verify(gradeAnalysisMapper, times(1)).findByStudentAndCourse(studentId, courseId);
        verify(gradeAnalysisMapper, times(1)).insert(argThat(ga ->
                ga.getStudentId().equals(studentId) &&
                        ga.getCourseId().equals(courseId) &&
                        ga.getOverallGrade() == 85.0
        ));
        verify(gradeAnalysisMapper, never()).update(any(GradeAnalysis.class));
    }

    @Test
    void updateStudentOverallGrade_shouldUpdateExistingGradeAnalysis_whenExists() {
        // Given
        Long studentId = 100L;
        Long courseId = 200L;
        StudentTaskSubmission s1 = new StudentTaskSubmission();
        s1.setScore(90.0);
        StudentTaskSubmission s2 = new StudentTaskSubmission();
        s2.setScore(80.0);
        List<StudentTaskSubmission> submissions = Arrays.asList(s1, s2);

        GradeAnalysis existingAnalysis = new GradeAnalysis();
        existingAnalysis.setId(5L);
        existingAnalysis.setStudentId(studentId);
        existingAnalysis.setCourseId(courseId);
        existingAnalysis.setOverallGrade(70.0); // Old grade

        when(submissionMapper.findByStudentAndCourse(studentId, courseId)).thenReturn(submissions);
        when(gradeAnalysisMapper.findByStudentAndCourse(studentId, courseId)).thenReturn(existingAnalysis); // Existing analysis
        doNothing().when(gradeAnalysisMapper).update(any(GradeAnalysis.class));

        // When
        gradeService.updateStudentOverallGrade(studentId, courseId);

        // Then
        verify(submissionMapper, times(1)).findByStudentAndCourse(studentId, courseId);
        verify(gradeAnalysisMapper, times(1)).findByStudentAndCourse(studentId, courseId);
        verify(gradeAnalysisMapper, never()).insert(any(GradeAnalysis.class));
        verify(gradeAnalysisMapper, times(1)).update(argThat(ga ->
                ga.getId().equals(5L) &&
                        ga.getOverallGrade() == 85.0 // Updated grade
        ));
    }

    @Test
    void updateStudentOverallGrade_shouldHandleNoGradedSubmissions() {
        // Given
        Long studentId = 100L;
        Long courseId = 200L;
        StudentTaskSubmission s1 = new StudentTaskSubmission(); // No score
        List<StudentTaskSubmission> submissions = Arrays.asList(s1);

        when(submissionMapper.findByStudentAndCourse(studentId, courseId)).thenReturn(submissions);
        when(gradeAnalysisMapper.findByStudentAndCourse(studentId, courseId)).thenReturn(null);
        doNothing().when(gradeAnalysisMapper).insert(any(GradeAnalysis.class));

        // When
        gradeService.updateStudentOverallGrade(studentId, courseId);

        // Then
        verify(gradeAnalysisMapper, times(1)).insert(argThat(ga ->
                ga.getOverallGrade() == 0.0 // Should be 0.0 as no scores
        ));
    }

    @Test
    void getStudentCourseGrade_shouldReturnGradeAnalysis() {
        // Given
        Long studentId = 1L;
        Long courseId = 100L;
        GradeAnalysis expectedGrade = new GradeAnalysis();
        when(gradeAnalysisMapper.findByStudentAndCourse(studentId, courseId)).thenReturn(expectedGrade);

        // When
        GradeAnalysis result = gradeService.getStudentCourseGrade(studentId, courseId);

        // Then
        assertNotNull(result);
        assertEquals(expectedGrade, result);
        verify(gradeAnalysisMapper, times(1)).findByStudentAndCourse(studentId, courseId);
    }

    @Test
    void getCourseGrades_shouldReturnListOfGradeAnalysis() {
        // Given
        Long courseId = 100L;
        List<GradeAnalysis> expectedGrades = Arrays.asList(new GradeAnalysis(), new GradeAnalysis());
        when(gradeAnalysisMapper.findByCourseId(courseId)).thenReturn(expectedGrades);

        // When
        List<GradeAnalysis> result = gradeService.getCourseGrades(courseId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(gradeAnalysisMapper, times(1)).findByCourseId(courseId);
    }

    @Test
    void exportGradesToExcel_shouldReturnByteArray() {
        // Given
        Long courseId = 100L;
        when(gradeAnalysisMapper.findByCourseId(courseId)).thenReturn(Collections.emptyList()); // Mock an empty list for simplicity

        // When
        byte[] result = gradeService.exportGradesToExcel(courseId);

        // Then
        assertNotNull(result);
        // Additional checks for byte array content would require actual Excel generation logic.
        // For a unit test, verifying it returns a non-null array is sufficient given the placeholder.
        verify(gradeAnalysisMapper, times(1)).findByCourseId(courseId);
    }
}