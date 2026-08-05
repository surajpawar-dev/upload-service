package com.suraj.rag.upload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableRetry
public class RagUploadServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagUploadServiceApplication.class, args);
    }
}
