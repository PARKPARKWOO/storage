import com.google.protobuf.gradle.id

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.google.protobuf") version "0.9.4"
}

group = "org.woo"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/PARKPARKWOO/common-module")
        credentials {
            username = project.findProperty("gpr.user")?.toString() ?: System.getenv("GITHUB_USERNAME")
            password = project.findProperty("gpr.key")?.toString() ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

val protobufVersion = "3.23.4"
val grpcVersion = "1.63.0"
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // mysql
    runtimeOnly("com.mysql:mysql-connector-j")
    implementation("io.asyncer:r2dbc-mysql:1.3.0")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")

    // flyway for db migration
    implementation("org.flywaydb:flyway-core:10.19.0")
    implementation("org.flywaydb:flyway-mysql:10.19.0")

    implementation("org.woo:grpc:0.1.3")
    implementation("io.grpc:grpc-netty-shaded:$grpcVersion")
    // https://mvnrepository.com/artifact/net.devh/grpc-spring-boot-starter
//    implementation("net.devh:grpc-spring-boot-starter:3.1.0.RELEASE")
    implementation("net.devh:grpc-server-spring-boot-starter:3.1.0.RELEASE") {
        exclude(group = "io.grpc", module = "grpc-netty-shaded")
        exclude(group = "io.grpc", module = "grpc-protobuf")
//        exclude(group = "io.grpc", module = "grpc-")
    }
    implementation("io.grpc:grpc-kotlin-stub:1.4.1")

    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

    // my utils
    implementation("org.woo:util:0.0.1")

    // cassandra
    implementation("org.springframework.boot:spring-boot-starter-data-cassandra-reactive")

    // tsid
    implementation("io.hypersistence:hypersistence-tsid:2.1.4")

    // for metric
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    implementation("org.woo:apm:+")

    // log-loki
    implementation("com.github.loki4j:loki-logback-appender:1.5.1")
}

extra["springCloudVersion"] = "2023.0.0"

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.2.0:jdk7@jar"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc") { }
                id("grpckt") {}
            }
        }
    }
}
