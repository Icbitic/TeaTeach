package org.bedrock.teateach.mappers;

import org.apache.ibatis.annotations.*;
import org.bedrock.teateach.beans.CourseEnrollment;
import org.bedrock.teateach.beans.Student;
import org.bedrock.teateach.beans.Course;

import java.util.List;

@Mapper
public interface CourseEnrollmentMapper {
    
    @Insert("INSERT INTO course_enrollments(course_id, student_id, enrollment_date, status) " +
            "VALUES(#{courseId}, #{studentId}, #{enrollmentDate}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(CourseEnrollment enrollment);
    
    @Update("UPDATE course_enrollments SET status=#{status}, updated_at=CURRENT_TIMESTAMP WHERE id=#{id}")
    void update(CourseEnrollment enrollment);
    
    @Delete("DELETE FROM course_enrollments WHERE id=#{id}")
    void delete(@Param("id") Long id);
    
    @Delete("DELETE FROM course_enrollments WHERE course_id=#{courseId} AND student_id=#{studentId}")
    void deleteByStudentAndCourse(@Param("courseId") Long courseId, @Param("studentId") Long studentId);
    
    @Select("SELECT * FROM course_enrollments WHERE id=#{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "enrollmentDate", column = "enrollment_date"),
            @Result(property = "status", column = "status"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    CourseEnrollment findById(@Param("id") Long id);
    
    @Select("SELECT * FROM course_enrollments WHERE course_id=#{courseId} AND student_id=#{studentId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "enrollmentDate", column = "enrollment_date"),
            @Result(property = "status", column = "status"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    CourseEnrollment findByStudentAndCourse(@Param("courseId") Long courseId, @Param("studentId") Long studentId);
    
    @Select("SELECT s.* FROM students s " +
            "INNER JOIN course_enrollments ce ON s.id = ce.student_id " +
            "WHERE ce.course_id = #{courseId} AND ce.status = 'ACTIVE'")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "name", column = "name"),
            @Result(property = "email", column = "email"),
            @Result(property = "major", column = "major"),
            @Result(property = "dateOfBirth", column = "date_of_birth")
    })
    List<Student> findStudentsByCourseId(@Param("courseId") Long courseId);
    
    @Select("SELECT c.* FROM courses c " +
            "INNER JOIN course_enrollments ce ON c.id = ce.course_id " +
            "WHERE ce.student_id = #{studentId} AND ce.status = 'ACTIVE'")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "courseCode", column = "course_code"),
            @Result(property = "courseName", column = "course_name"),
            @Result(property = "instructor", column = "instructor"),
            @Result(property = "credits", column = "credits"),
            @Result(property = "hours", column = "hours"),
            @Result(property = "description", column = "description")
    })
    List<Course> findCoursesByStudentId(@Param("studentId") Long studentId);
    
    @Select("SELECT * FROM course_enrollments WHERE course_id=#{courseId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "enrollmentDate", column = "enrollment_date"),
            @Result(property = "status", column = "status"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    List<CourseEnrollment> findByCourseId(@Param("courseId") Long courseId);
    
    @Select("SELECT COUNT(*) FROM course_enrollments WHERE course_id=#{courseId} AND status='ACTIVE'")
    int countActiveEnrollmentsByCourseId(@Param("courseId") Long courseId);
    
    @Select("SELECT s.* FROM students s " +
            "WHERE s.id NOT IN (" +
            "SELECT ce.student_id FROM course_enrollments ce " +
            "WHERE ce.course_id = #{courseId} AND ce.status = 'ACTIVE')")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "studentId", column = "student_id"),
            @Result(property = "name", column = "name"),
            @Result(property = "email", column = "email"),
            @Result(property = "major", column = "major"),
            @Result(property = "dateOfBirth", column = "date_of_birth")
    })
    List<Student> findAvailableStudentsForCourse(@Param("courseId") Long courseId);
}