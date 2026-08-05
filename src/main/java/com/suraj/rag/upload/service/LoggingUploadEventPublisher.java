package com.suraj.rag.upload.service;

import com.suraj.rag.upload.infrastructure.opensearch.document.FileDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.document-processing", name = "enabled", havingValue = "false")
public class LoggingUploadEventPublisher implements UploadEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingUploadEventPublisher.class);

    @Override
    public void publishUploadCompleted(FileDocument document) {
        log.info(
                "Upload lifecycle event published eventType=UPLOAD_COMPLETED fileId={} bookId={} s3Key={}",
                document.getFileId(),
                document.getBookId(),
                document.getS3Key());
    }
}
