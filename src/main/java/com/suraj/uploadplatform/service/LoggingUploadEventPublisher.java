package com.suraj.uploadplatform.service;

import com.suraj.uploadplatform.infrastructure.opensearch.document.FileDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
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
