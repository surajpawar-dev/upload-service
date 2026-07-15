package com.suraj.uploadplatform.common.properties;

import com.suraj.uploadplatform.common.constants.ApplicationConstants;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = ApplicationConstants.Property.CLOUDWATCH_LOGGING_PREFIX)
@Getter
@Setter
@Validated
public class CloudWatchLoggingProperties {

    private String region = ApplicationConstants.Aws.REGION_DEFAULT;

    private String logGroup = ApplicationConstants.Logging.DEFAULT_CLOUDWATCH_LOG_GROUP;

    private String logStream = ApplicationConstants.Logging.DEFAULT_CLOUDWATCH_LOG_STREAM;
}
