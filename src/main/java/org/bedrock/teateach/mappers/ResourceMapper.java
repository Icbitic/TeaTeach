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
    @Insert("INSERT INTO resources (resource_name, file_path, file_type, file_size, description, uploaded_by, created_at, updated_at) " +
            "VALUES (#{resourceName}, #{filePath}, #{fileType}, #{fileSize}, #{description}, #{uploadedBy}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Resource resource);

    /**
     * Finds a resource by its ID.
     *
     * @param id The ID of the resource.
     * @return The Resource object if found, otherwise null.
     */
    @Select("SELECT id, resource_name, file_path, file_type, file_size, description, uploaded_by, created_at, updated_at " +
            "FROM resources WHERE id = #{id}")
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
    Resource findById(Long id);

    /**
     * Updates an existing resource in the database.
     *
     * @param resource The Resource object with updated fields.
     */
    @Update("UPDATE resources SET resource_name = #{resourceName}, file_path = #{filePath}, file_type = #{fileType}, " +
            "file_size = #{fileSize}, description = #{description}, " +
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
     * Retrieves all resources from the database.
     *
     * @return A list of all Resource objects.
     */
    @Select("SELECT id, resource_name, file_path, file_type, file_size, description, uploaded_by, created_at, updated_at " +
            "FROM resources")
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
    List<Resource> findAll();

    /**
     * Finds all resources uploaded by a specific user.
     *
     * @param uploadedBy The ID of the user who uploaded the resources.
     * @return A list of Resource objects uploaded by the specified user.
     */
    @Select("SELECT id, resource_name, file_path, file_type, file_size, description, uploaded_by, created_at, updated_at " +
            "FROM resources WHERE uploaded_by = #{uploadedBy} ORDER BY created_at DESC")
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
    List<Resource> findByUploadedBy(@Param("uploadedBy") Long uploadedBy);

    /**
     * Finds resources associated with a specific task.
     * This joins with learning_tasks table to find resources linked to a task.
     *
     * @param taskId The ID of the task.
     * @return A list of Resource objects associated with the specified task.
     */
    @Select("SELECT r.id, r.resource_name, r.file_path, r.file_type, r.file_size, r.description, r.uploaded_by, r.created_at, r.updated_at " +
            "FROM resources r INNER JOIN learning_tasks lt ON r.id = lt.resource_id WHERE lt.id = #{taskId}")
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
    List<Resource> findByTaskId(@Param("taskId") Long taskId);

    /**
     * Finds resources uploaded by a specific user for a specific task.
     *
     * @param uploadedBy The ID of the user who uploaded the resources.
     * @param taskId The ID of the task.
     * @return A list of Resource objects uploaded by the user for the specified task.
     */
    @Select("SELECT r.id, r.resource_name, r.file_path, r.file_type, r.file_size, r.description, r.uploaded_by, r.created_at, r.updated_at " +
            "FROM resources r INNER JOIN learning_tasks lt ON r.id = lt.resource_id " +
            "WHERE r.uploaded_by = #{uploadedBy} AND lt.id = #{taskId} ORDER BY r.created_at DESC")
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
    List<Resource> findByUploadedByAndTaskId(@Param("uploadedBy") Long uploadedBy, @Param("taskId") Long taskId);

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
            @Result(property = "description", column = "description"),
            @Result(property = "uploadedBy", column = "uploaded_by"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    Resource defineResourceResultMap();
}