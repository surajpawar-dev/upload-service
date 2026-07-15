package com.suraj.uploadplatform.infrastructure.opensearch.service.repo;

import com.suraj.uploadplatform.infrastructure.opensearch.document.FileDocument;
import java.util.Optional;

public interface FileMetadataRepository {

    Optional<FileDocument> findById(String fileId);

    Optional<FileDocument> findByIdempotencyKey(String idempotencyKey);

    FileDocument save(FileDocument document);
}
