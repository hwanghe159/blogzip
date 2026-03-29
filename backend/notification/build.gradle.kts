java {
  sourceCompatibility = JavaVersion.VERSION_21
}

dependencies {
  implementation(project(":logging"))

  implementation("org.springframework.boot:spring-boot-starter-mail")
  implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
}
