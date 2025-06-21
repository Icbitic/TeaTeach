package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.LearningTask;
import org.bedrock.teateach.mappers.LearningTaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
    public LearningTask createTask(LearningTask task) {
        learningTaskMapper.insert(task);
        return task;
    }

    @Transactional
    public LearningTask updateTask(LearningTask task) {
        learningTaskMapper.update(task);
        return task;
    }

    @Transactional
    public void deleteTask(Long id) {
        learningTaskMapper.delete(id);
    }

    public LearningTask getTaskById(Long id) {
        return learningTaskMapper.findById(id);
    }

    public List<LearningTask> getTasksByCourseId(Long courseId) {
        return learningTaskMapper.findByCourseId(courseId);
    }
}