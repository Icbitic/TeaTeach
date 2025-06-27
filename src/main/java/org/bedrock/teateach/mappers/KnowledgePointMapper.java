package org.bedrock.teateach.mappers;

import org.apache.ibatis.annotations.*;
import org.bedrock.teateach.beans.KnowledgePoint;

import java.util.List;

@Mapper
public interface KnowledgePointMapper {

    @Insert("INSERT INTO knowledge_points(name, brief_description, detailed_content, course_id, difficulty_level, " +
            "prerequisite_knowledge_point_ids, related_knowledge_point_ids) " +
            "VALUES(#{name}, #{briefDescription}, #{detailedContent}, #{courseId}, #{difficultyLevel}, " +
            "#{prerequisiteKnowledgePointIds,typeHandler=org.bedrock.teateach.typehandler.LongListTypeHandler}, " + // Custom TypeHandler
            "#{relatedKnowledgePointIds,typeHandler=org.bedrock.teateach.typehandler.LongListTypeHandler})")     // Custom TypeHandler
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(KnowledgePoint knowledgePoint);

    @Update("UPDATE knowledge_points SET name=#{name}, brief_description=#{briefDescription}, " +
            "detailed_content=#{detailedContent}, course_id=#{courseId}, difficulty_level=#{difficultyLevel}, " +
            "prerequisite_knowledge_point_ids=#{prerequisiteKnowledgePointIds,typeHandler=org.bedrock.teateach.typehandler.LongListTypeHandler}, " +
            "related_knowledge_point_ids=#{relatedKnowledgePointIds,typeHandler=org.bedrock.teateach.typehandler.LongListTypeHandler}, " +
            "updated_at=NOW() WHERE id=#{id}")
    void update(KnowledgePoint knowledgePoint);

    @Delete("DELETE FROM knowledge_points WHERE id=#{id}")
    void delete(@Param("id") Long id);

    @Select("SELECT kp.id, kp.name, kp.brief_description, kp.detailed_content, kp.course_id, kp.difficulty_level, " +
            "kp.prerequisite_knowledge_point_ids, kp.related_knowledge_point_ids, kp.created_at, kp.updated_at, " +
            "c.course_name " +
            "FROM knowledge_points kp LEFT JOIN courses c ON kp.course_id = c.id WHERE kp.id=#{id}")
    @Results({
            @Result(column="id", property="id"),
            @Result(column="name", property="name"),
            @Result(column="brief_description", property="briefDescription"),
            @Result(column="detailed_content", property="detailedContent"),
            @Result(column="course_id", property="courseId"),
            @Result(column="course_name", property="courseName"),
            @Result(column="difficulty_level", property="difficultyLevel"),
            @Result(column="prerequisite_knowledge_point_ids", property="prerequisiteKnowledgePointIds", javaType=List.class, typeHandler=org.bedrock.teateach.typehandler.LongListTypeHandler.class),
            @Result(column="related_knowledge_point_ids", property="relatedKnowledgePointIds", javaType=List.class, typeHandler=org.bedrock.teateach.typehandler.LongListTypeHandler.class),
            @Result(column="created_at", property="createdAt"),
            @Result(column="updated_at", property="updatedAt")
    })
    KnowledgePoint findById(@Param("id") Long id);

    @Select("SELECT kp.id, kp.name, kp.brief_description, kp.detailed_content, kp.course_id, kp.difficulty_level, " +
            "kp.prerequisite_knowledge_point_ids, kp.related_knowledge_point_ids, kp.created_at, kp.updated_at, " +
            "c.course_name " +
            "FROM knowledge_points kp LEFT JOIN courses c ON kp.course_id = c.id WHERE kp.course_id=#{courseId}")
    @Results({
            @Result(column="id", property="id"),
            @Result(column="name", property="name"),
            @Result(column="brief_description", property="briefDescription"),
            @Result(column="detailed_content", property="detailedContent"),
            @Result(column="course_id", property="courseId"),
            @Result(column="course_name", property="courseName"),
            @Result(column="difficulty_level", property="difficultyLevel"),
            @Result(column="prerequisite_knowledge_point_ids", property="prerequisiteKnowledgePointIds", javaType=List.class, typeHandler=org.bedrock.teateach.typehandler.LongListTypeHandler.class),
            @Result(column="related_knowledge_point_ids", property="relatedKnowledgePointIds", javaType=List.class, typeHandler=org.bedrock.teateach.typehandler.LongListTypeHandler.class),
            @Result(column="created_at", property="createdAt"),
            @Result(column="updated_at", property="updatedAt")
    })
    List<KnowledgePoint> findByCourseId(@Param("courseId") Long courseId);

    @Select("SELECT kp.id, kp.name, kp.brief_description, kp.detailed_content, kp.course_id, kp.difficulty_level, " +
            "kp.prerequisite_knowledge_point_ids, kp.related_knowledge_point_ids, kp.created_at, kp.updated_at, " +
            "c.course_name " +
            "FROM knowledge_points kp LEFT JOIN courses c ON kp.course_id = c.id")
    @Results({
            @Result(column="id", property="id"),
            @Result(column="name", property="name"),
            @Result(column="brief_description", property="briefDescription"),
            @Result(column="detailed_content", property="detailedContent"),
            @Result(column="course_id", property="courseId"),
            @Result(column="course_name", property="courseName"),
            @Result(column="difficulty_level", property="difficultyLevel"),
            @Result(column="prerequisite_knowledge_point_ids", property="prerequisiteKnowledgePointIds", javaType=List.class, typeHandler=org.bedrock.teateach.typehandler.LongListTypeHandler.class),
            @Result(column="related_knowledge_point_ids", property="relatedKnowledgePointIds", javaType=List.class, typeHandler=org.bedrock.teateach.typehandler.LongListTypeHandler.class),
            @Result(column="created_at", property="createdAt"),
            @Result(column="updated_at", property="updatedAt")
    })
    List<KnowledgePoint> findAll();

    /**
     * Remove a knowledge point ID from prerequisite_knowledge_point_ids field of all knowledge points
     * Uses JSON_REMOVE to remove the ID from the JSON array
     */
    @Update("UPDATE knowledge_points SET " +
            "prerequisite_knowledge_point_ids = CASE " +
            "  WHEN prerequisite_knowledge_point_ids = CONCAT('[', #{deletedId}, ']') THEN '[]' " +
            "  WHEN prerequisite_knowledge_point_ids LIKE CONCAT('[', #{deletedId}, ',%') THEN REPLACE(prerequisite_knowledge_point_ids, CONCAT(#{deletedId}, ','), '') " +
            "  WHEN prerequisite_knowledge_point_ids LIKE CONCAT('%,', #{deletedId}, ']') THEN REPLACE(prerequisite_knowledge_point_ids, CONCAT(',', #{deletedId}), '') " +
            "  WHEN prerequisite_knowledge_point_ids LIKE CONCAT('%,', #{deletedId}, ',%') THEN REPLACE(prerequisite_knowledge_point_ids, CONCAT(',', #{deletedId}, ','), ',') " +
            "  ELSE prerequisite_knowledge_point_ids " +
            "END, " +
            "updated_at = NOW() " +
            "WHERE prerequisite_knowledge_point_ids LIKE CONCAT('%', #{deletedId}, '%')")
    void removeFromPrerequisiteFields(@Param("deletedId") Long deletedId);

    /**
     * Remove a knowledge point ID from related_knowledge_point_ids field of all knowledge points
     * Uses JSON_REMOVE to remove the ID from the JSON array
     */
    @Update("UPDATE knowledge_points SET " +
            "related_knowledge_point_ids = CASE " +
            "  WHEN related_knowledge_point_ids = CONCAT('[', #{deletedId}, ']') THEN '[]' " +
            "  WHEN related_knowledge_point_ids LIKE CONCAT('[', #{deletedId}, ',%') THEN REPLACE(related_knowledge_point_ids, CONCAT(#{deletedId}, ','), '') " +
            "  WHEN related_knowledge_point_ids LIKE CONCAT('%,', #{deletedId}, ']') THEN REPLACE(related_knowledge_point_ids, CONCAT(',', #{deletedId}), '') " +
            "  WHEN related_knowledge_point_ids LIKE CONCAT('%,', #{deletedId}, ',%') THEN REPLACE(related_knowledge_point_ids, CONCAT(',', #{deletedId}, ','), ',') " +
            "  ELSE related_knowledge_point_ids " +
            "END, " +
            "updated_at = NOW() " +
            "WHERE related_knowledge_point_ids LIKE CONCAT('%', #{deletedId}, '%')")
    void removeFromRelatedFields(@Param("deletedId") Long deletedId);
}