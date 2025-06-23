package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.GradeAnalysis;
import org.bedrock.teateach.beans.LearningTask;
import org.bedrock.teateach.beans.StudentTaskSubmission;
import org.bedrock.teateach.mappers.GradeAnalysisMapper;
import org.bedrock.teateach.mappers.LearningTaskMapper;
import org.bedrock.teateach.mappers.StudentTaskSubmissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GradeService {

    private final StudentTaskSubmissionMapper submissionMapper;
    private final GradeAnalysisMapper gradeAnalysisMapper;
    private final LearningTaskMapper learningTaskMapper;
    // Potentially inject LLMService here for intelligent feedback/grading

    @Autowired
    public GradeService(StudentTaskSubmissionMapper submissionMapper,
                        GradeAnalysisMapper gradeAnalysisMapper, LearningTaskMapper learningTaskMapper) {
        this.submissionMapper = submissionMapper;
        this.gradeAnalysisMapper = gradeAnalysisMapper;
        this.learningTaskMapper = learningTaskMapper;
    }

    @Transactional
    @CacheEvict(value = {"studentGrades", "courseGrades"}, allEntries = true)
    public StudentTaskSubmission recordSubmissionScore(Long submissionId, Double score) {
        StudentTaskSubmission submission = submissionMapper.findById(submissionId);
        if (submission != null) {
            submission.setScore(score);
            submission.setCompletionStatus(3); // Graded
            submissionMapper.update(submission);

            // Fetch the LearningTask to get the courseId
            LearningTask learningTask = learningTaskMapper.findById(submission.getTaskId());
            if (learningTask != null) {
                Long courseId = learningTask.getCourseId();
                // After scoring, update overall grade for student/course
                updateStudentOverallGrade(submission.getStudentId(), courseId); // <--- CORRECTED CALL
            } else {
                // Handle case where associated learning task is not found (e.g., log error)
                System.err.println("Error: LearningTask not found for taskId: " + submission.getTaskId());
            }
        }
        return submission;
    }


    // This method aggregates and updates the overall grade for a student in a course
    @Transactional
    @CacheEvict(value = {"studentGrades", "courseGrades"}, allEntries = true)
    public void updateStudentOverallGrade(Long studentId, Long courseId) {
        List<StudentTaskSubmission> submissions = submissionMapper.findByStudentAndCourse(studentId, courseId);
        // Calculate overall grade based on task scores
        double totalScore = submissions.stream()
                .filter(s -> s.getScore() != null)
                .mapToDouble(StudentTaskSubmission::getScore)
                .average()
                .orElse(0.0);

        GradeAnalysis gradeAnalysis = gradeAnalysisMapper.findByStudentAndCourse(studentId, courseId);
        if (gradeAnalysis == null) {
            gradeAnalysis = new GradeAnalysis();
            gradeAnalysis.setStudentId(studentId);
            gradeAnalysis.setCourseId(courseId);
            gradeAnalysis.setOverallGrade(totalScore);
            // Initialize taskGrades and gradeTrend
            gradeAnalysisMapper.insert(gradeAnalysis);
        } else {
            gradeAnalysis.setOverallGrade(totalScore);
            // Update taskGrades and gradeTrend (logic for this would be more detailed)
            gradeAnalysisMapper.update(gradeAnalysis);
        }
    }

    @Cacheable(value = "studentGrades", key = "#studentId + '-' + #courseId")
    public GradeAnalysis getStudentCourseGrade(Long studentId, Long courseId) {
        return gradeAnalysisMapper.findByStudentAndCourse(studentId, courseId);
    }

    @Cacheable(value = "courseGrades", key = "#courseId")
    public List<GradeAnalysis> getCourseGrades(Long courseId) {
        return gradeAnalysisMapper.findByCourseId(courseId);
    }

    // Methods for grade report export (can use libraries like Apache POI)
    public byte[] exportGradesToExcel(Long courseId) {
        List<GradeAnalysis> grades = getCourseGrades(courseId);
        // Logic to generate Excel file
        return new byte[0]; // Placeholder
    }
}