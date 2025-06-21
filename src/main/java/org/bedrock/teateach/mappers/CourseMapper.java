package org.bedrock.teateach.mappers;

import org.apache.ibatis.annotations.*;
import org.bedrock.teateach.beans.Course;

import java.util.List;

@Mapper
public interface CourseMapper {
    @Insert("INSERT INTO courses(course_code, course_name, instructor, credits, hours, description) " +
            "VALUES(#{courseCode}, #{courseName}, #{instructor}, #{credits}, #{hours}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Course course);

    @Update("UPDATE courses SET course_code=#{courseCode}, course_name=#{courseName}, instructor=#{instructor}, " +
            "credits=#{credits}, hours=#{hours}, description=#{description} WHERE id=#{id}")
    void update(Course course);

    @Delete("DELETE FROM courses WHERE id=#{id}")
    void delete(@Param("id") Long id);

    @Select("SELECT * FROM courses WHERE id=#{id}")
    Course findById(@Param("id") Long id);

    @Select("SELECT * FROM courses")
    List<Course> findAll();
}
