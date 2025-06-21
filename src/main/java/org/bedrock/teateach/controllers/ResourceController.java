package org.bedrock.teateach.controllers;

import org.bedrock.teateach.beans.Resource;
import org.bedrock.teateach.services.ResourceService; // Assuming a ResourceService for CRUD and file handling
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;

    @Autowired
    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    /**
     * Creates a new resource entry in the database (metadata only).
     * For actual file upload, use /api/resources/upload.
     * POST /api/resources
     * @param resource The resource object to create.
     * @return The created resource with its generated ID.
     */
    @PostMapping
    public ResponseEntity<Resource> createResource(@RequestBody Resource resource) {
        Resource createdResource = resourceService.createResource(resource);
        return new ResponseEntity<>(createdResource, HttpStatus.CREATED);
    }

    /**
     * Uploads a file and creates a corresponding resource entry.
     * POST /api/resources/upload
     * @param file The file to upload.
     * @param courseId The course ID associated with the resource.
     * @param taskId Optional: The task ID associated with the resource.
     * @param resourceName The name of the resource.
     * @param description Optional: Description of the resource.
     * @return The created Resource object.
     */
    @PostMapping("/upload")
    public ResponseEntity<Resource> uploadResource(
            @RequestParam("file") MultipartFile file,
            @RequestParam("courseId") Long courseId,
            @RequestParam(value = "taskId", required = false) Long taskId,
            @RequestParam("resourceName") String resourceName,
            @RequestParam(value = "description", required = false) String description) {
        try {
            Resource createdResource = resourceService.uploadFile(file, courseId, taskId, resourceName, description);
            return new ResponseEntity<>(createdResource, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Downloads a resource file by its ID.
     * GET /api/resources/{id}/download
     * @param id The ID of the resource to download.
     * @return The resource file as a byte array.
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<org.springframework.core.io.Resource> downloadResource(@PathVariable Long id) {
        Optional<Resource> resourceOptional = resourceService.getResourceById(id);
        if (resourceOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = resourceOptional.get();

        try {
            byte[] data = resourceService.downloadFile(id); // This fetches the actual file content
            if (data == null || data.length == 0) {
                return ResponseEntity.notFound().build(); // File content not found
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getResourceName() + "." + resource.getFileType() + "\"");
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            headers.add(HttpHeaders.PRAGMA, "no-cache");
            headers.add(HttpHeaders.EXPIRES, "0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(data.length)
                    .contentType(MediaType.parseMediaType(getMediaTypeForFileType(resource.getFileType()))) // Helper method
                    .body(new ByteArrayResource(data));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Helper to determine media type based on file extension.
     */
    private String getMediaTypeForFileType(String fileType) {
        return switch (fileType.toLowerCase()) {
            case "pdf" -> "application/pdf";
            case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "mp4" -> "video/mp4";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "txt" -> "text/plain";
            default -> MediaType.APPLICATION_OCTET_STREAM_VALUE; // Generic binary
        };
    }

    /**
     * Retrieves a resource by its ID.
     * GET /api/resources/{id}
     * @param id The ID of the resource to retrieve.
     * @return The resource if found, or 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Resource> getResourceById(@PathVariable Long id) {
        Optional<Resource> resource = resourceService.getResourceById(id);
        return resource.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all resources for a specific course.
     * GET /api/resources/course/{courseId}
     * @param courseId The ID of the course.
     * @return A list of resources for the specified course.
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Resource>> getResourcesByCourseId(@PathVariable Long courseId) {
        List<Resource> resources = resourceService.getResourcesByCourseId(courseId);
        return ResponseEntity.ok(resources);
    }

    /**
     * Updates an existing resource.
     * PUT /api/resources/{id}
     * @param id The ID of the resource to update.
     * @param resource The updated resource object.
     * @return The updated resource, or 404 Not Found if the ID doesn't exist.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Resource> updateResource(@PathVariable Long id, @RequestBody Resource resource) {
        if (!id.equals(resource.getId())) {
            return ResponseEntity.badRequest().build();
        }
        Optional<Resource> existingResource = resourceService.getResourceById(id);
        if (existingResource.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Resource updatedResource = resourceService.updateResource(resource);
        return ResponseEntity.ok(updatedResource);
    }

    /**
     * Deletes a resource by its ID.
     * DELETE /api/resources/{id}
     * @param id The ID of the resource to delete.
     * @return 204 No Content if successful, or 404 Not Found.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable Long id) {
        Optional<Resource> existingResource = resourceService.getResourceById(id);
        if (existingResource.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        resourceService.deleteResource(id);
        return ResponseEntity.noContent().build();
    }
}