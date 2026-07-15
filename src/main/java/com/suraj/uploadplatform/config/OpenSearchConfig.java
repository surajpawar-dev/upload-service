package com.suraj.uploadplatform.config;

import com.suraj.uploadplatform.common.constants.ApplicationConstants;
import com.suraj.uploadplatform.common.properties.OpenSearchProperties;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.core5.http.HttpHost;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnProperty(
        prefix = ApplicationConstants.OpenSearch.PROPERTY_PREFIX,
        name = ApplicationConstants.Property.ENABLED,
        havingValue = ApplicationConstants.TRUE,
        matchIfMissing = true)
public class OpenSearchConfig {

    @Bean
    public OpenSearchClient openSearchClient(OpenSearchProperties properties) {
        HttpHost host =
                new HttpHost(properties.getScheme(), properties.getHost(), properties.getPort());

        ApacheHttpClient5TransportBuilder builder = ApacheHttpClient5TransportBuilder.builder(host);

        if (StringUtils.hasText(properties.getUsername())) {
            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();

            credentialsProvider.setCredentials(
                    new AuthScope(properties.getHost(), properties.getPort()),
                    new UsernamePasswordCredentials(
                            properties.getUsername(), password(properties).toCharArray()));

            builder.setHttpClientConfigCallback(
                    httpClientBuilder ->
                            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        }

        OpenSearchTransport transport = builder.setMapper(new JacksonJsonpMapper()).build();

        return new OpenSearchClient(transport);
    }

    private String password(OpenSearchProperties properties) {
        return properties.getPassword() == null
                ? ApplicationConstants.EMPTY_STRING
                : properties.getPassword();
    }
}
