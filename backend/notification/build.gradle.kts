java {
    sourceCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("software.amazon.awssdk:ses:2.17.100")
    implementation("com.slack.api:slack-api-client:1.39.1")
}
