package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.Course;
import org.bedrock.teateach.beans.LearningTask;
import org.bedrock.teateach.mappers.CourseMapper;
import org.bedrock.teateach.mappers.LearningTaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Course createCourse(Course course) {
        courseMapper.insert(course);
        return course;
    }

    @Transactional
    public Course updateCourse(Course course) {
        courseMapper.update(course);
        return course;
    }

    @Transactional
    public void deleteCourse(Long id) {
        // Potentially delete associated learning tasks as well
        // learningTaskMapper.deleteByCourseId(id);
        courseMapper.delete(id);
    }

    public Course getCourseById(Long id) {
        return courseMapper.findById(id);
    }

    public List<Course> getAllCourses() {
        return courseMapper.findAll();
    }

    // Methods for managing course-task relationships
    public List<LearningTask> getTasksForCourse(Long courseId) {
        return learningTaskMapper.findByCourseId(courseId);
    }

    public List<Course> findAll() {
        return courseMapper.findAll();
    }
}
