package org.woo.storage

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class StorageApplication

fun main(args: Array<String>) {
    runApplication<StorageApplication>(*args)
}
