package org.bedrock.teateach.controllers;

import org.bedrock.teateach.beans.Course;
import org.bedrock.teateach.beans.LearningTask;
import org.bedrock.teateach.services.CourseService;
import org.bedrock.teateach.dto.CommonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    @Autowired
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    public ResponseEntity<CommonResponse<Course>> createCourse(@RequestBody Course course) {
        Course createdCourse = courseService.createCourse(course);
        return ResponseEntity.ok(CommonResponse.success(createdCourse));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<Course>> updateCourse(@PathVariable Long id, @RequestBody Course course) {
        course.setId(id);
        Course updatedCourse = courseService.updateCourse(course);
        if (updatedCourse != null) {
            return ResponseEntity.ok(CommonResponse.success(updatedCourse));
        }
        return ResponseEntity.ok(CommonResponse.error(404, "Course not found"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<Course>> getCourseById(@PathVariable Long id) {
        Course course = courseService.getCourseById(id);
        if (course != null) {
            return ResponseEntity.ok(CommonResponse.success(course));
        }
        return ResponseEntity.ok(CommonResponse.error(404, "Course not found"));
    }

    @GetMapping
    public ResponseEntity<CommonResponse<List<Course>>> getAllCourses() {
        List<Course> courses = courseService.findAll();
        return ResponseEntity.ok(CommonResponse.success(courses));
    }

    @GetMapping("/{courseId}/tasks")
    public ResponseEntity<CommonResponse<List<LearningTask>>> getTasksForCourse(@PathVariable Long courseId) {
        List<LearningTask> tasks = courseService.getTasksForCourse(courseId);
        return ResponseEntity.ok(CommonResponse.success(tasks));
    }
}