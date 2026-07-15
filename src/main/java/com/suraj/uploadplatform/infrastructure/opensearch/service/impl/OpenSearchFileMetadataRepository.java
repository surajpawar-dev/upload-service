package com.suraj.uploadplatform.infrastructure.opensearch.service.impl;

import com.suraj.uploadplatform.common.constants.ApplicationConstants;
import com.suraj.uploadplatform.infrastructure.opensearch.document.FileDocument;
import com.suraj.uploadplatform.infrastructure.opensearch.service.repo.IFileMetadataRepository;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.Optional;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.GetResponse;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnBean(OpenSearchClient.class)
public class OpenSearchFileMetadataRepository implements IFileMetadataRepository {

    private final OpenSearchClient client;

    public OpenSearchFileMetadataRepository(OpenSearchClient client) {
        this.client = client;
    }

    @Override
    public void save(FileDocument document) {
        Instant now = Instant.now();
        if (document.getCreatedAt() == null) {
            document.setCreatedAt(now);
        }
        document.setUpdatedAt(now);

        try {
            IndexRequest<FileDocument> request =
                    IndexRequest.of(
                            i ->
                                    i.index(ApplicationConstants.OpenSearch.FILES_INDEX)
                                            .id(document.getFileId())
                                            .document(document));

            client.index(request);
        } catch (IOException ex) {
            throw new UncheckedIOException(
                    ApplicationConstants.ErrorMessage.FILE_METADATA_SAVE_FAILED, ex);
        }
    }

    @Override
    public Optional<FileDocument> findById(String fileId) {
        try {
            GetResponse<FileDocument> response =
                    client.get(
                            g -> g.index(ApplicationConstants.OpenSearch.FILES_INDEX).id(fileId),
                            FileDocument.class);

            if (!response.found()) {
                return Optional.empty();
            }

            return Optional.ofNullable(response.source());
        } catch (IOException ex) {
            throw new UncheckedIOException(
                    ApplicationConstants.ErrorMessage.FILE_METADATA_FETCH_FAILED, ex);
        }
    }

    @Override
    public Optional<FileDocument> findByIdempotencyKey(String idempotencyKey) {
        try {
            SearchResponse<FileDocument> response =
                    client.search(
                            s ->
                                    s.index(ApplicationConstants.OpenSearch.FILES_INDEX)
                                            .size(1)
                                            .query(
                                                    q ->
                                                            q.term(
                                                                    t ->
                                                                            t.field(
                                                                                            "idempotencyKey"
                                                                                                    + ".keyword")
                                                                                    .value(
                                                                                            v ->
                                                                                                    v
                                                                                                            .stringValue(
                                                                                                                    idempotencyKey)))),
                            FileDocument.class);

            return response.hits().hits().stream().findFirst().map(hit -> hit.source());
        } catch (IOException ex) {
            throw new UncheckedIOException(
                    ApplicationConstants.ErrorMessage.FILE_METADATA_FETCH_FAILED, ex);
        }
    }

    @Override
    public void update(FileDocument document) {
        save(document);
    }
}
