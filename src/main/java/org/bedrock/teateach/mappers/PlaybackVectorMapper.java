package org.bedrock.teateach.mappers;

import org.apache.ibatis.annotations.*;
import org.bedrock.teateach.beans.PlaybackVector;

import java.util.List;

@Mapper
public interface PlaybackVectorMapper {

    @Insert("INSERT INTO playback_vectors (student_id, resource_id, playback_data, video_duration, last_updated) " +
            "VALUES (#{studentId}, #{resourceId}, COALESCE(#{playbackData, typeHandler=org.bedrock.teateach.typehandler.IntArrayJsonTypeHandler}, CAST('[]' AS JSON)), #{videoDuration}, #{lastUpdated})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(PlaybackVector playbackVector);

    @Update("UPDATE playback_vectors SET playback_data = COALESCE(#{playbackData, typeHandler=org.bedrock.teateach.typehandler.IntArrayJsonTypeHandler}, CAST('[]' AS JSON)), last_updated = #{lastUpdated} " +
            "WHERE id = #{id}")
    void update(PlaybackVector playbackVector);

    @Delete("DELETE FROM playback_vectors WHERE id = #{id}")
    void delete(Long id);

    @Select("SELECT * FROM playback_vectors WHERE id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "resourceId", column = "resource_id"),
            @Result(property = "playbackData", column = "playback_data", typeHandler = org.bedrock.teateach.typehandler.IntArrayJsonTypeHandler.class),
            @Result(property = "videoDuration", column = "video_duration"),
            @Result(property = "lastUpdated", column = "last_updated")
    })
    PlaybackVector findById(Long id);

    @Select("SELECT * FROM playback_vectors WHERE student_id = #{studentId} AND resource_id = #{resourceId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "resourceId", column = "resource_id"),
            @Result(property = "playbackData", column = "playback_data", typeHandler = org.bedrock.teateach.typehandler.IntArrayJsonTypeHandler.class),
            @Result(property = "videoDuration", column = "video_duration"),
            @Result(property = "lastUpdated", column = "last_updated")
    })
    PlaybackVector findByStudentAndResource(Long studentId, Long resourceId);

    @Select("SELECT * FROM playback_vectors WHERE student_id = #{studentId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "resourceId", column = "resource_id"),
            @Result(property = "playbackData", column = "playback_data", typeHandler = org.bedrock.teateach.typehandler.IntArrayJsonTypeHandler.class),
            @Result(property = "videoDuration", column = "video_duration"),
            @Result(property = "lastUpdated", column = "last_updated")
    })
    List<PlaybackVector> findByStudentId(Long studentId);

    @Select("SELECT * FROM playback_vectors WHERE resource_id = #{resourceId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "resourceId", column = "resource_id"),
            @Result(property = "playbackData", column = "playback_data", typeHandler = org.bedrock.teateach.typehandler.IntArrayJsonTypeHandler.class),
            @Result(property = "videoDuration", column = "video_duration"),
            @Result(property = "lastUpdated", column = "last_updated")
    })
    List<PlaybackVector> findByResourceId(Long resourceId);

    @Select("SELECT * FROM playback_vectors")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "resourceId", column = "resource_id"),
            @Result(property = "playbackData", column = "playback_data", typeHandler = org.bedrock.teateach.typehandler.IntArrayJsonTypeHandler.class),
            @Result(property = "videoDuration", column = "video_duration"),
            @Result(property = "lastUpdated", column = "last_updated")
    })
    List<PlaybackVector> findAll();
}
