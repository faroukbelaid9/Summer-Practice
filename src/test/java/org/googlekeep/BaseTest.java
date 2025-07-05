package org.googlekeep;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.support.ui.*;
import org.testng.annotations.*;
import java.io.*;
import java.time.*;

/**
 * Base test class for setting up and tearing down the WebDriver.
 */
public class BaseTest {
    protected WebDriver driver;
    protected WebDriverWait wait;

    protected static final Duration IMPLICIT_WAIT = Duration.ofSeconds(5);
    protected static final Duration EXPLICIT_WAIT = Duration.ofSeconds(20);

    @BeforeMethod
    public void setUp() {
        killChromeProcesses();
        ChromeOptions options = configureChromeOptions();
        initializeDriver(options);
        open();
    }

    protected void open() {
        driver.get("https://keep.google.com/u/0/");
        wait.until(ExpectedConditions.jsReturnsValue("return document.readyState === 'complete'"));
    }

    private void killChromeProcesses() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                Runtime.getRuntime().exec("taskkill /F /IM chrome.exe /T");
                Runtime.getRuntime().exec("taskkill /F /IM chromedriver.exe /T");
            } else {
                Runtime.getRuntime().exec("pkill -f chrome");
                Runtime.getRuntime().exec("pkill -f chromedriver");
            }
            Thread.sleep(2000);
        } catch (Exception e) {
            System.out.println("Process cleanup warning: " + e.getMessage());
        }
    }

    private ChromeOptions configureChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        String tempProfile = "C:\\temp\\chrome_profile";
        new File(tempProfile).mkdirs();

        options.addArguments(
                "user-data-dir=" + tempProfile,
                "--remote-allow-origins=*",
                "--start-maximized",
                "--disable-extensions",
                "--disable-blink-features=AutomationControlled"
        );
        return options;
    }

    private void initializeDriver(ChromeOptions options) {
        System.setProperty("webdriver.chrome.driver", "C:\\chromedriver-win64\\chromedriver.exe");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT);
        wait = new WebDriverWait(driver, EXPLICIT_WAIT);
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            //driver.quit();
        }
    }
}