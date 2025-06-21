package org.bedrock.teateach.mappers;

import org.apache.ibatis.annotations.*;
import org.bedrock.teateach.beans.GradeAnalysis;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface GradeAnalysisMapper {

    /**
     * Inserts a new GradeAnalysis record into the database.
     * The generated primary key (id) will be set back into the GradeAnalysis object.
     *
     * @param gradeAnalysis The GradeAnalysis object to insert.
     */
    @Insert("INSERT INTO grade_analysis(" +
            "student_id, course_id, overall_grade, task_grades, grade_trend) " +
            "VALUES(#{studentId}, #{courseId}, #{overallGrade}, " +
            "#{taskGrades,typeHandler=org.bedrock.teateach.typehandler.MapStringTypeHandler}, " + // Custom TypeHandler
            "#{gradeTrend,typeHandler=org.bedrock.teateach.typehandler.LocalDateDoubleMapTypeHandler})") // Custom TypeHandler
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(GradeAnalysis gradeAnalysis);

    /**
     * Updates an existing GradeAnalysis record in the database.
     *
     * @param gradeAnalysis The GradeAnalysis object with updated information.
     */
    @Update("UPDATE grade_analysis SET " +
            "overall_grade = #{overallGrade}, " +
            "task_grades = #{taskGrades,typeHandler=org.bedrock.teateach.typehandler.MapStringTypeHandler}, " +
            "grade_trend = #{gradeTrend,typeHandler=org.bedrock.teateach.typehandler.LocalDateDoubleMapTypeHandler} " +
            "WHERE id = #{id}")
    void update(GradeAnalysis gradeAnalysis);

    /**
     * Deletes a GradeAnalysis record from the database by its ID.
     *
     * @param id The ID of the grade analysis record to delete.
     */
    @Delete("DELETE FROM grade_analysis WHERE id = #{id}")
    void delete(@Param("id") Long id);

    /**
     * Finds a GradeAnalysis record by its ID.
     *
     * @param id The ID of the grade analysis record to find.
     * @return The GradeAnalysis object if found, otherwise null.
     */
    @Select("SELECT id, student_id, course_id, overall_grade, task_grades, grade_trend " +
            "FROM grade_analysis WHERE id = #{id}")
    @Results({
            @Result(column="task_grades", property="taskGrades", javaType=Map.class, typeHandler=org.bedrock.teateach.typehandler.MapStringTypeHandler.class),
            @Result(column="grade_trend", property="gradeTrend", javaType=Map.class, typeHandler=org.bedrock.teateach.typehandler.LocalDateDoubleMapTypeHandler.class)
    })
    GradeAnalysis findById(@Param("id") Long id);

    /**
     * Finds a GradeAnalysis record for a specific student within a specific course.
     *
     * @param studentId The ID of the student.
     * @param courseId The ID of the course.
     * @return The GradeAnalysis object if found, otherwise null.
     */
    @Select("SELECT id, student_id, course_id, overall_grade, task_grades, grade_trend " +
            "FROM grade_analysis WHERE student_id = #{studentId} AND course_id = #{courseId} LIMIT 1")
    @Results({
            @Result(column="task_grades", property="taskGrades", javaType=Map.class, typeHandler=org.bedrock.teateach.typehandler.MapStringTypeHandler.class),
            @Result(column="grade_trend", property="gradeTrend", javaType=Map.class, typeHandler=org.bedrock.teateach.typehandler.LocalDateDoubleMapTypeHandler.class)
    })
    GradeAnalysis findByStudentAndCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    /**
     * Finds all GradeAnalysis records for a specific course.
     *
     * @param courseId The ID of the course.
     * @return A list of GradeAnalysis objects for the given course.
     */
    @Select("SELECT id, student_id, course_id, overall_grade, task_grades, grade_trend " +
            "FROM grade_analysis WHERE course_id = #{courseId}")
    @Results({
            @Result(column="task_grades", property="taskGrades", javaType=Map.class, typeHandler=org.bedrock.teateach.typehandler.MapStringTypeHandler.class),
            @Result(column="grade_trend", property="gradeTrend", javaType=Map.class, typeHandler=org.bedrock.teateach.typehandler.LocalDateDoubleMapTypeHandler.class)
    })
    List<GradeAnalysis> findByCourseId(@Param("courseId") Long courseId);

    /**
     * Finds all GradeAnalysis records for a specific student.
     *
     * @param studentId The ID of the student.
     * @return A list of GradeAnalysis objects for the given student across all courses.
     */
    @Select("SELECT id, student_id, course_id, overall_grade, task_grades, grade_trend " +
            "FROM grade_analysis WHERE student_id = #{studentId}")
    @Results({
            @Result(column="task_grades", property="taskGrades", javaType=Map.class, typeHandler=org.bedrock.teateach.typehandler.MapStringTypeHandler.class),
            @Result(column="grade_trend", property="gradeTrend", javaType=Map.class, typeHandler=org.bedrock.teateach.typehandler.LocalDateDoubleMapTypeHandler.class)
    })
    List<GradeAnalysis> findByStudentId(@Param("studentId") Long studentId);


    /**
     * Finds all GradeAnalysis records in the database.
     *
     * @return A list of all GradeAnalysis objects.
     */
    @Select("SELECT id, student_id, course_id, overall_grade, task_grades, grade_trend " +
            "FROM grade_analysis")
    @Results({
            @Result(column="task_grades", property="taskGrades", javaType=Map.class, typeHandler=org.bedrock.teateach.typehandler.MapStringTypeHandler.class),
            @Result(column="grade_trend", property="gradeTrend", javaType=Map.class, typeHandler=org.bedrock.teateach.typehandler.LocalDateDoubleMapTypeHandler.class)
    })
    List<GradeAnalysis> findAll();
}