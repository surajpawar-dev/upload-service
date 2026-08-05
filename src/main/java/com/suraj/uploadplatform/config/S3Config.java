package com.suraj.uploadplatform.config;

import com.suraj.uploadplatform.common.constants.ApplicationConstants;
import com.suraj.uploadplatform.common.properties.S3Properties;
import java.net.URI;
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

    @Bean
    public S3Client s3Client(S3Properties properties) {
        System.out.println("Region = " + properties.getRegion());
        System.out.println("Bucket = " + properties.getS3().getBucketName());

        var builder = S3Client.builder()
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(properties.getS3().isPathStyleAccessEnabled())
                        .build());
        if (properties.getS3().getEndpoint() != null && !properties.getS3().getEndpoint().isBlank()) {
            builder.endpointOverride(URI.create(properties.getS3().getEndpoint()));
        }
        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner(S3Properties properties) {
        var builder = S3Presigner.builder()
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(DefaultCredentialsProvider.builder().build());
        if (properties.getS3().getEndpoint() != null && !properties.getS3().getEndpoint().isBlank()) {
            builder.endpointOverride(URI.create(properties.getS3().getEndpoint()));
        }
        return builder.build();
    }
}


