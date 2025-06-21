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
    @Insert("INSERT INTO questions (question_text, question_type, difficulty, knowledge_point_ids, options, correct_answer, created_at, updated_at) " +
            "VALUES (#{questionText}, #{questionType}, #{difficulty}, #{knowledgePointIds}, #{options}, #{correctAnswer}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Question question);

    /**
     * Finds a question by its ID.
     *
     * @param id The ID of the question.
     * @return The Question object if found, otherwise null.
     */
    @Select("SELECT id, question_text, question_type, difficulty, knowledge_point_ids, options, correct_answer, created_at, updated_at " +
            "FROM questions WHERE id = #{id}")
    @ResultMap("questionResultMap")
    Question findById(Long id);

    /**
     * Updates an existing question in the database.
     *
     * @param question The Question object with updated fields.
     */
    @Update("UPDATE questions SET question_text = #{questionText}, question_type = #{questionType}, " +
            "difficulty = #{difficulty}, knowledge_point_ids = #{knowledgePointIds}, options = #{options}, " +
            "correct_answer = #{correctAnswer}, updated_at = #{updatedAt} WHERE id = #{id}")
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
    @Select("SELECT id, question_text, question_type, difficulty, knowledge_point_ids, options, correct_answer, created_at, updated_at " +
            "FROM questions")
    @ResultMap("questionResultMap")
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
            "SELECT id, question_text, question_type, difficulty, knowledge_point_ids, options, correct_answer, created_at, updated_at",
            "FROM questions",
            "<where>",
            "   <if test='type != null and type != \"\"'>",
            "       question_type = #{type}",
            "   </if>",
            "   <if test='difficulty != null and difficulty != \"\"'>",
            "       AND difficulty = #{difficulty}",
            "   </if>",
            "</where>",
            "</script>"
    })
    @ResultMap("questionResultMap")
    List<Question> findByTypeAndDifficulty(@Param("type") String type, @Param("difficulty") String difficulty);

    /**
     * Finds questions associated with a specific knowledge point.
     *
     * @param knowledgePointId The ID of the knowledge point.
     * @return A list of questions related to the knowledge point.
     */
    @Select("SELECT id, question_text, question_type, difficulty, knowledge_point_ids, options, correct_answer, created_at, updated_at " +
            "FROM questions WHERE knowledge_point_ids = #{knowledgePointId}") // Changed from knowledge_point_id to knowledge_point_ids
    @ResultMap("questionResultMap")
    List<Question> findByKnowledgePointId(Long knowledgePointId);

    // This ResultMap helps MyBatis map snake_case database columns to camelCase Java bean properties.
    @Results(id = "questionResultMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "questionText", column = "question_text"),
            @Result(property = "questionType", column = "question_type"),
            @Result(property = "difficulty", column = "difficulty"), // Changed from difficultyLevel
            @Result(property = "knowledgePointIds", column = "knowledge_point_ids"), // Changed from knowledgePointId
            @Result(property = "options", column = "options"),
            @Result(property = "correctAnswer", column = "correct_answer"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    // Empty method, just to hold the @Results annotation
    default void questionResultMap() {}
}