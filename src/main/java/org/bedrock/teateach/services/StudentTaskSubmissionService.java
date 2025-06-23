// src/main/java/org/bedrock/teateach/services/StudentTaskSubmissionService.java
package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.StudentTaskSubmission;
import org.bedrock.teateach.mappers.StudentTaskSubmissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class StudentTaskSubmissionService {

    private final StudentTaskSubmissionMapper submissionMapper;

    @Autowired
    public StudentTaskSubmissionService(StudentTaskSubmissionMapper submissionMapper) {
        this.submissionMapper = submissionMapper;
    }

    @Transactional
    @CacheEvict(value = {"studentSubmissions", "taskSubmissions"}, allEntries = true)
    public StudentTaskSubmission createSubmission(StudentTaskSubmission submission) {
        submissionMapper.insert(submission);
        return submission;
    }

    @Transactional
    @CacheEvict(value = {"submissions", "studentSubmissions", "taskSubmissions"}, allEntries = true)
    public StudentTaskSubmission updateSubmission(StudentTaskSubmission submission) {
        submissionMapper.update(submission);
        return submission;
    }

    @Transactional
    @CacheEvict(value = {"submissions", "studentSubmissions", "taskSubmissions"}, allEntries = true)
    public void deleteSubmission(Long id) {
        submissionMapper.delete(id);
    }

    @Cacheable(value = "submissions", key = "#id")
    public Optional<StudentTaskSubmission> getSubmissionById(Long id) {
        return Optional.ofNullable(submissionMapper.findById(id));
    }

    @Cacheable(value = "studentSubmissions", key = "#studentId")
    public List<StudentTaskSubmission> getSubmissionsByStudentId(Long studentId) {
        return submissionMapper.findByStudentId(studentId);
    }

    @Cacheable(value = "taskSubmissions", key = "#taskId")
    public List<StudentTaskSubmission> getSubmissionsByTaskId(Long taskId) {
        return submissionMapper.findByTaskId(taskId);
    }

    // You might also add methods here for:
    // - findByStudentAndCourse (if not already in GradeService)
    // - getSubmissionContent (if content is large and needs specific handling)
}