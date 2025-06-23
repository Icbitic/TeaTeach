package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.Student;
import org.bedrock.teateach.mappers.StudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentService {

    private final StudentMapper studentMapper;

    @Autowired
    public StudentService(StudentMapper studentMapper) {
        this.studentMapper = studentMapper;
    }

    @Transactional
    @CacheEvict(value = "allStudents", allEntries = true)
    public Student createStudent(Student student) {
        studentMapper.insert(student);
        return student;
    }

    @Transactional
    @CachePut(value = "students", key = "#student.id")
    @CacheEvict(value = "allStudents", allEntries = true)
    public Student updateStudent(Student student) {
        studentMapper.update(student);
        return student;
    }

    @Transactional
    @CacheEvict(value = {"students", "allStudents"}, allEntries = true)
    public void deleteStudent(Long id) {
        studentMapper.delete(id);
    }

    @Cacheable(value = "students", key = "#id")
    public Student getStudentById(Long id) {
        return studentMapper.findById(id);
    }

    @Cacheable(value = "allStudents")
    public List<Student> getAllStudents() {
        return studentMapper.findAll();
    }

    // Add methods for bulk import/export logic here, interacting with file I/O
    // and potentially a temporary table or batch inserts.
}
