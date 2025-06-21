package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.Course;
import org.bedrock.teateach.beans.LearningTask;
import org.bedrock.teateach.mappers.CourseMapper;
import org.bedrock.teateach.mappers.LearningTaskMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private LearningTaskMapper learningTaskMapper;

    @InjectMocks
    private CourseService courseService;

    private Course testCourse;

    @BeforeEach
    void setUp() {
        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setCourseName("Introduction to Programming");
        testCourse.setCourseCode("CS101");
    }

    @Test
    void createCourse_shouldInsertAndReturnCourse() {
        // Given
        doNothing().when(courseMapper).insert(testCourse);

        // When
        Course result = courseService.createCourse(testCourse);

        // Then
        assertNotNull(result);
        assertEquals(testCourse.getId(), result.getId());
        verify(courseMapper, times(1)).insert(testCourse);
    }

    @Test
    void updateCourse_shouldUpdateAndReturnCourse() {
        // Given
        testCourse.setCourseName("Advanced Programming");
        doNothing().when(courseMapper).update(testCourse);

        // When
        Course result = courseService.updateCourse(testCourse);

        // Then
        assertNotNull(result);
        assertEquals("Advanced Programming", result.getCourseName());
        verify(courseMapper, times(1)).update(testCourse);
    }

    @Test
    void deleteCourse_shouldDeleteCourse() {
        // Given
        Long courseId = 1L;
        doNothing().when(courseMapper).delete(courseId);

        // When
        courseService.deleteCourse(courseId);

        // Then
        verify(courseMapper, times(1)).delete(courseId);
        // If you uncommented deleteByCourseId in service, you would verify it here too:
        // verify(learningTaskMapper, times(1)).deleteByCourseId(courseId);
    }

    @Test
    void getCourseById_shouldReturnCourse_whenFound() {
        // Given
        Long courseId = 1L;
        when(courseMapper.findById(courseId)).thenReturn(testCourse);

        // When
        Course result = courseService.getCourseById(courseId);

        // Then
        assertNotNull(result);
        assertEquals(testCourse, result);
        verify(courseMapper, times(1)).findById(courseId);
    }

    @Test
    void getCourseById_shouldReturnNull_whenNotFound() {
        // Given
        Long courseId = 99L;
        when(courseMapper.findById(courseId)).thenReturn(null);

        // When
        Course result = courseService.getCourseById(courseId);

        // Then
        assertNull(result);
        verify(courseMapper, times(1)).findById(courseId);
    }

    @Test
    void getAllCourses_shouldReturnAllCourses() {
        // Given
        List<Course> expectedCourses = Arrays.asList(testCourse, new Course());
        when(courseMapper.findAll()).thenReturn(expectedCourses);

        // When
        List<Course> result = courseService.getAllCourses();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(courseMapper, times(1)).findAll();
    }

    @Test
    void getTasksForCourse_shouldReturnListOfLearningTasks() {
        // Given
        Long courseId = 1L;
        LearningTask task1 = new LearningTask();
        task1.setId(10L);
        task1.setCourseId(courseId);
        List<LearningTask> expectedTasks = Arrays.asList(task1);
        when(learningTaskMapper.findByCourseId(courseId)).thenReturn(expectedTasks);

        // When
        List<LearningTask> result = courseService.getTasksForCourse(courseId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(courseId, result.get(0).getCourseId());
        verify(learningTaskMapper, times(1)).findByCourseId(courseId);
    }

    @Test
    void findAll_shouldReturnAllCourses_aliasForGetAllCourses() {
        // Given
        List<Course> expectedCourses = Arrays.asList(testCourse, new Course());
        when(courseMapper.findAll()).thenReturn(expectedCourses);

        // When
        List<Course> result = courseService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(courseMapper, times(1)).findAll();
    }
}