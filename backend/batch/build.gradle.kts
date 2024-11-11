java {
    sourceCompatibility = JavaVersion.VERSION_21
}

val springCloudVersion by extra("2023.0.3")
dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":crawler"))
    implementation(project(":notification"))
    implementation(project(":logging"))
    implementation(project(":ai"))

    implementation("org.springframework.boot:spring-boot-starter-batch")

    // OpenAI
    implementation(platform("com.aallam.openai:openai-client-bom:3.7.0"))
    implementation("com.aallam.openai:openai-client")
    runtimeOnly("io.ktor:ktor-client-okhttp")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")

    testImplementation("org.springframework.batch:spring-batch-test")
    testImplementation("io.mockk:mockk:1.13.10")
}
