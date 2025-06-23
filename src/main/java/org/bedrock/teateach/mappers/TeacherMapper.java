package org.bedrock.teateach.mappers;

import org.apache.ibatis.annotations.*;
import org.bedrock.teateach.beans.Teacher;

import java.util.Optional;

@Mapper
public interface TeacherMapper {

    @Insert("INSERT INTO teachers (teacher_id, name, email, department, date_of_birth) " +
            "VALUES (#{teacherId}, #{name}, #{email}, #{department}, #{dateOfBirth})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Teacher teacher);

    @Select("SELECT * FROM teachers WHERE email = #{email}")
    Optional<Teacher> findByEmail(String email);

    @Select("SELECT * FROM teachers WHERE teacher_id = #{teacherId}")
    Optional<Teacher> findByTeacherId(String teacherId);
}
