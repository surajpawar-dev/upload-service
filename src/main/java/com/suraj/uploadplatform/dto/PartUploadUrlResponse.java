package com.suraj.uploadplatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "Multipart part upload URL")
@Builder
@Getter
public class PartUploadUrlResponse {

    @Schema(description = "S3 multipart part number")
    private int partNumber;

    @Schema(description = "Presigned URL for uploading the part")
    private String uploadUrl;
}
