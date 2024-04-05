import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"
}

group = "com.blogzip"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("com.rometools:rome:2.1.0")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("org.seleniumhq.selenium:selenium-java:4.19.1")
//    implementation("org.seleniumhq.selenium:selenium-chrome-driver:4.19.1")
//    implementation("org.seleniumhq.selenium:selenium-devtools-v123:4.19.1")
    implementation("io.github.bonigarcia:webdrivermanager:5.7.0")
    implementation("com.vladsch.flexmark:flexmark-all:0.64.8")


    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
