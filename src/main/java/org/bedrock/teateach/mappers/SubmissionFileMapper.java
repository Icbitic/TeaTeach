package org.bedrock.teateach.mappers;

import org.apache.ibatis.annotations.*;
import org.bedrock.teateach.beans.SubmissionFile;

import java.util.List;

@Mapper
public interface SubmissionFileMapper {

    /**
     * Inserts a new SubmissionFile record into the database.
     * The generated primary key (id) will be set back into the SubmissionFile object.
     *
     * @param submissionFile The SubmissionFile object to insert.
     */
    @Insert("INSERT INTO submission_files(" +
            "submission_id, file_name, stored_file_name, file_path, file_type, file_size, mime_type) " +
            "VALUES(#{submissionId}, #{fileName}, #{storedFileName}, #{filePath}, #{fileType}, #{fileSize}, #{mimeType})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(SubmissionFile submissionFile);

    /**
     * Updates an existing SubmissionFile record in the database.
     *
     * @param submissionFile The SubmissionFile object with updated information.
     */
    @Update("UPDATE submission_files SET " +
            "submission_id = #{submissionId}, " +
            "file_name = #{fileName}, " +
            "stored_file_name = #{storedFileName}, " +
            "file_path = #{filePath}, " +
            "file_type = #{fileType}, " +
            "file_size = #{fileSize}, " +
            "mime_type = #{mimeType} " +
            "WHERE id = #{id}")
    void update(SubmissionFile submissionFile);

    /**
     * Deletes a SubmissionFile record from the database by its ID.
     *
     * @param id The ID of the SubmissionFile to delete.
     */
    @Delete("DELETE FROM submission_files WHERE id = #{id}")
    void deleteById(Long id);

    /**
     * Retrieves a SubmissionFile record from the database by its ID.
     *
     * @param id The ID of the SubmissionFile to retrieve.
     * @return The SubmissionFile object, or null if not found.
     */
    @Select("SELECT * FROM submission_files WHERE id = #{id}")
    SubmissionFile findById(Long id);

    /**
     * Retrieves all SubmissionFile records from the database.
     *
     * @return A list of all SubmissionFile objects.
     */
    @Select("SELECT * FROM submission_files")
    List<SubmissionFile> findAll();

    /**
     * Retrieves all SubmissionFile records for a specific submission.
     *
     * @param submissionId The ID of the submission.
     * @return A list of SubmissionFile objects for the submission.
     */
    @Select("SELECT * FROM submission_files WHERE submission_id = #{submissionId}")
    List<SubmissionFile> findBySubmissionId(Long submissionId);

    /**
     * Deletes all SubmissionFile records for a specific submission.
     *
     * @param submissionId The ID of the submission.
     */
    @Delete("DELETE FROM submission_files WHERE submission_id = #{submissionId}")
    void deleteBySubmissionId(Long submissionId);
}