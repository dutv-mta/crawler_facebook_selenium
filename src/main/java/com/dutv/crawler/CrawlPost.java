package com.dutv.crawler;

import com.dutv.utils.FileAppend;
import com.dutv.utils.TimeUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class CrawlPost {
    static Logger logger = LoggerFactory.getLogger(Crawler.class);
    private static final SimpleDateFormat localFormatter = new SimpleDateFormat("yyyyMMdd");

    public static void main(String[] args) throws InterruptedException, IOException {
        String email = "dangchiengi5955@69postix.info";
        String password = "dangchien123";
        String fileName = "/home/dutv/Desktop/urlPost.txt";
        Set<String> set = new HashSet<>();
        String pageId = "402546526524902";

        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--disable-notifications");
        String chromePath = "/home/dutv/Desktop/Selenium/chromedriver";
        System.setProperty("webdriver.chrome.driver", chromePath);
        WebDriver driver = new ChromeDriver(ops);
        driver.navigate().to("https://www.facebook.com/login");
        driver.manage().window().maximize();
        loginFacebook(driver, email, password);
        driver.navigate().to("https://www.facebook.com/" + pageId);
        JavascriptExecutor jse = (JavascriptExecutor) driver;
        do {
            for (int i = 0; i < 5; i++) {
                TimeUnit.SECONDS.sleep(2);
                jse.executeScript("window.scrollTo(0, document.body.scrollHeight)");
            }
            List<WebElement> webElementList = driver.findElements(By.cssSelector(".userContentWrapper"));
            if (webElementList.size() > 0) {
                for (WebElement webElement : webElementList) {
                    String timePost = webElement.findElement(By.cssSelector(".userContentWrapper div[data-testid=\"story-subtitle\"] abbr")).getAttribute("data-utime");
                    LocalDate nowLocalDate = LocalDate.now();
                    Date datePost = new Date(Long.parseLong(timePost) * 1000);
                    String strLocalTimePost = localFormatter.format(datePost);
                    LocalDate localTimePost = LocalDate.parse(strLocalTimePost, TimeUtils.format);
                    if (localTimePost == nowLocalDate.minusDays(2)) {
                        return;
                    }
                    getPost(webElement, set, fileName, pageId);
                }
            }
        } while (true);
    }

    private static void getPost(WebElement webElement, Set<String> set, String fileName, String pageId) throws IOException {
        int size = webElement.findElements(By.cssSelector("[data-testid=\"UFI2CommentsCount/root\"]")).size();
        if (size == 0) {
            logger.error("Can not get url post");
        }
        String urlPost = webElement.findElement(By.cssSelector("[data-testid=\"UFI2CommentsCount/root\"]")).getAttribute("href");
        if (!set.contains(urlPost)) {
            String splitUrl = urlPost.substring(urlPost.indexOf("posts/"), urlPost.lastIndexOf(""));
            String url = String.join("/", "https://www.facebook.com", pageId, splitUrl);
            set.add(urlPost);
            FileAppend.append(url + "\n", fileName);
        }
    }

    private static void loginFacebook(WebDriver webDriver, String emai, String password) {
        // login
        try {
            while (true) {
                System.out.println("PREPARE LOGIN");
                int size = webDriver.findElements(By.cssSelector("#email")).size();
                if (size > 0) {
                    webDriver.findElement(By.cssSelector("#email")).sendKeys(emai);
                    TimeUnit.SECONDS.sleep(1);
                    webDriver.findElement(By.cssSelector("#pass")).sendKeys(password);

                    WebElement element = webDriver.findElement(By.cssSelector("#loginbutton"));
                    if (element.isDisplayed()) {
                        element.click();
                    }
                    System.out.println("login success");
                    break;
                }
            }
        } catch (InterruptedException e) {
            logger.error("InterruptedException: ", e);
        }
    }
}
