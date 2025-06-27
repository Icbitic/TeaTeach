package org.bedrock.teateach.mappers;

import org.apache.ibatis.annotations.*;
import org.bedrock.teateach.beans.TestPaper;

import java.util.List;

@Mapper
public interface TestPaperMapper {

    /**
     * Inserts a new test paper into the database.
     * The ID of the test paper will be generated and set back into the TestPaper object.
     *
     * @param testPaper The TestPaper object to insert.
     */
    @Insert("INSERT INTO test_papers (paper_name, course_id, instructor_id, question_ids, total_score, " +
            "duration_minutes, generation_method, created_at) " +
            "VALUES (#{paperName}, #{courseId}, #{instructorId}, #{questionIds}, #{totalScore}, " +
            "#{durationMinutes}, #{generationMethod}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(TestPaper testPaper);

    /**
     * Finds a test paper by its ID.
     *
     * @param id The ID of the test paper.
     * @return The TestPaper object if found, otherwise null.
     */
    @Select("SELECT id, paper_name, course_id, instructor_id, question_ids, total_score, " +
            "duration_minutes, generation_method, created_at " +
            "FROM test_papers WHERE id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "paperName", column = "paper_name"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "instructorId", column = "instructor_id"),
            @Result(property = "questionIds", column = "question_ids"),
            @Result(property = "totalScore", column = "total_score"),
            @Result(property = "durationMinutes", column = "duration_minutes"),
            @Result(property = "generationMethod", column = "generation_method"),
            @Result(property = "createdAt", column = "created_at")
    })
    TestPaper findById(Long id);

    /**
     * Updates an existing test paper in the database.
     *
     * @param testPaper The TestPaper object with updated fields.
     */
    @Update("UPDATE test_papers SET paper_name = #{paperName}, course_id = #{courseId}, " +
            "instructor_id = #{instructorId}, question_ids = #{questionIds}, total_score = #{totalScore}, " +
            "duration_minutes = #{durationMinutes}, generation_method = #{generationMethod} " +
            "WHERE id = #{id}")
    void update(TestPaper testPaper);

    /**
     * Deletes a test paper by its ID.
     *
     * @param id The ID of the test paper to delete.
     */
    @Delete("DELETE FROM test_papers WHERE id = #{id}")
    void delete(Long id);

    /**
     * Retrieves all test papers from the database.
     *
     * @return A list of all TestPaper objects.
     */
    @Select("SELECT id, paper_name, course_id, instructor_id, question_ids, total_score, " +
            "duration_minutes, generation_method, created_at " +
            "FROM test_papers ORDER BY created_at DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "paperName", column = "paper_name"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "instructorId", column = "instructor_id"),
            @Result(property = "questionIds", column = "question_ids"),
            @Result(property = "totalScore", column = "total_score"),
            @Result(property = "durationMinutes", column = "duration_minutes"),
            @Result(property = "generationMethod", column = "generation_method"),
            @Result(property = "createdAt", column = "created_at")
    })
    List<TestPaper> findAll();

    /**
     * Finds test papers by course ID.
     *
     * @param courseId The ID of the course.
     * @return A list of test papers for the course.
     */
    @Select("SELECT id, paper_name, course_id, instructor_id, question_ids, total_score, " +
            "duration_minutes, generation_method, created_at " +
            "FROM test_papers WHERE course_id = #{courseId} ORDER BY created_at DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "paperName", column = "paper_name"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "instructorId", column = "instructor_id"),
            @Result(property = "questionIds", column = "question_ids"),
            @Result(property = "totalScore", column = "total_score"),
            @Result(property = "durationMinutes", column = "duration_minutes"),
            @Result(property = "generationMethod", column = "generation_method"),
            @Result(property = "createdAt", column = "created_at")
    })
    List<TestPaper> findByCourseId(Long courseId);

    /**
     * Finds test papers by instructor ID.
     *
     * @param instructorId The ID of the instructor.
     * @return A list of test papers created by the instructor.
     */
    @Select("SELECT id, paper_name, course_id, instructor_id, question_ids, total_score, " +
            "duration_minutes, generation_method, created_at " +
            "FROM test_papers WHERE instructor_id = #{instructorId} ORDER BY created_at DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "paperName", column = "paper_name"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "instructorId", column = "instructor_id"),
            @Result(property = "questionIds", column = "question_ids"),
            @Result(property = "totalScore", column = "total_score"),
            @Result(property = "durationMinutes", column = "duration_minutes"),
            @Result(property = "generationMethod", column = "generation_method"),
            @Result(property = "createdAt", column = "created_at")
    })
    List<TestPaper> findByInstructorId(Long instructorId);

    /**
     * Finds test papers that contain a specific question.
     *
     * @param questionId The ID of the question.
     * @return A list of test papers containing the question.
     */
    @Select("SELECT id, paper_name, course_id, instructor_id, question_ids, total_score, " +
            "duration_minutes, generation_method, created_at " +
            "FROM test_papers WHERE JSON_CONTAINS(question_ids, #{questionId}) ORDER BY created_at DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "paperName", column = "paper_name"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "instructorId", column = "instructor_id"),
            @Result(property = "questionIds", column = "question_ids"),
            @Result(property = "totalScore", column = "total_score"),
            @Result(property = "durationMinutes", column = "duration_minutes"),
            @Result(property = "generationMethod", column = "generation_method"),
            @Result(property = "createdAt", column = "created_at")
    })
    List<TestPaper> findByQuestionId(Long questionId);

    /**
     * Finds all test papers with pagination.
     *
     * @param offset The offset for pagination.
     * @param limit The limit for pagination.
     * @return A list of test papers.
     */
    @Select("SELECT id, paper_name, course_id, instructor_id, question_ids, total_score, " +
            "duration_minutes, generation_method, created_at " +
            "FROM test_papers ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "paperName", column = "paper_name"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "instructorId", column = "instructor_id"),
            @Result(property = "questionIds", column = "question_ids"),
            @Result(property = "totalScore", column = "total_score"),
            @Result(property = "durationMinutes", column = "duration_minutes"),
            @Result(property = "generationMethod", column = "generation_method"),
            @Result(property = "createdAt", column = "created_at")
    })
    List<TestPaper> findAllWithPagination(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * Finds all test papers with pagination and search.
     *
     * @param offset The offset for pagination.
     * @param limit The limit for pagination.
     * @param search The search term.
     * @return A list of test papers matching the search criteria.
     */
    @Select("SELECT id, paper_name, course_id, instructor_id, question_ids, total_score, " +
            "duration_minutes, generation_method, created_at " +
            "FROM test_papers WHERE paper_name LIKE CONCAT('%', #{search}, '%') " +
            "ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "paperName", column = "paper_name"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "instructorId", column = "instructor_id"),
            @Result(property = "questionIds", column = "question_ids"),
            @Result(property = "totalScore", column = "total_score"),
            @Result(property = "durationMinutes", column = "duration_minutes"),
            @Result(property = "generationMethod", column = "generation_method"),
            @Result(property = "createdAt", column = "created_at")
    })
    List<TestPaper> findAllWithPaginationAndSearch(@Param("offset") int offset, @Param("limit") int limit, @Param("search") String search);
}