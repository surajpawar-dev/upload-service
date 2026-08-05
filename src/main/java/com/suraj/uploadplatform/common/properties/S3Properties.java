package com.suraj.uploadplatform.common.properties;

import com.suraj.uploadplatform.common.constants.ApplicationConstants;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = ApplicationConstants.Property.AWS_PREFIX)
@Getter
@Setter
@Validated
public class S3Properties {

    private String region = ApplicationConstants.Aws.REGION_DEFAULT;

    private S3 s3 = new S3();

    @Getter
    @Setter
    public static class S3 {

        private String bucketName;

        private String endpoint;

        private boolean pathStyleAccessEnabled;
    }
}


