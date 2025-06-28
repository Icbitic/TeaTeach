package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.Resource;
import org.bedrock.teateach.beans.User;
import org.bedrock.teateach.mappers.ResourceMapper;
import org.bedrock.teateach.mappers.UserMapper;
import org.bedrock.teateach.llm.LLMService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock
    private ResourceMapper resourceMapper;
    
    @Mock
    private UserMapper userMapper;
    
    @Mock
    private LLMService llmService;
    
    @Mock
    private Authentication authentication;
    
    @Mock
    private SecurityContext securityContext;
    
    @Mock
    private MultipartFile multipartFile;
    
    @TempDir
    Path tempDir;
    
    private ResourceService resourceService;
    
    private Resource testResource;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        // Initialize ResourceService with temp directory
        resourceService = new ResourceService(resourceMapper, userMapper, llmService, tempDir.toString());
        
        // Setup test data
        testResource = new Resource();
        testResource.setId(1L);
        testResource.setResourceName("Test Resource");
        testResource.setFilePath("test-file.pdf");
        testResource.setFileType("pdf");
        testResource.setUploadedBy(1L);
        testResource.setFileSize(1024L);
        testResource.setDescription("Test resource description");
        testResource.setCreatedAt(LocalDateTime.now());
        testResource.setUpdatedAt(LocalDateTime.now());
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setUserType("STUDENT");
        testUser.setActive(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }
    
    @Test
    void createResource_shouldInsertResourceAndAddToVectorStore() {
        // Given
        doNothing().when(resourceMapper).insert(testResource);
        doNothing().when(llmService).addResourceToVectorStore(testResource);
        
        // When
        Resource result = resourceService.createResource(testResource);
        
        // Then
        assertNotNull(result);
        assertEquals(testResource, result);
        verify(resourceMapper, times(1)).insert(testResource);
        verify(llmService, times(1)).addResourceToVectorStore(testResource);
    }
    
    @Test
    void createResource_shouldHandleVectorStoreException() {
        // Given
        doNothing().when(resourceMapper).insert(testResource);
        doThrow(new RuntimeException("Vector store error")).when(llmService).addResourceToVectorStore(testResource);
        
        // When
        Resource result = resourceService.createResource(testResource);
        
        // Then
        assertNotNull(result);
        assertEquals(testResource, result);
        verify(resourceMapper, times(1)).insert(testResource);
        verify(llmService, times(1)).addResourceToVectorStore(testResource);
    }
    
    @Test
    void getResourceById_shouldReturnResource_whenFound() {
        // Given
        Long resourceId = 1L;
        when(resourceMapper.findById(resourceId)).thenReturn(testResource);
        
        // When
        Optional<Resource> result = resourceService.getResourceById(resourceId);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(testResource, result.get());
        verify(resourceMapper, times(1)).findById(resourceId);
    }
    
    @Test
    void getResourceById_shouldReturnEmpty_whenNotFound() {
        // Given
        Long resourceId = 999L;
        when(resourceMapper.findById(resourceId)).thenReturn(null);
        
        // When
        Optional<Resource> result = resourceService.getResourceById(resourceId);
        
        // Then
        assertFalse(result.isPresent());
        verify(resourceMapper, times(1)).findById(resourceId);
    }
    
    @Test
    void getAllResources_shouldReturnAllResources() {
        // Given
        List<Resource> expectedResources = Arrays.asList(testResource, new Resource());
        when(resourceMapper.findAll()).thenReturn(expectedResources);
        
        // When
        List<Resource> result = resourceService.getAllResources();
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(resourceMapper, times(1)).findAll();
    }
    
    @Test
    void getAllResources_shouldReturnEmptyList_whenNoResourcesExist() {
        // Given
        when(resourceMapper.findAll()).thenReturn(Collections.emptyList());
        
        // When
        List<Resource> result = resourceService.getAllResources();
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(resourceMapper, times(1)).findAll();
    }
    
    @Test
    void updateResource_shouldUpdateResourceWithTimestamp() {
        // Given
        testResource.setResourceName("Updated Resource");
        doNothing().when(resourceMapper).update(testResource);
        
        // When
        Resource result = resourceService.updateResource(testResource);
        
        // Then
        assertNotNull(result);
        assertEquals("Updated Resource", result.getResourceName());
        assertNotNull(result.getUpdatedAt());
        verify(resourceMapper, times(1)).update(testResource);
    }
    
    @Test
    void deleteResource_shouldDeleteResourceAndFile_whenResourceExists() throws IOException {
        // Given
        Long resourceId = 1L;
        Path testFile = tempDir.resolve(testResource.getFilePath());
        Files.createFile(testFile);
        
        when(resourceMapper.findById(resourceId)).thenReturn(testResource);
        doNothing().when(resourceMapper).delete(resourceId);
        
        // When
        resourceService.deleteResource(resourceId);
        
        // Then
        assertFalse(Files.exists(testFile));
        verify(resourceMapper, times(1)).findById(resourceId);
        verify(resourceMapper, times(1)).delete(resourceId);
    }
    
    @Test
    void deleteResource_shouldHandleFileNotFound() {
        // Given
        Long resourceId = 1L;
        when(resourceMapper.findById(resourceId)).thenReturn(testResource);
        doNothing().when(resourceMapper).delete(resourceId);
        
        // When
        resourceService.deleteResource(resourceId);
        
        // Then
        verify(resourceMapper, times(1)).findById(resourceId);
        verify(resourceMapper, times(1)).delete(resourceId);
    }
    
    @Test
    void deleteResource_shouldDoNothing_whenResourceNotFound() {
        // Given
        Long resourceId = 999L;
        when(resourceMapper.findById(resourceId)).thenReturn(null);
        
        // When
        resourceService.deleteResource(resourceId);
        
        // Then
        verify(resourceMapper, times(1)).findById(resourceId);
        verify(resourceMapper, never()).delete(resourceId);
    }
    
    @Test
    void uploadFile_shouldCreateResourceWithAuthenticatedUser() throws IOException {
        // Given
        String resourceName = "Test Upload";
        String description = "Test description";
        byte[] fileContent = "test content".getBytes();
        
        when(multipartFile.getOriginalFilename()).thenReturn("test.pdf");
        when(multipartFile.getSize()).thenReturn((long) fileContent.length);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent));
        
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("testuser");
            when(userMapper.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            
            doNothing().when(resourceMapper).insert(any(Resource.class));
            doNothing().when(llmService).addResourceToVectorStore(any(Resource.class));
            
            // When
            Resource result = resourceService.uploadFile(multipartFile, resourceName, description);
            
            // Then
            assertNotNull(result);
            assertEquals(resourceName, result.getResourceName());
            assertEquals(description, result.getDescription());
            assertEquals("pdf", result.getFileType());
            assertEquals((long) fileContent.length, result.getFileSize());
            assertEquals(testUser.getId(), result.getUploadedBy());
            assertNotNull(result.getCreatedAt());
            assertNotNull(result.getUpdatedAt());
            
            verify(resourceMapper, times(1)).insert(any(Resource.class));
            verify(llmService, times(1)).addResourceToVectorStore(any(Resource.class));
        }
    }
    
    @Test
    void uploadFile_shouldCreateResourceWithoutUser_whenNotAuthenticated() throws IOException {
        // Given
        String resourceName = "Test Upload";
        String description = "Test description";
        byte[] fileContent = "test content".getBytes();
        
        when(multipartFile.getOriginalFilename()).thenReturn("test.pdf");
        when(multipartFile.getSize()).thenReturn((long) fileContent.length);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent));
        
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);
            
            doNothing().when(resourceMapper).insert(any(Resource.class));
            doNothing().when(llmService).addResourceToVectorStore(any(Resource.class));
            
            // When
            Resource result = resourceService.uploadFile(multipartFile, resourceName, description);
            
            // Then
            assertNotNull(result);
            assertEquals(resourceName, result.getResourceName());
            assertNull(result.getUploadedBy());
            
            verify(resourceMapper, times(1)).insert(any(Resource.class));
        }
    }
    
    @Test
    void uploadFile_shouldHandleFileWithoutExtension() throws IOException {
        // Given
        String resourceName = "Test Upload";
        String description = "Test description";
        byte[] fileContent = "test content".getBytes();
        
        when(multipartFile.getOriginalFilename()).thenReturn("testfile");
        when(multipartFile.getSize()).thenReturn((long) fileContent.length);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(fileContent));
        
        doNothing().when(resourceMapper).insert(any(Resource.class));
        doNothing().when(llmService).addResourceToVectorStore(any(Resource.class));
        
        // When
        Resource result = resourceService.uploadFile(multipartFile, resourceName, description);
        
        // Then
        assertNotNull(result);
        assertEquals("", result.getFileType());
        verify(resourceMapper, times(1)).insert(any(Resource.class));
    }
    
    @Test
    void uploadFile_shouldThrowException_whenFileUploadFails() throws IOException {
        // Given
        when(multipartFile.getOriginalFilename()).thenReturn("test.pdf");
        when(multipartFile.getInputStream()).thenThrow(new IOException("File upload failed"));
        
        // When & Then
        assertThrows(IOException.class, () -> {
            resourceService.uploadFile(multipartFile, "Test", "Description");
        });
        
        verify(resourceMapper, never()).insert(any(Resource.class));
    }
    
    @Test
    void downloadFile_shouldReturnFileBytes_whenResourceExists() throws IOException {
        // Given
        Long resourceId = 1L;
        byte[] expectedContent = "test file content".getBytes();
        Path testFile = tempDir.resolve(testResource.getFilePath());
        Files.write(testFile, expectedContent);
        
        when(resourceMapper.findById(resourceId)).thenReturn(testResource);
        
        // When
        byte[] result = resourceService.downloadFile(resourceId);
        
        // Then
        assertNotNull(result);
        assertArrayEquals(expectedContent, result);
        verify(resourceMapper, times(1)).findById(resourceId);
    }
    
    @Test
    void downloadFile_shouldReturnNull_whenResourceNotFound() throws IOException {
        // Given
        Long resourceId = 999L;
        when(resourceMapper.findById(resourceId)).thenReturn(null);
        
        // When
        byte[] result = resourceService.downloadFile(resourceId);
        
        // Then
        assertNull(result);
        verify(resourceMapper, times(1)).findById(resourceId);
    }
    
    @Test
    void downloadFile_shouldReturnNull_whenFileNotExists() throws IOException {
        // Given
        Long resourceId = 1L;
        when(resourceMapper.findById(resourceId)).thenReturn(testResource);
        
        // When
        byte[] result = resourceService.downloadFile(resourceId);
        
        // Then
        assertNull(result);
        verify(resourceMapper, times(1)).findById(resourceId);
    }
    
    @Test
    void getResourcesByUploadedBy_shouldReturnUserResources() {
        // Given
        Long uploadedBy = 1L;
        List<Resource> expectedResources = Arrays.asList(testResource, new Resource());
        when(resourceMapper.findByUploadedBy(uploadedBy)).thenReturn(expectedResources);
        
        // When
        List<Resource> result = resourceService.getResourcesByUploadedBy(uploadedBy);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(resourceMapper, times(1)).findByUploadedBy(uploadedBy);
    }
    
    @Test
    void getMyResources_shouldReturnCurrentUserResources_whenAuthenticated() {
        // Given
        List<Resource> expectedResources = Arrays.asList(testResource);
        
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("testuser");
            when(userMapper.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(resourceMapper.findByUploadedBy(testUser.getId())).thenReturn(expectedResources);
            
            // When
            List<Resource> result = resourceService.getMyResources();
            
            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testResource, result.get(0));
            verify(resourceMapper, times(1)).findByUploadedBy(testUser.getId());
        }
    }
    
    @Test
    void getMyResources_shouldReturnEmptyList_whenNotAuthenticated() {
        // Given
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);
            
            // When
            List<Resource> result = resourceService.getMyResources();
            
            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(resourceMapper, never()).findByUploadedBy(any());
        }
    }
    
    @Test
    void getMyResources_shouldReturnEmptyList_whenUserNotFound() {
        // Given
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("unknownuser");
            when(userMapper.findByUsername("unknownuser")).thenReturn(Optional.empty());
            
            // When
            List<Resource> result = resourceService.getMyResources();
            
            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(resourceMapper, never()).findByUploadedBy(any());
        }
    }
    
    @Test
    void getResourcesByTaskId_shouldReturnTaskResources() {
        // Given
        Long taskId = 1L;
        List<Resource> expectedResources = Arrays.asList(testResource, new Resource());
        when(resourceMapper.findByTaskId(taskId)).thenReturn(expectedResources);
        
        // When
        List<Resource> result = resourceService.getResourcesByTaskId(taskId);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(resourceMapper, times(1)).findByTaskId(taskId);
    }
    
    @Test
    void getResourcesByUploadedByAndTaskId_shouldReturnUserTaskResources() {
        // Given
        Long uploadedBy = 1L;
        Long taskId = 1L;
        List<Resource> expectedResources = Arrays.asList(testResource);
        when(resourceMapper.findByUploadedByAndTaskId(uploadedBy, taskId)).thenReturn(expectedResources);
        
        // When
        List<Resource> result = resourceService.getResourcesByUploadedByAndTaskId(uploadedBy, taskId);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(resourceMapper, times(1)).findByUploadedByAndTaskId(uploadedBy, taskId);
    }
    
    @Test
    void getMyResourcesForTask_shouldReturnCurrentUserTaskResources_whenAuthenticated() {
        // Given
        Long taskId = 1L;
        List<Resource> expectedResources = Arrays.asList(testResource);
        
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getName()).thenReturn("testuser");
            when(userMapper.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(resourceMapper.findByUploadedByAndTaskId(testUser.getId(), taskId)).thenReturn(expectedResources);
            
            // When
            List<Resource> result = resourceService.getMyResourcesForTask(taskId);
            
            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(testResource, result.get(0));
            verify(resourceMapper, times(1)).findByUploadedByAndTaskId(testUser.getId(), taskId);
        }
    }
    
    @Test
    void getMyResourcesForTask_shouldReturnEmptyList_whenNotAuthenticated() {
        // Given
        Long taskId = 1L;
        
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);
            
            // When
            List<Resource> result = resourceService.getMyResourcesForTask(taskId);
            
            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(resourceMapper, never()).findByUploadedByAndTaskId(any(), any());
        }
    }
}