package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.SubmissionFile;
import org.bedrock.teateach.mappers.SubmissionFileMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionFileServiceTest {

    @Mock
    private SubmissionFileMapper submissionFileMapper;

    @Mock
    private MultipartFile mockFile;

    private SubmissionFileService submissionFileService;

    @TempDir
    Path tempDir;

    private SubmissionFile testSubmissionFile;
    private Long testSubmissionId = 1L;
    private Long testFileId = 1L;

    @BeforeEach
    void setUp() {
        // Create service instance with temp directory
        submissionFileService = new SubmissionFileService(submissionFileMapper, tempDir.toString());
        
        // Create test SubmissionFile
        testSubmissionFile = new SubmissionFile();
        testSubmissionFile.setId(testFileId);
        testSubmissionFile.setSubmissionId(testSubmissionId);
        testSubmissionFile.setFileName("test.txt");
        testSubmissionFile.setStoredFileName("uuid-test.txt");
        testSubmissionFile.setFilePath(tempDir.resolve("uuid-test.txt").toString());
        testSubmissionFile.setFileType("txt");
        testSubmissionFile.setFileSize(100L);
        testSubmissionFile.setMimeType("text/plain");
        testSubmissionFile.setCreatedAt(LocalDateTime.now());
        testSubmissionFile.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void uploadFile_shouldUploadFileSuccessfully() throws IOException {
        // Given
        String originalFileName = "test.txt";
        String fileContent = "Test file content";
        byte[] fileBytes = fileContent.getBytes();
        
        when(mockFile.getOriginalFilename()).thenReturn(originalFileName);
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(fileBytes));
        when(mockFile.getSize()).thenReturn((long) fileBytes.length);
        when(mockFile.getContentType()).thenReturn("text/plain");
        doNothing().when(submissionFileMapper).insert(any(SubmissionFile.class));
        
        // When
        SubmissionFile result = submissionFileService.uploadFile(mockFile, testSubmissionId);
        
        // Then
        assertNotNull(result);
        assertEquals(testSubmissionId, result.getSubmissionId());
        assertEquals(originalFileName, result.getFileName());
        assertEquals("txt", result.getFileType());
        assertEquals((long) fileBytes.length, result.getFileSize());
        assertEquals("text/plain", result.getMimeType());
        assertNotNull(result.getStoredFileName());
        assertNotNull(result.getFilePath());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        
        verify(submissionFileMapper, times(1)).insert(any(SubmissionFile.class));
        
        // Verify file was actually created
        Path uploadedFile = Paths.get(result.getFilePath());
        assertTrue(Files.exists(uploadedFile));
        assertEquals(fileContent, Files.readString(uploadedFile));
    }

    @Test
    void uploadFile_shouldHandleFileWithoutExtension() throws IOException {
        // Given
        String originalFileName = "testfile";
        String fileContent = "Test content";
        byte[] fileBytes = fileContent.getBytes();
        
        when(mockFile.getOriginalFilename()).thenReturn(originalFileName);
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(fileBytes));
        when(mockFile.getSize()).thenReturn((long) fileBytes.length);
        when(mockFile.getContentType()).thenReturn("application/octet-stream");
        doNothing().when(submissionFileMapper).insert(any(SubmissionFile.class));
        
        // When
        SubmissionFile result = submissionFileService.uploadFile(mockFile, testSubmissionId);
        
        // Then
        assertNotNull(result);
        assertEquals("", result.getFileType());
        assertTrue(result.getStoredFileName().endsWith("."));
    }

    @Test
    void uploadFile_shouldHandleNullFileName() throws IOException {
        // Given
        String fileContent = "Test content";
        byte[] fileBytes = fileContent.getBytes();
        
        when(mockFile.getOriginalFilename()).thenReturn(null);
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(fileBytes));
        when(mockFile.getSize()).thenReturn((long) fileBytes.length);
        when(mockFile.getContentType()).thenReturn("application/octet-stream");
        doNothing().when(submissionFileMapper).insert(any(SubmissionFile.class));
        
        // When
        SubmissionFile result = submissionFileService.uploadFile(mockFile, testSubmissionId);
        
        // Then
        assertNotNull(result);
        assertNull(result.getFileName());
        assertEquals("", result.getFileType());
    }

    @Test
    void uploadFile_shouldThrowIOException_whenFileCopyFails() throws IOException {
        // Given
        when(mockFile.getOriginalFilename()).thenReturn("test.txt");
        when(mockFile.getInputStream()).thenThrow(new IOException("Stream error"));
        
        // When & Then
        IOException exception = assertThrows(IOException.class, () -> {
            submissionFileService.uploadFile(mockFile, testSubmissionId);
        });
        
        assertTrue(exception.getMessage().contains("Could not store file"));
        verify(submissionFileMapper, never()).insert(any(SubmissionFile.class));
    }

    @Test
    void getFilesBySubmissionId_shouldReturnFiles() {
        // Given
        List<SubmissionFile> expectedFiles = Arrays.asList(testSubmissionFile);
        when(submissionFileMapper.findBySubmissionId(testSubmissionId)).thenReturn(expectedFiles);
        
        // When
        List<SubmissionFile> result = submissionFileService.getFilesBySubmissionId(testSubmissionId);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSubmissionFile, result.get(0));
        verify(submissionFileMapper, times(1)).findBySubmissionId(testSubmissionId);
    }

    @Test
    void getFilesBySubmissionId_shouldReturnEmptyList_whenNoFiles() {
        // Given
        when(submissionFileMapper.findBySubmissionId(testSubmissionId)).thenReturn(Collections.emptyList());
        
        // When
        List<SubmissionFile> result = submissionFileService.getFilesBySubmissionId(testSubmissionId);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(submissionFileMapper, times(1)).findBySubmissionId(testSubmissionId);
    }

    @Test
    void getFileById_shouldReturnFile() {
        // Given
        when(submissionFileMapper.findById(testFileId)).thenReturn(testSubmissionFile);
        
        // When
        SubmissionFile result = submissionFileService.getFileById(testFileId);
        
        // Then
        assertNotNull(result);
        assertEquals(testSubmissionFile, result);
        verify(submissionFileMapper, times(1)).findById(testFileId);
    }

    @Test
    void getFileById_shouldReturnNull_whenFileNotFound() {
        // Given
        when(submissionFileMapper.findById(testFileId)).thenReturn(null);
        
        // When
        SubmissionFile result = submissionFileService.getFileById(testFileId);
        
        // Then
        assertNull(result);
        verify(submissionFileMapper, times(1)).findById(testFileId);
    }

    @Test
    void deleteFile_shouldDeleteFileSuccessfully() throws IOException {
        // Given
        Path testFile = tempDir.resolve("test-file.txt");
        Files.write(testFile, "test content".getBytes());
        testSubmissionFile.setFilePath(testFile.toString());
        
        when(submissionFileMapper.findById(testFileId)).thenReturn(testSubmissionFile);
        doNothing().when(submissionFileMapper).deleteById(testFileId);
        
        // When
        submissionFileService.deleteFile(testFileId);
        
        // Then
        assertFalse(Files.exists(testFile));
        verify(submissionFileMapper, times(1)).findById(testFileId);
        verify(submissionFileMapper, times(1)).deleteById(testFileId);
    }

    @Test
    void deleteFile_shouldHandleNonExistentFile() throws IOException {
        // Given
        when(submissionFileMapper.findById(testFileId)).thenReturn(null);
        
        // When
        submissionFileService.deleteFile(testFileId);
        
        // Then
        verify(submissionFileMapper, times(1)).findById(testFileId);
        verify(submissionFileMapper, never()).deleteById(any());
    }

    @Test
    void deleteFile_shouldContinueWhenPhysicalFileDeleteFails() throws IOException {
        // Given
        testSubmissionFile.setFilePath("/non/existent/path/file.txt");
        when(submissionFileMapper.findById(testFileId)).thenReturn(testSubmissionFile);
        doNothing().when(submissionFileMapper).deleteById(testFileId);
        
        // When
        submissionFileService.deleteFile(testFileId);
        
        // Then
        verify(submissionFileMapper, times(1)).findById(testFileId);
        verify(submissionFileMapper, times(1)).deleteById(testFileId);
    }

    @Test
    void deleteFilesBySubmissionId_shouldDeleteAllFiles() throws IOException {
        // Given
        Path testFile1 = tempDir.resolve("test-file1.txt");
        Path testFile2 = tempDir.resolve("test-file2.txt");
        Files.write(testFile1, "content1".getBytes());
        Files.write(testFile2, "content2".getBytes());
        
        SubmissionFile file1 = new SubmissionFile();
        file1.setFilePath(testFile1.toString());
        SubmissionFile file2 = new SubmissionFile();
        file2.setFilePath(testFile2.toString());
        
        List<SubmissionFile> files = Arrays.asList(file1, file2);
        when(submissionFileMapper.findBySubmissionId(testSubmissionId)).thenReturn(files);
        doNothing().when(submissionFileMapper).deleteBySubmissionId(testSubmissionId);
        
        // When
        submissionFileService.deleteFilesBySubmissionId(testSubmissionId);
        
        // Then
        assertFalse(Files.exists(testFile1));
        assertFalse(Files.exists(testFile2));
        verify(submissionFileMapper, times(1)).findBySubmissionId(testSubmissionId);
        verify(submissionFileMapper, times(1)).deleteBySubmissionId(testSubmissionId);
    }

    @Test
    void deleteFilesBySubmissionId_shouldHandleEmptyFileList() throws IOException {
        // Given
        when(submissionFileMapper.findBySubmissionId(testSubmissionId)).thenReturn(Collections.emptyList());
        doNothing().when(submissionFileMapper).deleteBySubmissionId(testSubmissionId);
        
        // When
        submissionFileService.deleteFilesBySubmissionId(testSubmissionId);
        
        // Then
        verify(submissionFileMapper, times(1)).findBySubmissionId(testSubmissionId);
        verify(submissionFileMapper, times(1)).deleteBySubmissionId(testSubmissionId);
    }

    @Test
    void getFileContent_shouldReturnFileContent() throws IOException {
        // Given
        Path testFile = tempDir.resolve("content-test.txt");
        String expectedContent = "This is test file content";
        Files.write(testFile, expectedContent.getBytes());
        testSubmissionFile.setFilePath(testFile.toString());
        
        when(submissionFileMapper.findById(testFileId)).thenReturn(testSubmissionFile);
        
        // When
        byte[] result = submissionFileService.getFileContent(testFileId);
        
        // Then
        assertNotNull(result);
        assertEquals(expectedContent, new String(result));
        verify(submissionFileMapper, times(1)).findById(testFileId);
    }

    @Test
    void getFileContent_shouldThrowException_whenFileNotFound() {
        // Given
        when(submissionFileMapper.findById(testFileId)).thenReturn(null);
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            submissionFileService.getFileContent(testFileId);
        });
        
        assertTrue(exception.getMessage().contains("File not found with id"));
        verify(submissionFileMapper, times(1)).findById(testFileId);
    }

    @Test
    void getFileContent_shouldThrowIOException_whenPhysicalFileNotFound() {
        // Given
        testSubmissionFile.setFilePath("/non/existent/file.txt");
        when(submissionFileMapper.findById(testFileId)).thenReturn(testSubmissionFile);
        
        // When & Then
        assertThrows(IOException.class, () -> {
            submissionFileService.getFileContent(testFileId);
        });
        
        verify(submissionFileMapper, times(1)).findById(testFileId);
    }

    @Test
    void constructor_shouldCreateUploadDirectory() {
        // Given
        Path newTempDir = tempDir.resolve("new-upload-dir");
        assertFalse(Files.exists(newTempDir));
        
        // When
        new SubmissionFileService(submissionFileMapper, newTempDir.toString());
        
        // Then
        assertTrue(Files.exists(newTempDir));
    }

    @Test
    void constructor_shouldThrowException_whenCannotCreateDirectory() {
        // Given - Use an invalid path that cannot be created
        String invalidPath = "\0invalid\0path";
        
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            new SubmissionFileService(submissionFileMapper, invalidPath);
        });
    }
}