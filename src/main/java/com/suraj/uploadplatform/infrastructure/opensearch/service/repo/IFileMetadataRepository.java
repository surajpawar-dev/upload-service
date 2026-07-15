package com.suraj.uploadplatform.infrastructure.opensearch.service.repo;

import com.suraj.uploadplatform.infrastructure.opensearch.document.FileDocument;
import java.util.Optional;

public interface IFileMetadataRepository {

    void save(FileDocument document);

    Optional<FileDocument> findById(String fileId);

    Optional<FileDocument> findByIdempotencyKey(String idempotencyKey);

    void update(FileDocument document);
}
