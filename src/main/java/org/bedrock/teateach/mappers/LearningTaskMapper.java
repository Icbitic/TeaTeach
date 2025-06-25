package org.bedrock.teateach.mappers;

import org.apache.ibatis.annotations.*;
import org.bedrock.teateach.beans.LearningTask;
import org.bedrock.teateach.enums.TaskType;

import java.util.List;

@Mapper // This annotation tells Spring that this is a MyBatis mapper interface
public interface LearningTaskMapper {

    /**
     * Inserts a new LearningTask record into the database.
     * The generated primary key (id) will be set back into the LearningTask object.
     *
     * @param task The LearningTask object to insert.
     */
    @Insert("INSERT INTO learning_tasks(course_id, task_name, task_type, task_description, deadline, submission_method) " +
            "VALUES(#{courseId}, #{taskName}, #{taskType}, #{taskDescription}, #{deadline}, #{submissionMethod})")
    @Options(useGeneratedKeys = true, keyProperty = "id") // Tells MyBatis to set the generated ID back to the object
    void insert(LearningTask task);

    /**
     * Updates an existing LearningTask record in the database.
     *
     * @param task The LearningTask object with updated information.
     */
    @Update("UPDATE learning_tasks SET " +
            "course_id = #{courseId}, " +
            "task_name = #{taskName}, " +
            "task_type = #{taskType}, " +
            "task_description = #{taskDescription}, " +
            "deadline = #{deadline}, " +
            "submission_method = #{submissionMethod} " +
            "WHERE id = #{id}")
    void update(LearningTask task);

    /**
     * Deletes a LearningTask record from the database by its ID.
     *
     * @param id The ID of the LearningTask to delete.
     */
    @Delete("DELETE FROM learning_tasks WHERE id = #{id}")
    void delete(@Param("id") Long id); // @Param is used when there's a single parameter in the query

    /**
     * Finds a LearningTask record by its ID.
     *
     * @param id The ID of the LearningTask to find.
     * @return The LearningTask object if found, otherwise null.
     */
    @Select("SELECT id, course_id, task_name, task_type, task_description, deadline, submission_method " +
            "FROM learning_tasks WHERE id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "taskName", column = "task_name"),
            @Result(property = "taskType", column = "task_type", javaType = TaskType.class),
            @Result(property = "taskDescription", column = "task_description"),
            @Result(property = "deadline", column = "deadline"),
            @Result(property = "submissionMethod", column = "submission_method")
    })
    LearningTask findById(@Param("id") Long id);

    /**
     * Finds all LearningTask records associated with a specific course.
     *
     * @param courseId The ID of the course.
     * @return A list of LearningTask objects for the given course.
     */
    @Select("SELECT id, course_id, task_name, task_type, task_description, deadline, submission_method " +
            "FROM learning_tasks WHERE course_id = #{courseId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "taskName", column = "task_name"),
            @Result(property = "taskType", column = "task_type", javaType = TaskType.class),
            @Result(property = "taskDescription", column = "task_description"),
            @Result(property = "deadline", column = "deadline"),
            @Result(property = "submissionMethod", column = "submission_method")
    })
    List<LearningTask> findByCourseId(@Param("courseId") Long courseId);

    /**
     * Finds all LearningTask records in the database.
     *
     * @return A list of all LearningTask objects.
     */
    @Select("SELECT id, course_id, task_name, task_type, task_description, deadline, submission_method " +
            "FROM learning_tasks")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "taskName", column = "task_name"),
            @Result(property = "taskType", column = "task_type", javaType = TaskType.class),
            @Result(property = "taskDescription", column = "task_description"),
            @Result(property = "deadline", column = "deadline"),
            @Result(property = "submissionMethod", column = "submission_method")
    })
    List<LearningTask> findAll();

    // You might add more specific queries here based on your needs, e.g.:
    // List<LearningTask> findOverdueTasks(@Param("currentDateTime") LocalDateTime currentDateTime);
    // List<LearningTask> findTasksByType(@Param("taskType") TaskType taskType);
}