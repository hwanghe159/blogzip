java {
    sourceCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":crawler"))
    implementation(project(":notification"))

    implementation("org.springframework.boot:spring-boot-starter-batch")

    implementation(platform("com.aallam.openai:openai-client-bom:3.7.0"))
    implementation("com.aallam.openai:openai-client")
    runtimeOnly("io.ktor:ktor-client-okhttp")

    testImplementation("org.springframework.batch:spring-batch-test")
    testImplementation("io.mockk:mockk:1.13.10")
}
