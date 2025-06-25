// src/main/java/org/bedrock/teateach/services/ResourceService.java
package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.Resource;
import org.bedrock.teateach.beans.User;
import org.bedrock.teateach.mappers.ResourceMapper;
import org.bedrock.teateach.mappers.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID; // For unique file names

@Service
public class ResourceService {

    private final ResourceMapper resourceMapper;
    private final UserMapper userMapper;

    private final String fileStorageLocation = System.getProperty("user.home") + "/teateach_uploads";

    @Autowired
    public ResourceService(ResourceMapper resourceMapper, UserMapper userMapper) {
        this.resourceMapper = resourceMapper;
        this.userMapper = userMapper;
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

    @Cacheable(value = "resources", key = "#id")
    public Optional<Resource> getResourceById(Long id) {
        return Optional.ofNullable(resourceMapper.findById(id));
    }



    @Transactional
    @CacheEvict(value = {"resources"}, allEntries = true)
    public Resource updateResource(Resource resource) {
        resource.setUpdatedAt(LocalDateTime.now());
        resourceMapper.update(resource);
        return resource;
    }

    @Transactional
    @CacheEvict(value = {"resources"}, allEntries = true)
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
    public Resource uploadFile(MultipartFile file, String resourceName, String description) throws IOException {
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
        resource.setResourceName(resourceName);
        resource.setFilePath(storedFileName); // Store internal path/name, not full path
        resource.setFileType(fileExtension);
        resource.setFileSize(file.getSize());
        resource.setDescription(description);
        resource.setCreatedAt(LocalDateTime.now());
        resource.setUpdatedAt(LocalDateTime.now());
        // Automatically set uploadedBy from current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            String username = authentication.getName();
            Optional<User> user = userMapper.findByUsername(username);
            if (user.isPresent()) {
                resource.setUploadedBy(user.get().getId());
            }
        }

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

    /**
     * Gets all resources uploaded by a specific user.
     *
     * @param uploadedBy The ID of the user who uploaded the resources.
     * @return A list of resources uploaded by the specified user.
     */
    public List<Resource> getResourcesByUploadedBy(Long uploadedBy) {
        return resourceMapper.findByUploadedBy(uploadedBy);
    }

    /**
     * Gets all resources uploaded by the current authenticated user.
     *
     * @return A list of resources uploaded by the current user.
     */
    public List<Resource> getMyResources() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            String username = authentication.getName();
            Optional<User> user = userMapper.findByUsername(username);
            if (user.isPresent()) {
                return resourceMapper.findByUploadedBy(user.get().getId());
            }
        }
        return List.of(); // Return empty list if user not authenticated
    }

    /**
     * Gets resources associated with a specific task.
     *
     * @param taskId The ID of the task.
     * @return A list of resources associated with the specified task.
     */
    public List<Resource> getResourcesByTaskId(Long taskId) {
        return resourceMapper.findByTaskId(taskId);
    }

    /**
     * Gets resources uploaded by a specific user for a specific task.
     *
     * @param uploadedBy The ID of the user who uploaded the resources.
     * @param taskId The ID of the task.
     * @return A list of resources uploaded by the user for the specified task.
     */
    public List<Resource> getResourcesByUploadedByAndTaskId(Long uploadedBy, Long taskId) {
        return resourceMapper.findByUploadedByAndTaskId(uploadedBy, taskId);
    }

    /**
     * Gets resources uploaded by the current authenticated user for a specific task.
     *
     * @param taskId The ID of the task.
     * @return A list of resources uploaded by the current user for the specified task.
     */
    public List<Resource> getMyResourcesForTask(Long taskId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            String username = authentication.getName();
            Optional<User> user = userMapper.findByUsername(username);
            if (user.isPresent()) {
                return resourceMapper.findByUploadedByAndTaskId(user.get().getId(), taskId);
            }
        }
        return List.of(); // Return empty list if user not authenticated
    }
}