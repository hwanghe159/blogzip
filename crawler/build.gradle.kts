java {
    sourceCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // rss XML 파싱
    implementation("com.rometools:rome:2.1.0")

    // HTML 파싱
    implementation("org.jsoup:jsoup:1.17.2")

    // 동적 웹페이지 크롤링
    implementation("org.seleniumhq.selenium:selenium-java:4.19.1")
    implementation("io.github.bonigarcia:webdrivermanager:5.7.0")

    // HTML -> markdown 변환
    implementation("com.vladsch.flexmark:flexmark-all:0.64.8")
}
