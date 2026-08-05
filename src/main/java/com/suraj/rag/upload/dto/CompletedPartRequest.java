package com.suraj.rag.upload.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Completed S3 multipart upload part")
@Getter
@Setter
public class CompletedPartRequest {

    @Schema(description = "Part number uploaded to S3", example = "1")
    @NotNull @Positive private Integer partNumber;

    @Schema(description = "ETag returned by S3 for the uploaded part")
    @NotBlank private String eTag;
}
