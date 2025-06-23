package org.bedrock.teateach.mappers;

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

    @Select("SELECT * FROM students WHERE id=#{id}")
    Student findById(@Param("id") Long id);

    @Select("SELECT * FROM students WHERE student_id=#{studentId}")
    Student findByStudentId(@Param("studentId") String studentId);

    @Select("SELECT * FROM students")
    List<Student> findAll();

    @Select("SELECT * FROM students WHERE email = #{email}")
    Optional<Student> findByEmail(String email);
    // You'd add more complex queries here, e.g., for search
}
