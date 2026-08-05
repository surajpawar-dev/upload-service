package com.suraj.rag.upload.infrastructure.opensearch.service.repo;

import com.suraj.rag.upload.infrastructure.opensearch.document.FileDocument;
import java.util.Optional;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface IFileMetadataRepository
        extends ElasticsearchRepository<FileDocument, String>, FileMetadataRepository {

    @Override
    Optional<FileDocument> findByIdempotencyKey(String idempotencyKey);
}
