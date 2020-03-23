package com.dutv.crawler;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SeleniumCrawler {
    public static void main(String[] args) throws InterruptedException, IOException {
        Set<String> set = new HashSet<>();
        Set<String> commentSet = new HashSet<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd.HH0000");
        Date now = new Date();
        String pageId = "1756687557948931";
        String path = "/home/dutv/Desktop/test/selenium/";
        String outputFile = path + format.format(now) + ".tsv";
        String chromePath = "/home/dutv/Desktop/Selenium/chromedriver";
        String email = "duongmanhag2467@69postix.info";
        String password = "duongmanh123";

        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--disable-notifications");
        System.setProperty("webdriver.chrome.driver", chromePath);
        WebDriver driver = new ChromeDriver(ops);
        driver.navigate().to("https://www.facebook.com");
        loginFacebook(driver, email, password);
        TimeUnit.SECONDS.sleep(8);
        visitPage(driver, pageId);
        TimeUnit.SECONDS.sleep(5);
        //scroll
        while (true) {
            for (int i = 0; i < 7; i++) {
                TimeUnit.SECONDS.sleep(3);
                scroll();
                driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
//                WebElement postShow = driver.findElement(By.cssSelector("._50f8"));
//                if (postShow.isDisplayed()) {
//                    scroll();
//                    break;
//                }
            }

            TimeUnit.SECONDS.sleep(2);


            List<WebElement> webElementList = driver.findElements(By.cssSelector(".userContentWrapper"));
            if (webElementList.size() > 0) {
                getInfoFromPost(webElementList, driver, pageId, outputFile, set, commentSet);
                TimeUnit.SECONDS.sleep(2);
            }
            scroll();
        }

    }

    private static void run(String pageId, String email, String passWord, String chromePath, String outputFile) throws InterruptedException, IOException {
        Set<String> set = new HashSet<>();
        Set<String> commentSet = new HashSet<>();
        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--disable-notifications");
        System.setProperty("webdriver.chrome.driver", chromePath);
        WebDriver driver = new ChromeDriver(ops);
        driver.navigate().to("https://www.facebook.com");
        loginFacebook(driver, email, passWord);
        TimeUnit.SECONDS.sleep(8);
        visitPage(driver, pageId);
        TimeUnit.SECONDS.sleep(5);
        //scroll
        while (true) {
            for (int i = 0; i < 7; i++) {
                TimeUnit.SECONDS.sleep(3);
                scroll();
                driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
//                WebElement postShow = driver.findElement(By.cssSelector("._50f8"));
//                if (postShow.isDisplayed()) {
//                    scroll();
//                    break;
//                }
            }

            TimeUnit.SECONDS.sleep(2);


            List<WebElement> webElementList = driver.findElements(By.cssSelector(".userContentWrapper"));
            if (webElementList.size() > 0) {
                getInfoFromPost(webElementList, driver, pageId, outputFile, set, commentSet);
                TimeUnit.SECONDS.sleep(2);
            }
            scroll();
        }
    }

    private static void loginFacebook(WebDriver webDriver, String emai, String password) throws InterruptedException {
        webDriver.findElement(By.cssSelector("#email")).sendKeys(emai);
        TimeUnit.SECONDS.sleep(2);
        webDriver.findElement(By.cssSelector("#pass")).sendKeys(password);

        WebElement element = webDriver.findElement(By.cssSelector(".login_form_login_button"));
        if (element.isDisplayed()) {
            element.click();
        }
    }

    private static void visitPage(WebDriver driver, String pageId) {
        String url = "https://www.facebook.com/";
        driver.navigate().to(url + pageId);
    }

    private static void getInfoFromPost(List<WebElement> webElementList, WebDriver driver, String pageId, String outputFile, Set<String> set, Set<String> commentSet) throws IOException, InterruptedException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Pattern numberPattern = Pattern.compile("\\d+");
        Pattern floatPattern = Pattern.compile("[0-9]+[.][0-9]+");
        String commentNumber = "";
        String shareNumber = "";
        String likeNumber = "";
        for (WebElement webElement : webElementList) {
            TimeUnit.SECONDS.sleep(2);
            String urlPost = webElement.findElement(By.cssSelector("[data-testid=\"UFI2CommentsCount/root\"]")).getAttribute("href");
            if (set.contains(urlPost)) {
                TimeUnit.SECONDS.sleep(2);
            } else {
                moveToElement(webElement, driver);
                boolean checkSeeMore = checkElement(webElement, ".see_more_link_inner");
                if (checkSeeMore) {
                    String namePage = driver.findElement(By.cssSelector("#seo_h1_tag > a > span")).getText();
                    String timePost = webElement.findElement(By.cssSelector(".userContentWrapper div[data-testid=\"story-subtitle\"] abbr")).getAttribute("data-utime");
                    String contentPost = webElement.findElement(By.cssSelector("[data-testid=\"post_message\"]")).getText();
                    String totalLike = webElement.findElement(By.cssSelector(".userContentWrapper [data-testid=\"fbFeedStoryUFI/feedbackSummary\"] [data-testid=\"UFI2ReactionsCount/sentenceWithSocialContext\"] span")).getText();
                    String totalCommentText = webElement.findElement(By.cssSelector("[data-testid=\"UFI2CommentsCount/root\"]")).getText();
                    String totalShareText = webElement.findElement(By.cssSelector("[data-testid=\"UFI2SharesCount/root\"]")).getText();
                    if (totalCommentText.contains(".")) {
                        Matcher matcher = floatPattern.matcher(totalCommentText);
                        if (matcher.find()) {
                            commentNumber = String.valueOf(Double.parseDouble(matcher.group()) * 1000);
                        }
                    } else {
                        Matcher matcher = numberPattern.matcher(totalCommentText);
                        if (matcher.find()) {
                            commentNumber = matcher.group();
                        }
                    }

                    if (totalShareText.contains(".")) {
                        Matcher matcher = floatPattern.matcher(totalCommentText);
                        if (matcher.find()) {
                            shareNumber = String.valueOf(Double.parseDouble(matcher.group()) * 1000);
                        }
                    } else {
                        Matcher matcher = numberPattern.matcher(totalCommentText);
                        if (matcher.find()) {
                            shareNumber = matcher.group();
                        }
                    }
                    if (totalLike.contains(".")) {
                        Matcher matcher = floatPattern.matcher(totalCommentText);
                        if (matcher.find()) {
                            likeNumber = String.valueOf(Double.parseDouble(matcher.group()) * 1000);
                        }
                    } else {
                        Matcher matcher = numberPattern.matcher(totalCommentText);
                        if (matcher.find()) {
                            likeNumber = matcher.group();
                        }
                    }

                    String subPostId = urlPost.substring(urlPost.indexOf("posts/") + 6, urlPost.lastIndexOf(""));
                    String postId = String.join("_", pageId, subPostId);

                    Date timeCrawl = new Date();
                    Date timeCreated = new Date(Long.parseLong(timePost) * 1000);

                    JSONObject result = new JSONObject();

                    JSONObject jsonNode = new JSONObject();
                    jsonNode.put("name", namePage);
                    jsonNode.put("id", pageId);
                    jsonNode.put("type", "page");

                    JSONObject jsonPost = new JSONObject();

                    JSONObject jsonSummary = new JSONObject();
                    jsonSummary.put("like", likeNumber);
                    jsonSummary.put("comment", commentNumber);
                    jsonSummary.put("share", shareNumber);
                    jsonPost.put("summary", jsonSummary);
                    jsonPost.put("id", postId);

                    result.put("post", jsonPost);

                    JSONObject jsonFrom = new JSONObject();
                    jsonFrom.put("id", pageId);
                    jsonFrom.put("name", namePage);
                    result.put("from", jsonFrom);

                    result.put("title", namePage);
                    result.put("content", contentPost);

                    result.put("pub", Long.parseLong(timePost));
                    result.put("crawl", timeCrawl.getTime() / 1000);

                    String finalResult = String.join("\t", urlPost, format.format(timeCrawl), format.format(timeCreated), result.toString());

                    FileUtils.write(new File(outputFile), finalResult + "\n", true);

                    // check Many comment was show
                    boolean manyCommentIsShow = checkElement(webElement, "[data-testid=\"UFI2CommentsPagerRenderer/pager_depth_0\"]");
                    if (manyCommentIsShow) {
                        TimeUnit.SECONDS.sleep(2);
                    }

                    List<WebElement> elementCommentList = webElement.findElements(By.cssSelector("[data-testid=\"UFI2Comment/root_depth_0\"]"));
                    getInforComment(elementCommentList, contentPost, postId, pageId, namePage, outputFile, commentSet);
                    getSubComment(elementCommentList,postId, pageId, pageId, outputFile);
                }
            }
        }
        scroll();
    }

    private static void getInforComment(List<WebElement> commentElementList, String contentPost, String postId, String pageId, String pageName, String outputFile, Set<String> set) {
        System.out.println("COMMENT");
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        try {

            for (WebElement webElement : commentElementList) {
                String urlComment =
                        webElement.findElement(By.cssSelector("ul[data-testid=\"UFI2CommentActionLinks/root\"] a[class=\"_6qw7\"]")).getAttribute("href");
                if (set.contains(urlComment)) {
                    TimeUnit.SECONDS.sleep(2);
                } else {

                    String userId =
                            webElement.findElement(By.cssSelector("a[aria-hidden=\"true\"]")).getAttribute("data-hovercard");
                    String subUserId = userId.substring(userId.indexOf("?id=") + 4, userId.lastIndexOf(""));
                    String userName =
                            webElement.findElement(By.cssSelector("a[aria-hidden=\"true\"] img")).getAttribute("alt");
                    String contentComment = "";

                    int size = webElement.findElements(By.cssSelector("[data-testid=\"UFI2Comment/body\"] span span")).size();
                    if (size != 0) {
                        contentComment =
                                webElement.findElement(By.cssSelector("[data-testid=\"UFI2Comment/body\"] span span")).getText();
                    }

                    String idPostFromUrlComment = urlComment.substring(urlComment.indexOf("posts/") + 6, urlComment.indexOf("?comment_id"));
                    String idCommentFromUrlComment = urlComment.substring(urlComment.indexOf("_id=") + 4, urlComment.lastIndexOf(""));
                    String commentId = String.join("_", idPostFromUrlComment, idCommentFromUrlComment);


                    String timeComment =
                            webElement.findElement(By.cssSelector("ul[data-testid=\"UFI2CommentActionLinks/root\"] a[class=\"_6qw7\"] abbr")).getAttribute("data-utime");

                    Date timeCrawl = new Date();
                    Date timCreate = new Date(Long.parseLong(timeComment) * 1000);

                    JSONObject jsonResult = new JSONObject();

                    // node
                    JSONObject jsonNode = new JSONObject();
                    jsonNode.put("name", pageName);
                    jsonNode.put("id", pageId);
                    jsonNode.put("type", "page");

                    jsonResult.put("node", jsonNode);

                    JSONObject jsonPost = new JSONObject();
                    jsonPost.put("id", postId);
                    jsonResult.put("post", jsonPost);

                    JSONObject jsonComment = new JSONObject();
                    jsonComment.put("id", commentId);
                    jsonResult.put("comment", jsonComment);

                    JSONObject jsonFrom = new JSONObject();
                    jsonFrom.put("name", userName);
                    jsonFrom.put("id", subUserId);
                    jsonResult.put("from", jsonFrom);

                    jsonResult.put("title", contentPost);
                    jsonResult.put("pub", Long.parseLong(timeComment));
                    jsonResult.put("content", contentComment);
                    jsonResult.put("crawl", timeCrawl.getTime() / 1000);

                    String finalResult = String.join("\t", urlComment, format.format(timeCrawl), format.format(timCreate), jsonResult.toString());

                    FileUtils.write(new File(outputFile), finalResult + "\n", true);
                    set.add(urlComment);
                }
            }
        } catch (NoSuchElementException | IOException | InterruptedException e) {
            System.out.println("ERROR");
        }
    }

    private static void getSubComment(List<WebElement> elementList, String postId, String pageId, String pageName, String outputFile) {
        for (WebElement webElement : elementList) {
            boolean checkSubElement = checkElement(webElement, "[data-testid=\"UFI2CommentsPagerRenderer/pager_depth_1\"] span");
            if (checkSubElement) {
                List<WebElement> subList = webElement.findElements(By.cssSelector("[data-testid=\"UFI2Comment/root_depth_1\"]"));
                if (subList.size() > 0) {
                    for (WebElement elementSubComment : subList) {
                        String userName = elementSubComment.findElement(By.cssSelector("[data-testid=\"UFI2Comment/body\"] >div >a")).getText();
                        System.out.println("USERNAME: " + userName);
                        String userUrlId = elementSubComment.findElement(By.cssSelector("[data-testid=\"UFI2Comment/body\"] >div >a")).getAttribute("data-hovercard");
                        System.out.println("URL ID: " + userUrlId);
                        String contentMessage = elementSubComment.findElement(By.cssSelector("[data-testid=\"UFI2Comment/body\"] span")).getText();
                        System.out.println("CONTENT: " + contentMessage);
                        String timeSubComment = elementSubComment.findElement(By.cssSelector("a[class=\"_6qw7\"] abbr")).getAttribute("data-utime");
                        System.out.println("TIME: " + timeSubComment);
                    }
                }
            }
        }
    }

    private static boolean checkElement(WebElement element, String css) {
        try {
            int size = element.findElements(By.cssSelector(css)).size();
            if (size != 0) {
                WebElement webElement = element.findElement(By.cssSelector(css));
                if (webElement.isDisplayed() && webElement.isEnabled()) {
                    webElement.click();
                }
                return true;
            }
        } catch (NoSuchElementException e) {
            System.out.println("Not found Element");
        }
        return false;
    }

    private static void scroll() {
        Robot robot = null;
        try {
            robot = new Robot();
            robot.keyPress(KeyEvent.VK_PAGE_DOWN);
            robot.keyRelease(KeyEvent.VK_PAGE_DOWN);
        } catch (AWTException e) {
            System.out.println("EXCEPTION");
        }
    }

    private static void moveToElement(WebElement webElement, WebDriver driver) throws InterruptedException {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", webElement);
        Thread.sleep(500);
    }
}
