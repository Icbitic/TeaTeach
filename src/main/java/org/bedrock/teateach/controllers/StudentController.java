package org.bedrock.teateach.controllers;

import org.bedrock.teateach.beans.Student;
import org.bedrock.teateach.services.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        Student createdStudent = studentService.createStudent(student);
        return new ResponseEntity<>(createdStudent, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @RequestBody Student student) {
        student.setId(id); // Ensure the ID from path is used
        Student updatedStudent = studentService.updateStudent(student);
        if (updatedStudent != null) {
            return ResponseEntity.ok(updatedStudent);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {
        Student student = studentService.getStudentById(id);
        if (student != null) {
            return ResponseEntity.ok(student);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/by-student-id/{studentId}")
    public ResponseEntity<Student> getStudentByStudentId(@PathVariable String studentId) {
        Student student = studentService.getStudentByStudentId(studentId);
        if (student != null) {
            return ResponseEntity.ok(student);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/convert/student-id-to-id/{studentId}")
    public ResponseEntity<Long> convertStudentIdToId(@PathVariable String studentId) {
        Long id = studentService.convertStudentIdToId(studentId);
        if (id != null) {
            return ResponseEntity.ok(id);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/convert/id-to-student-id/{id}")
    public ResponseEntity<String> convertIdToStudentId(@PathVariable Long id) {
        String studentId = studentService.convertIdToStudentId(id);
        if (studentId != null) {
            return ResponseEntity.ok(studentId);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/exists/by-student-id/{studentId}")
    public ResponseEntity<Boolean> existsByStudentId(@PathVariable String studentId) {
        boolean exists = studentService.existsByStudentId(studentId);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/exists/by-id/{id}")
    public ResponseEntity<Boolean> existsById(@PathVariable Long id) {
        boolean exists = studentService.existsById(id);
        return ResponseEntity.ok(exists);
    }
    
    /**
     * Export all students to Excel file
     * GET /api/students/export
     * @return Excel file as byte array
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportStudents() {
        try {
            byte[] excelBytes = studentService.exportStudentsToExcel();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "students_" + LocalDate.now() + ".xlsx");
            headers.setContentLength(excelBytes.length);
            
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Import students from Excel file
     * POST /api/students/import
     * @param file Excel file containing student data
     * @return List of imported students
     */
    @PostMapping("/import")
    public ResponseEntity<?> importStudents(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ImportResponse(false, "File is empty", 0, null));
            }
            
            List<Student> importedStudents = studentService.importStudentsFromExcel(file);
            
            String message = String.format("Successfully imported %d students", importedStudents.size());
            return ResponseEntity.ok(
                new ImportResponse(true, message, importedStudents.size(), importedStudents)
            );
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ImportResponse(false, e.getMessage(), 0, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ImportResponse(false, "Failed to import students: " + e.getMessage(), 0, null));
        }
    }
    
    /**
     * Response class for import operations
     */
    public static class ImportResponse {
        private boolean success;
        private String message;
        private int importedCount;
        private List<Student> students;
        
        public ImportResponse(boolean success, String message, int importedCount, List<Student> students) {
            this.success = success;
            this.message = message;
            this.importedCount = importedCount;
            this.students = students;
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public int getImportedCount() { return importedCount; }
        public List<Student> getStudents() { return students; }
        
        // Setters
        public void setSuccess(boolean success) { this.success = success; }
        public void setMessage(String message) { this.message = message; }
        public void setImportedCount(int importedCount) { this.importedCount = importedCount; }
        public void setStudents(List<Student> students) { this.students = students; }
    }
}
