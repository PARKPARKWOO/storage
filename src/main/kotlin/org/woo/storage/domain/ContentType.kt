package org.woo.storage.domain

enum class ContentType(
//    val mimeType: MimeType,
) {
    PDF,
    JPEG,
    PNG,
//    GIF(MimeType.valueOf()),
    MP4,

    // MIME 미디어 타입을 크게 분류 한다?
    IMAGE,
    VIDEO,
}