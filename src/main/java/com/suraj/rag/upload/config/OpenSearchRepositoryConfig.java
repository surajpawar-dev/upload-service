package com.suraj.rag.upload.config;

import com.suraj.rag.upload.infrastructure.opensearch.service.repo.IFileMetadataRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@ConditionalOnProperty(prefix = "opensearch", name = "enabled", havingValue = "true")
@EnableElasticsearchRepositories(basePackageClasses = IFileMetadataRepository.class)
public class OpenSearchRepositoryConfig {}
