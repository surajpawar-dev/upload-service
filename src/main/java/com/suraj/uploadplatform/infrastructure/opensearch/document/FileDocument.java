package com.suraj.uploadplatform.infrastructure.opensearch.document;

import com.suraj.uploadplatform.common.enums.UploadStatus;
import com.suraj.uploadplatform.common.enums.UploadStrategy;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDocument {

    private String fileId;

    private String fileName;

    private String bookId;

    private String title;

    private String s3Key;

    private String uploadId;

    private String contentType;

    private Long size;

    private UploadStatus status;

    private UploadStrategy strategy;

    private String uploadedBy;

    private String idempotencyKey;

    private Instant createdAt;

    private Instant updatedAt;
}
