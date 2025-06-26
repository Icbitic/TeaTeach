package org.bedrock.teateach.services;

import org.bedrock.teateach.beans.SubmissionFile;
import org.bedrock.teateach.mappers.SubmissionFileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SubmissionFileService {

    private final SubmissionFileMapper submissionFileMapper;
    private final String fileStorageLocation;

    @Autowired
    public SubmissionFileService(SubmissionFileMapper submissionFileMapper,
                                @Value("${file.upload-dir:uploads/submissions}") String fileStorageLocation) {
        this.submissionFileMapper = submissionFileMapper;
        this.fileStorageLocation = fileStorageLocation;
        
        // Create the upload directory if it doesn't exist
        try {
            Path uploadPath = Paths.get(fileStorageLocation);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    @Transactional
    @CacheEvict(value = {"submissionFiles"}, allEntries = true)
    public SubmissionFile uploadFile(MultipartFile file, Long submissionId) throws IOException {
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
            throw new IOException("Could not store file " + originalFileName + ". Please try again!", e);
        }

        // Create SubmissionFile entity
        SubmissionFile submissionFile = new SubmissionFile();
        submissionFile.setSubmissionId(submissionId);
        submissionFile.setFileName(originalFileName);
        submissionFile.setStoredFileName(storedFileName);
        submissionFile.setFilePath(targetLocation.toString());
        submissionFile.setFileType(fileExtension);
        submissionFile.setFileSize(file.getSize());
        submissionFile.setMimeType(file.getContentType());
        submissionFile.setCreatedAt(LocalDateTime.now());
        submissionFile.setUpdatedAt(LocalDateTime.now());

        submissionFileMapper.insert(submissionFile);
        return submissionFile;
    }

    @Cacheable(value = "submissionFiles", key = "#submissionId")
    public List<SubmissionFile> getFilesBySubmissionId(Long submissionId) {
        return submissionFileMapper.findBySubmissionId(submissionId);
    }

    @Cacheable(value = "submissionFiles", key = "#id")
    public SubmissionFile getFileById(Long id) {
        return submissionFileMapper.findById(id);
    }

    @Transactional
    @CacheEvict(value = {"submissionFiles"}, allEntries = true)
    public void deleteFile(Long id) throws IOException {
        SubmissionFile submissionFile = submissionFileMapper.findById(id);
        if (submissionFile != null) {
            // Delete physical file
            Path filePath = Paths.get(submissionFile.getFilePath());
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                System.err.println("Failed to delete physical file: " + filePath + " Error: " + e.getMessage());
            }
            
            // Delete database record
            submissionFileMapper.deleteById(id);
        }
    }

    @Transactional
    @CacheEvict(value = {"submissionFiles"}, allEntries = true)
    public void deleteFilesBySubmissionId(Long submissionId) throws IOException {
        List<SubmissionFile> files = submissionFileMapper.findBySubmissionId(submissionId);
        
        // Delete physical files
        for (SubmissionFile file : files) {
            Path filePath = Paths.get(file.getFilePath());
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                System.err.println("Failed to delete physical file: " + filePath + " Error: " + e.getMessage());
            }
        }
        
        // Delete database records
        submissionFileMapper.deleteBySubmissionId(submissionId);
    }

    public byte[] getFileContent(Long id) throws IOException {
        SubmissionFile submissionFile = submissionFileMapper.findById(id);
        if (submissionFile == null) {
            throw new RuntimeException("File not found with id: " + id);
        }
        
        Path filePath = Paths.get(submissionFile.getFilePath());
        return Files.readAllBytes(filePath);
    }
}