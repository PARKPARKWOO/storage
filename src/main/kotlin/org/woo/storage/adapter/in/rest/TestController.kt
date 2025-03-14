package org.woo.storage.adapter.`in`.rest

import io.hypersistence.tsid.TSID
import kotlinx.coroutines.Dispatchers
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
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import org.woo.storage.application.facade.RetrieveFacade
import org.woo.storage.domain.metadata.Metadata
import org.woo.storage.ports.`in`.UploadUseCase
import java.nio.ByteBuffer

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

    @PostMapping(value = ["/upload"])
    suspend fun uploadFile(@RequestPart("file") file: MultipartFile): ResponseEntity<String> =
        withContext(Dispatchers.IO) {
            try {
                // 파일 메타데이터 추출
                val fileOriginName = file.originalFilename ?: "unknown"
                val contentType = file.contentType ?: "application/octet-stream"
                val contentLength = file.size
                val chunkSize = 1024 * 1024  // 1MB
                val id = TSID.fast().toLong()
                uploadFacade.metadata(fileOriginName, "user1", chunkSize, contentLength, 1L, id, 0)

                // 파일을 청크 단위로 읽어서 Cassandra에 저장
                file.inputStream.use { inputStream ->
                    var chunkIndex = 0
                    val buffer = ByteArray(chunkSize)
                    while (true) {
                        val bytesRead = inputStream.read(buffer)
                        if (bytesRead <= 0) break

                        // 실제 읽은 바이트만큼 배열 복사 후 ByteBuffer로 변환
                        val chunkData: ByteArray = if (bytesRead < chunkSize) {
                            buffer.copyOf(bytesRead)
                        } else {
                            buffer.copyOf()
                        }
                        val chunkBuffer = ByteBuffer.wrap(chunkData)
                        uploadFacade.file(chunkBuffer, id, chunkIndex)
                        chunkIndex++
                    }
                }
                ResponseEntity("File uploaded successfully with id: $id", HttpStatus.OK)
            } catch (e: Exception) {
                ResponseEntity("File upload failed: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }

}