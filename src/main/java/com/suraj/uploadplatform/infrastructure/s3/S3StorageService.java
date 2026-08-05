package com.suraj.uploadplatform.infrastructure.s3;

import com.suraj.uploadplatform.common.constants.ApplicationConstants;
import com.suraj.uploadplatform.common.properties.S3Properties;
import com.suraj.uploadplatform.common.properties.UploadProperties;
import com.suraj.uploadplatform.dto.CompletedPartRequest;
import com.suraj.uploadplatform.infrastructure.storage.ObjectStorageService;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.text.Normalizer;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

@Service
@ConditionalOnProperty(
        prefix = ApplicationConstants.Aws.S3_PROPERTY_PREFIX,
        name = ApplicationConstants.Property.ENABLED,
        havingValue = ApplicationConstants.TRUE,
        matchIfMissing = true)
public class S3StorageService implements ObjectStorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;
    private final UploadProperties uploadProperties;

    public S3StorageService(
            S3Client s3Client,
            S3Presigner s3Presigner,
            S3Properties s3Properties,
            UploadProperties uploadProperties) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.s3Properties = s3Properties;
        this.uploadProperties = uploadProperties;
    }

    @Override
    public String buildObjectKey(String fileId, String fileName) {
        return fileId + ApplicationConstants.FORWARD_SLASH + normalizeFileName(fileName);
    }

    @Override
    public URL presignPutObject(String s3Key, String contentType) {
        PutObjectRequest putObjectRequest =
                PutObjectRequest.builder()
                        .bucket(bucketName())
                        .key(s3Key)
                        .contentType(contentType)
                        .build();

        PutObjectPresignRequest presignRequest =
                PutObjectPresignRequest.builder()
                        .signatureDuration(signatureDuration())
                        .putObjectRequest(putObjectRequest)
                        .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        return presignedRequest.url();
    }

    @Override
    public void uploadThroughBackend(String s3Key, String contentType, MultipartFile file) {
        PutObjectRequest request =
                PutObjectRequest.builder()
                        .bucket(bucketName())
                        .key(s3Key)
                        .contentType(contentType)
                        .contentLength(file.getSize())
                        .build();

        try {
            s3Client.putObject(
                    request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public String initiateMultipartUpload(String s3Key, String contentType) {
        CreateMultipartUploadRequest request =
                CreateMultipartUploadRequest.builder()
                        .bucket(bucketName())
                        .key(s3Key)
                        .contentType(contentType)
                        .build();
        return s3Client.createMultipartUpload(request).uploadId();
    }

    @Override
    public URL presignUploadPart(String s3Key, String uploadId, int partNumber) {
        UploadPartPresignRequest request =
                UploadPartPresignRequest.builder()
                        .signatureDuration(signatureDuration())
                        .uploadPartRequest(
                                uploadPartRequest ->
                                        uploadPartRequest
                                                .bucket(bucketName())
                                                .key(s3Key)
                                                .uploadId(uploadId)
                                                .partNumber(partNumber))
                        .build();

        PresignedUploadPartRequest presignedRequest = s3Presigner.presignUploadPart(request);
        return presignedRequest.url();
    }

    @Override
    public void completeMultipartUpload(
            String s3Key, String uploadId, List<CompletedPartRequest> partRequests) {
        List<CompletedPart> completedParts =
                partRequests.stream()
                        .sorted(Comparator.comparing(CompletedPartRequest::getPartNumber))
                        .map(
                                part ->
                                        CompletedPart.builder()
                                                .partNumber(part.getPartNumber())
                                                .eTag(part.getETag())
                                                .build())
                        .toList();

        CompleteMultipartUploadRequest request =
                CompleteMultipartUploadRequest.builder()
                        .bucket(bucketName())
                        .key(s3Key)
                        .uploadId(uploadId)
                        .multipartUpload(
                                CompletedMultipartUpload.builder().parts(completedParts).build())
                        .build();

        s3Client.completeMultipartUpload(request);
    }

    @Override
    public boolean objectExists(String s3Key) {
        try {
            s3Client.headObject(
                    HeadObjectRequest.builder().bucket(bucketName()).key(s3Key).build());
            return true;
        } catch (NoSuchKeyException ex) {
            return false;
        }
    }

    @Override
    public Instant presignedUrlExpiresAt() {
        return Instant.now().plus(signatureDuration());
    }

    private Duration signatureDuration() {
        return Duration.ofMinutes(uploadProperties.getPresignedUrlExpirationMinutes());
    }

    private String bucketName() {
        return s3Properties.getS3().getBucketName();
    }

    private String normalizeFileName(String fileName) {
        String normalized = Normalizer.normalize(fileName, Normalizer.Form.NFKD);
        return normalized.replaceAll("[^a-zA-Z0-9._-]", "-").toLowerCase(Locale.ROOT);
    }
}

