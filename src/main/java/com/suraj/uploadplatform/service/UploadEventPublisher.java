package com.suraj.uploadplatform.service;

import com.suraj.uploadplatform.infrastructure.opensearch.document.FileDocument;

public interface UploadEventPublisher {

    void publishUploadCompleted(FileDocument document);
}

