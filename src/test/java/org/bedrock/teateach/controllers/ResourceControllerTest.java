package org.bedrock.teateach.controllers;

import org.bedrock.teateach.beans.Resource;
import org.bedrock.teateach.services.ResourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceControllerTest {

    @Mock
    private ResourceService resourceService;

    @InjectMocks
    private ResourceController resourceController;

    private Resource testResource;
    private List<Resource> testResources;
    private byte[] testFileContent;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testResource = new Resource();
        testResource.setId(1L);
        testResource.setResourceName("Test Resource");
        testResource.setCourseId(1L);
        testResource.setFilePath("test-file.pdf");
        testResource.setFileType("pdf");
        testResource.setFileSize(1024L);
        testResource.setDescription("Test resource description");

        Resource resource2 = new Resource();
        resource2.setId(2L);
        resource2.setResourceName("Another Resource");
        resource2.setCourseId(1L);
        resource2.setFilePath("another-file.docx");
        resource2.setFileType("docx");
        resource2.setFileSize(2048L);

        testResources = Arrays.asList(testResource, resource2);
        testFileContent = "Test file content".getBytes();
    }

    @Test
    void createResource_ShouldReturnCreatedResource() {
        // Given
        when(resourceService.createResource(any(Resource.class))).thenReturn(testResource);

        // When
        ResponseEntity<Resource> response = resourceController.createResource(new Resource());

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testResource, response.getBody());
        verify(resourceService, times(1)).createResource(any(Resource.class));
    }

    @Test
    void uploadResource_WhenSuccessful_ShouldReturnCreatedResource() throws Exception {
        // Given
        MultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", testFileContent);
        Long courseId = 1L;
        Long taskId = 10L;
        String resourceName = "Uploaded Resource";
        String description = "Uploaded resource description";

        when(resourceService.uploadFile(file, courseId, taskId, resourceName, description)).thenReturn(testResource);

        // When
        ResponseEntity<Resource> response = resourceController.uploadResource(file, courseId, taskId, resourceName, description);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testResource, response.getBody());
        verify(resourceService, times(1)).uploadFile(file, courseId, taskId, resourceName, description);
    }

    @Test
    void uploadResource_WhenException_ShouldReturnInternalServerError() throws Exception {
        // Given
        MultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", testFileContent);
        Long courseId = 1L;
        Long taskId = 10L;
        String resourceName = "Uploaded Resource";
        String description = "Uploaded resource description";

        when(resourceService.uploadFile(any(), anyLong(), anyLong(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Upload failed"));

        // When
        ResponseEntity<Resource> response = resourceController.uploadResource(file, courseId, taskId, resourceName, description);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        verify(resourceService, times(1)).uploadFile(file, courseId, taskId, resourceName, description);
    }

    @Test
    void downloadResource_WhenResourceExists_AndFileExists_ShouldReturnFile() throws Exception {
        // Given
        when(resourceService.getResourceById(1L)).thenReturn(Optional.of(testResource));
        when(resourceService.downloadFile(1L)).thenReturn(testFileContent);

        // When
        ResponseEntity<org.springframework.core.io.Resource> response = resourceController.downloadResource(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ByteArrayResource);
        assertEquals(testFileContent.length, response.getBody().contentLength());
        assertNotNull(response.getHeaders().getContentDisposition());
        assertTrue(response.getHeaders().getContentDisposition().toString().contains(testResource.getResourceName()));
        verify(resourceService, times(1)).getResourceById(1L);
        verify(resourceService, times(1)).downloadFile(1L);
    }

    @Test
    void downloadResource_WhenResourceExists_ButFileDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Given
        when(resourceService.getResourceById(1L)).thenReturn(Optional.of(testResource));
        when(resourceService.downloadFile(1L)).thenReturn(null); // File not found

        // When
        ResponseEntity<org.springframework.core.io.Resource> response = resourceController.downloadResource(1L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(resourceService, times(1)).getResourceById(1L);
        verify(resourceService, times(1)).downloadFile(1L);
    }

    @Test
    void downloadResource_WhenResourceDoesNotExist_ShouldReturnNotFound() throws IOException {
        // Given
        when(resourceService.getResourceById(anyLong())).thenReturn(Optional.empty());

        // When
        ResponseEntity<org.springframework.core.io.Resource> response = resourceController.downloadResource(999L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(resourceService, times(1)).getResourceById(999L);
        verify(resourceService, never()).downloadFile(anyLong());
    }

    @Test
    void downloadResource_WhenExceptionOccurs_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(resourceService.getResourceById(1L)).thenReturn(Optional.of(testResource));
        when(resourceService.downloadFile(1L)).thenThrow(new RuntimeException("Download failed"));

        // When
        ResponseEntity<org.springframework.core.io.Resource> response = resourceController.downloadResource(1L);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        verify(resourceService, times(1)).getResourceById(1L);
        verify(resourceService, times(1)).downloadFile(1L);
    }

    @Test
    void getResourceById_WhenResourceExists_ShouldReturnResource() {
        // Given
        when(resourceService.getResourceById(1L)).thenReturn(Optional.of(testResource));

        // When
        ResponseEntity<Resource> response = resourceController.getResourceById(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testResource, response.getBody());
        verify(resourceService, times(1)).getResourceById(1L);
    }

    @Test
    void getResourceById_WhenResourceDoesNotExist_ShouldReturnNotFound() {
        // Given
        when(resourceService.getResourceById(anyLong())).thenReturn(Optional.empty());

        // When
        ResponseEntity<Resource> response = resourceController.getResourceById(999L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(resourceService, times(1)).getResourceById(999L);
    }

    @Test
    void getResourcesByCourseId_ShouldReturnListOfResources() {
        // Given
        Long courseId = 1L;
        when(resourceService.getResourcesByCourseId(courseId)).thenReturn(testResources);

        // When
        ResponseEntity<List<Resource>> response = resourceController.getResourcesByCourseId(courseId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testResources.size(), response.getBody().size());
        verify(resourceService, times(1)).getResourcesByCourseId(courseId);
    }

    @Test
    void updateResource_WhenIdMatches_AndResourceExists_ShouldReturnUpdatedResource() {
        // Given
        when(resourceService.getResourceById(1L)).thenReturn(Optional.of(testResource));
        when(resourceService.updateResource(any(Resource.class))).thenReturn(testResource);

        // When
        ResponseEntity<Resource> response = resourceController.updateResource(1L, testResource);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testResource, response.getBody());
        verify(resourceService, times(1)).getResourceById(1L);
        verify(resourceService, times(1)).updateResource(testResource);
    }

    @Test
    void updateResource_WhenIdMismatch_ShouldReturnBadRequest() {
        // Given
        Resource resource = new Resource();
        resource.setId(2L); // Different from path ID

        // When
        ResponseEntity<Resource> response = resourceController.updateResource(1L, resource);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(resourceService, never()).updateResource(any(Resource.class));
    }

    @Test
    void updateResource_WhenResourceDoesNotExist_ShouldReturnNotFound() {
        // Given
        when(resourceService.getResourceById(anyLong())).thenReturn(Optional.empty());

        // When
        ResponseEntity<Resource> response = resourceController.updateResource(1L, testResource);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(resourceService, times(1)).getResourceById(1L);
        verify(resourceService, never()).updateResource(any(Resource.class));
    }

    @Test
    void deleteResource_WhenResourceExists_ShouldReturnNoContent() {
        // Given
        when(resourceService.getResourceById(1L)).thenReturn(Optional.of(testResource));
        doNothing().when(resourceService).deleteResource(anyLong());

        // When
        ResponseEntity<Void> response = resourceController.deleteResource(1L);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(resourceService, times(1)).getResourceById(1L);
        verify(resourceService, times(1)).deleteResource(1L);
    }

    @Test
    void deleteResource_WhenResourceDoesNotExist_ShouldReturnNotFound() {
        // Given
        when(resourceService.getResourceById(anyLong())).thenReturn(Optional.empty());

        // When
        ResponseEntity<Void> response = resourceController.deleteResource(999L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(resourceService, times(1)).getResourceById(999L);
        verify(resourceService, never()).deleteResource(anyLong());
    }

    @Test
    void getMediaTypeForFileType_ShouldReturnCorrectMediaType() throws IOException {
        // Testing the private helper method through its usage in downloadResource
        // We test a few common file types to ensure they map to the correct media types

        // PDF test
        testResource.setFileType("pdf");
        when(resourceService.getResourceById(1L)).thenReturn(Optional.of(testResource));
        when(resourceService.downloadFile(1L)).thenReturn(testFileContent);

        ResponseEntity<org.springframework.core.io.Resource> pdfResponse = resourceController.downloadResource(1L);
        assertEquals(MediaType.parseMediaType("application/pdf"), pdfResponse.getHeaders().getContentType());

        // JPEG test
        testResource.setFileType("jpeg");
        when(resourceService.getResourceById(2L)).thenReturn(Optional.of(testResource));
        when(resourceService.downloadFile(2L)).thenReturn(testFileContent);

        ResponseEntity<org.springframework.core.io.Resource> jpegResponse = resourceController.downloadResource(2L);
        assertEquals(MediaType.parseMediaType("image/jpeg"), jpegResponse.getHeaders().getContentType());

        // Unknown type test
        testResource.setFileType("unknown");
        when(resourceService.getResourceById(3L)).thenReturn(Optional.of(testResource));
        when(resourceService.downloadFile(3L)).thenReturn(testFileContent);

        ResponseEntity<org.springframework.core.io.Resource> unknownResponse = resourceController.downloadResource(3L);
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, unknownResponse.getHeaders().getContentType());
    }
}
