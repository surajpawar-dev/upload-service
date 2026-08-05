package com.suraj.rag.upload.dto;

import com.suraj.rag.upload.common.enums.UploadStatus;
import com.suraj.rag.upload.common.enums.UploadStrategy;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "Stored file metadata")
@Builder
@Getter
public class FileMetadataResponse {

    private String fileId;

    private String fileName;

    private String bookId;

    private String title;

    private String s3Key;

    private String checksum;

    private String contentType;

    private Long size;

    private UploadStatus status;

    private UploadStrategy strategy;

    private String uploadedBy;

    private String idempotencyKey;

    private Instant createdAt;

    private Instant updatedAt;
}
