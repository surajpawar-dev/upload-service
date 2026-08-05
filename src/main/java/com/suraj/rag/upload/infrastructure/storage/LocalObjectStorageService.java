package com.suraj.rag.upload.infrastructure.storage;

import com.suraj.rag.upload.common.constants.ApplicationConstants;
import com.suraj.rag.upload.common.properties.UploadProperties;
import com.suraj.rag.upload.dto.CompletedPartRequest;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@ConditionalOnProperty(
        prefix = ApplicationConstants.Aws.S3_PROPERTY_PREFIX,
        name = ApplicationConstants.Property.ENABLED,
        havingValue = "false")
public class LocalObjectStorageService implements ObjectStorageService {

    private final Path storageDirectory;
    private final UploadProperties uploadProperties;

    public LocalObjectStorageService(
            @Value("${storage.local.directory:./data/uploads}") Path storageDirectory,
            UploadProperties uploadProperties) {
        this.storageDirectory = storageDirectory.normalize();
        this.uploadProperties = uploadProperties;
    }

    @Override
    public String buildObjectKey(String fileId, String fileName) {
        return fileId + ApplicationConstants.FORWARD_SLASH + normalizeFileName(fileName);
    }

    @Override
    public URL presignPutObject(String objectKey, String contentType) {
        return localUrl(objectKey);
    }

    @Override
    public void uploadThroughBackend(String objectKey, String contentType, MultipartFile file) {
        Path destination = resolveObjectPath(objectKey);
        try {
            Files.createDirectories(destination.getParent());
            file.transferTo(destination);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public String initiateMultipartUpload(String objectKey, String contentType) {
        return "local-" + Instant.now().toEpochMilli();
    }

    @Override
    public URL presignUploadPart(String objectKey, String uploadId, int partNumber) {
        return localUrl(objectKey + ".part-" + partNumber);
    }

    @Override
    public void completeMultipartUpload(
            String objectKey, String uploadId, List<CompletedPartRequest> partRequests) {
        throw new UnsupportedOperationException(
                "Multipart upload requires S3. Set AWS_S3_ENABLED=true for multipart uploads.");
    }

    @Override
    public boolean objectExists(String objectKey) {
        return Files.isRegularFile(resolveObjectPath(objectKey));
    }

    @Override
    public Instant presignedUrlExpiresAt() {
        return Instant.now().plus(Duration.ofMinutes(uploadProperties.getPresignedUrlExpirationMinutes()));
    }

    private Path resolveObjectPath(String objectKey) {
        Path resolved = storageDirectory.resolve(objectKey).normalize();
        if (!resolved.startsWith(storageDirectory)) {
            throw new IllegalArgumentException("Invalid object key");
        }
        return resolved;
    }

    private URL localUrl(String objectKey) {
        try {
            return URI.create("file:///" + resolveObjectPath(objectKey).toString().replace("\\", "/"))
                    .toURL();
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("Invalid local object URL", ex);
        }
    }

    private String normalizeFileName(String fileName) {
        String normalized = Normalizer.normalize(fileName, Normalizer.Form.NFKD);
        return normalized.replaceAll("[^a-zA-Z0-9._-]", "-").toLowerCase(Locale.ROOT);
    }
}
