// src/main/java/org/bedrock/teateach/mappers/ResourceMapper.java
package org.bedrock.teateach.mappers;

import org.apache.ibatis.annotations.*;
import org.bedrock.teateach.beans.Resource;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ResourceMapper {

    /**
     * Inserts a new resource into the database.
     * The ID of the resource will be generated and set back into the Resource object.
     *
     * @param resource The Resource object to insert.
     */
    @Insert("INSERT INTO resources (resource_name, file_path, file_type, file_size, course_id, task_id, description, uploaded_by, created_at, updated_at) " +
            "VALUES (#{resourceName}, #{filePath}, #{fileType}, #{fileSize}, #{courseId}, #{taskId}, #{description}, #{uploadedBy}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Resource resource);

    /**
     * Finds a resource by its ID.
     *
     * @param id The ID of the resource.
     * @return The Resource object if found, otherwise null.
     */
    @Select("SELECT id, resource_name, file_path, file_type, file_size, course_id, task_id, description, uploaded_by, created_at, updated_at " +
            "FROM resources WHERE id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "resourceName", column = "resource_name"),
            @Result(property = "filePath", column = "file_path"),
            @Result(property = "fileType", column = "file_type"),
            @Result(property = "fileSize", column = "file_size"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "taskId", column = "task_id"),
            @Result(property = "description", column = "description"),
            @Result(property = "uploadedBy", column = "uploaded_by"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    Resource findById(Long id);

    /**
     * Updates an existing resource in the database.
     *
     * @param resource The Resource object with updated fields.
     */
    @Update("UPDATE resources SET resource_name = #{resourceName}, file_path = #{filePath}, file_type = #{fileType}, " +
            "file_size = #{fileSize}, course_id = #{courseId}, task_id = #{taskId}, description = #{description}, " +
            "uploaded_by = #{uploadedBy}, updated_at = #{updatedAt} WHERE id = #{id}")
    void update(Resource resource);

    /**
     * Deletes a resource by its ID.
     *
     * @param id The ID of the resource to delete.
     */
    @Delete("DELETE FROM resources WHERE id = #{id}")
    void delete(Long id);

    /**
     * Retrieves all resources associated with a specific course.
     *
     * @param courseId The ID of the course.
     * @return A list of Resource objects for the specified course.
     */
    @Select("SELECT id, resource_name, file_path, file_type, file_size, course_id, task_id, description, uploaded_by, created_at, updated_at " +
            "FROM resources WHERE course_id = #{courseId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "resourceName", column = "resource_name"),
            @Result(property = "filePath", column = "file_path"),
            @Result(property = "fileType", column = "file_type"),
            @Result(property = "fileSize", column = "file_size"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "taskId", column = "task_id"),
            @Result(property = "description", column = "description"),
            @Result(property = "uploadedBy", column = "uploaded_by"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    List<Resource> findByCourseId(Long courseId);

    /**
     * Retrieves all resources associated with a specific task.
     *
     * @param taskId The ID of the task.
     * @return A list of Resource objects for the specified task.
     */
    @Select("SELECT id, resource_name, file_path, file_type, file_size, course_id, task_id, description, uploaded_by, created_at, updated_at " +
            "FROM resources WHERE task_id = #{taskId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "resourceName", column = "resource_name"),
            @Result(property = "filePath", column = "file_path"),
            @Result(property = "fileType", column = "file_type"),
            @Result(property = "fileSize", column = "file_size"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "taskId", column = "task_id"),
            @Result(property = "description", column = "description"),
            @Result(property = "uploadedBy", column = "uploaded_by"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    List<Resource> findByTaskId(Long taskId);

    /**
     * Retrieves all resources from the database.
     *
     * @return A list of all Resource objects.
     */
    @Select("SELECT id, resource_name, file_path, file_type, file_size, course_id, task_id, description, uploaded_by, created_at, updated_at " +
            "FROM resources")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "resourceName", column = "resource_name"),
            @Result(property = "filePath", column = "file_path"),
            @Result(property = "fileType", column = "file_type"),
            @Result(property = "fileSize", column = "file_size"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "taskId", column = "task_id"),
            @Result(property = "description", column = "description"),
            @Result(property = "uploadedBy", column = "uploaded_by"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    List<Resource> findAll();


    /**
     * Defines the result mapping for Resource objects.
     * This is a placeholder select method that's never called directly,
     * but MyBatis uses its @Results annotation for mapping.
     */
    @Select("SELECT 1 FROM dual")
    @Results(id = "resourceResultMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "resourceName", column = "resource_name"),
            @Result(property = "filePath", column = "file_path"),
            @Result(property = "fileType", column = "file_type"),
            @Result(property = "fileSize", column = "file_size"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "taskId", column = "task_id"),
            @Result(property = "description", column = "description"),
            @Result(property = "uploadedBy", column = "uploaded_by"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    Resource defineResourceResultMap();
}