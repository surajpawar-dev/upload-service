package com.suraj.uploadplatform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        properties = {
            "aws.s3.enabled=false",
            "opensearch.enabled=false",
            "spring.data.elasticsearch.repositories.enabled=false"
        })
class UploadPlatformApplicationTests {

    @Test
    void contextLoads() {}
}
