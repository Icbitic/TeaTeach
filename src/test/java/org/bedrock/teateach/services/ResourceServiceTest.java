// src/test/java/org/bedrock/teateach/services/ResourceServiceTest.java
package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.Resource;
import org.bedrock.teateach.mappers.ResourceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock
    private ResourceMapper resourceMapper;

    @InjectMocks
    private ResourceService resourceService;

    private Resource testResource;

    @BeforeEach
    void setUp() throws IOException {
        testResource = new Resource();
        testResource.setId(1L);
        testResource.setResourceName("Test Doc");
        testResource.setFilePath("test_doc.pdf");
        testResource.setFileType("pdf");
        testResource.setFileSize(1024L);
        testResource.setCourseId(10L);
        testResource.setDescription("A test document.");

        // Mock the file storage location creation in the constructor
        // This static mock needs to be inside a try-with-resources block or managed carefully
        // for each test method that calls the constructor implicitly.
        // For simplicity in BeforeEach, we'll ensure service is re-initialized.
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.createDirectories(any(Path.class))).thenReturn(Paths.get(System.getProperty("user.home") + "/teateach_uploads"));
            // Re-initialize service to ensure constructor runs with mocked Files
            resourceService = new ResourceService(resourceMapper);
        }
    }

    @Test
    void testCreateResource() {
        doNothing().when(resourceMapper).insert(any(Resource.class));

        Resource createdResource = resourceService.createResource(testResource);

        assertNotNull(createdResource);
        assertEquals(testResource.getId(), createdResource.getId());
        verify(resourceMapper, times(1)).insert(testResource);
    }

    @Test
    void testGetResourceByIdFound() {
        when(resourceMapper.findById(1L)).thenReturn(testResource);

        Optional<Resource> foundResource = resourceService.getResourceById(1L);

        assertTrue(foundResource.isPresent());
        assertEquals(testResource.getId(), foundResource.get().getId());
        verify(resourceMapper, times(1)).findById(1L);
    }

    @Test
    void testGetResourceByIdNotFound() {
        when(resourceMapper.findById(anyLong())).thenReturn(null);

        Optional<Resource> foundResource = resourceService.getResourceById(999L);

        assertFalse(foundResource.isPresent());
        verify(resourceMapper, times(1)).findById(999L);
    }

    @Test
    void testGetResourcesByCourseId() {
        List<Resource> resources = Arrays.asList(testResource, new Resource());
        when(resourceMapper.findByCourseId(10L)).thenReturn(resources);

        List<Resource> result = resourceService.getResourcesByCourseId(10L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        verify(resourceMapper, times(1)).findByCourseId(10L);
    }

    @Test
    void testUpdateResource() {
        // Corrected for void update method
        doNothing().when(resourceMapper).update(any(Resource.class));

        Resource updatedResource = resourceService.updateResource(testResource);

        assertNotNull(updatedResource);
        assertEquals(testResource.getId(), updatedResource.getId());
        verify(resourceMapper, times(1)).update(testResource);
    }

    @Test
    void testDeleteResourceSuccess() throws IOException {
        when(resourceMapper.findById(1L)).thenReturn(testResource);
        doNothing().when(resourceMapper).delete(1L);

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.deleteIfExists(any(Path.class))).thenReturn(true);

            resourceService.deleteResource(1L);

            mockedFiles.verify(() -> Files.deleteIfExists(any(Path.class)), times(1));
            verify(resourceMapper, times(1)).delete(1L);
        }
    }

    @Test
    void testDeleteResourceFileNotFoundInService() throws IOException {
        when(resourceMapper.findById(1L)).thenReturn(testResource);
        doNothing().when(resourceMapper).delete(1L);

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.deleteIfExists(any(Path.class))).thenReturn(false); // Simulate file not found/deleted

            resourceService.deleteResource(1L);

            mockedFiles.verify(() -> Files.deleteIfExists(any(Path.class)), times(1));
            verify(resourceMapper, times(1)).delete(1L); // DB entry should still be deleted
        }
    }

    @Test
    void testUploadFileSuccess() throws IOException {
        // Prepare a mock file
        byte[] fileContent = "Test file content".getBytes();
        MultipartFile mockFile = new MockMultipartFile(
                "file",
                "test_document.pdf",
                "application/pdf",
                fileContent
        );

        // Mock UUID to ensure predictable file name
        UUID fixedUUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        String expectedFileName = fixedUUID + ".pdf";

        // Mock static UUID class
        try (MockedStatic<UUID> mockedUUID = mockStatic(UUID.class);
             MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {

            mockedUUID.when(UUID::randomUUID).thenReturn(fixedUUID);

            Path targetPath = Paths.get( "./teateach_uploads").resolve(expectedFileName);
            mockedFiles.when(() -> Files.copy(any(InputStream.class), eq(targetPath))).thenReturn((long) fileContent.length);

            doNothing().when(resourceMapper).insert(any(Resource.class));

            Resource result = resourceService.uploadFile(
                    mockFile,
                    10L,
                    20L,
                    "Uploaded Test Document",
                    "This is a test upload."
            );

            assertNotNull(result);
            assertEquals("pdf", result.getFileType());
            assertEquals(expectedFileName, result.getFilePath());
            assertEquals(10L, result.getCourseId());
            assertEquals(20L, result.getTaskId());
            assertEquals("Uploaded Test Document", result.getResourceName());
            assertEquals("This is a test upload.", result.getDescription());

            verify(resourceMapper, times(1)).insert(any(Resource.class));
            mockedFiles.verify(() -> Files.copy(any(InputStream.class), eq(targetPath)), times(1));
        }
    }


    @Test
    void testDownloadFileSuccess() throws IOException {
        byte[] fileContent = "Downloaded file content".getBytes();
        when(resourceMapper.findById(1L)).thenReturn(testResource);

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            mockedFiles.when(() -> Files.readAllBytes(any(Path.class))).thenReturn(fileContent);

            byte[] downloadedBytes = resourceService.downloadFile(1L);

            assertNotNull(downloadedBytes);
            assertArrayEquals(fileContent, downloadedBytes);
            verify(resourceMapper, times(1)).findById(1L);
            mockedFiles.verify(() -> Files.readAllBytes(any(Path.class)), times(1));
        }
    }

    @Test
    void testDownloadFileResourceNotFound() throws IOException {
        when(resourceMapper.findById(anyLong())).thenReturn(null);

        byte[] downloadedBytes = resourceService.downloadFile(999L);

        assertNull(downloadedBytes);
        verify(resourceMapper, times(1)).findById(999L);
    }

    @Test
    void testDownloadFileNotFoundOnDisk() throws IOException {
        when(resourceMapper.findById(1L)).thenReturn(testResource);

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.exists(any(Path.class))).thenReturn(false);

            byte[] downloadedBytes = resourceService.downloadFile(1L);

            assertNull(downloadedBytes);
            verify(resourceMapper, times(1)).findById(1L);
            mockedFiles.verify(() -> Files.exists(any(Path.class)), times(1));
            mockedFiles.verify(() -> Files.readAllBytes(any(Path.class)), never()); // Should not attempt to read
        }
    }
}