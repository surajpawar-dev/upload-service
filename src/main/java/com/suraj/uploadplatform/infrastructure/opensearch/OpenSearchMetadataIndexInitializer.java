package com.suraj.uploadplatform.infrastructure.opensearch;

import com.suraj.uploadplatform.common.constants.ApplicationConstants;
import com.suraj.uploadplatform.infrastructure.opensearch.document.FileDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "opensearch", name = "enabled", havingValue = "true")
public class OpenSearchMetadataIndexInitializer implements ApplicationRunner {

    private static final Logger log =
            LoggerFactory.getLogger(OpenSearchMetadataIndexInitializer.class);

    private final ElasticsearchOperations operations;

    public OpenSearchMetadataIndexInitializer(ElasticsearchOperations operations) {
        this.operations = operations;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            ensureFilesIndex();
        } catch (RuntimeException ex) {
            log.warn(
                    "OpenSearch metadata index initialization skipped index={} reason={}",
                    ApplicationConstants.OpenSearch.FILES_INDEX,
                    ex.getMessage());
        }
    }

    private void ensureFilesIndex() {
        IndexOperations indexOperations = operations.indexOps(FileDocument.class);

        if (indexOperations.exists()) {
            log.info(
                    "OpenSearch metadata index already exists index={}",
                    ApplicationConstants.OpenSearch.FILES_INDEX);
            return;
        }

        indexOperations.create();
        indexOperations.putMapping(indexOperations.createMapping(FileDocument.class));
        log.info(
                "OpenSearch metadata index created index={}",
                ApplicationConstants.OpenSearch.FILES_INDEX);
    }
}

