package com.pfa.pfaproject.service;

import com.pfa.pfaproject.exception.CustomException;
import com.pfa.pfaproject.model.Admin;
import com.pfa.pfaproject.model.Document;
import com.pfa.pfaproject.repository.DocumentRepository;
import com.pfa.pfaproject.validation.ValidationUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing Document entities and file storage.<br/><br/>
 * 
 * This service handles all operations related to Document entities including:
 * - CRUD operations for document management
 * - File system storage and retrieval
 * - Document versioning by year
 * - File metadata management
 * 
 * Documents represent annual reports and official publications in the
 * Afriq-AI Ranking system, providing context and background for rankings.
 * 
 * @since 1.0
 * @version 1.1
 */
@Service
@AllArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final AdminService adminService;
    
    // Constants for document management
    private static final String UPLOAD_DIR = "uploads/documents";
    private static final String[] ALLOWED_FILE_TYPES = {"application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    /**
     * Returns all documents in the system, ordered by year descending.
     * @return List of all documents
     */
    public List<Document> findAll() {
        return documentRepository.findAllByOrderByYearDesc();
    }

    /**
     * Finds a document by ID.
     * @param id The document ID
     * @return The found document
     * @throws CustomException if document is not found
     */
    public Document findById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new CustomException("Document not found", HttpStatus.NOT_FOUND));
    }

    /**
     * Finds a document by year.
     * @param year The year to search
     * @return The found document
     * @throws CustomException if document is not found
     */
    public Document findByYear(Integer year) {
        Document document = documentRepository.findByYear(year);
        if (document == null) {
            throw new CustomException("Document not found for year: " + year, HttpStatus.NOT_FOUND);
        }
        return document;
    }

    /**
     * Checks if a document exists for a specific year.
     * @param year The year to check
     * @return true if exists, false otherwise
     */
    public boolean existsByYear(Integer year) {
        return documentRepository.existsByYear(year);
    }

    /**
     * Gets the file path for a document.
     * @param id The document ID
     * @return The file path
     * @throws CustomException if document is not found
     */
    public Path getFilePath(Long id) {
        Document document = findById(id);
        return Paths.get(document.getFilePath());
    }

    /**
     * Uploads and saves a new document.
     * @param title The document title
     * @param year The document year
     * @param file The uploaded file
     * @param adminId The ID of the admin uploading the document
     * @return The saved document with ID
     * @throws CustomException if validation fails or file upload has issues
     */
    @Transactional
    public Document uploadDocument(String title, Integer year, MultipartFile file, Long adminId) {
        validateDocumentUpload(title, year, file);
        
        Admin admin = adminService.findById(adminId);
        
        try {
            String filePath = saveFileToStorage(file);
            
            // Create document entity
            Document document = Document.builder()
                    .title(title)
                    .year(year)
                    .fileName(file.getOriginalFilename())
                    .filePath(filePath)
                    .fileSize(file.getSize())
                    .fileType(file.getContentType())
                    .admin(admin)
                    .build();

            return documentRepository.save(document);
            
        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new CustomException("Failed to store file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Updates an existing document's metadata (not the file itself).
     * @param id The document ID
     * @param title The new title
     * @return The updated document
     * @throws CustomException if document is not found
     */
    @Transactional
    public Document updateDocumentMetadata(Long id, String title) {
        ValidationUtils.validateNotEmpty(title, "Document title");
        
        Document document = findById(id);
        document.setTitle(title);

        return documentRepository.save(document);
    }

    /**
     * Replaces the file for an existing document.
     * @param id The document ID
     * @param newFile The new file
     * @return The updated document
     * @throws CustomException if document is not found or file validation fails
     */
    @Transactional
    public Document replaceFile(Long id, MultipartFile newFile) {
        validateFile(newFile);
        
        Document document = findById(id);
        
        try {
            // Delete old file if it exists
            deleteFileFromStorage(document.getFilePath());
            
            // Save new file
            String newFilePath = saveFileToStorage(newFile);
            
            // Update document entity
            document.setFileName(newFile.getOriginalFilename());
            document.setFilePath(newFilePath);
            document.setFileSize(newFile.getSize());
            document.setFileType(newFile.getContentType());

            return documentRepository.save(document);
            
        } catch (IOException e) {
            log.error("Failed to replace file", e);
            throw new CustomException("Failed to replace file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes a document by ID.
     * @param id The document ID to delete
     * @throws CustomException if document is not found
     */
    @Transactional
    public void delete(Long id) {
        Document document = findById(id);
        
        // Delete file from disk
        try {
            deleteFileFromStorage(document.getFilePath());
        } catch (IOException e) {
            log.warn("Could not delete file: {}", document.getFilePath(), e);
        }
        
        log.info("Deleting document with ID: {}", id);
        documentRepository.deleteById(id);
    }
    
    /**
     * Saves a file to the storage system.
     * @param file The file to save
     * @return The path where the file is saved
     * @throws IOException if file saving fails
     */
    private String saveFileToStorage(MultipartFile file) throws IOException {
        // Create directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        
        // Save file to disk
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        return filePath.toString();
    }
    
    /**
     * Deletes a file from the storage system.
     * @param filePath The path of the file to delete
     * @throws IOException if file deletion fails
     */
    private void deleteFileFromStorage(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.deleteIfExists(path);
    }

    
    /**
     * Validates a file for upload.
     * @param file The file to validate
     * @throws CustomException if validation fails
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException("Cannot upload empty file", HttpStatus.BAD_REQUEST);
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new CustomException("File size exceeds maximum allowed (10MB)", HttpStatus.BAD_REQUEST);
        }
        
        String contentType = file.getContentType();
        boolean isAllowed = false;
        
        if (contentType != null) {
            for (String allowedType : ALLOWED_FILE_TYPES) {
                if (contentType.equals(allowedType)) {
                    isAllowed = true;
                    break;
                }
            }
        }
        
        if (!isAllowed) {
            throw new CustomException("File type not allowed. Please upload PDF or Word documents", HttpStatus.BAD_REQUEST);
        }
    }
    
    /**
     * Validates document upload parameters.
     * @param title The document title
     * @param year The document year
     * @param file The uploaded file
     * @throws CustomException if validation fails
     */
    private void validateDocumentUpload(String title, Integer year, MultipartFile file) {
        ValidationUtils.validateNotEmpty(title, "Document title");
        ValidationUtils.validateYear(year);
        validateFile(file);
        
        if (documentRepository.existsByYear(year)) {
            throw new CustomException("A document for year " + year + " already exists", HttpStatus.CONFLICT);
        }
    }
} 