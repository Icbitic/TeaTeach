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
            "related_knowledge_point_ids=#{relatedKnowledgePointIds,typeHandler=org.bedrock.teateach.typehandler.LongListTypeHandler} " +
            "WHERE id=#{id}")
    void update(KnowledgePoint knowledgePoint);

    @Delete("DELETE FROM knowledge_points WHERE id=#{id}")
    void delete(@Param("id") Long id);

    @Select("SELECT id, name, brief_description, detailed_content, course_id, difficulty_level, " +
            "prerequisite_knowledge_point_ids, related_knowledge_point_ids " +
            "FROM knowledge_points WHERE id=#{id}")
    @Results({
            @Result(column="prerequisite_knowledge_point_ids", property="prerequisiteKnowledgePointIds", javaType=List.class, typeHandler=org.bedrock.teateach.typehandler.LongListTypeHandler.class),
            @Result(column="related_knowledge_point_ids", property="relatedKnowledgePointIds", javaType=List.class, typeHandler=org.bedrock.teateach.typehandler.LongListTypeHandler.class)
    })
    KnowledgePoint findById(@Param("id") Long id);

    @Select("SELECT id, name, brief_description, detailed_content, course_id, difficulty_level, " +
            "prerequisite_knowledge_point_ids, related_knowledge_point_ids " +
            "FROM knowledge_points WHERE course_id=#{courseId}")
    @Results({
            @Result(column="prerequisite_knowledge_point_ids", property="prerequisiteKnowledgePointIds", javaType=List.class, typeHandler=org.bedrock.teateach.typehandler.LongListTypeHandler.class),
            @Result(column="related_knowledge_point_ids", property="relatedKnowledgePointIds", javaType=List.class, typeHandler=org.bedrock.teateach.typehandler.LongListTypeHandler.class)
    })
    List<KnowledgePoint> findByCourseId(@Param("courseId") Long courseId);

    @Select("SELECT id, name, brief_description, detailed_content, course_id, difficulty_level, " +
            "prerequisite_knowledge_point_ids, related_knowledge_point_ids " +
            "FROM knowledge_points")
    @Results({
            @Result(column="prerequisite_knowledge_point_ids", property="prerequisiteKnowledgePointIds", javaType=List.class, typeHandler=org.bedrock.teateach.typehandler.LongListTypeHandler.class),
            @Result(column="related_knowledge_point_ids", property="relatedKnowledgePointIds", javaType=List.class, typeHandler=org.bedrock.teateach.typehandler.LongListTypeHandler.class)
    })
    List<KnowledgePoint> findAll();
}