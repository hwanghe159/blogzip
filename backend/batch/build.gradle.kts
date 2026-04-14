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
  implementation(project(":crawler-client"))
  implementation(project(":notification"))
  implementation(project(":logging"))
  implementation(project(":ai"))

  implementation("org.springframework.boot:spring-boot-starter-batch")
  implementation("org.springframework.boot:spring-boot-starter-webflux")

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
  implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")

  val isMacOs = System.getProperty("os.name").contains("Mac", ignoreCase = true)
  val arch = System.getProperty("os.arch").lowercase()
  if (isMacOs) {
    val classifier = if (arch.contains("aarch64") || arch.contains("arm64")) "osx-aarch_64" else "osx-x86_64"
    runtimeOnly("io.netty:netty-resolver-dns-native-macos") {
      artifact {
        this.classifier = classifier
      }
    }
  }

  testImplementation("org.springframework.batch:spring-batch-test")
  testImplementation("io.mockk:mockk:1.13.10")
}
