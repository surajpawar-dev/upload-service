package com.suraj.uploadplatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Request used to initiate a PDF upload")
@Getter
@Setter
public class InitiateUploadRequest {

    @Schema(description = "Business book id associated with this PDF", example = "book-123")
    @NotBlank private String bookId;

    @Schema(description = "Document title", example = "Distributed Systems Notes")
    @NotBlank private String title;

    @Schema(description = "Original file name", example = "invoice.pdf")
    @NotBlank private String fileName;

    @Schema(description = "MIME type of the file", example = "application/pdf")
    @NotBlank private String contentType;

    @Schema(description = "File size in bytes", example = "1048576")
    @NotNull @Positive private Long size;

    @Schema(
            description = "Application user id or principal that owns the upload",
            example = "user-123")
    @NotBlank private String uploadedBy;

    @Schema(description = "Client-generated key used to safely retry initiate requests")
    @NotBlank private String idempotencyKey;
}
