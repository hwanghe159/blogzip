java {
  sourceCompatibility = JavaVersion.VERSION_21
}

dependencies {
  implementation(project(":domain"))
  implementation(project(":crawler-client"))
  implementation(project(":notification"))
  implementation(project(":logging"))
  implementation(project(":ai"))

  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
  implementation("org.jsoup:jsoup:1.17.2")

  // auth
  implementation("org.springframework.security:spring-security-crypto")
  implementation("io.jsonwebtoken:jjwt-api:0.12.5")

  runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
  runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")
}

// plain.jar 생성 방지
tasks.getByName<Jar>("jar") {
  enabled = false
}
