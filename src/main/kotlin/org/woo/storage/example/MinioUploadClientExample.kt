package org.woo.storage.example

import com.example.grpc.fileupload.FileUploadSpec
import com.example.grpc.fileupload.GetPresignedDownloadUrlRequest
import com.example.grpc.fileupload.GetPresignedUploadUrlRequest
import com.example.grpc.fileupload.StorageServiceGrpcKt
import com.example.grpc.fileupload.UploadFileRequest
import com.google.protobuf.ByteString
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files

/**
 * MinIO 다이렉트 업로드 클라이언트 예제
 * 
 * 이 클래스는 Storage 서비스의 MinIO 업로드 기능을 사용하는 예제를 제공합니다.
 */
class MinioUploadClientExample(
    private val serverHost: String = "localhost",
    private val serverPort: Int = 9090
) {
    
    private val channel = ManagedChannelBuilder
        .forAddress(serverHost, serverPort)
        .usePlaintext()
        .build()
    
    private val stub = StorageServiceGrpcKt.StorageServiceCoroutineStub(channel)
    
    /**
     * 예제 1: Presigned URL을 사용한 다이렉트 업로드
     * 
     * 이 방식이 가장 권장되는 방식입니다:
     * 1. 서버로부터 Presigned URL을 받습니다
     * 2. 클라이언트가 HTTP PUT으로 MinIO에 직접 업로드합니다
     * 3. 서버 부하가 없고 성능이 좋습니다
     */
    suspend fun uploadWithPresignedUrl(file: File, bucket: String, objectKey: String) {
        println("=== Presigned URL 업로드 시작 ===")
        
        // 1단계: Presigned Upload URL 요청
        val request = GetPresignedUploadUrlRequest.newBuilder()
            .setSpec(
                FileUploadSpec.newBuilder()
                    .setBucket(bucket)
                    .setObjectKey(objectKey)
                    .setContentType(Files.probeContentType(file.toPath()) ?: "application/octet-stream")
                    .setContentDisposition("inline; filename=\"${file.name}\"")
                    .putMetadata("original-name", file.name)
                    .putMetadata("file-size", file.length().toString())
                    .build()
            )
            .setContentLength(file.length())
            .setExpirySeconds(3600) // 1시간 유효
            .build()
        
        val response = stub.getPresignedUploadUrl(request)
        println("Presigned URL 받음: ${response.url}")
        println("만료 시간: ${response.expiresAt}")
        
        // 2단계: HTTP PUT으로 직접 업로드
        val success = uploadFileToUrl(response.url, file)
        
        if (success) {
            println("✅ 업로드 성공!")
            println("Bucket: $bucket")
            println("Object Key: $objectKey")
        } else {
            println("❌ 업로드 실패!")
        }
    }
    
    /**
     * HTTP PUT으로 파일 업로드
     */
    private fun uploadFileToUrl(presignedUrl: String, file: File): Boolean {
        return try {
            val url = URL(presignedUrl)
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "PUT"
                doOutput = true
                setRequestProperty("Content-Type", Files.probeContentType(file.toPath()) ?: "application/octet-stream")
                setRequestProperty("Content-Length", file.length().toString())
            }
            
            file.inputStream().use { input ->
                connection.outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            
            val responseCode = connection.responseCode
            println("HTTP 응답 코드: $responseCode")
            
            responseCode == 200
        } catch (e: Exception) {
            println("업로드 중 오류 발생: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 예제 2: gRPC 스트리밍 업로드
     * 
     * 서버를 거쳐서 업로드하는 방식입니다:
     * 1. 서버 제어가 필요한 경우
     * 2. 업로드 중 검증이 필요한 경우
     * 3. MinIO에 직접 접근할 수 없는 경우
     */
    suspend fun uploadViaGrpcStream(file: File, bucket: String, objectKey: String) {
        println("=== gRPC 스트리밍 업로드 시작 ===")
        
        val chunkSize = 64 * 1024 // 64KB chunks
        var chunkCount = 0
        
        val requestFlow = flow {
            // 1단계: 헤더 전송
            emit(
                UploadFileRequest.newBuilder()
                    .setHeader(
                        FileUploadSpec.newBuilder()
                            .setBucket(bucket)
                            .setObjectKey(objectKey)
                            .setContentType(Files.probeContentType(file.toPath()) ?: "application/octet-stream")
                            .putMetadata("original-name", file.name)
                            .putMetadata("size", file.length().toString())
                            .build()
                    )
                    .build()
            )
            println("헤더 전송 완료")
            
            // 2단계: 파일 청크 전송
            file.inputStream().use { input ->
                val buffer = ByteArray(chunkSize)
                var bytesRead: Int
                
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    emit(
                        UploadFileRequest.newBuilder()
                            .setChunk(ByteString.copyFrom(buffer, 0, bytesRead))
                            .build()
                    )
                    chunkCount++
                    
                    if (chunkCount % 10 == 0) {
                        println("청크 #$chunkCount 전송 완료 (${chunkCount * chunkSize / 1024}KB)")
                    }
                }
            }
            
            println("총 $chunkCount 개의 청크 전송 완료")
        }
        
        // 업로드 실행
        val response = stub.uploadFile(requestFlow)
        
        println("✅ 업로드 성공!")
        println("Bucket: ${response.bucket}")
        println("Object Key: ${response.objectKey}")
        println("Size: ${response.size} bytes (${response.size / 1024}KB)")
        println("ETag: ${response.etag}")
    }
    
    /**
     * 예제 3: Presigned Download URL 받기
     */
    suspend fun getDownloadUrl(bucket: String, objectKey: String): String {
        println("=== Download URL 요청 ===")
        
        val request = GetPresignedDownloadUrlRequest.newBuilder()
            .setBucket(bucket)
            .setObjectKey(objectKey)
            .setExpirySeconds(3600) // 1시간 유효
            .setResponseContentType("application/octet-stream")
            .setResponseContentDisposition("attachment; filename=\"downloaded-file\"")
            .build()
        
        val response = stub.getPresignedDownloadUrl(request)
        
        println("Download URL: ${response.url}")
        println("만료 시간: ${response.expiresAt}")
        
        return response.url
    }
    
    /**
     * 예제 4: 여러 파일을 병렬로 업로드
     */
    suspend fun uploadMultipleFiles(files: List<File>, bucket: String, baseDir: String = "uploads") {
        println("=== 다중 파일 업로드 시작 (${files.size}개) ===")
        
        val startTime = System.currentTimeMillis()
        
        files.forEachIndexed { index, file ->
            val objectKey = "$baseDir/${System.currentTimeMillis()}_${file.name}"
            println("[$index/${files.size}] ${file.name} 업로드 중...")
            
            uploadWithPresignedUrl(file, bucket, objectKey)
        }
        
        val duration = System.currentTimeMillis() - startTime
        println("✅ 모든 파일 업로드 완료! (소요 시간: ${duration}ms)")
    }
    
    /**
     * 리소스 정리
     */
    fun shutdown() {
        channel.shutdown()
    }
}

/**
 * 실행 예제
 */
fun main() = runBlocking {
    val client = MinioUploadClientExample()
    
    try {
        val testFile = File("/path/to/your/file.txt") // 실제 파일 경로로 변경
        val bucket = "my-bucket"
        
        if (testFile.exists()) {
            // 방법 1: Presigned URL 사용 (권장)
            println("\n========================================")
            println("방법 1: Presigned URL 사용")
            println("========================================")
            client.uploadWithPresignedUrl(
                file = testFile,
                bucket = bucket,
                objectKey = "uploads/test-${System.currentTimeMillis()}.txt"
            )
            
            // 방법 2: gRPC 스트리밍 사용
            println("\n========================================")
            println("방법 2: gRPC 스트리밍 사용")
            println("========================================")
            client.uploadViaGrpcStream(
                file = testFile,
                bucket = bucket,
                objectKey = "uploads/stream-${System.currentTimeMillis()}.txt"
            )
            
            // 방법 3: 다운로드 URL 받기
            println("\n========================================")
            println("방법 3: 다운로드 URL")
            println("========================================")
            val downloadUrl = client.getDownloadUrl(
                bucket = bucket,
                objectKey = "uploads/test.txt"
            )
            println("이 URL로 파일을 다운로드할 수 있습니다: $downloadUrl")
            
        } else {
            println("테스트 파일을 찾을 수 없습니다: ${testFile.absolutePath}")
            println("실제 파일 경로로 변경해주세요.")
        }
        
    } catch (e: Exception) {
        println("오류 발생: ${e.message}")
        e.printStackTrace()
    } finally {
        client.shutdown()
    }
}


