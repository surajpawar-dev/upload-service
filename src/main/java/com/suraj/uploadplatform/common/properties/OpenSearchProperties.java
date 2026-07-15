package com.suraj.uploadplatform.common.properties;

import com.suraj.uploadplatform.common.constants.ApplicationConstants;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = ApplicationConstants.OpenSearch.PROPERTY_PREFIX)
@Getter
@Setter
@Validated
public class OpenSearchProperties {

    private boolean enabled = true;

    private String host = ApplicationConstants.OpenSearch.DEFAULT_HOST;

    private int port = ApplicationConstants.OpenSearch.DEFAULT_PORT;

    private String scheme = ApplicationConstants.OpenSearch.DEFAULT_SCHEME;

    private String username;

    private String password;
}
