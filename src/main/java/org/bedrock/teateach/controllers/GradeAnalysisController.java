package org.bedrock.teateach.controllers;

import org.bedrock.teateach.beans.GradeAnalysis;
import org.bedrock.teateach.services.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
public class GradeAnalysisController {

    private final GradeService gradeService;

    @Autowired
    public GradeAnalysisController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    /**
     * Retrieves the overall grade analysis for a specific student in a specific course.
     * GET /api/grades/student/{studentId}/course/{courseId}
     * @param studentId The ID of the student.
     * @param courseId The ID of the course.
     * @return The GradeAnalysis object if found, or 404 Not Found.
     */
    @GetMapping("/student/{studentId}/course/{courseId}")
    public ResponseEntity<GradeAnalysis> getStudentCourseGrade(@PathVariable Long studentId,
                                                               @PathVariable Long courseId) {
        GradeAnalysis gradeAnalysis = gradeService.getStudentCourseGrade(studentId, courseId);
        return gradeAnalysis != null ? ResponseEntity.ok(gradeAnalysis) : ResponseEntity.notFound().build();
    }

    /**
     * Retrieves all grade analyses for a specific course.
     * GET /api/grades/course/{courseId}
     * @param courseId The ID of the course.
     * @return A list of GradeAnalysis objects for the specified course.
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<GradeAnalysis>> getCourseGrades(@PathVariable Long courseId) {
        List<GradeAnalysis> grades = gradeService.getCourseGrades(courseId);
        return ResponseEntity.ok(grades);
    }

    /**
     * Exports grade data for a specific course to an Excel file.
     * GET /api/grades/course/{courseId}/export-excel
     * @param courseId The ID of the course.
     * @return An Excel file as a byte array.
     */
    @GetMapping("/course/{courseId}/export-excel")
    public ResponseEntity<byte[]> exportGradesToExcel(@PathVariable Long courseId) {
        byte[] excelBytes = gradeService.exportGradesToExcel(courseId);

        // Set headers for file download
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        String filename = "course_grades_" + courseId + ".xlsx";
        headers.setContentDispositionFormData("attachment", filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }
}