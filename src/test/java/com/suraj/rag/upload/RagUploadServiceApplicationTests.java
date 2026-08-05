package com.suraj.rag.upload;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        properties = {
            "aws.s3.enabled=false",
            "opensearch.enabled=false",
            "spring.data.elasticsearch.repositories.enabled=false",
            "app.document-processing.enabled=false"
        })
class RagUploadServiceApplicationTests {

    @Test
    void contextLoads() {}
}
