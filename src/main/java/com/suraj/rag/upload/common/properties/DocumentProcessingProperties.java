package com.suraj.rag.upload.common.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.document-processing")
public class DocumentProcessingProperties {

    private boolean enabled = true;

    @NotBlank private String baseUrl = "http://localhost:8081";

    private String language = "en";
}
