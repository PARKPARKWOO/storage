package org.woo.storage.adapter.`in`.rest

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.woo.storage.application.RetrieveFacade
import org.woo.storage.application.UploadFacade
import org.woo.storage.domain.metadata.Metadata
import org.woo.storage.ports.`in`.UploadUseCase

@RestController
@RequestMapping("/api/v1/image")
class TestController(
    private val retrieveFacade: RetrieveFacade,
    private val uploadFacade: UploadUseCase,
) {
    @GetMapping("/{path}")
    suspend fun download(@PathVariable("path") path: String): ResponseEntity<Resource> {
        val result: Pair<Resource, Metadata> = retrieveFacade.retrieveResource(path)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${result.second.fileName}\"")
            .contentType(MediaType.parseMediaType(result.second.contentType))
            .body(result.first)
    }

    @PostMapping("/upload")
    suspend fun uploadFile(@RequestParam("file") file: MultipartFile): ResponseEntity<String> =
        withContext(Dispatchers.IO) {
            try {
                // 파일 메타데이터 추출
                val fileName = file.originalFilename ?: "unknown"
                val contentType = file.contentType ?: "application/octet-stream"
                val contentLength = file.size
                val chunkSize = 1024 * 1024 // 예: 1MB 단위 청크

                // 메타데이터 저장: 관계형 DB에 저장하고 고유 fileId 반환 (예: uploadUseCase.metadata)
                val fileId = uploadFacade.metadata(fileName)

                // 파일을 청크 단위로 읽어서 Cassandra에 저장
                file.inputStream.use { inputStream ->
                    var chunkIndex = 0
                    val buffer = ByteArray(chunkSize)
                    while (true) {
                        val bytesRead = inputStream.read(buffer)
                        if (bytesRead <= 0) break
                        // bytesRead가 청크 크기보다 작으면 실제 읽은 바이트만큼 복사
                        val chunkData: ByteArray = if (bytesRead < chunkSize) {
                            buffer.copyOf(bytesRead)
                        } else {
                            buffer.copyOf()  // buffer 전체 복사
                        }
                        // 각 청크를 Cassandra에 저장 (uploadUseCase.upload 구현 필요)
                        uploadFacade.upload(chunkData, fileId, chunkIndex)
                        chunkIndex++
                    }
                }
                ResponseEntity("File uploaded successfully with id: $fileId", HttpStatus.OK)
            } catch (e: Exception) {
                ResponseEntity("File upload failed: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
}