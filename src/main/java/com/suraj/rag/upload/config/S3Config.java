package com.suraj.rag.upload.config;

import com.suraj.rag.upload.common.constants.ApplicationConstants;
import com.suraj.rag.upload.common.properties.S3Properties;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@ConditionalOnProperty(
        prefix = ApplicationConstants.Aws.S3_PROPERTY_PREFIX,
        name = ApplicationConstants.Property.ENABLED,
        havingValue = ApplicationConstants.TRUE,
        matchIfMissing = true)
public class S3Config {

    private static final Logger log = LoggerFactory.getLogger(S3Config.class);

    @Bean
    public S3Client s3Client(S3Properties properties) {
        log.info(
                "Configuring S3 client region={} bucket={} endpointConfigured={}",
                properties.getRegion(),
                properties.getS3().getBucketName(),
                properties.getS3().getEndpoint() != null
                        && !properties.getS3().getEndpoint().isBlank());

        var builder =
                S3Client.builder()
                        .region(Region.of(properties.getRegion()))
                        .credentialsProvider(DefaultCredentialsProvider.builder().build())
                        .serviceConfiguration(
                                S3Configuration.builder()
                                        .pathStyleAccessEnabled(
                                                properties.getS3().isPathStyleAccessEnabled())
                                        .build());
        if (properties.getS3().getEndpoint() != null
                && !properties.getS3().getEndpoint().isBlank()) {
            builder.endpointOverride(URI.create(properties.getS3().getEndpoint()));
        }
        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner(S3Properties properties) {
        var builder =
                S3Presigner.builder()
                        .region(Region.of(properties.getRegion()))
                        .credentialsProvider(DefaultCredentialsProvider.builder().build());
        if (properties.getS3().getEndpoint() != null
                && !properties.getS3().getEndpoint().isBlank()) {
            builder.endpointOverride(URI.create(properties.getS3().getEndpoint()));
        }
        return builder.build();
    }
}
