package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.CourseEnrollment;
import org.bedrock.teateach.beans.Student;
import org.bedrock.teateach.beans.Course;
import org.bedrock.teateach.mappers.CourseEnrollmentMapper;
import org.bedrock.teateach.mappers.CourseMapper;
import org.bedrock.teateach.mappers.StudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseEnrollmentService {
    
    private final CourseEnrollmentMapper courseEnrollmentMapper;
    private final CourseMapper courseMapper;
    private final StudentMapper studentMapper;
    
    @Autowired
    public CourseEnrollmentService(CourseEnrollmentMapper courseEnrollmentMapper,
                                 CourseMapper courseMapper,
                                 StudentMapper studentMapper) {
        this.courseEnrollmentMapper = courseEnrollmentMapper;
        this.courseMapper = courseMapper;
        this.studentMapper = studentMapper;
    }
    
    @Transactional
    @CacheEvict(value = {"courseStudents", "studentCourses"}, allEntries = true)
    public CourseEnrollment enrollStudent(Long courseId, Long studentId) {
        // Check if course exists
        Course course = courseMapper.findById(courseId);
        if (course == null) {
            throw new RuntimeException("Course not found with id: " + courseId);
        }
        
        // Check if student exists
        Student student = studentMapper.findById(studentId);
        if (student == null) {
            throw new RuntimeException("Student not found with id: " + studentId);
        }
        
        // Check if already enrolled
        CourseEnrollment existing = courseEnrollmentMapper.findByStudentAndCourse(courseId, studentId);
        if (existing != null && "ACTIVE".equals(existing.getStatus())) {
            throw new RuntimeException("Student is already enrolled in this course");
        }
        
        // Create new enrollment
        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setCourseId(courseId);
        enrollment.setStudentId(studentId);
        enrollment.setEnrollmentDate(LocalDateTime.now());
        enrollment.setStatus("ACTIVE");
        
        courseEnrollmentMapper.insert(enrollment);
        return enrollment;
    }
    
    @Transactional
    @CacheEvict(value = {"courseStudents", "studentCourses"}, allEntries = true)
    public void unenrollStudent(Long courseId, Long studentId) {
        CourseEnrollment enrollment = courseEnrollmentMapper.findByStudentAndCourse(courseId, studentId);
        if (enrollment == null) {
            throw new RuntimeException("Enrollment not found");
        }
        
        courseEnrollmentMapper.deleteByStudentAndCourse(courseId, studentId);
    }
    
    @Transactional
    @CacheEvict(value = {"courseStudents", "studentCourses"}, allEntries = true)
    public void updateEnrollmentStatus(Long enrollmentId, String status) {
        CourseEnrollment enrollment = courseEnrollmentMapper.findById(enrollmentId);
        if (enrollment == null) {
            throw new RuntimeException("Enrollment not found with id: " + enrollmentId);
        }
        
        enrollment.setStatus(status);
        courseEnrollmentMapper.update(enrollment);
    }
    
    @Cacheable(value = "courseStudents", key = "#courseId")
    public List<Student> getStudentsByCourseId(Long courseId) {
        return courseEnrollmentMapper.findStudentsByCourseId(courseId);
    }
    
    @Cacheable(value = "studentCourses", key = "#studentId")
    public List<Course> getCoursesByStudentId(Long studentId) {
        return courseEnrollmentMapper.findCoursesByStudentId(studentId);
    }
    
    public List<CourseEnrollment> getEnrollmentsByCourseId(Long courseId) {
        return courseEnrollmentMapper.findByCourseId(courseId);
    }
    
    public int getActiveEnrollmentCount(Long courseId) {
        return courseEnrollmentMapper.countActiveEnrollmentsByCourseId(courseId);
    }
    
    public List<Student> getAvailableStudentsForCourse(Long courseId) {
        return courseEnrollmentMapper.findAvailableStudentsForCourse(courseId);
    }
    
    public CourseEnrollment getEnrollmentByStudentAndCourse(Long courseId, Long studentId) {
        return courseEnrollmentMapper.findByStudentAndCourse(courseId, studentId);
    }
    
    @Transactional
    @CacheEvict(value = {"courseStudents", "studentCourses"}, allEntries = true)
    public void enrollMultipleStudents(Long courseId, List<Long> studentIds) {
        for (Long studentId : studentIds) {
            try {
                enrollStudent(courseId, studentId);
            } catch (RuntimeException e) {
                // Log the error but continue with other students
                System.err.println("Failed to enroll student " + studentId + ": " + e.getMessage());
            }
        }
    }
}