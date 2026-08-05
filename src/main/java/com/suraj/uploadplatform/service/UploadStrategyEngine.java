package com.suraj.uploadplatform.service;

import com.suraj.uploadplatform.common.enums.UploadStrategy;
import com.suraj.uploadplatform.common.properties.UploadProperties;
import org.springframework.stereotype.Component;

@Component
public class UploadStrategyEngine {

    private final UploadProperties properties;

    public UploadStrategyEngine(UploadProperties properties) {
        this.properties = properties;
    }

    public UploadStrategy determine(long fileSize) {
        if (fileSize <= properties.getSmallFileMaxBytes()) {
            return UploadStrategy.BACKEND;
        }
        if (fileSize < properties.getMultipartMinBytes()) {
            return UploadStrategy.DIRECT_S3;
        }
        return UploadStrategy.MULTIPART;
    }

    public int calculatePartCount(long fileSize) {
        return (int) Math.ceil((double) fileSize / properties.getMultipartPartSizeBytes());
    }
}

