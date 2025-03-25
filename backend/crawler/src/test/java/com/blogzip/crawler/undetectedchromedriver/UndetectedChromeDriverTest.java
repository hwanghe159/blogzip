package com.blogzip.crawler.undetectedchromedriver;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

class UndetectedChromeDriverTest {

  private ChromeDriver driver;

  @BeforeEach
  void setUp() {
    WebDriverManager webDriverManager = WebDriverManager.chromedriver();
    webDriverManager.setup();
    String driverHome = webDriverManager.getDownloadedDriverPath();
    ChromeOptions chromeOptions = new ChromeOptions();
    chromeOptions.addArguments("--window-size=1920,1080");
    this.driver = new ChromeDriverBuilder()
        .build(chromeOptions, driverHome);
  }

  @AfterEach
  void tearDown() {
    this.driver.quit();
  }

  @Test
  void get() {
    String result;
    try {
      driver.get("https://www.google.com/");
      result = driver.getPageSource();
    } catch (Exception e) {
      result = null;
    }
    System.out.println(result);
  }
}