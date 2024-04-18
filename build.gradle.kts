import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.4" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
    kotlin("jvm") version "1.9.23" apply false
    kotlin("plugin.spring") version "1.9.23" apply false
}

subprojects {
    if (name != "web") {
        apply(plugin = "org.springframework.boot")
        apply(plugin = "io.spring.dependency-management")
        apply(plugin = "kotlin")
        apply(plugin = "org.jetbrains.kotlin.jvm")
        apply(plugin = "kotlin-spring")

        group = "com.blogzip"
        version = "0.0.1-SNAPSHOT"

        repositories {
            mavenCentral()
        }

        dependencies {
            val implementation by configurations
            val annotationProcessor by configurations
            val testImplementation by configurations

            implementation("org.jetbrains.kotlin:kotlin-reflect")
            annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
            testImplementation("org.springframework.boot:spring-boot-starter-test")
        }

        tasks.withType<KotlinCompile> {
            kotlinOptions {
                freeCompilerArgs += "-Xjsr305=strict"
                jvmTarget = "21"
            }
        }

        tasks.withType<Test> {
            useJUnitPlatform {
                excludeTags("exclude") // @Tag("exclude") 으로 빌드 시 테스트 제외
            }
        }
    }
}
