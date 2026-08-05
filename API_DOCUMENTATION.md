# Upload Platform API

Base URL:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

Health check:

```http
GET /actuator/health
```

## Run With Docker

Build the image:

```powershell
docker build -t rag-upload-service:latest .
```

Run locally without AWS/OpenSearch:

```powershell
docker run --rm -p 8080:8080 --name rag-upload-service rag-upload-service:latest
```

Allow a different frontend origin:

```powershell
docker run --rm -p 8080:8080 -e CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173 rag-upload-service:latest
```

This local mode stores metadata in memory and uploaded files under `/app/data/uploads` inside the container. Use a volume if you want uploaded files to survive container removal:

```powershell
docker run --rm -p 8080:8080 -v rag-upload-service-data:/app/data/uploads --name rag-upload-service rag-upload-service:latest
```

Run with real AWS S3 and OpenSearch:

```powershell
docker run --rm -p 8080:8080 `
  -e AWS_S3_ENABLED=true `
  -e OPENSEARCH_ENABLED=true `
  -e SPRING_DATA_ELASTICSEARCH_REPOSITORIES_ENABLED=true `
  -e AWS_REGION=us-east-1 `
  -e AWS_S3_BUCKET=your-bucket-name `
  -e AWS_ACCESS_KEY_ID=your-access-key `
  -e AWS_SECRET_ACCESS_KEY=your-secret-key `
  -e OPENSEARCH_URIS=http://your-opensearch-host:9200 `
  --name rag-upload-service rag-upload-service:latest
```

## Initiate Upload

```http
POST /api/uploads/initiate
Content-Type: application/json
```

Body:

```json
{
  "bookId": "book-123",
  "title": "Sample PDF",
  "fileName": "sample.pdf",
  "contentType": "application/pdf",
  "size": 1024,
  "uploadedBy": "user-123",
  "idempotencyKey": "book-123-sample-v1"
}
```

Frontend example:

```js
const response = await fetch("http://localhost:8080/api/uploads/initiate", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({
    bookId: "book-123",
    title: "Sample PDF",
    fileName: file.name,
    contentType: file.type,
    size: file.size,
    uploadedBy: "user-123",
    idempotencyKey: crypto.randomUUID()
  })
});

const upload = await response.json();
```

Postman: choose `POST`, set `Body -> raw -> JSON`, paste the JSON body above.

## Backend Upload

Use this when the initiate response has:

```json
{ "strategy": "BACKEND" }
```

```http
POST /api/uploads/{fileId}/content
Content-Type: multipart/form-data
```

Form field:

```text
file = your PDF file
```

Frontend example:

```js
const formData = new FormData();
formData.append("file", file);

const uploaded = await fetch(`http://localhost:8080/api/uploads/${upload.fileId}/content`, {
  method: "POST",
  body: formData
}).then((res) => res.json());
```

Postman: choose `Body -> form-data`, add key `file`, change type from `Text` to `File`, and select a `.pdf` file.

## Direct S3 Upload

Use this only when running with real S3 and the initiate response has:

```json
{ "strategy": "DIRECT_S3", "uploadUrl": "https://..." }
```

Frontend flow:

```js
await fetch(upload.uploadUrl, {
  method: "PUT",
  headers: { "Content-Type": "application/pdf" },
  body: file
});

await fetch(`http://localhost:8080/api/uploads/${upload.fileId}/complete`, {
  method: "POST"
});
```

## Multipart S3 Upload

Use this only when running with real S3 and the initiate response has:

```json
{ "strategy": "MULTIPART", "uploadId": "...", "partUploadUrls": [] }
```

Upload each file chunk to its matching `partUploadUrls[n].uploadUrl`, collect each S3 `ETag` response header, then complete:

```http
POST /api/uploads/{fileId}/multipart/complete
Content-Type: application/json
```

```json
{
  "parts": [
    {
      "partNumber": 1,
      "eTag": "\"etag-from-s3\""
    }
  ]
}
```

## Fetch Metadata

```http
GET /api/uploads/{fileId}
```

## Validation

Only PDF uploads are accepted:

- `fileName` must end with `.pdf`.
- `contentType` must be `application/pdf`.
- Backend-uploaded file content must start with the PDF header.
- Default Docker local mode accepts files up to 100 MB.

## Error Shape

```json
{
  "timestamp": "2026-07-15T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Only PDF uploads are supported",
  "path": "/api/uploads/initiate"
}
```
