plugins {
    kotlin("plugin.jpa") version "1.9.23"
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation(project(":logging"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("com.h2database:h2")
}
