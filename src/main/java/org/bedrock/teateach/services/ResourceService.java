// src/main/java/org/bedrock/teateach/services/ResourceService.java
package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.Resource;
import org.bedrock.teateach.mappers.ResourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID; // For unique file names

@Service
public class ResourceService {

    private final ResourceMapper resourceMapper;
    // Configure your file storage path in application.properties or similar
    // private final String fileStorageLocation = "/path/to/your/upload/directory";
    // For simplicity, using a dummy path here. In real app, inject from properties.
    private final String fileStorageLocation = System.getProperty("user.home") + "/teateach_uploads";

    @Autowired
    public ResourceService(ResourceMapper resourceMapper) {
        this.resourceMapper = resourceMapper;
        // Ensure the upload directory exists
        try {
            Files.createDirectories(Paths.get(fileStorageLocation));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    @Transactional
    public Resource createResource(Resource resource) {
        resourceMapper.insert(resource);
        return resource;
    }

    public Optional<Resource> getResourceById(Long id) {
        return Optional.ofNullable(resourceMapper.findById(id));
    }

    public List<Resource> getResourcesByCourseId(Long courseId) {
        return resourceMapper.findByCourseId(courseId);
    }

    @Transactional
    public Resource updateResource(Resource resource) {
        resourceMapper.update(resource);
        return resource;
    }

    @Transactional
    public void deleteResource(Long id) {
        Optional<Resource> resourceOptional = getResourceById(id);
        if (resourceOptional.isPresent()) {
            Resource resource = resourceOptional.get();
            // Delete the physical file first
            Path filePath = Paths.get(fileStorageLocation).resolve(resource.getFilePath());
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // Log the error but don't prevent DB deletion if file couldn't be deleted
                System.err.println("Failed to delete physical file: " + filePath + " Error: " + e.getMessage());
            }
            // Then delete the database entry
            resourceMapper.delete(id);
        }
    }

    @Transactional
    public Resource uploadFile(MultipartFile file, Long courseId, Long taskId, String resourceName, String description) throws IOException {
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
        }
        String storedFileName = UUID.randomUUID().toString() + "." + fileExtension;
        Path targetLocation = Paths.get(fileStorageLocation).resolve(storedFileName);

        try {
            Files.copy(file.getInputStream(), targetLocation);
        } catch (IOException e) {
            // If file copy fails, attempt to delete any partially created file
            try {
                Files.deleteIfExists(targetLocation);
            } catch (IOException cleanupException) {
                System.err.println("Failed to clean up partial file after upload failure: " + targetLocation + " Error: " + cleanupException.getMessage());
            }
            throw e; // Re-throw the original exception to ensure transaction rollback
        }

        Resource resource = new Resource();
        resource.setCourseId(courseId);
        resource.setTaskId(taskId); // Can be null
        resource.setResourceName(resourceName);
        resource.setFilePath(storedFileName); // Store internal path/name, not full path
        resource.setFileType(fileExtension);
        resource.setFileSize(file.getSize());
        resource.setDescription(description);
//        resource.setCreatedAt(LocalDateTime.now());
//        resource.setUpdatedAt(LocalDateTime.now());
        // Set uploadedBy if you have user context (e.g., from security principal)
        // resource.setUploadedBy(currentUser.getId());

        resourceMapper.insert(resource);
        return resource;
    }

    public byte[] downloadFile(Long resourceId) throws IOException {
        Optional<Resource> resourceOptional = getResourceById(resourceId);
        if (resourceOptional.isPresent()) {
            Resource resource = resourceOptional.get();
            Path filePath = Paths.get(fileStorageLocation).resolve(resource.getFilePath());
            if (Files.exists(filePath)) {
                return Files.readAllBytes(filePath);
            }
        }
        return null; // Or throw ResourceNotFoundException
    }
}