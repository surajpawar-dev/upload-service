package com.suraj.rag.upload.client.document;

public record ProcessDocumentRequest(
        String fileName, String s3Bucket, String s3Key, String checksum, String language) {}
