package com.suraj.uploadplatform.infrastructure.opensearch.document;

import com.suraj.uploadplatform.common.enums.UploadStatus;
import com.suraj.uploadplatform.common.enums.UploadStrategy;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "files", createIndex = false)
public class FileDocument {

    @Id
    @Field(type = FieldType.Keyword)
    private String fileId;

    @Field(type = FieldType.Keyword)
    private String fileName;

    @Field(type = FieldType.Keyword)
    private String bookId;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Keyword)
    private String s3Key;

    @Field(type = FieldType.Keyword)
    private String checksum;

    @Field(type = FieldType.Keyword)
    private String uploadId;

    @Field(type = FieldType.Keyword)
    private String contentType;

    @Field(type = FieldType.Long)
    private Long size;

    @Field(type = FieldType.Keyword)
    private UploadStatus status;

    @Field(type = FieldType.Keyword)
    private UploadStrategy strategy;

    @Field(type = FieldType.Keyword)
    private String uploadedBy;

    @Field(type = FieldType.Keyword)
    private String idempotencyKey;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant updatedAt;
}


