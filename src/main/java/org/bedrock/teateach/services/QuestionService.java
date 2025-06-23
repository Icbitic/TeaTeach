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
    @CacheEvict(value = {"allQuestions", "questionsByTypeAndDifficulty"}, allEntries = true)
    public Question createQuestion(Question question) {
        questionMapper.insert(question);
        return question;
    }

    @Transactional
    @CacheEvict(value = {"questions", "allQuestions", "questionsByTypeAndDifficulty"}, allEntries = true)
    public Question updateQuestion(Question question) {
        questionMapper.update(question);
        return question;
    }

    @Transactional
    @CacheEvict(value = {"questions", "allQuestions", "questionsByTypeAndDifficulty"}, allEntries = true)
    public void deleteQuestion(Long id) {
        questionMapper.delete(id);
    }

    @Cacheable(value = "questions", key = "#id")
    public Optional<Question> getQuestionById(Long id) {
        return Optional.ofNullable(questionMapper.findById(id));
    }

    @Cacheable(value = "questionsByTypeAndDifficulty", key = "#type + '-' + #difficulty")
    public List<Question> getQuestionsByTypeAndDifficulty(String type, String difficulty) {
        // This method assumes your mapper has a flexible find method.
        // You might need to adjust your QuestionMapper to handle nullable parameters.
        // Example: If type is null, find all types. If difficulty is null, find all difficulties.
        return questionMapper.findByTypeAndDifficulty(type, difficulty);
    }

    @Cacheable(value = "allQuestions")
    public List<Question> getAllQuestions() {
        return questionMapper.findAll();
    }
}