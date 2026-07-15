package com.suraj.uploadplatform.common.constants;

public final class ApplicationConstants {

    public static final String EMPTY_STRING = "";
    public static final String TRUE = "true";
    public static final String FORWARD_SLASH = "/";
    public static final String HYPHEN = "-";

    private ApplicationConstants() {}

    public static final class Aws {

        public static final String REGION_DEFAULT = "us-east-1";
        public static final String S3_PROPERTY_PREFIX = "aws.s3";

        private Aws() {}
    }

    public static final class OpenSearch {

        public static final String PROPERTY_PREFIX = "opensearch";
        public static final String DEFAULT_HOST = "localhost";
        public static final int DEFAULT_PORT = 9200;
        public static final String DEFAULT_SCHEME = "http";
        public static final String FILES_INDEX = "files";

        private OpenSearch() {}
    }

    public static final class Property {

        public static final String AWS_PREFIX = "aws";
        public static final String UPLOAD_PREFIX = "upload";
        public static final String CLOUDWATCH_LOGGING_PREFIX = "logging.cloudwatch";
        public static final String ENABLED = "enabled";

        private Property() {}
    }

    public static final class Logging {

        public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
        public static final String CORRELATION_ID_MDC_KEY = "correlationId";
        public static final String REQUEST_STARTED = "HTTP request started";
        public static final String REQUEST_COMPLETED = "HTTP request completed";
        public static final String DEFAULT_CLOUDWATCH_LOG_GROUP = "/upload-platform/application";
        public static final String DEFAULT_CLOUDWATCH_LOG_STREAM = "local";

        private Logging() {}
    }

    public static final class Upload {

        public static final long SMALL_FILE_MAX_BYTES = 10L * 1024L * 1024L;
        public static final long MULTIPART_MIN_BYTES = 100L * 1024L * 1024L;
        public static final long MULTIPART_PART_SIZE_BYTES = 10L * 1024L * 1024L;
        public static final int PRESIGNED_URL_EXPIRATION_MINUTES = 15;
        public static final String PDF_CONTENT_TYPE = "application/pdf";
        public static final String PDF_EXTENSION = ".pdf";
        public static final String FILE_ID_PATH = "/{fileId}";
        public static final String CONTENT_PATH = "/{fileId}/content";
        public static final String COMPLETE_PATH = "/{fileId}/complete";
        public static final String COMPLETE_MULTIPART_PATH = "/{fileId}/multipart/complete";
        public static final String BASE_API_PATH = "/api/uploads";
        public static final String INITIATE_PATH = "/initiate";

        private Upload() {}
    }

    public static final class ErrorMessage {

        public static final String FILE_METADATA_SAVE_FAILED = "Failed to save file metadata";
        public static final String FILE_METADATA_FETCH_FAILED = "Failed to fetch file metadata";
        public static final String FILE_NOT_FOUND = "File metadata was not found";
        public static final String BACKEND_UPLOAD_REQUIRED =
                "This file must be uploaded through the backend upload endpoint";
        public static final String BACKEND_UPLOAD_NOT_ALLOWED =
                "This file is not configured for backend upload";
        public static final String MULTIPART_UPLOAD_REQUIRED =
                "This file must be completed through multipart upload";
        public static final String MULTIPART_UPLOAD_NOT_ALLOWED =
                "This file is not configured for multipart upload";
        public static final String S3_OBJECT_NOT_FOUND = "S3 object was not found";
        public static final String ONLY_PDF_ALLOWED = "Only PDF uploads are supported";
        public static final String FILE_SIZE_EXCEEDED = "File size exceeds the configured limit";
        public static final String BACKEND_FILE_METADATA_MISMATCH =
                "Uploaded file does not match initiated metadata";
        public static final String IDEMPOTENCY_KEY_CONFLICT =
                "Idempotency key was already used with different upload metadata";
        public static final String INTERNAL_SERVER_ERROR = "Unexpected server error";
        public static final String CLOUDWATCH_LOG_APPEND_FAILED =
                "Failed to append log event to CloudWatch";

        private ErrorMessage() {}
    }
}
