package com.suraj.uploadplatform.infrastructure.opensearch.service.repo;

import com.suraj.uploadplatform.infrastructure.opensearch.document.FileDocument;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "opensearch", name = "enabled", havingValue = "false")
public class InMemoryFileMetadataRepository implements FileMetadataRepository {

    private final Map<String, FileDocument> documentsById = new ConcurrentHashMap<>();
    private final Map<String, String> fileIdsByIdempotencyKey = new ConcurrentHashMap<>();

    @Override
    public Optional<FileDocument> findById(String fileId) {
        return Optional.ofNullable(documentsById.get(fileId));
    }

    @Override
    public Optional<FileDocument> findByIdempotencyKey(String idempotencyKey) {
        return Optional.ofNullable(fileIdsByIdempotencyKey.get(idempotencyKey))
                .flatMap(this::findById);
    }

    @Override
    public FileDocument save(FileDocument document) {
        documentsById.put(document.getFileId(), document);
        fileIdsByIdempotencyKey.put(document.getIdempotencyKey(), document.getFileId());
        return document;
    }
}
