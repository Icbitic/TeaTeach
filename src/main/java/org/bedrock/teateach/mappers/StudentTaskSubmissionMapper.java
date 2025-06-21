package org.bedrock.teateach.mappers;

import org.apache.ibatis.annotations.*;
import org.bedrock.teateach.beans.StudentTaskSubmission;

import java.util.List;

@Mapper
public interface StudentTaskSubmissionMapper {

    /**
     * Inserts a new StudentTaskSubmission record into the database.
     * The generated primary key (id) will be set back into the StudentTaskSubmission object.
     *
     * @param submission The StudentTaskSubmission object to insert.
     */
    @Insert("INSERT INTO student_task_submissions(" +
            "task_id, student_id, submission_content, submission_time, score, feedback, completion_status) " +
            "VALUES(#{taskId}, #{studentId}, #{submissionContent}, #{submissionTime}, #{score}, #{feedback}, #{completionStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(StudentTaskSubmission submission);

    /**
     * Updates an existing StudentTaskSubmission record in the database.
     *
     * @param submission The StudentTaskSubmission object with updated information.
     */
    @Update("UPDATE student_task_submissions SET " +
            "task_id = #{taskId}, " +
            "student_id = #{studentId}, " +
            "submission_content = #{submissionContent}, " +
            "submission_time = #{submissionTime}, " +
            "score = #{score}, " +
            "feedback = #{feedback}, " +
            "completion_status = #{completionStatus} " +
            "WHERE id = #{id}")
    void update(StudentTaskSubmission submission);

    /**
     * Deletes a StudentTaskSubmission record from the database by its ID.
     *
     * @param id The ID of the submission to delete.
     */
    @Delete("DELETE FROM student_task_submissions WHERE id = #{id}")
    void delete(@Param("id") Long id);

    /**
     * Finds a StudentTaskSubmission record by its ID.
     *
     * @param id The ID of the submission to find.
     * @return The StudentTaskSubmission object if found, otherwise null.
     */
    @Select("SELECT id, task_id, student_id, submission_content, submission_time, score, feedback, completion_status " +
            "FROM student_task_submissions WHERE id = #{id}")
    StudentTaskSubmission findById(@Param("id") Long id);

    /**
     * Finds all StudentTaskSubmission records for a specific student and task.
     * Useful for checking if a student has submitted a specific task.
     *
     * @param studentId The ID of the student.
     * @param taskId The ID of the task.
     * @return The StudentTaskSubmission object if found, otherwise null (assuming one submission per student per task).
     */
    @Select("SELECT id, task_id, student_id, submission_content, submission_time, score, feedback, completion_status " +
            "FROM student_task_submissions WHERE student_id = #{studentId} AND task_id = #{taskId} LIMIT 1")
    StudentTaskSubmission findByStudentAndTask(@Param("studentId") Long studentId, @Param("taskId") Long taskId);

    /**
     * Finds all StudentTaskSubmission records for a specific student within a course.
     * This query requires joining with the `learning_tasks` table to filter by course.
     *
     * @param studentId The ID of the student.
     * @param courseId The ID of the course.
     * @return A list of StudentTaskSubmission objects for the given student in the specified course.
     */
    @Select("SELECT sts.id, sts.task_id, sts.student_id, sts.submission_content, " +
            "sts.submission_time, sts.score, sts.feedback, sts.completion_status " +
            "FROM student_task_submissions sts " +
            "JOIN learning_tasks lt ON sts.task_id = lt.id " +
            "WHERE sts.student_id = #{studentId} AND lt.course_id = #{courseId}")
    List<StudentTaskSubmission> findByStudentAndCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    /**
     * Finds all StudentTaskSubmission records for a specific task.
     * Useful for teachers to see all submissions for a particular assignment.
     *
     * @param taskId The ID of the task.
     * @return A list of StudentTaskSubmission objects for the given task.
     */
    @Select("SELECT id, task_id, student_id, submission_content, submission_time, score, feedback, completion_status " +
            "FROM student_task_submissions WHERE task_id = #{taskId}")
    List<StudentTaskSubmission> findByTaskId(@Param("taskId") Long taskId);

    /**
     * Finds all StudentTaskSubmission records in the database.
     *
     * @return A list of all StudentTaskSubmission objects.
     */
    @Select("SELECT id, task_id, student_id, submission_content, submission_time, score, feedback, completion_status " +
            "FROM student_task_submissions")
    List<StudentTaskSubmission> findAll();
}