java {
    sourceCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation("org.springframework.boot:spring-boot:3.2.4") // todo 버전 변수로 추출
    implementation("com.slack.api:slack-api-client:1.39.1")
    implementation("org.slf4j:slf4j-api:2.0.16")
}
