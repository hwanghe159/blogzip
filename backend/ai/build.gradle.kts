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
}
