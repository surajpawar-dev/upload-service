package com.suraj.uploadplatform.dto;

import com.suraj.uploadplatform.common.enums.UploadStatus;
import com.suraj.uploadplatform.common.enums.UploadStrategy;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "Upload initiation result")
@Builder
@Getter
public class InitiateUploadResponse {

    @Schema(description = "Generated file id")
    private String fileId;

    @Schema(description = "Business book id associated with this PDF")
    private String bookId;

    @Schema(description = "Document title")
    private String title;

    @Schema(description = "Private S3 object key")
    private String s3Key;

    @Schema(description = "Current upload status")
    private UploadStatus status;

    @Schema(description = "Selected upload strategy")
    private UploadStrategy strategy;

    @Schema(description = "Presigned PUT URL for direct S3 upload")
    private String uploadUrl;

    @Schema(description = "S3 multipart upload id")
    private String uploadId;

    @Schema(description = "Presigned URLs for multipart upload parts")
    private List<PartUploadUrlResponse> partUploadUrls;
}
