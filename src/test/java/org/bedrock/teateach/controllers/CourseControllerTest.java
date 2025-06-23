package org.bedrock.teateach.controllers;

import org.bedrock.teateach.beans.Course;
import org.bedrock.teateach.beans.LearningTask;
import org.bedrock.teateach.services.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseControllerTest {

    @Mock
    private CourseService courseService;

    @InjectMocks
    private CourseController courseController;

    private Course testCourse;
    private List<Course> testCourses;
    private List<LearningTask> testTasks;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setCourseName("Test Course");

        Course course2 = new Course();
        course2.setId(2L);
        course2.setCourseName("Another Course");

        testCourses = Arrays.asList(testCourse, course2);

        LearningTask task1 = new LearningTask();
        task1.setId(1L);
        task1.setTaskName("Test Task 1");

        LearningTask task2 = new LearningTask();
        task2.setId(2L);
        task2.setTaskName("Test Task 2");

        testTasks = Arrays.asList(task1, task2);
    }

    @Test
    void createCourse_ShouldReturnCreatedCourse() {
        // Given
        when(courseService.createCourse(any(Course.class))).thenReturn(testCourse);

        // When
        ResponseEntity<Course> response = courseController.createCourse(new Course());

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testCourse, response.getBody());
        verify(courseService, times(1)).createCourse(any(Course.class));
    }

    @Test
    void updateCourse_WhenCourseExists_ShouldReturnUpdatedCourse() {
        // Given
        when(courseService.updateCourse(any(Course.class))).thenReturn(testCourse);

        // When
        ResponseEntity<Course> response = courseController.updateCourse(1L, testCourse);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testCourse, response.getBody());
        verify(courseService, times(1)).updateCourse(any(Course.class));
    }

    @Test
    void updateCourse_WhenCourseDoesNotExist_ShouldReturnNotFound() {
        // Given
        when(courseService.updateCourse(any(Course.class))).thenReturn(null);

        // When
        ResponseEntity<Course> response = courseController.updateCourse(1L, testCourse);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(courseService, times(1)).updateCourse(any(Course.class));
    }

    @Test
    void deleteCourse_ShouldReturnNoContent() {
        // Given
        doNothing().when(courseService).deleteCourse(anyLong());

        // When
        ResponseEntity<Void> response = courseController.deleteCourse(1L);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(courseService, times(1)).deleteCourse(1L);
    }

    @Test
    void getCourseById_WhenCourseExists_ShouldReturnCourse() {
        // Given
        when(courseService.getCourseById(1L)).thenReturn(testCourse);

        // When
        ResponseEntity<Course> response = courseController.getCourseById(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testCourse, response.getBody());
        verify(courseService, times(1)).getCourseById(1L);
    }

    @Test
    void getCourseById_WhenCourseDoesNotExist_ShouldReturnNotFound() {
        // Given
        when(courseService.getCourseById(anyLong())).thenReturn(null);

        // When
        ResponseEntity<Course> response = courseController.getCourseById(999L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(courseService, times(1)).getCourseById(999L);
    }

    @Test
    void getAllCourses_ShouldReturnListOfCourses() {
        // Given
        when(courseService.findAll()).thenReturn(testCourses);

        // When
        ResponseEntity<List<Course>> response = courseController.getAllCourses();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testCourses.size(), response.getBody().size());
        verify(courseService, times(1)).findAll();
    }

    @Test
    void getTasksForCourse_ShouldReturnListOfTasks() {
        // Given
        when(courseService.getTasksForCourse(anyLong())).thenReturn(testTasks);

        // When
        ResponseEntity<List<LearningTask>> response = courseController.getTasksForCourse(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testTasks, response.getBody());
        verify(courseService, times(1)).getTasksForCourse(1L);
    }
}
