package com.suraj.rag.upload.controller;

import com.suraj.rag.upload.common.constants.ApplicationConstants;
import com.suraj.rag.upload.dto.CompleteMultipartUploadRequest;
import com.suraj.rag.upload.dto.FileMetadataResponse;
import com.suraj.rag.upload.dto.InitiateUploadRequest;
import com.suraj.rag.upload.dto.InitiateUploadResponse;
import com.suraj.rag.upload.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApplicationConstants.Upload.BASE_API_PATH)
@Tag(name = "PDF Uploads", description = "PDF upload lifecycle and metadata APIs")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping(ApplicationConstants.Upload.INITIATE_PATH)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Initiate an upload",
            description =
                    "Creates metadata and returns the selected upload strategy with presigned URLs when needed.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Upload initialized"),
        @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
    })
    public InitiateUploadResponse initiateUpload(
            @Valid @RequestBody InitiateUploadRequest request) {
        return fileUploadService.initiateUpload(request);
    }

    @PostMapping(
            value = ApplicationConstants.Upload.CONTENT_PATH,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a small file through the backend")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "File uploaded"),
        @ApiResponse(
                responseCode = "404",
                description = "File metadata not found",
                content = @Content),
        @ApiResponse(
                responseCode = "409",
                description = "Invalid upload strategy",
                content = @Content)
    })
    public FileMetadataResponse uploadThroughBackend(
            @PathVariable @Schema(description = "File metadata id") String fileId,
            @RequestPart MultipartFile file) {
        return fileUploadService.uploadThroughBackend(fileId, file);
    }

    @PostMapping(ApplicationConstants.Upload.COMPLETE_PATH)
    @Operation(summary = "Complete a direct S3 upload")
    public FileMetadataResponse completeUpload(@PathVariable String fileId) {
        return fileUploadService.completeUpload(fileId);
    }

    @PostMapping(ApplicationConstants.Upload.COMPLETE_MULTIPART_PATH)
    @Operation(summary = "Complete a multipart upload")
    public FileMetadataResponse completeMultipartUpload(
            @PathVariable String fileId,
            @Valid @RequestBody CompleteMultipartUploadRequest request) {
        return fileUploadService.completeMultipartUpload(fileId, request);
    }

    @GetMapping(ApplicationConstants.Upload.FILE_ID_PATH)
    @Operation(summary = "Get file metadata")
    public FileMetadataResponse getMetadata(@PathVariable String fileId) {
        return fileUploadService.getMetadata(fileId);
    }
}
