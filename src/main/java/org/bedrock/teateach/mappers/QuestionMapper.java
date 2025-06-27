// src/main/java/org/bedrock/teateach/mappers/QuestionMapper.java
package org.bedrock.teateach.mappers;

import org.apache.ibatis.annotations.*;
import org.bedrock.teateach.beans.Question;

import java.util.List;
import java.util.Optional;

@Mapper
public interface QuestionMapper {

    /**
     * Inserts a new question into the database.
     * The ID of the question will be generated and set back into the Question object.
     *
     * @param question The Question object to insert.
     */
    @Insert("INSERT INTO questions (question_text, question_type, difficulty, knowledge_point_ids, options, correct_answer, " +
            "explanation, programming_language, template_code, test_cases, created_at, updated_at) " +
            "VALUES (#{questionText}, #{questionType}, #{difficulty}, #{knowledgePointIds, typeHandler=org.bedrock.teateach.typehandler.LongListJsonTypeHandler}, #{options, typeHandler=org.bedrock.teateach.typehandler.StringArrayJsonTypeHandler}, #{correctAnswer}, " +
            "#{explanation}, #{programmingLanguage}, #{templateCode}, #{testCases}, " +
            "#{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Question question);

    /**
     * Finds a question by its ID.
     *
     * @param id The ID of the question.
     * @return The Question object if found, otherwise null.
     */
    @Select("SELECT id, question_text, question_type, difficulty, knowledge_point_ids, options, correct_answer, " +
            "explanation, programming_language, template_code, test_cases, created_at, updated_at " +
            "FROM questions WHERE id = #{id}")
    @Results({
            @Result(property = "id",                 column = "id"),
            @Result(property = "questionText",       column = "question_text"),
            @Result(property = "questionType",       column = "question_type"),
            @Result(property = "difficulty",         column = "difficulty"),
            @Result(property = "knowledgePointIds",  column = "knowledge_point_ids", typeHandler = org.bedrock.teateach.typehandler.LongListJsonTypeHandler.class),
            @Result(property = "options",            column = "options", typeHandler = org.bedrock.teateach.typehandler.StringArrayJsonTypeHandler.class),
            @Result(property = "correctAnswer",      column = "correct_answer"),
            @Result(property = "explanation",        column = "explanation"),
            @Result(property = "programmingLanguage", column = "programming_language"),
            @Result(property = "templateCode",       column = "template_code"),
            @Result(property = "testCases",          column = "test_cases"),
            @Result(property = "createdAt",          column = "created_at"),
            @Result(property = "updatedAt",          column = "updated_at")
    })
    Question findById(Long id);

    /**
     * Updates an existing question in the database.
     *
     * @param question The Question object with updated fields.
     */
    @Update("UPDATE questions SET question_text = #{questionText}, question_type = #{questionType}, " +
            "difficulty = #{difficulty}, knowledge_point_ids = #{knowledgePointIds, typeHandler=org.bedrock.teateach.typehandler.LongListJsonTypeHandler}, options = #{options, typeHandler=org.bedrock.teateach.typehandler.StringArrayJsonTypeHandler}, " +
            "correct_answer = #{correctAnswer}, explanation = #{explanation}, programming_language = #{programmingLanguage}, " +
            "template_code = #{templateCode}, test_cases = #{testCases}, " +
            "updated_at = #{updatedAt} WHERE id = #{id}")
    void update(Question question);

    /**
     * Deletes a question by its ID.
     *
     * @param id The ID of the question to delete.
     */
    @Delete("DELETE FROM questions WHERE id = #{id}")
    void delete(Long id);

    /**
     * Retrieves all questions from the database.
     *
     * @return A list of all Question objects.
     */
    @Select("SELECT id, question_text, question_type, difficulty, knowledge_point_ids, options, correct_answer, " +
            "explanation, programming_language, template_code, test_cases, created_at, updated_at " +
            "FROM questions ORDER BY created_at DESC")
    @Results({
            @Result(property = "id",                 column = "id"),
            @Result(property = "questionText",       column = "question_text"),
            @Result(property = "questionType",       column = "question_type"),
            @Result(property = "difficulty",         column = "difficulty"),
            @Result(property = "knowledgePointIds",  column = "knowledge_point_ids", typeHandler = org.bedrock.teateach.typehandler.LongListJsonTypeHandler.class),
            @Result(property = "options",            column = "options", typeHandler = org.bedrock.teateach.typehandler.StringArrayJsonTypeHandler.class),
            @Result(property = "correctAnswer",      column = "correct_answer"),
            @Result(property = "explanation",        column = "explanation"),
            @Result(property = "programmingLanguage", column = "programming_language"),
            @Result(property = "templateCode",       column = "template_code"),
            @Result(property = "testCases",          column = "test_cases"),
            @Result(property = "createdAt",          column = "created_at"),
            @Result(property = "updatedAt",          column = "updated_at")
    })
    List<Question> findAll();

    /**
     * Finds questions based on optional type and difficulty filters.
     *
     * @param type       The type of question (e.g., "SINGLE_CHOICE", "TRUE_FALSE"). Can be null.
     * @param difficulty The difficulty level (e.g., "EASY", "MEDIUM", "HARD"). Can be null.
     * @return A list of questions matching the criteria.
     */
    @Select({
            "<script>",
            "SELECT id, question_text, question_type, difficulty, knowledge_point_ids, options, correct_answer,",
            "explanation, programming_language, template_code, test_cases, created_at, updated_at",
            "FROM questions",
            "<where>",
            "   <if test='type != null and type != \"\"'>",
            "       question_type = #{type}",
            "   </if>",
            "   <if test='difficulty != null and difficulty != \"\"'>",
            "       AND difficulty = #{difficulty}",
            "   </if>",
            "</where>",
            "ORDER BY created_at DESC",
            "</script>"
    })
    @Results({
            @Result(property = "id",                 column = "id"),
            @Result(property = "questionText",       column = "question_text"),
            @Result(property = "questionType",       column = "question_type"),
            @Result(property = "difficulty",         column = "difficulty"),
            @Result(property = "knowledgePointIds",  column = "knowledge_point_ids", typeHandler = org.bedrock.teateach.typehandler.LongListJsonTypeHandler.class),
            @Result(property = "options",            column = "options", typeHandler = org.bedrock.teateach.typehandler.StringArrayJsonTypeHandler.class),
            @Result(property = "correctAnswer",      column = "correct_answer"),
            @Result(property = "explanation",        column = "explanation"),
            @Result(property = "programmingLanguage", column = "programming_language"),
            @Result(property = "templateCode",       column = "template_code"),
            @Result(property = "testCases",          column = "test_cases"),
            @Result(property = "createdAt",          column = "created_at"),
            @Result(property = "updatedAt",          column = "updated_at")
    })
    List<Question> findByTypeAndDifficulty(@Param("type") String type, @Param("difficulty") String difficulty);

    /**
     * Finds questions associated with a specific knowledge point.
     *
     * @param knowledgePointId The ID of the knowledge point.
     * @return A list of questions related to the knowledge point.
     */
    @Select("SELECT id, question_text, question_type, difficulty, knowledge_point_ids, options, correct_answer, " +
            "explanation, programming_language, template_code, test_cases, created_at, updated_at FROM questions " +
            "WHERE JSON_CONTAINS(knowledge_point_ids, #{knowledgePointId}) ORDER BY created_at DESC")
    @Results({
            @Result(property = "id",                 column = "id"),
            @Result(property = "questionText",       column = "question_text"),
            @Result(property = "questionType",       column = "question_type"),
            @Result(property = "difficulty",         column = "difficulty"),
            @Result(property = "knowledgePointIds",  column = "knowledge_point_ids", typeHandler = org.bedrock.teateach.typehandler.LongListJsonTypeHandler.class),
            @Result(property = "options",            column = "options", typeHandler = org.bedrock.teateach.typehandler.StringArrayJsonTypeHandler.class),
            @Result(property = "correctAnswer",      column = "correct_answer"),
            @Result(property = "explanation",        column = "explanation"),
            @Result(property = "programmingLanguage", column = "programming_language"),
            @Result(property = "templateCode",       column = "template_code"),
            @Result(property = "testCases",          column = "test_cases"),
            @Result(property = "createdAt",          column = "created_at"),
            @Result(property = "updatedAt",          column = "updated_at")
    })
    List<Question> findByKnowledgePointId(Long knowledgePointId);

    /**
     * Finds all questions with pagination.
     *
     * @param offset The offset for pagination.
     * @param limit The limit for pagination.
     * @return A list of questions.
     */
    @Select("SELECT id, question_text, question_type, difficulty, knowledge_point_ids, options, correct_answer, " +
            "explanation, programming_language, template_code, test_cases, created_at, updated_at " +
            "FROM questions ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    @Results({
            @Result(property = "id",                 column = "id"),
            @Result(property = "questionText",       column = "question_text"),
            @Result(property = "questionType",       column = "question_type"),
            @Result(property = "difficulty",         column = "difficulty"),
            @Result(property = "knowledgePointIds",  column = "knowledge_point_ids", typeHandler = org.bedrock.teateach.typehandler.LongListJsonTypeHandler.class),
            @Result(property = "options",            column = "options", typeHandler = org.bedrock.teateach.typehandler.StringArrayJsonTypeHandler.class),
            @Result(property = "correctAnswer",      column = "correct_answer"),
            @Result(property = "explanation",        column = "explanation"),
            @Result(property = "programmingLanguage", column = "programming_language"),
            @Result(property = "templateCode",       column = "template_code"),
            @Result(property = "testCases",          column = "test_cases"),
            @Result(property = "createdAt",          column = "created_at"),
            @Result(property = "updatedAt",          column = "updated_at")
    })
    List<Question> findAllWithPagination(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * Finds all questions with pagination and search.
     *
     * @param offset The offset for pagination.
     * @param limit The limit for pagination.
     * @param search The search term.
     * @return A list of questions matching the search criteria.
     */
    @Select("SELECT id, question_text, question_type, difficulty, knowledge_point_ids, options, correct_answer, " +
            "explanation, programming_language, template_code, test_cases, created_at, updated_at " +
            "FROM questions WHERE (question_text LIKE CONCAT('%', #{search}, '%') " +
            "OR explanation LIKE CONCAT('%', #{search}, '%')) ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    @Results({
            @Result(property = "id",                 column = "id"),
            @Result(property = "questionText",       column = "question_text"),
            @Result(property = "questionType",       column = "question_type"),
            @Result(property = "difficulty",         column = "difficulty"),
            @Result(property = "knowledgePointIds",  column = "knowledge_point_ids", typeHandler = org.bedrock.teateach.typehandler.LongListJsonTypeHandler.class),
            @Result(property = "options",            column = "options", typeHandler = org.bedrock.teateach.typehandler.StringArrayJsonTypeHandler.class),
            @Result(property = "correctAnswer",      column = "correct_answer"),
            @Result(property = "explanation",        column = "explanation"),
            @Result(property = "programmingLanguage", column = "programming_language"),
            @Result(property = "templateCode",       column = "template_code"),
            @Result(property = "testCases",          column = "test_cases"),
            @Result(property = "createdAt",          column = "created_at"),
            @Result(property = "updatedAt",          column = "updated_at")
    })
    List<Question> findAllWithPaginationAndSearch(@Param("offset") int offset, @Param("limit") int limit, @Param("search") String search);
}