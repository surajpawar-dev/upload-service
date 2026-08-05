package com.suraj.uploadplatform.infrastructure.opensearch.service.repo;

import com.suraj.uploadplatform.infrastructure.opensearch.document.FileDocument;
import java.util.Optional;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface IFileMetadataRepository
        extends ElasticsearchRepository<FileDocument, String>, FileMetadataRepository {

    @Override
    Optional<FileDocument> findByIdempotencyKey(String idempotencyKey);
}

