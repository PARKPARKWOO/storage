package org.woo.storage.application

import com.example.grpc.fileupload.FileUploadSpec
import com.example.grpc.fileupload.UploadFileRequest
import com.google.protobuf.ByteString
import io.minio.MinioAsyncClient
import io.minio.MinioClient
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.io.ByteArrayInputStream

/**
 * MinioStorageService 단위 테스트
 */
class MinioStorageServiceTest {
    
    private val mockMinioClient = mock(MinioClient::class.java)
    private val mockAsyncClient = mock(MinioAsyncClient::class.java)
    private val service = MinioStorageService(mockMinioClient, mockAsyncClient)
    
    @Test
    fun `getPresignUploadUrl should return valid url`() = runBlocking {
        // Given
        val bucket = "test-bucket"
        val objectKey = "test/file.txt"
        val contentType = "text/plain"
        val expiry = 600
        val expectedUrl = "http://localhost:9000/test-bucket/test/file.txt?X-Amz-..."
        
        `when`(mockAsyncClient.getPresignedObjectUrl(any())).thenReturn(expectedUrl)
        
        // When
        val result = service.getPresignUploadUrl(bucket, objectKey, contentType, expiry)
        
        // Then
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
    }
    
    @Test
    fun `getPresignDownloadUrl should return valid url`() = runBlocking {
        // Given
        val bucket = "test-bucket"
        val objectKey = "test/file.txt"
        val expiry = 600
        val queryParams = mapOf("response-content-type" to "text/plain")
        val expectedUrl = "http://localhost:9000/test-bucket/test/file.txt?X-Amz-..."
        
        `when`(mockAsyncClient.getPresignedObjectUrl(any())).thenReturn(expectedUrl)
        
        // When
        val result = service.getPresignDownloadUrl(bucket, objectKey, expiry, queryParams)
        
        // Then
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
    }
    
    @Test
    fun `upload should successfully upload file`() = runBlocking {
        // Given
        val bucket = "test-bucket"
        val objectKey = "test/file.txt"
        val contentType = "text/plain"
        val data = "Hello MinIO!".toByteArray()
        val inputStream = ByteArrayInputStream(data)
        
        // When
        service.upload(bucket, objectKey, contentType, inputStream)
        
        // Then
        verify(mockMinioClient, times(1)).putObject(any())
    }
    
    @Test
    fun `uploadStream should process header and chunks correctly`() = runBlocking {
        // Given
        val bucket = "test-bucket"
        val objectKey = "test/file.txt"
        val contentType = "text/plain"
        val testData = "Hello MinIO from stream!".toByteArray()
        
        val requestFlow = flow {
            // 헤더
            emit(UploadFileRequest.newBuilder()
                .setHeader(FileUploadSpec.newBuilder()
                    .setBucket(bucket)
                    .setObjectKey(objectKey)
                    .setContentType(contentType)
                    .build())
                .build())
            
            // 청크
            emit(UploadFileRequest.newBuilder()
                .setChunk(ByteString.copyFrom(testData))
                .build())
        }
        
        // Mock MinIO response
        val mockResult = mock(io.minio.ObjectWriteResponse::class.java)
        `when`(mockResult.etag()).thenReturn("test-etag")
        `when`(mockMinioClient.putObject(any())).thenReturn(mockResult)
        
        // When
        val response = service.uploadStream(requestFlow)
        
        // Then
        assertEquals(bucket, response.bucket)
        assertEquals(objectKey, response.objectKey)
        assertEquals(testData.size.toLong(), response.size)
        assertNotNull(response.etag)
    }
    
    @Test
    fun `uploadStream should throw exception when header is missing`() = runBlocking {
        // Given
        val requestFlow = flow {
            // 헤더 없이 바로 청크 전송
            emit(UploadFileRequest.newBuilder()
                .setChunk(ByteString.copyFrom("data".toByteArray()))
                .build())
        }
        
        // When & Then
        assertThrows(IllegalStateException::class.java) {
            runBlocking {
                service.uploadStream(requestFlow)
            }
        }
    }
    
    @Test
    fun `uploadStream should handle large files in chunks`() = runBlocking {
        // Given
        val bucket = "test-bucket"
        val objectKey = "large-file.bin"
        val chunkSize = 64 * 1024 // 64KB
        val totalChunks = 10
        
        val requestFlow = flow {
            // 헤더
            emit(UploadFileRequest.newBuilder()
                .setHeader(FileUploadSpec.newBuilder()
                    .setBucket(bucket)
                    .setObjectKey(objectKey)
                    .setContentType("application/octet-stream")
                    .build())
                .build())
            
            // 여러 청크
            repeat(totalChunks) {
                val chunk = ByteArray(chunkSize) { it.toByte() }
                emit(UploadFileRequest.newBuilder()
                    .setChunk(ByteString.copyFrom(chunk))
                    .build())
            }
        }
        
        // Mock MinIO response
        val mockResult = mock(io.minio.ObjectWriteResponse::class.java)
        `when`(mockResult.etag()).thenReturn("large-file-etag")
        `when`(mockMinioClient.putObject(any())).thenReturn(mockResult)
        
        // When
        val response = service.uploadStream(requestFlow)
        
        // Then
        assertEquals(bucket, response.bucket)
        assertEquals(objectKey, response.objectKey)
        assertEquals((chunkSize * totalChunks).toLong(), response.size)
    }
    
    @Test
    fun `uploadStream should handle metadata correctly`() = runBlocking {
        // Given
        val bucket = "test-bucket"
        val objectKey = "file-with-metadata.txt"
        val metadata = mapOf(
            "user-id" to "12345",
            "original-name" to "document.txt",
            "upload-date" to "2024-01-01"
        )
        
        val requestFlow = flow {
            emit(UploadFileRequest.newBuilder()
                .setHeader(FileUploadSpec.newBuilder()
                    .setBucket(bucket)
                    .setObjectKey(objectKey)
                    .setContentType("text/plain")
                    .putAllMetadata(metadata)
                    .build())
                .build())
            
            emit(UploadFileRequest.newBuilder()
                .setChunk(ByteString.copyFrom("test data".toByteArray()))
                .build())
        }
        
        // Mock MinIO response
        val mockResult = mock(io.minio.ObjectWriteResponse::class.java)
        `when`(mockResult.etag()).thenReturn("metadata-etag")
        `when`(mockMinioClient.putObject(any())).thenReturn(mockResult)
        
        // When
        val response = service.uploadStream(requestFlow)
        
        // Then
        assertNotNull(response)
        verify(mockMinioClient, times(1)).putObject(any())
    }
}


