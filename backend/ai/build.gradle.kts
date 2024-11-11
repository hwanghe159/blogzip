java {
    sourceCompatibility = JavaVersion.VERSION_21
}

// https://spring.io/projects/spring-cloud
val springCloudVersion by extra("2023.0.3")
dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
    }
}

dependencies {
    // https://github.com/spring-cloud/spring-cloud-release/wiki/Supported-Versions#supported-releases
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign:4.1.3")
//    implementation("io.github.openfeign:feign-okhttp")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
