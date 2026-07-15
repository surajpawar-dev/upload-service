package com.suraj.uploadplatform.service;

import com.suraj.uploadplatform.common.constants.ApplicationConstants;
import com.suraj.uploadplatform.common.enums.UploadStatus;
import com.suraj.uploadplatform.common.enums.UploadStrategy;
import com.suraj.uploadplatform.common.properties.UploadProperties;
import com.suraj.uploadplatform.dto.*;
import com.suraj.uploadplatform.exception.InvalidUploadRequestException;
import com.suraj.uploadplatform.exception.ResourceNotFoundException;
import com.suraj.uploadplatform.exception.UploadStateException;
import com.suraj.uploadplatform.infrastructure.opensearch.document.FileDocument;
import com.suraj.uploadplatform.infrastructure.opensearch.service.repo.FileMetadataRepository;
import com.suraj.uploadplatform.infrastructure.storage.ObjectStorageService;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileUploadService {

    private static final Logger log = LoggerFactory.getLogger(FileUploadService.class);

    private final UploadStrategyEngine strategyEngine;
    private final ObjectStorageService objectStorageService;
    private final FileMetadataRepository fileMetadataRepository;
    private final UploadEventPublisher uploadEventPublisher;
    private final UploadProperties uploadProperties;

    public FileUploadService(
            UploadStrategyEngine strategyEngine,
            ObjectStorageService objectStorageService,
            FileMetadataRepository fileMetadataRepository,
            UploadEventPublisher uploadEventPublisher,
            UploadProperties uploadProperties) {
        this.strategyEngine = strategyEngine;
        this.objectStorageService = objectStorageService;
        this.fileMetadataRepository = fileMetadataRepository;
        this.uploadEventPublisher = uploadEventPublisher;
        this.uploadProperties = uploadProperties;
    }

    public InitiateUploadResponse initiateUpload(InitiateUploadRequest request) {
        validateInitiateRequest(request);
        FileDocument existingDocument =
                fileMetadataRepository
                        .findByIdempotencyKey(request.getIdempotencyKey())
                        .orElse(null);
        if (existingDocument != null) {
            if (!matchesExistingUpload(request, existingDocument)) {
                throw new UploadStateException(
                        ApplicationConstants.ErrorMessage.IDEMPOTENCY_KEY_CONFLICT);
            }
            log.info(
                    "Idempotent upload initiate replayed fileId={} idempotencyKey={}",
                    existingDocument.getFileId(),
                    request.getIdempotencyKey());
            return buildInitiateResponse(existingDocument, existingDocument.getStrategy());
        }

        String fileId = UUID.randomUUID().toString();
        UploadStrategy strategy = strategyEngine.determine(request.getSize());
        String s3Key = objectStorageService.buildObjectKey(fileId, request.getFileName());
        Instant now = Instant.now();

        FileDocument document = new FileDocument();
        document.setFileId(fileId);
        document.setBookId(request.getBookId());
        document.setTitle(request.getTitle());
        document.setFileName(request.getFileName());
        document.setS3Key(s3Key);
        document.setContentType(request.getContentType());
        document.setSize(request.getSize());
        document.setUploadedBy(request.getUploadedBy());
        document.setIdempotencyKey(request.getIdempotencyKey());
        document.setStatus(UploadStatus.PENDING);
        document.setStrategy(strategy);
        document.setCreatedAt(now);
        document.setUpdatedAt(now);

        log.info(
                "Upload initiated fileId={} strategy={} size={} contentType={}",
                fileId,
                strategy,
                request.getSize(),
                request.getContentType());

        InitiateUploadResponse response = buildInitiateResponse(document, strategy);
        fileMetadataRepository.save(document);
        return response;
    }

    public FileMetadataResponse uploadThroughBackend(String fileId, MultipartFile file) {
        FileDocument document = findDocument(fileId);
        if (document.getStatus() == UploadStatus.UPLOADED) {
            return toResponse(document);
        }
        if (document.getStrategy() != UploadStrategy.BACKEND) {
            throw new UploadStateException(
                    ApplicationConstants.ErrorMessage.BACKEND_UPLOAD_NOT_ALLOWED);
        }
        validateBackendFile(document, file);

        document.setStatus(UploadStatus.UPLOADING);
        document.setUpdatedAt(Instant.now());
        fileMetadataRepository.save(document);

        log.info("Backend upload started fileId={} size={}", fileId, file.getSize());
        try {
            objectStorageService.uploadThroughBackend(
                    document.getS3Key(), document.getContentType(), file);
            return markUploaded(document);
        } catch (RuntimeException ex) {
            markFailed(document);
            throw ex;
        }
    }

    public FileMetadataResponse completeUpload(String fileId) {
        FileDocument document = findDocument(fileId);
        if (document.getStatus() == UploadStatus.UPLOADED) {
            return toResponse(document);
        }
        if (document.getStrategy() == UploadStrategy.BACKEND) {
            throw new UploadStateException(
                    ApplicationConstants.ErrorMessage.BACKEND_UPLOAD_REQUIRED);
        }
        if (document.getStrategy() == UploadStrategy.MULTIPART) {
            throw new UploadStateException(
                    ApplicationConstants.ErrorMessage.MULTIPART_UPLOAD_REQUIRED);
        }
        verifyObjectExists(document);
        return markUploaded(document);
    }

    public FileMetadataResponse completeMultipartUpload(
            String fileId, CompleteMultipartUploadRequest request) {
        FileDocument document = findDocument(fileId);
        if (document.getStatus() == UploadStatus.UPLOADED) {
            return toResponse(document);
        }
        if (document.getStrategy() != UploadStrategy.MULTIPART) {
            throw new UploadStateException(
                    ApplicationConstants.ErrorMessage.MULTIPART_UPLOAD_NOT_ALLOWED);
        }

        try {
            objectStorageService.completeMultipartUpload(
                    document.getS3Key(), document.getUploadId(), request.getParts());
            verifyObjectExists(document);
            log.info(
                    "Multipart upload completed fileId={} partCount={}",
                    fileId,
                    request.getParts().size());
            return markUploaded(document);
        } catch (RuntimeException ex) {
            markFailed(document);
            throw ex;
        }
    }

    public FileMetadataResponse getMetadata(String fileId) {
        return toResponse(findDocument(fileId));
    }

    private InitiateUploadResponse buildInitiateResponse(
            FileDocument document, UploadStrategy strategy) {
        if (document.getStatus() == UploadStatus.UPLOADED) {
            return baseResponse(document).build();
        }
        return switch (strategy) {
            case BACKEND -> baseResponse(document).build();
            case DIRECT_S3 -> {
                URL uploadUrl =
                        objectStorageService.presignPutObject(
                                document.getS3Key(), document.getContentType());
                yield baseResponse(document).uploadUrl(uploadUrl.toString()).build();
            }
            case MULTIPART -> {
                String multipartUploadId = document.getUploadId();
                if (multipartUploadId == null || multipartUploadId.isBlank()) {
                    multipartUploadId =
                            objectStorageService.initiateMultipartUpload(
                                    document.getS3Key(), document.getContentType());
                    document.setUploadId(multipartUploadId);
                }
                final String uploadId = multipartUploadId;
                List<PartUploadUrlResponse> partUploadUrls =
                        IntStream.rangeClosed(
                                        1, strategyEngine.calculatePartCount(document.getSize()))
                                .mapToObj(
                                        partNumber ->
                                                PartUploadUrlResponse.builder()
                                                        .partNumber(partNumber)
                                                        .uploadUrl(
                                                                objectStorageService
                                                                        .presignUploadPart(
                                                                                document.getS3Key(),
                                                                                uploadId,
                                                                                partNumber)
                                                                        .toString())
                                                        .build())
                                .toList();

                yield baseResponse(document)
                        .uploadId(uploadId)
                        .partUploadUrls(partUploadUrls)
                        .build();
            }
        };
    }

    private InitiateUploadResponse.InitiateUploadResponseBuilder baseResponse(
            FileDocument document) {
        return InitiateUploadResponse.builder()
                .fileId(document.getFileId())
                .bookId(document.getBookId())
                .title(document.getTitle())
                .s3Key(document.getS3Key())
                .status(document.getStatus())
                .strategy(document.getStrategy());
    }

    private FileDocument findDocument(String fileId) {
        return fileMetadataRepository
                .findById(fileId)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        ApplicationConstants.ErrorMessage.FILE_NOT_FOUND));
    }

    private void verifyObjectExists(FileDocument document) {
        if (!objectStorageService.objectExists(document.getS3Key())) {
            document.setStatus(UploadStatus.FAILED);
            document.setUpdatedAt(Instant.now());
            fileMetadataRepository.save(document);
            throw new ResourceNotFoundException(
                    ApplicationConstants.ErrorMessage.S3_OBJECT_NOT_FOUND);
        }
    }

    private void validateInitiateRequest(InitiateUploadRequest request) {
        if (!ApplicationConstants.Upload.PDF_CONTENT_TYPE.equalsIgnoreCase(request.getContentType())
                || !request.getFileName()
                        .toLowerCase(Locale.ROOT)
                        .endsWith(ApplicationConstants.Upload.PDF_EXTENSION)) {
            throw new InvalidUploadRequestException(
                    ApplicationConstants.ErrorMessage.ONLY_PDF_ALLOWED);
        }
        if (request.getSize() > uploadProperties.getMaxFileSizeBytes()) {
            throw new InvalidUploadRequestException(
                    ApplicationConstants.ErrorMessage.FILE_SIZE_EXCEEDED);
        }
    }

    private void validateBackendFile(FileDocument document, MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null
                || !ApplicationConstants.Upload.PDF_CONTENT_TYPE.equalsIgnoreCase(contentType)
                || file.isEmpty()
                || file.getSize() > uploadProperties.getMaxFileSizeBytes()
                || !isPdfFileName(file.getOriginalFilename())
                || !hasPdfHeader(file)) {
            throw new InvalidUploadRequestException(
                    ApplicationConstants.ErrorMessage.BACKEND_FILE_METADATA_MISMATCH);
        }
    }

    private boolean isPdfFileName(String fileName) {
        return fileName != null
                && fileName.toLowerCase(Locale.ROOT).endsWith(ApplicationConstants.Upload.PDF_EXTENSION);
    }

    private boolean hasPdfHeader(MultipartFile file) {
        byte[] expectedHeader = "%PDF-".getBytes(StandardCharsets.US_ASCII);
        byte[] actualHeader = new byte[expectedHeader.length];
        try (InputStream inputStream = file.getInputStream()) {
            int bytesRead = inputStream.read(actualHeader);
            return bytesRead == expectedHeader.length && Arrays.equals(expectedHeader, actualHeader);
        } catch (IOException ex) {
            throw new InvalidUploadRequestException(
                    ApplicationConstants.ErrorMessage.BACKEND_FILE_METADATA_MISMATCH);
        }
    }

    private boolean matchesExistingUpload(InitiateUploadRequest request, FileDocument document) {
        return document.getBookId().equals(request.getBookId())
                && document.getTitle().equals(request.getTitle())
                && document.getFileName().equals(request.getFileName())
                && document.getContentType().equals(request.getContentType())
                && document.getSize().equals(request.getSize())
                && document.getUploadedBy().equals(request.getUploadedBy());
    }

    private FileMetadataResponse markUploaded(FileDocument document) {
        document.setStatus(UploadStatus.UPLOADED);
        document.setUpdatedAt(Instant.now());
        fileMetadataRepository.save(document);
        uploadEventPublisher.publishUploadCompleted(document);
        log.info("Upload completed fileId={}", document.getFileId());
        return toResponse(document);
    }

    private void markFailed(FileDocument document) {
        document.setStatus(UploadStatus.FAILED);
        document.setUpdatedAt(Instant.now());
        fileMetadataRepository.save(document);
        log.warn("Upload failed fileId={}", document.getFileId());
    }

    private FileMetadataResponse toResponse(FileDocument document) {
        return FileMetadataResponse.builder()
                .fileId(document.getFileId())
                .fileName(document.getFileName())
                .bookId(document.getBookId())
                .title(document.getTitle())
                .s3Key(document.getS3Key())
                .contentType(document.getContentType())
                .size(document.getSize())
                .status(document.getStatus())
                .strategy(document.getStrategy())
                .uploadedBy(document.getUploadedBy())
                .idempotencyKey(document.getIdempotencyKey())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }
}
