package org.bedrock.teateach.controllers;

import org.bedrock.teateach.beans.CourseEnrollment;
import org.bedrock.teateach.beans.Student;
import org.bedrock.teateach.beans.Course;
import org.bedrock.teateach.services.CourseEnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/course-enrollments")
public class CourseEnrollmentController {

    private final CourseEnrollmentService courseEnrollmentService;

    @Autowired
    public CourseEnrollmentController(CourseEnrollmentService courseEnrollmentService) {
        this.courseEnrollmentService = courseEnrollmentService;
    }

    @PostMapping("/enroll")
    public ResponseEntity<?> enrollStudent(@RequestBody Map<String, Long> request) {
        try {
            Long courseId = request.get("courseId");
            Long studentId = request.get("studentId");

            if (courseId == null || studentId == null) {
                return ResponseEntity.badRequest().body("Course ID and Student ID are required");
            }

            CourseEnrollment enrollment = courseEnrollmentService.enrollStudent(courseId, studentId);
            return new ResponseEntity<>(enrollment, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/enroll-multiple")
    public ResponseEntity<?> enrollMultipleStudents(@RequestBody Map<String, Object> request) {
        try {
            Long courseId = Long.valueOf(request.get("courseId").toString());

            List<?> studentIdObjects = (List<?>) request.get("studentIds");
            if (courseId == null || studentIdObjects == null || studentIdObjects.isEmpty()) {
                return ResponseEntity.badRequest().body("Course ID and Student IDs are required");
            }

            List<Long> studentIds = studentIdObjects.stream()
                    .map(id -> Long.valueOf(id.toString()))
                    .collect(Collectors.toList());

            courseEnrollmentService.enrollMultipleStudents(courseId, studentIds);
            return ResponseEntity.ok().body("Students enrolled successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @DeleteMapping("/unenroll")
    public ResponseEntity<?> unenrollStudent(@RequestParam Long courseId, @RequestParam Long studentId) {
        try {
            courseEnrollmentService.unenrollStudent(courseId, studentId);
            return ResponseEntity.ok().body("Student unenrolled successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{enrollmentId}/status")
    public ResponseEntity<?> updateEnrollmentStatus(@PathVariable Long enrollmentId, @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            if (status == null) {
                return ResponseEntity.badRequest().body("Status is required");
            }

            courseEnrollmentService.updateEnrollmentStatus(enrollmentId, status);
            return ResponseEntity.ok().body("Enrollment status updated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/course/{courseId}/students")
    public ResponseEntity<List<Student>> getStudentsByCourse(@PathVariable Long courseId) {
        List<Student> students = courseEnrollmentService.getStudentsByCourseId(courseId);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/student/{studentId}/courses")
    public ResponseEntity<List<Course>> getCoursesByStudent(@PathVariable Long studentId) {
        List<Course> courses = courseEnrollmentService.getCoursesByStudentId(studentId);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/course/{courseId}/enrollments")
    public ResponseEntity<List<CourseEnrollment>> getEnrollmentsByCourse(@PathVariable Long courseId) {
        List<CourseEnrollment> enrollments = courseEnrollmentService.getEnrollmentsByCourseId(courseId);
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/course/{courseId}/count")
    public ResponseEntity<Integer> getEnrollmentCount(@PathVariable Long courseId) {
        int count = courseEnrollmentService.getActiveEnrollmentCount(courseId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/course/{courseId}/available-students")
    public ResponseEntity<List<Student>> getAvailableStudents(@PathVariable Long courseId) {
        List<Student> students = courseEnrollmentService.getAvailableStudentsForCourse(courseId);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/check-enrollment")
    public ResponseEntity<CourseEnrollment> checkEnrollment(@RequestParam Long courseId, @RequestParam Long studentId) {
        CourseEnrollment enrollment = courseEnrollmentService.getEnrollmentByStudentAndCourse(courseId, studentId);
        if (enrollment != null) {
            return ResponseEntity.ok(enrollment);
        }
        return ResponseEntity.notFound().build();
    }
}