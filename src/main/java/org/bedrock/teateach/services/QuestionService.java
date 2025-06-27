// src/main/java/org/bedrock/teateach/services/QuestionService.java
package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.Question;
import org.bedrock.teateach.mappers.QuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class QuestionService {

    private final QuestionMapper questionMapper;

    @Autowired
    public QuestionService(QuestionMapper questionMapper) {
        this.questionMapper = questionMapper;
    }

    @Transactional
    public Question createQuestion(Question question) {
        // Set default values for new questions
        question.setCreatedAt(java.time.LocalDateTime.now());
        question.setUpdatedAt(java.time.LocalDateTime.now());
        question.setIsActive(true);
        question.setUsageCount(0);
        question.setAverageScore(0.0);
        
        if (question.getPoints() == null) {
            question.setPoints(1.0); // Default points
        }
        
        questionMapper.insert(question);
        return question;
    }

    @Transactional
    public Question updateQuestion(Question question) {
        question.setUpdatedAt(java.time.LocalDateTime.now());
        questionMapper.update(question);
        return question;
    }

    @Transactional
    public void deleteQuestion(Long id) {
        questionMapper.delete(id);
    }

    public Optional<Question> getQuestionById(Long id) {
        return Optional.ofNullable(questionMapper.findById(id));
    }

    public List<Question> getQuestionsByTypeAndDifficulty(String type, String difficulty) {
        // This method assumes your mapper has a flexible find method.
        // You might need to adjust your QuestionMapper to handle nullable parameters.
        // Example: If type is null, find all types. If difficulty is null, find all difficulties.
        return questionMapper.findByTypeAndDifficulty(type, difficulty);
    }

    public List<Question> getAllQuestions() {
        return questionMapper.findAll();
    }

    public List<Question> getAllQuestions(int page, int size, String search) {
        int offset = page * size;
        if (search == null || search.trim().isEmpty()) {
            return questionMapper.findAllWithPagination(offset, size);
        } else {
            return questionMapper.findAllWithPaginationAndSearch(offset, size, search.trim());
        }
    }
}