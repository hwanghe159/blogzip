java {
  sourceCompatibility = JavaVersion.VERSION_21
}

dependencies {
  implementation(project(":logging"))

  implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
  implementation("software.amazon.awssdk:ses:2.17.100")
}
