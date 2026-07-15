package com.suraj.uploadplatform.infrastructure.storage;

import com.suraj.uploadplatform.dto.CompletedPartRequest;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ObjectStorageService {

    String buildObjectKey(String fileId, String fileName);

    URL presignPutObject(String objectKey, String contentType);

    void uploadThroughBackend(String objectKey, String contentType, MultipartFile file);

    String initiateMultipartUpload(String objectKey, String contentType);

    URL presignUploadPart(String objectKey, String uploadId, int partNumber);

    void completeMultipartUpload(
            String objectKey, String uploadId, List<CompletedPartRequest> partRequests);

    boolean objectExists(String objectKey);

    Instant presignedUrlExpiresAt();
}
