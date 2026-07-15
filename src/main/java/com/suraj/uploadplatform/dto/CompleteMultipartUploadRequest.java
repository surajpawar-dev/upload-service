package com.suraj.uploadplatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Request used to complete an S3 multipart upload")
@Getter
@Setter
public class CompleteMultipartUploadRequest {

    @Schema(description = "Completed multipart upload parts returned by S3")
    @NotEmpty private List<@Valid CompletedPartRequest> parts;
}
