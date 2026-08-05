package com.suraj.uploadplatform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI uploadPlatformOpenApi() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Upload Platform API")
                                .version("v1")
                                .description(
                                        "Intelligent file upload platform with backend, direct S3 and multipart upload strategies.")
                                .contact(new Contact().name("Upload Platform Team"))
                                .license(new License().name("Private")));
    }
}

