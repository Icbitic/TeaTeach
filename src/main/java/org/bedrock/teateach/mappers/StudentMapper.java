package org.bedrock.teateach.mappers;

import org.apache.ibatis.type.DateTypeHandler;
import org.bedrock.teateach.beans.Student;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface StudentMapper {

    @Insert("INSERT INTO students(student_id, name, email, major, date_of_birth) " +
            "VALUES(#{studentId}, #{name}, #{email}, #{major}, #{dateOfBirth})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Student student);

    @Update("UPDATE students SET name=#{name}, email=#{email}, major=#{major}, date_of_birth=#{dateOfBirth} WHERE id=#{id}")
    void update(Student student);

    @Delete("DELETE FROM students WHERE id=#{id}")
    void delete(@Param("id") Long id);

    // guess which one is right?
    // 修正后的查询
    // 问题查询 - 混合了ID类型
    @Select("SELECT * FROM students WHERE id=#{id} OR student_id=#{id}")
    Student findById(@Param("id") Long id);

    @Select("SELECT * FROM students WHERE student_id=#{studentId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "name", column = "name"),
            @Result(property = "email", column = "email"),
            @Result(property = "major", column = "major"),
            @Result(property = "dateOfBirth", column = "date_of_birth")
    })
    Student findByStudentId(@Param("studentId") String studentId);

    @Select("SELECT * FROM students")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "name", column = "name"),
            @Result(property = "email", column = "email"),
            @Result(property = "major", column = "major"),
            @Result(property = "dateOfBirth", column = "date_of_birth")
    })
    List<Student> findAll();

    @Select("SELECT * FROM students WHERE email = #{email}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "name", column = "name"),
            @Result(property = "email", column = "email"),
            @Result(property = "major", column = "major"),
            @Result(property = "dateOfBirth", column = "date_of_birth", typeHandler = DateTypeHandler.class)
    })
    Optional<Student> findByEmail(String email);
    // You'd add more complex queries here, e.g., for search
}


@Insert({"<script>",
    "INSERT INTO students(student_id, name, email, major, date_of_birth) VALUES",
    "<foreach collection='students' item='student' separator=','>",
    "(#{student.studentId}, #{student.name}, #{student.email}, #{student.major}, #{student.dateOfBirth})",
    "</foreach>",
    "</script>"})
void batchInsert(@Param("students") List<Student> students);
