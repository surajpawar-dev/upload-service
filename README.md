# RAG Upload Service

Spring Boot service for securely managing the PDF upload lifecycle. This service owns upload metadata, S3 upload coordination, upload status, validation, logging, and upload-completed event publication.

It does not perform PDF text extraction, chunking, embedding generation, vector database writes, or LLM/chat operations. Those belong in separate ingestion and AI services.

## Features

- Upload PDFs to Amazon S3.
- Store upload metadata in OpenSearch.
- Support backend upload, direct browser-to-S3 upload, and multipart browser-to-S3 upload.
- Validate PDF file type, size, and required metadata.
- Track upload lifecycle status: `PENDING`, `UPLOADING`, `UPLOADED`, `FAILED`.
- Support idempotent initiate requests through `idempotencyKey`.
- Publish an `UPLOAD_COMPLETED` lifecycle event after successful upload completion.
- Provide structured JSON logs with request correlation IDs.
- Expose health, info, and metrics through Spring Boot Actuator.
- Expose OpenAPI documentation through Swagger UI.

## Tech Stack

- Java 21+
- Spring Boot 3.4
- Maven 3.9+
- Amazon S3
- OpenSearch
- Spring Validation
- Spring Actuator
- Springdoc OpenAPI
- Spring Boot console logging locally
- Optional CloudWatch Logs appender

## Prerequisites

Install:

- JDK 21 or newer.
- Maven 3.9.0 or newer.
- AWS credentials configured through the default AWS SDK credential chain.
- An S3 bucket for uploaded PDFs.
- An OpenSearch instance for metadata storage.

AWS credentials can come from any standard AWS SDK source:

- Environment variables: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, optional `AWS_SESSION_TOKEN`.
- AWS profile files under `~/.aws`.
- IAM role when running on AWS infrastructure.

## Required Infrastructure

For local platform runs, the parent `document-rag-platform/docker-compose.yml` provides LocalStack, OpenSearch, and the downstream Document Processing Service URL. The service runs with:

```text
SPRING_PROFILES_ACTIVE=local
```

For higher environments such as dev, staging, or production, run with:

```text
SPRING_PROFILES_ACTIVE=prod
```

Do not point this service at LocalStack in higher environments. Leave `AWS_S3_ENDPOINT` empty or unset so the AWS SDK uses the real AWS S3 endpoint for the configured region.

### Amazon S3

Create a bucket and provide its name through `AWS_S3_BUCKET`.

The service writes PDFs into object keys shaped like:

```text
{fileId}/{normalized-file-name}.pdf
```

### OpenSearch

The service stores metadata in the `files` index.

The service automatically checks for this index at startup and creates it if it is missing. If OpenSearch is temporarily unavailable during startup, the application logs a warning and continues running. Upload metadata operations still require OpenSearch to be available when the API is called.

The auto-created index uses this mapping:

```json
PUT /files
{
  "mappings": {
    "properties": {
      "fileId": { "type": "keyword" },
      "bookId": { "type": "keyword" },
      "title": { "type": "text" },
      "fileName": { "type": "keyword" },
      "s3Key": { "type": "keyword" },
      "uploadId": { "type": "keyword" },
      "contentType": { "type": "keyword" },
      "size": { "type": "long" },
      "status": { "type": "keyword" },
      "strategy": { "type": "keyword" },
      "uploadedBy": { "type": "keyword" },
      "idempotencyKey": { "type": "keyword" },
      "createdAt": { "type": "date" },
      "updatedAt": { "type": "date" }
    }
  }
}
```

The idempotency lookup requires `idempotencyKey` to be searchable as a keyword field. The startup initializer configures that automatically.

### Document Processing Service

The upload service calls the Document Processing Service after a successful upload. In higher environments, deploy `rag-document-processing-service` first or expose it through an internal load balancer/service DNS name.

Configure:

```text
DOCUMENT_PROCESSING_BASE_URL=https://document-processing.internal.example.com
```

Use an internal network URL when possible. Public internet exposure is not required for service-to-service traffic.

### CloudWatch Logs

Console logging is the default local behavior. If CloudWatch logging is enabled for a higher environment, create the log group and allow the runtime IAM role to write log events.

Required permissions when CloudWatch logging is used:

```text
logs:CreateLogStream
logs:PutLogEvents
logs:DescribeLogStreams
```

Configure:

```text
CLOUDWATCH_LOG_GROUP=/document-rag-platform/rag-upload-service
CLOUDWATCH_LOG_STREAM=prod
```

## Higher Environment Configuration

Minimum production variables:

```text
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080

AWS_REGION=us-east-1
AWS_S3_BUCKET=documents-prod
AWS_S3_ENDPOINT=
AWS_S3_PATH_STYLE_ACCESS_ENABLED=false

OPENSEARCH_ENABLED=true
OPENSEARCH_URIS=https://opensearch-prod.example.com
OPENSEARCH_USERNAME=<from-secret>
OPENSEARCH_PASSWORD=<from-secret>

DOCUMENT_PROCESSING_ENABLED=true
DOCUMENT_PROCESSING_BASE_URL=https://document-processing.internal.example.com

CLOUDWATCH_LOG_STREAM=prod
```

AWS credentials should come from the hosting platform, not hardcoded variables:

- ECS task role.
- EKS IRSA/service account role.
- EC2 instance profile.
- CI/CD or secret manager only when a role is not available.

Required AWS permissions:

```text
s3:PutObject
s3:GetObject
s3:HeadObject
s3:AbortMultipartUpload
s3:CreateMultipartUpload
s3:UploadPart
s3:CompleteMultipartUpload
```

Production notes:

- Use private networking for OpenSearch and downstream service calls.
- Use TLS endpoints for OpenSearch and internal service URLs.
- Keep S3 bucket names environment-specific, for example `documents-dev`, `documents-staging`, and `documents-prod`.
- Do not set `AWS_ACCESS_KEY_ID=test`, `AWS_SECRET_ACCESS_KEY=test`, or LocalStack endpoint values outside local development.

## Configuration

Configuration is loaded from `src/main/resources/application.properties`. Most settings can be overridden with environment variables.

| Environment variable | Default | Purpose |
| --- | --- | --- |
| `AWS_REGION` | `us-east-1` | AWS region for S3 and CloudWatch Logs. |
| `AWS_S3_ENABLED` | `true` | Enables S3 client and presigner beans. |
| `AWS_S3_BUCKET` | empty | Required S3 bucket name. |
| `OPENSEARCH_ENABLED` | `true` | Enables OpenSearch metadata repository. |
| `OPENSEARCH_SCHEME` | `http` | OpenSearch scheme. |
| `OPENSEARCH_HOST` | `localhost` | OpenSearch host. |
| `OPENSEARCH_PORT` | `9200` | OpenSearch port. |
| `OPENSEARCH_USERNAME` | empty | Optional OpenSearch username. |
| `OPENSEARCH_PASSWORD` | empty | Optional OpenSearch password. |
| `UPLOAD_SMALL_FILE_MAX_BYTES` | `10485760` | Max size for backend upload strategy, default 10 MB. |
| `UPLOAD_MULTIPART_MIN_BYTES` | `104857600` | Minimum size for multipart upload strategy, default 100 MB. |
| `UPLOAD_MULTIPART_PART_SIZE_BYTES` | `10485760` | Multipart part size, default 10 MB. |
| `UPLOAD_PRESIGNED_URL_EXPIRATION_MINUTES` | `15` | Presigned URL expiry window. |
| `UPLOAD_MAX_FILE_SIZE_BYTES` | `1048576000` | Max accepted PDF size, default 1 GB. |
| `CLOUDWATCH_LOG_GROUP` | `/document-rag-platform/rag-upload-service` | CloudWatch log group. |
| `CLOUDWATCH_LOG_STREAM` | `local` | CloudWatch log stream. |

Spring multipart limits are configured as:

```properties
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB
```

If `UPLOAD_SMALL_FILE_MAX_BYTES` is raised above 100 MB, also raise these Spring multipart values.

## Local Setup

From the project root:

```powershell
cd C:\Users\ASUS\OneDrive\Desktop\mycodes\document-rag-platform\rag-upload-service
```

Set required environment variables in PowerShell:

```powershell
$env:AWS_REGION = "us-east-1"
$env:AWS_S3_BUCKET = "your-s3-bucket-name"
$env:OPENSEARCH_HOST = "localhost"
$env:OPENSEARCH_PORT = "9200"
$env:OPENSEARCH_SCHEME = "http"
```

If OpenSearch requires authentication:

```powershell
$env:OPENSEARCH_USERNAME = "admin"
$env:OPENSEARCH_PASSWORD = "your-password"
```

Run the test suite:

```powershell
mvn test
```

Run the application:

```powershell
mvn spring-boot:run
```

The service starts on the default Spring Boot port:

```text
http://localhost:8080
```

## Docker

Build and run the image:

```powershell
docker build -t rag-upload-service:latest .
docker run --rm -p 8080:8080 --name rag-upload-service rag-upload-service:latest
```

The Docker image defaults to local mode with S3 and OpenSearch disabled, so it can start immediately. See `API_DOCUMENTATION.md` for frontend/Postman examples and production Docker environment variables.

## Maven Version Note

The project enforces Maven `3.9.0+` in `pom.xml`.

If your machine has an older Maven version, install Maven 3.9+ before running normal builds. For temporary local verification only, you can skip the enforcer:

```powershell
mvn "-Denforcer.skip=true" test
```

## API Documentation

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

Actuator endpoints:

```text
GET http://localhost:8080/actuator/health
GET http://localhost:8080/actuator/info
GET http://localhost:8080/actuator/metrics
```

## Endpoint Structure

Base path:

```text
/api/uploads
```

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/uploads/initiate` | Create metadata and receive the selected upload strategy. |
| `POST` | `/api/uploads/{fileId}/content` | Upload a small PDF through the backend. |
| `POST` | `/api/uploads/{fileId}/complete` | Complete a direct S3 upload after the browser uploads to S3. |
| `POST` | `/api/uploads/{fileId}/multipart/complete` | Complete a multipart S3 upload. |
| `GET` | `/api/uploads/{fileId}` | Fetch upload metadata. |

There are no download, text extraction, embedding, vector, or chat endpoints in this service.

## Upload Strategy Selection

The service chooses the upload strategy from the requested file size:

| Size condition | Strategy |
| --- | --- |
| `size <= UPLOAD_SMALL_FILE_MAX_BYTES` | `BACKEND` |
| `size < UPLOAD_MULTIPART_MIN_BYTES` | `DIRECT_S3` |
| `size >= UPLOAD_MULTIPART_MIN_BYTES` | `MULTIPART` |

Default behavior:

- Up to 10 MB: backend upload.
- 10 MB to under 100 MB: direct S3 upload.
- 100 MB and above: multipart S3 upload.
- Above 1 GB: rejected by validation unless `UPLOAD_MAX_FILE_SIZE_BYTES` is increased.

## Request Headers

Optional correlation ID header:

```text
X-Correlation-Id: your-correlation-id
```

If it is not provided, the service generates one and includes it in structured logs.

## Initiate Upload

```http
POST /api/uploads/initiate
Content-Type: application/json
```

Request:

```json
{
  "bookId": "book-123",
  "title": "Distributed Systems Notes",
  "fileName": "distributed-systems.pdf",
  "contentType": "application/pdf",
  "size": 1048576,
  "uploadedBy": "user-123",
  "idempotencyKey": "book-123-distributed-systems-v1"
}
```

Validation rules:

- `bookId` is required.
- `title` is required.
- `fileName` is required and must end with `.pdf`.
- `contentType` must be `application/pdf`.
- `size` must be positive and not exceed `UPLOAD_MAX_FILE_SIZE_BYTES`.
- `uploadedBy` is required.
- `idempotencyKey` is required.

Backend strategy response:

```json
{
  "fileId": "d8d2fd6b-9d11-4374-b353-ff9829d3471a",
  "bookId": "book-123",
  "title": "Distributed Systems Notes",
  "s3Key": "d8d2fd6b-9d11-4374-b353-ff9829d3471a/distributed-systems.pdf",
  "status": "PENDING",
  "strategy": "BACKEND",
  "uploadUrl": null,
  "uploadId": null,
  "partUploadUrls": null
}
```

Direct S3 strategy response includes `uploadUrl`:

```json
{
  "fileId": "file-id",
  "bookId": "book-123",
  "title": "Distributed Systems Notes",
  "s3Key": "file-id/distributed-systems.pdf",
  "status": "PENDING",
  "strategy": "DIRECT_S3",
  "uploadUrl": "https://s3-presigned-put-url",
  "uploadId": null,
  "partUploadUrls": null
}
```

Multipart strategy response includes `uploadId` and `partUploadUrls`:

```json
{
  "fileId": "file-id",
  "bookId": "book-123",
  "title": "Distributed Systems Notes",
  "s3Key": "file-id/distributed-systems.pdf",
  "status": "PENDING",
  "strategy": "MULTIPART",
  "uploadUrl": null,
  "uploadId": "s3-multipart-upload-id",
  "partUploadUrls": [
    {
      "partNumber": 1,
      "uploadUrl": "https://s3-presigned-part-1-url"
    }
  ]
}
```

## Backend Upload Flow

Use this flow when `strategy` is `BACKEND`.

```http
POST /api/uploads/{fileId}/content
Content-Type: multipart/form-data
```

Form field:

```text
file=<PDF file>
```

PowerShell example:

```powershell
curl.exe -X POST `
  "http://localhost:8080/api/uploads/{fileId}/content" `
  -F "file=@C:\path\to\document.pdf;type=application/pdf"
```

Successful response:

```json
{
  "fileId": "file-id",
  "fileName": "document.pdf",
  "bookId": "book-123",
  "title": "Distributed Systems Notes",
  "s3Key": "file-id/document.pdf",
  "contentType": "application/pdf",
  "size": 1048576,
  "status": "UPLOADED",
  "strategy": "BACKEND",
  "uploadedBy": "user-123",
  "idempotencyKey": "book-123-document-v1",
  "createdAt": "2026-07-15T12:00:00Z",
  "updatedAt": "2026-07-15T12:01:00Z"
}
```

## Direct Browser-to-S3 Flow

Use this flow when `strategy` is `DIRECT_S3`.

1. Call `POST /api/uploads/initiate`.
2. Upload the PDF bytes directly to the returned `uploadUrl`.
3. Call `POST /api/uploads/{fileId}/complete`.

S3 upload example:

```powershell
curl.exe -X PUT `
  "https://s3-presigned-put-url" `
  -H "Content-Type: application/pdf" `
  --data-binary "@C:\path\to\document.pdf"
```

Complete upload:

```http
POST /api/uploads/{fileId}/complete
```

The service verifies that the S3 object exists, marks metadata as `UPLOADED`, and publishes an upload-completed event.

## Multipart Upload Flow

Use this flow when `strategy` is `MULTIPART`.

1. Call `POST /api/uploads/initiate`.
2. Split the PDF into parts using `UPLOAD_MULTIPART_PART_SIZE_BYTES`.
3. Upload each part to its matching `partUploadUrls[n].uploadUrl`.
4. Capture the `ETag` response header from each S3 part upload.
5. Call `POST /api/uploads/{fileId}/multipart/complete`.

Complete request:

```json
{
  "parts": [
    {
      "partNumber": 1,
      "eTag": "\"etag-from-s3-part-1\""
    },
    {
      "partNumber": 2,
      "eTag": "\"etag-from-s3-part-2\""
    }
  ]
}
```

The service sends the part list to S3, verifies the final object exists, marks metadata as `UPLOADED`, and publishes an upload-completed event.

## Fetch Metadata

```http
GET /api/uploads/{fileId}
```

Response:

```json
{
  "fileId": "file-id",
  "fileName": "document.pdf",
  "bookId": "book-123",
  "title": "Distributed Systems Notes",
  "s3Key": "file-id/document.pdf",
  "contentType": "application/pdf",
  "size": 1048576,
  "status": "UPLOADED",
  "strategy": "BACKEND",
  "uploadedBy": "user-123",
  "idempotencyKey": "book-123-document-v1",
  "createdAt": "2026-07-15T12:00:00Z",
  "updatedAt": "2026-07-15T12:01:00Z"
}
```

## Error Response Format

Errors use a standard response body:

```json
{
  "timestamp": "2026-07-15T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Only PDF uploads are supported",
  "path": "/api/uploads/initiate"
}
```

Common status codes:

| Status | Meaning |
| --- | --- |
| `400` | Validation failed or invalid upload request. |
| `404` | Metadata or S3 object was not found. |
| `409` | Upload state conflict, wrong upload strategy, or idempotency key conflict. |
| `500` | Unexpected server error. |

## Idempotency Behavior

`POST /api/uploads/initiate` requires `idempotencyKey`.

If the same key is sent again with the same metadata, the service returns the existing upload record and reusable presigned upload information.

If the same key is sent with different metadata, the service returns `409 Conflict`.

This prevents accidental duplicate upload records during client retries.

## Upload Completed Event

After a successful backend, direct S3, or multipart completion, the service calls `UploadEventPublisher.publishUploadCompleted`.

The current implementation logs:

```text
eventType=UPLOAD_COMPLETED fileId={fileId} bookId={bookId} s3Key={s3Key}
```

In production, this abstraction can be replaced with an implementation that publishes to SNS, SQS, EventBridge, Kafka, or another ingestion trigger.

## Logging

The service uses Spring Boot's default console log format locally. When the `cloudwatch` Spring profile is active,
the CloudWatch appender sends structured log events that include:

- HTTP method.
- Request path.
- Response status.
- Duration.
- Correlation ID.

Send your own correlation ID with:

```text
X-Correlation-Id: your-request-id
```

CloudWatch logging properties are available through:

```properties
logging.cloudwatch.region
logging.cloudwatch.log-group
logging.cloudwatch.log-stream
```

## Out of Scope

This upload service must not own:

- Text extraction.
- PDF parsing.
- Chunking.
- Embedding generation.
- Vector database operations.
- LLM or chat interactions.

Those responsibilities should be implemented in separate downstream services, triggered after upload completion.
