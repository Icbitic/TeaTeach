package org.bedrock.teateach.mappers;

import org.apache.ibatis.annotations.*;
import org.bedrock.teateach.beans.PlaybackVector;

import java.util.List;

@Mapper
public interface PlaybackVectorMapper {

    @Insert("INSERT INTO playback_vectors (student_id, resource_id, playback_data, video_duration, last_updated) " +
           "VALUES (#{studentId}, #{resourceId}, #{playbackData}, #{videoDuration}, #{lastUpdated})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(PlaybackVector playbackVector);

    @Update("UPDATE playback_vectors SET playback_data = #{playbackData}, last_updated = #{lastUpdated} " +
           "WHERE id = #{id}")
    void update(PlaybackVector playbackVector);

    @Delete("DELETE FROM playback_vectors WHERE id = #{id}")
    void delete(Long id);

    @Select("SELECT * FROM playback_vectors WHERE id = #{id}")
    PlaybackVector findById(Long id);

    @Select("SELECT * FROM playback_vectors WHERE student_id = #{studentId} AND resource_id = #{resourceId}")
    PlaybackVector findByStudentAndResource(Long studentId, Long resourceId);

    @Select("SELECT * FROM playback_vectors WHERE student_id = #{studentId}")
    List<PlaybackVector> findByStudentId(Long studentId);

    @Select("SELECT * FROM playback_vectors WHERE resource_id = #{resourceId}")
    List<PlaybackVector> findByResourceId(Long resourceId);

    @Select("SELECT * FROM playback_vectors")
    List<PlaybackVector> findAll();
}
