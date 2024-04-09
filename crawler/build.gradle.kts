java {
    sourceCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    implementation("com.rometools:rome:2.1.0")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("org.seleniumhq.selenium:selenium-java:4.19.1")
//    implementation("org.seleniumhq.selenium:selenium-chrome-driver:4.19.1")
//    implementation("org.seleniumhq.selenium:selenium-devtools-v123:4.19.1")
    implementation("io.github.bonigarcia:webdrivermanager:5.7.0")
    implementation("com.vladsch.flexmark:flexmark-all:0.64.8")


    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
