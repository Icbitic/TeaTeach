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
import org.bedrock.teateach.dto.CommonResponse;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping
    public ResponseEntity<CommonResponse<Student>> createStudent(@RequestBody Student student) {
        Student createdStudent = studentService.createStudent(student);
        return ResponseEntity.ok(CommonResponse.success(createdStudent));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<Student>> updateStudent(@PathVariable Long id, @RequestBody Student student) {
        student.setId(id); // Ensure the ID from path is used
        Student updatedStudent = studentService.updateStudent(student);
        if (updatedStudent != null) {
            return ResponseEntity.ok(CommonResponse.success(updatedStudent));
        }
        return ResponseEntity.ok(CommonResponse.error(404, "Student not found"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok(CommonResponse.success(null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<Student>> getStudentById(@PathVariable Long id) {
        Student student = studentService.getStudentById(id);
        if (student != null) {
            return ResponseEntity.ok(CommonResponse.success(student));
        }
        return ResponseEntity.ok(CommonResponse.error(404, "Student not found"));
    }

    @GetMapping
    public ResponseEntity<CommonResponse<List<Student>>> getAllStudents() {
        List<Student> students = studentService.getAllStudents();
        return ResponseEntity.ok(CommonResponse.success(students));
    }

    @GetMapping("/by-student-id/{studentId}")
    public ResponseEntity<CommonResponse<Student>> getStudentByStudentId(@PathVariable String studentId) {
        Student student = studentService.getStudentByStudentId(studentId);
        if (student != null) {
            return ResponseEntity.ok(CommonResponse.success(student));
        }
        return ResponseEntity.ok(CommonResponse.error(404, "Student not found"));
    }

    @GetMapping("/convert/student-id-to-id/{studentId}")
    public ResponseEntity<CommonResponse<Long>> convertStudentIdToId(@PathVariable String studentId) {
        Long id = studentService.convertStudentIdToId(studentId);
        if (id != null) {
            return ResponseEntity.ok(CommonResponse.success(id));
        }
        return ResponseEntity.ok(CommonResponse.error(404, "Student not found"));
    }

    @GetMapping("/convert/id-to-student-id/{id}")
    public ResponseEntity<CommonResponse<String>> convertIdToStudentId(@PathVariable Long id) {
        String studentId = studentService.convertIdToStudentId(id);
        if (studentId != null) {
            return ResponseEntity.ok(CommonResponse.success(studentId));
        }
        return ResponseEntity.ok(CommonResponse.error(404, "Student not found"));
    }

    @GetMapping("/exists/by-student-id/{studentId}")
    public ResponseEntity<CommonResponse<Boolean>> existsByStudentId(@PathVariable String studentId) {
        boolean exists = studentService.existsByStudentId(studentId);
        return ResponseEntity.ok(CommonResponse.success(exists));
    }

    @GetMapping("/exists/by-id/{id}")
    public ResponseEntity<CommonResponse<Boolean>> existsById(@PathVariable Long id) {
        boolean exists = studentService.existsById(id);
        return ResponseEntity.ok(CommonResponse.success(exists));
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
    public ResponseEntity<CommonResponse<ImportResponse>> importStudents(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.ok(CommonResponse.error(400, "File is empty"));
            }
            
            List<Student> importedStudents = studentService.importStudentsFromExcel(file);
            
            String message = String.format("Successfully imported %d students", importedStudents.size());
            ImportResponse resp = new ImportResponse(true, message, importedStudents.size(), importedStudents);
            return ResponseEntity.ok(CommonResponse.success(resp));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(CommonResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(CommonResponse.error(500, "Failed to import students: " + e.getMessage()));
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
