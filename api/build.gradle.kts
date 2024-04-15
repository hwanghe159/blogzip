java {
    sourceCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":crawler"))
    implementation(project(":notification"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
}

// plain.jar 생성 방지
tasks.getByName<Jar>("jar") {
    enabled = false
}