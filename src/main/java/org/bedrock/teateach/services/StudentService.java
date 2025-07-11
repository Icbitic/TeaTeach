package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.Student;
import org.bedrock.teateach.beans.StudentLearningData;
import org.bedrock.teateach.mappers.StudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
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

    /**
     * Get student by student ID (string identifier)
     * @param studentId the student ID string
     * @return Student entity or null if not found
     */
    @Cacheable(value = "students", key = "'studentId:' + #studentId")
    public Student getStudentByStudentId(String studentId) {
        return studentMapper.findByStudentId(studentId);
    }

    /**
     * Convert student ID (string) to database ID (Long)
     * @param studentId the student ID string
     * @return database ID (Long) or null if student not found
     */
    public Long convertStudentIdToId(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            return null;
        }
        Student student = getStudentByStudentId(studentId);
        return student != null ? student.getId() : null;
    }

    /**
     * Convert database ID (Long) to student ID (string)
     * @param id the database ID
     * @return student ID string or null if student not found
     */
    public String convertIdToStudentId(Long id) {
        if (id == null) {
            return null;
        }
        Student student = getStudentById(id);
        return student != null ? student.getStudentId() : null;
    }

    /**
     * Check if a student exists by student ID
     * @param studentId the student ID string
     * @return true if student exists, false otherwise
     */
    public boolean existsByStudentId(String studentId) {
        return getStudentByStudentId(studentId) != null;
    }

    /**
     * Check if a student exists by database ID
     * @param id the database ID
     * @return true if student exists, false otherwise
     */
    public boolean existsById(Long id) {
        return getStudentById(id) != null;
    }

    // Add methods for bulk import/export logic here, interacting with file I/O
    // and potentially a temporary table or batch inserts.
    
    /**
     * Export all students to Excel format
     * @return byte array containing Excel file data
     */
    public byte[] exportStudentsToExcel() {
        List<Student> students = getAllStudents();
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Students");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Student ID", "Name", "Email", "Major", "Date of Birth"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                
                // Style header cells
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);
                cell.setCellStyle(headerStyle);
            }
            
            // Create data rows
            int rowNum = 1;
            for (Student student : students) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(student.getId() != null ? student.getId() : 0);
                row.createCell(1).setCellValue(student.getStudentId() != null ? student.getStudentId() : "");
                row.createCell(2).setCellValue(student.getName() != null ? student.getName() : "");
                row.createCell(3).setCellValue(student.getEmail() != null ? student.getEmail() : "");
                row.createCell(4).setCellValue(student.getMajor() != null ? student.getMajor() : "");
                row.createCell(5).setCellValue(student.getDateOfBirth() != null ? student.getDateOfBirth().toString() : "");
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write workbook to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to export students to Excel", e);
        }
    }
    
    /**
     * Import students from Excel file
     * @param file MultipartFile containing Excel data
     * @return List of imported students
     * @throws IOException if file processing fails
     */
    @Transactional
    @CacheEvict(value = "allStudents", allEntries = true)
    public List<Student> importStudentsFromExcel(MultipartFile file) throws IOException {
        List<Student> importedStudents = new ArrayList<>();
        
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        // Validate file type
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls") && !fileName.endsWith(".csv"))) {
            throw new IllegalArgumentException("Unsupported file format. Please use .xlsx, .xls, or .csv files");
        }
        
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            // Skip header row (row 0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    Student student = parseStudentFromRow(row);
                    if (student != null && isValidStudent(student)) {
                        // Check if student with same studentId already exists
                        if (studentMapper.findByStudentId(student.getStudentId()) == null) {
                            studentMapper.insert(student);
                            importedStudents.add(student);
                        }
                    }
                } catch (Exception e) {
                    // Log the error but continue processing other rows
                    System.err.println("Error processing row " + (i + 1) + ": " + e.getMessage());
                }
            }
            
        } catch (IOException e) {
            throw new IOException("Failed to process Excel file: " + e.getMessage(), e);
        }
        
        return importedStudents;
    }
    
    /**
     * Parse a Student object from an Excel row
     * Expected columns: Student ID, Name, Email, Major, Date of Birth
     */
    private Student parseStudentFromRow(Row row) {
        Student student = new Student();
        
        // Student ID (column 1, skip ID column 0)
        Cell studentIdCell = row.getCell(1);
        if (studentIdCell != null) {
            student.setStudentId(getCellValueAsString(studentIdCell));
        }
        
        // Name (column 2)
        Cell nameCell = row.getCell(2);
        if (nameCell != null) {
            student.setName(getCellValueAsString(nameCell));
        }
        
        // Email (column 3)
        Cell emailCell = row.getCell(3);
        if (emailCell != null) {
            student.setEmail(getCellValueAsString(emailCell));
        }
        
        // Major (column 4)
        Cell majorCell = row.getCell(4);
        if (majorCell != null) {
            student.setMajor(getCellValueAsString(majorCell));
        }
        
        // Date of Birth (column 5)
        Cell dobCell = row.getCell(5);
        if (dobCell != null) {
            student.setDateOfBirth(parseDateFromCell(dobCell));
        }
        
        return student;
    }
    
    /**
     * Get cell value as string regardless of cell type
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
    
    /**
     * Parse date from cell, supporting multiple formats
     */
    private LocalDate parseDateFromCell(Cell cell) {
        if (cell == null) return null;
        
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            } else {
                String dateStr = getCellValueAsString(cell);
                if (dateStr.isEmpty()) return null;
                
                // Try different date formats
                DateTimeFormatter[] formatters = {
                    DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                    DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                    DateTimeFormatter.ofPattern("yyyy/MM/dd")
                };
                
                for (DateTimeFormatter formatter : formatters) {
                    try {
                        return LocalDate.parse(dateStr, formatter);
                    } catch (DateTimeParseException ignored) {
                        // Try next format
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing date: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get student learning data for recommendations
     * TODO: Implement actual database queries when StudentLearningData mapper is available
     * @param studentId The student's ID
     * @param courseId Optional course ID to filter data
     * @return List of student learning data
     */
    public List<StudentLearningData> getStudentLearningData(String studentId, Long courseId) {
        // TODO: Replace with actual database query
        // For now, return mock data to demonstrate the functionality
        List<StudentLearningData> mockData = new ArrayList<>();
        
        // Create sample learning data
        StudentLearningData data1 = new StudentLearningData();
        data1.setId(1L);
        data1.setStudentId(Long.parseLong(studentId));
        data1.setCourseId(courseId != null ? courseId : 1L);
        data1.setTaskId(1L);
        data1.setQuizScore(75.0);
        data1.setCompletionRate(0.8);
        data1.setTimeSpentSeconds(3600L);
        data1.setAdditionalData("{\"topic\": \"Mathematics\", \"difficulty\": \"medium\"}");
        
        StudentLearningData data2 = new StudentLearningData();
        data2.setId(2L);
        data2.setStudentId(Long.parseLong(studentId));
        data2.setCourseId(courseId != null ? courseId : 1L);
        data2.setTaskId(2L);
        data2.setQuizScore(60.0);
        data2.setCompletionRate(0.6);
        data2.setTimeSpentSeconds(2400L);
        data2.setAdditionalData("{\"topic\": \"Physics\", \"difficulty\": \"hard\"}");
        
        mockData.add(data1);
        mockData.add(data2);
        
        return mockData;
    }
    
    /**
     * Validate student data
     */
    private boolean isValidStudent(Student student) {
        return student.getStudentId() != null && !student.getStudentId().trim().isEmpty() &&
               student.getName() != null && !student.getName().trim().isEmpty() &&
               student.getEmail() != null && !student.getEmail().trim().isEmpty() &&
               student.getEmail().contains("@");
    }
    
    // 在StudentService中添加批量插入方法
    @Transactional
    @CacheEvict(value = "allStudents", allEntries = true)
    public List<Student> batchCreateStudents(List<Student> students) {
    // 使用批量插入而不是逐个插入
    studentMapper.batchInsert(students);
    return students;
    }
}
