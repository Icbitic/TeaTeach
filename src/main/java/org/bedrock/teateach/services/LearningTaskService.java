package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.LearningTask;
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
public class LearningTaskService {

    private final LearningTaskMapper learningTaskMapper;

    @Autowired
    public LearningTaskService(LearningTaskMapper learningTaskMapper) {
        this.learningTaskMapper = learningTaskMapper;
    }

    @Transactional
    @CacheEvict(value = {"courseTasks"}, key = "#task.courseId")
    public LearningTask createTask(LearningTask task) {
        learningTaskMapper.insert(task);
        return task;
    }

    @Transactional
    @Caching(put = { @CachePut(value = "learningTasks", key = "#task.id") },
             evict = { @CacheEvict(value = "courseTasks", key = "#task.courseId") })
    public LearningTask updateTask(LearningTask task) {
        learningTaskMapper.update(task);
        return task;
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "learningTasks", key = "#id"),
        @CacheEvict(value = "courseTasks", allEntries = true)
    })
    public void deleteTask(Long id) {
        // Get the task first to know which course cache to invalidate
        LearningTask task = getTaskById(id);
        learningTaskMapper.delete(id);
    }

    @Cacheable(value = "learningTasks", key = "#id")
    public LearningTask getTaskById(Long id) {
        return learningTaskMapper.findById(id);
    }

    @Cacheable(value = "courseTasks", key = "#courseId")
    public List<LearningTask> getTasksByCourseId(Long courseId) {
        return learningTaskMapper.findByCourseId(courseId);
    }
}