package com.suraj.rag.upload.service;

import com.suraj.rag.upload.infrastructure.opensearch.document.FileDocument;

public interface UploadEventPublisher {

    void publishUploadCompleted(FileDocument document);
}
