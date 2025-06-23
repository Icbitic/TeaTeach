package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.Course;
import org.bedrock.teateach.beans.LearningTask;
import org.bedrock.teateach.mappers.CourseMapper;
import org.bedrock.teateach.mappers.LearningTaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseService {

    private final CourseMapper courseMapper;
    private final LearningTaskMapper learningTaskMapper;

    @Autowired
    public CourseService(CourseMapper courseMapper, LearningTaskMapper learningTaskMapper) {
        this.courseMapper = courseMapper;
        this.learningTaskMapper = learningTaskMapper;
    }

    @Transactional
    @CacheEvict(value = "allCourses", allEntries = true)
    public Course createCourse(Course course) {
        courseMapper.insert(course);
        return course;
    }

    @Transactional
    @Caching(put = { @CachePut(value = "courses", key = "#course.id") },
             evict = { @CacheEvict(value = "allCourses", allEntries = true) })
    public Course updateCourse(Course course) {
        courseMapper.update(course);
        return course;
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "courses", key = "#id"),
        @CacheEvict(value = "allCourses", allEntries = true)
    })
    public void deleteCourse(Long id) {
        // Potentially delete associated learning tasks as well
        // learningTaskMapper.deleteByCourseId(id);
        courseMapper.delete(id);
    }

    @Cacheable(value = "courses", key = "#id")
    public Course getCourseById(Long id) {
        return courseMapper.findById(id);
    }

    @Cacheable(value = "allCourses")
    public List<Course> getAllCourses() {
        return courseMapper.findAll();
    }

    // Methods for managing course-task relationships
    @Cacheable(value = "courseTasks", key = "#courseId")
    public List<LearningTask> getTasksForCourse(Long courseId) {
        return learningTaskMapper.findByCourseId(courseId);
    }

    @Cacheable(value = "allCourses")
    public List<Course> findAll() {
        return courseMapper.findAll();
    }
}
