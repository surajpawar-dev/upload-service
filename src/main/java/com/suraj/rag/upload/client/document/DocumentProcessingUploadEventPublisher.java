package com.suraj.rag.upload.client.document;

import com.suraj.rag.upload.common.properties.DocumentProcessingProperties;
import com.suraj.rag.upload.common.properties.S3Properties;
import com.suraj.rag.upload.infrastructure.opensearch.document.FileDocument;
import com.suraj.rag.upload.service.UploadEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@ConditionalOnProperty(
        prefix = "app.document-processing",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class DocumentProcessingUploadEventPublisher implements UploadEventPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(DocumentProcessingUploadEventPublisher.class);

    private final RestTemplate restTemplate;
    private final DocumentProcessingProperties properties;
    private final S3Properties s3Properties;

    public DocumentProcessingUploadEventPublisher(
            RestTemplateBuilder builder,
            DocumentProcessingProperties properties,
            S3Properties s3Properties) {
        this.restTemplate = builder.rootUri(properties.getBaseUrl()).build();
        this.properties = properties;
        this.s3Properties = s3Properties;
    }

    @Override
    @Retryable(
            retryFor = RestClientException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0))
    public void publishUploadCompleted(FileDocument document) {
        ProcessDocumentRequest request =
                new ProcessDocumentRequest(
                        document.getFileName(),
                        s3Properties.getS3().getBucketName(),
                        document.getS3Key(),
                        document.getChecksum(),
                        properties.getLanguage());
        restTemplate.postForObject("/documents/process", request, Object.class);
        log.info(
                "Submitted uploaded PDF to rag-document-processing-service fileId={} s3Key={}",
                document.getFileId(),
                document.getS3Key());
    }
}
