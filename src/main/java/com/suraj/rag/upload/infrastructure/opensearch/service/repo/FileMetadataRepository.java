package com.suraj.rag.upload.infrastructure.opensearch.service.repo;

import com.suraj.rag.upload.infrastructure.opensearch.document.FileDocument;
import java.util.Optional;

public interface FileMetadataRepository {

    Optional<FileDocument> findById(String fileId);

    Optional<FileDocument> findByIdempotencyKey(String idempotencyKey);

    FileDocument save(FileDocument document);
}
