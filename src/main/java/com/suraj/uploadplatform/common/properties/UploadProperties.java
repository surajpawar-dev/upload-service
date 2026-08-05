package com.suraj.uploadplatform.common.properties;

import com.suraj.uploadplatform.common.constants.ApplicationConstants;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = ApplicationConstants.Property.UPLOAD_PREFIX)
@Getter
@Setter
@Validated
public class UploadProperties {

    private long smallFileMaxBytes = ApplicationConstants.Upload.SMALL_FILE_MAX_BYTES;

    private long multipartMinBytes = ApplicationConstants.Upload.MULTIPART_MIN_BYTES;

    private long multipartPartSizeBytes = ApplicationConstants.Upload.MULTIPART_PART_SIZE_BYTES;

    private int presignedUrlExpirationMinutes =
            ApplicationConstants.Upload.PRESIGNED_URL_EXPIRATION_MINUTES;

    private long maxFileSizeBytes = ApplicationConstants.Upload.MULTIPART_MIN_BYTES * 10;
}

