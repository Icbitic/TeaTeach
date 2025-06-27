package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.Question;
import org.bedrock.teateach.beans.TestPaper;
import org.bedrock.teateach.dto.TestPaperGenerationRequest;
import org.bedrock.teateach.enums.QuestionType;
import org.bedrock.teateach.mappers.TestPaperMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TestPaperService {

    private final TestPaperMapper testPaperMapper;
    private final QuestionService questionService;
    private final Random random = new Random();

    @Autowired
    public TestPaperService(TestPaperMapper testPaperMapper, QuestionService questionService) {
        this.testPaperMapper = testPaperMapper;
        this.questionService = questionService;
    }

    @Transactional
    public TestPaper createTestPaper(TestPaper testPaper) {
        testPaper.setCreatedAt(LocalDateTime.now());
        testPaperMapper.insert(testPaper);
        return testPaper;
    }

    @Transactional
    public TestPaper generateTestPaper(TestPaperGenerationRequest request) {
        List<Question> selectedQuestions = selectQuestionsBasedOnCriteria(request);
        
        TestPaper testPaper = new TestPaper();
        testPaper.setPaperName(request.getPaperName());
        testPaper.setCourseId(request.getCourseId());
        testPaper.setInstructorId(request.getInstructorId());
        testPaper.setQuestionIds(selectedQuestions.stream().map(Question::getId).collect(Collectors.toList()));
        testPaper.setTotalScore(request.getTotalScore());
        testPaper.setDurationMinutes(request.getDurationMinutes());
        testPaper.setGenerationMethod(request.getGenerationMethod());
        testPaper.setCreatedAt(LocalDateTime.now());
        
        testPaperMapper.insert(testPaper);
        return testPaper;
    }

    public Optional<TestPaper> getTestPaperById(Long id) {
        return Optional.ofNullable(testPaperMapper.findById(id));
    }

    public List<TestPaper> getTestPapersByCourse(Long courseId) {
        return testPaperMapper.findByCourseId(courseId);
    }

    public List<TestPaper> getTestPapersByInstructor(Long instructorId) {
        return testPaperMapper.findByInstructorId(instructorId);
    }

    public List<TestPaper> getAllTestPapers(int page, int size, String search) {
        int offset = page * size;
        if (search == null || search.trim().isEmpty()) {
            return testPaperMapper.findAllWithPagination(offset, size);
        } else {
            return testPaperMapper.findAllWithPaginationAndSearch(offset, size, search.trim());
        }
    }

    @Transactional
    public TestPaper updateTestPaper(TestPaper testPaper) {
        testPaperMapper.update(testPaper);
        return testPaper;
    }

    @Transactional
    public void deleteTestPaper(Long id) {
        testPaperMapper.delete(id);
    }

    public List<Question> previewQuestions(TestPaperGenerationRequest request) {
        return selectQuestionsBasedOnCriteria(request);
    }

    private List<Question> selectQuestionsBasedOnCriteria(TestPaperGenerationRequest request) {
        List<Question> allQuestions = questionService.getAllQuestions();
        List<Question> selectedQuestions = new ArrayList<>();

        switch (request.getGenerationMethod()) {
            case "RANDOM":
                selectedQuestions = selectRandomQuestions(allQuestions, request);
                break;
            case "BY_KNOWLEDGE_POINT":
                selectedQuestions = selectQuestionsByKnowledgePoints(allQuestions, request);
                break;
            case "BY_DIFFICULTY":
                selectedQuestions = selectQuestionsByDifficulty(allQuestions, request);
                break;
            case "BALANCED":
                selectedQuestions = selectBalancedQuestions(allQuestions, request);
                break;
            default:
                throw new IllegalArgumentException("Unknown generation method: " + request.getGenerationMethod());
        }

        return selectedQuestions;
    }

    private List<Question> selectRandomQuestions(List<Question> allQuestions, TestPaperGenerationRequest request) {
        List<Question> filteredQuestions = allQuestions;
        
        // Filter by question types if specified
        if (request.getQuestionTypes() != null && !request.getQuestionTypes().isEmpty()) {
            filteredQuestions = filteredQuestions.stream()
                    .filter(q -> request.getQuestionTypes().contains(q.getQuestionType()))
                    .collect(Collectors.toList());
        }
        
        // Shuffle and select required number
        Collections.shuffle(filteredQuestions, random);
        return filteredQuestions.stream()
                .limit(request.getTotalQuestions())
                .collect(Collectors.toList());
    }

    private List<Question> selectQuestionsByKnowledgePoints(List<Question> allQuestions, TestPaperGenerationRequest request) {
        List<Question> selectedQuestions = new ArrayList<>();
        
        // Handle null knowledgePointQuestionCounts by providing default behavior
        Map<Long, Integer> questionCounts = request.getKnowledgePointQuestionCounts();
        if (questionCounts == null) {
            questionCounts = new HashMap<>();
        }
        
        for (Long knowledgePointId : request.getKnowledgePointIds()) {
            List<Question> questionsForKP = allQuestions.stream()
                    .filter(q -> q.getKnowledgePointIds() != null && q.getKnowledgePointIds().contains(knowledgePointId))
                    .collect(Collectors.toList());
            
            int requiredCount = questionCounts.getOrDefault(knowledgePointId, 1);
            Collections.shuffle(questionsForKP, random);
            
            selectedQuestions.addAll(questionsForKP.stream()
                    .limit(requiredCount)
                    .collect(Collectors.toList()));
        }
        
        return selectedQuestions;
    }

    private List<Question> selectQuestionsByDifficulty(List<Question> allQuestions, TestPaperGenerationRequest request) {
        List<Question> selectedQuestions = new ArrayList<>();
        
        for (String difficulty : request.getDifficulties()) {
            List<Question> questionsForDifficulty = allQuestions.stream()
                    .filter(q -> difficulty.equals(q.getDifficulty()))
                    .collect(Collectors.toList());
            
            int requiredCount = request.getDifficultyQuestionCounts().getOrDefault(difficulty, 1);
            Collections.shuffle(questionsForDifficulty, random);
            
            selectedQuestions.addAll(questionsForDifficulty.stream()
                    .limit(requiredCount)
                    .collect(Collectors.toList()));
        }
        
        return selectedQuestions;
    }

    private List<Question> selectBalancedQuestions(List<Question> allQuestions, TestPaperGenerationRequest request) {
        List<Question> selectedQuestions = new ArrayList<>();

        // Select questions by type distribution
        if (request.getQuestionTypeDistribution() != null) {
            for (Map.Entry<QuestionType, Integer> entry : request.getQuestionTypeDistribution().entrySet()) {
                List<Question> questionsOfType = allQuestions.stream()
                        .filter(q -> q.getQuestionType() == entry.getKey())
                        .collect(Collectors.toList());

                // Apply difficulty weights if specified
                if (request.getDifficultyWeights() != null) {
                    questionsOfType = applyDifficultyWeights(questionsOfType, request.getDifficultyWeights());
                }

                Collections.shuffle(questionsOfType, random);
                selectedQuestions.addAll(questionsOfType.stream()
                        .limit(entry.getValue())
                        .collect(Collectors.toList()));
            }
        }

        // Ensure knowledge point coverage if requested
        if (Boolean.TRUE.equals(request.getIncludeAllKnowledgePoints()) && request.getKnowledgePointIds() != null) {
            ensureKnowledgePointCoverage(selectedQuestions, allQuestions, request);
        }

        return selectedQuestions;
    }

    private List<Question> applyDifficultyWeights(List<Question> questions, Map<String, Double> difficultyWeights) {
        return questions.stream()
                .filter(q -> {
                    Double weight = difficultyWeights.get(q.getDifficulty());
                    return weight != null && random.nextDouble() < weight;
                })
                .collect(Collectors.toList());
    }

    private void ensureKnowledgePointCoverage(List<Question> selectedQuestions, List<Question> allQuestions, TestPaperGenerationRequest request) {
        Set<Long> coveredKnowledgePoints = selectedQuestions.stream()
                .flatMap(q -> q.getKnowledgePointIds() != null ? q.getKnowledgePointIds().stream() : Stream.empty())
                .collect(Collectors.toSet());

        for (Long knowledgePointId : request.getKnowledgePointIds()) {
            if (!coveredKnowledgePoints.contains(knowledgePointId)) {
                List<Question> questionsForKP = allQuestions.stream()
                        .filter(q -> q.getKnowledgePointIds() != null && q.getKnowledgePointIds().contains(knowledgePointId))
                        .filter(q -> !selectedQuestions.contains(q))
                        .collect(Collectors.toList());

                if (!questionsForKP.isEmpty()) {
                    selectedQuestions.add(questionsForKP.get(random.nextInt(questionsForKP.size())));
                }
            }
        }
    }
}