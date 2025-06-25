package org.bedrock.teateach.mappers;

import org.apache.ibatis.annotations.*;
import org.bedrock.teateach.beans.TaskResource;
import org.bedrock.teateach.beans.Resource;

import java.util.List;

@Mapper
public interface TaskResourceMapper {

    /**
     * Inserts a new task-resource relationship into the database.
     *
     * @param taskResource The TaskResource object to insert.
     */
    @Insert("INSERT INTO task_resources (task_id, resource_id, created_at, updated_at) " +
            "VALUES (#{taskId}, #{resourceId}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(TaskResource taskResource);

    /**
     * Finds all resources associated with a specific task.
     *
     * @param taskId The ID of the task.
     * @return List of Resource objects associated with the task.
     */
    @Select("SELECT r.id, r.resource_name, r.file_path, r.file_type, r.file_size, r.description, " +
            "r.uploaded_by, r.created_at, r.updated_at " +
            "FROM resources r " +
            "INNER JOIN task_resources tr ON r.id = tr.resource_id " +
            "WHERE tr.task_id = #{taskId} " +
            "ORDER BY tr.created_at ASC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "resourceName", column = "resource_name"),
            @Result(property = "filePath", column = "file_path"),
            @Result(property = "fileType", column = "file_type"),
            @Result(property = "fileSize", column = "file_size"),
            @Result(property = "description", column = "description"),
            @Result(property = "uploadedBy", column = "uploaded_by"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    List<Resource> findResourcesByTaskId(Long taskId);

    /**
     * Finds all task-resource relationships for a specific task.
     *
     * @param taskId The ID of the task.
     * @return List of TaskResource objects.
     */
    @Select("SELECT id, task_id, resource_id, created_at, updated_at " +
            "FROM task_resources WHERE task_id = #{taskId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "taskId", column = "task_id"),
            @Result(property = "resourceId", column = "resource_id"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    List<TaskResource> findByTaskId(Long taskId);

    /**
     * Deletes a specific task-resource relationship.
     *
     * @param taskId The ID of the task.
     * @param resourceId The ID of the resource.
     */
    @Delete("DELETE FROM task_resources WHERE task_id = #{taskId} AND resource_id = #{resourceId}")
    void deleteByTaskIdAndResourceId(@Param("taskId") Long taskId, @Param("resourceId") Long resourceId);

    /**
     * Deletes all task-resource relationships for a specific task.
     *
     * @param taskId The ID of the task.
     */
    @Delete("DELETE FROM task_resources WHERE task_id = #{taskId}")
    void deleteByTaskId(Long taskId);

    /**
     * Checks if a task-resource relationship exists.
     *
     * @param taskId The ID of the task.
     * @param resourceId The ID of the resource.
     * @return true if the relationship exists, false otherwise.
     */
    @Select("SELECT COUNT(*) > 0 FROM task_resources WHERE task_id = #{taskId} AND resource_id = #{resourceId}")
    boolean existsByTaskIdAndResourceId(@Param("taskId") Long taskId, @Param("resourceId") Long resourceId);
}