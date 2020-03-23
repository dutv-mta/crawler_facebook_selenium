package com.dutv.crawler;

import com.dutv.utils.FileAppend;
import org.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetDataPost {
    private static Logger logger = LoggerFactory.getLogger(Crawler.class);

    public static void main(String[] args) throws IOException {
        List<String> list = Files.readAllLines(Paths.get("/home/dutv/Desktop/test/selenium/result.txt"));
//        List<String> list = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd.HH0000");
        Date now = new Date();
        String path = "/home/dutv/Desktop/test/selenium/";
        String chromePath = "/home/dutv/Desktop/Selenium/chromedriver";
        String email = "buimittb6333@69postix.info";
        String password = "buimit123";
        Set<Map<String, String>> dataPost = new HashSet<>();

        try {
            dataPost = ElasticSearch.getDataPostTest();
        } catch (UnknownHostException e ) {
            logger.error("Exception when connect to Elastic");
        }
        for (Map<String, String> map : dataPost) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String pageId = entry.getKey();
                String url = entry.getValue();
                String result = String.join(",", pageId, url);
                FileAppend.append(result + "\n", "/home/dutv/Desktop/test/selenium/20191221_url.txt");
            }
        }
        System.out.println(dataPost.size());
        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--disable-notifications");
        ops.addArguments("--start-maximized");
        System.setProperty("webdriver.chrome.driver", chromePath);
        WebDriver driver = new ChromeDriver(ops);
        driver.navigate().to("https://www.facebook.com");
        loginFacebook(driver, email, password, chromePath);
        try {
            list.forEach(line -> {
                String[] split = line.split(",");
                String pageId = split[0];
                String url = split[1];
                driver.get(url);
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    logger.error("InterruptedException: ", e);
                }
                Actions action = new Actions(driver);
                action.sendKeys(Keys.ESCAPE).build().perform();
                checkNotNowButton(driver);
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    logger.error("InterruptedException: ", e);
                }
                run(driver, url, pageId, email, password, path, chromePath);
            });
        } catch (Exception e) {
            logger.error("Exception: ", e);
        }

    }

    private static void checkNotNowButton(WebDriver driver) {
        try {
            int notNowButton = driver.findElements(By.cssSelector("#expanding_cta_close_button")).size();
            if (notNowButton > 0) {
                driver.findElement(By.cssSelector("#expanding_cta_close_button")).click();
            }
        } catch (Exception e) {
            logger.error("Can not click Not Now Button ", e);
        }
    }

    private static void run(WebDriver driver, String url, String pageId, String email, String password, String outputFile, String chromePath) {
        try {

            Set<String> set = new HashSet<>();
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd.HH0000");
            Date now = new Date();
            String fileName = outputFile + "post." + format.format(now) + ".tsv";
            TimeUnit.SECONDS.sleep(2);

            List<WebElement> webElementList = driver.findElements(By.cssSelector(".userContentWrapper"));
            if (webElementList.size() > 0) {
                for (WebElement webElement : webElementList) {
                    try {
                        Actions actions = new Actions(driver);
                        actions.sendKeys(Keys.ESCAPE).build().perform();
                        String inputUrlSub = url.substring(url.indexOf("posts/") + 6, url.lastIndexOf(""));
                        int size = webElement.findElements(By.cssSelector("[data-testid=\"UFI2CommentsCount/root\"]")).size();
                        if (size > 0) {
                            String urlPost = webElement.findElement(By.cssSelector("[data-testid=\"UFI2CommentsCount/root\"]")).getAttribute("href");
                            if (urlPost.contains(inputUrlSub)) {

                                getTotal(webElement, driver, set, pageId, fileName);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Exception when run: ", e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception when run outside. ", e);
        }

    }

    private static void getTotal(WebElement webElement, WebDriver driver, Set<String> set, String pageId, String outputFile) throws InterruptedException {
        int size = webElement.findElements(By.cssSelector("[data-testid=\"UFI2CommentsCount/root\"]")).size();
        if (size == 0) {
            return;
        }
        String urlPost = webElement.findElement(By.cssSelector("[data-testid=\"UFI2CommentsCount/root\"]")).getAttribute("href");
        if (set.contains(urlPost)) {
            return;
        }

        moveToElement(webElement, driver);
        TimeUnit.SECONDS.sleep(1);

        // Check See More
        checkSeeMore(webElement);
        TimeUnit.SECONDS.sleep(1);

        // get Post
        LinkedList<String> linkedList = getDataPost(webElement, driver, urlPost, pageId, outputFile);

        // Check View More
        checkViewMore(webElement);
        TimeUnit.SECONDS.sleep(1);


        // Get Comment 0
//        getDataComment(webElement, pageId, outputFile, linkedList);


        // Check ReplyComment
//        checkReply(webElement, driver, linkedList, pageId, outputFile);


        // add link post to set
//        set.add(urlPost);

    }

    private static void checkSeeMore(WebElement webElement) {
        try {
            boolean checkSeeMore = checkElement(webElement, ".see_more_link_inner");
            if (checkSeeMore) {
                WebElement element = webElement.findElement(By.cssSelector(".see_more_link_inner"));
                element.click();
            }
        } catch (Exception e) {
            logger.error("Exception when check Xem Thêm: ", e);
        }

    }

    private static void checkViewMore(WebElement webElement) {

        while (true) {
            try {
                boolean viewMoreComment = checkElement(webElement, "[data-testid=\"UFI2CommentsPagerRenderer/pager_depth_0\"]");
                if (viewMoreComment) {
                    int size = webElement.findElements(By.cssSelector("[data-testid=\"UFI2CommentsPagerRenderer/pager_depth_0\"]")).size();
                    if (size > 0) {
                        webElement.findElement(By.cssSelector("[data-testid=\"UFI2CommentsPagerRenderer/pager_depth_0\"]")).click();
                        scroll();
                        TimeUnit.SECONDS.sleep(1);
                    }
                    break;
                } else {
                    break;
                }
            } catch (InterruptedException e) {
                logger.error("InterruptedException: ", e);
            }
        }

    }

    private static void checkReply(WebElement webElement, WebDriver driver, LinkedList<String> linkedList, String pageId, String outputFile) {
        Set<String> set = new HashSet<>();
        if (linkedList == null) {
            return;
        }
        try {
            List<WebElement> elements = webElement.findElements(By.cssSelector("[data-testid=\"UFI2CommentsPagerRenderer/pager_depth_1\"] span"));
            for (WebElement element : elements) {
                try {
                    moveToElement(webElement, driver);
                    element.click();

                    // get Comment 1
                    getSubComment(webElement, linkedList, pageId, outputFile, set);
                } catch (Exception e) {
                    logger.error("Exception when check reply button: ", e);
                }
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
        }

    }

    private static boolean checkElement(WebElement element, String css) {
        try {
            int size = element.findElements(By.cssSelector(css)).size();
            if (size != 0) {
                return true;
            }
        } catch (NoSuchElementException e) {
            logger.error("Not found Element");
        }
        return false;
    }

    private static LinkedList<String> getDataPost(WebElement element, WebDriver driver, String urlPost, String pageId, String outputFile) {
        LinkedList<String> linkedList = new LinkedList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        SimpleDateFormat localFormatter = new SimpleDateFormat("yyyyMMdd");
        try {
            Pattern numberPattern = Pattern.compile("\\d+");
            Pattern floatPattern = Pattern.compile("[0-9]+[.][0-9]+");
            String commentNumber = "";
            String shareNumber = "";
            String likeNumber = "";

            String namePage = driver.findElement(By.cssSelector(".fwb a")).getText();
            String timePost = element.findElement(By.cssSelector(".fsm abbr")).getAttribute("data-utime");

            String contentPost = "";
            int size = element.findElements(By.cssSelector("[data-testid=\"post_message\"]")).size();
            if (size > 0) {
                contentPost = element.findElement(By.cssSelector("[data-testid=\"post_message\"]")).getText().replace("<br> See Translation", "");
            }

            String totalLike = "";
            int likeSize = element.findElements(By.cssSelector(".userContentWrapper [data-testid=\"fbFeedStoryUFI/feedbackSummary\"] [data-testid=\"UFI2ReactionsCount/sentenceWithSocialContext\"] span")).size();
            if (likeSize > 0) {
                totalLike = element.findElement(By.cssSelector(".userContentWrapper [data-testid=\"fbFeedStoryUFI/feedbackSummary\"] [data-testid=\"UFI2ReactionsCount/sentenceWithSocialContext\"] span")).getText();
            }

            String totalCommentText = "";

            int sizeComment = element.findElements(By.cssSelector("[data-testid=\"UFI2CommentsCount/root\"]")).size();
            if (sizeComment > 0) {
                totalCommentText = element.findElement(By.cssSelector("[data-testid=\"UFI2CommentsCount/root\"]")).getText();
            }

            String totalShareText = "";
            int sizeShare = element.findElements(By.cssSelector("[data-testid=\"UFI2SharesCount/root\"]")).size();
            if (sizeShare > 0) {
                totalShareText = element.findElement(By.cssSelector("[data-testid=\"UFI2SharesCount/root\"]")).getText();
            }

            if (totalCommentText.contains(".")) {
                Matcher matcher = floatPattern.matcher(totalCommentText);
                if (matcher.find()) {
                    commentNumber = String.valueOf((int) (Double.parseDouble(matcher.group()) * 1000));
                }
            } else if (totalCommentText.contains(",")) {
                totalCommentText = totalCommentText.replaceAll(",",".");
                Matcher matcher = floatPattern.matcher(totalCommentText);
                if (matcher.find()) {
                    commentNumber = String.valueOf((int) (Double.parseDouble(matcher.group()) * 1000));
                }
            }
            else {
                if (totalCommentText.contains("K")) {
                    Matcher matcher = numberPattern.matcher(totalCommentText);
                    if (matcher.find()) {
                        commentNumber = String.valueOf(Integer.parseInt(matcher.group()) * 1000);
                    }
                } else {
                    Matcher matcher = numberPattern.matcher(totalCommentText);
                    if (matcher.find()) {
                        commentNumber = matcher.group();
                    }
                }
            }

            if (totalShareText.contains(".")) {
                Matcher matcher = floatPattern.matcher(totalShareText);
                if (matcher.find()) {
                    shareNumber = String.valueOf((int) (Double.parseDouble(matcher.group()) * 1000));
                }
            } else if (totalShareText.contains(",")) {
                totalShareText = totalShareText.replaceAll(",", ".");
                Matcher matcher = floatPattern.matcher(totalShareText);
                if (matcher.find()) {
                    shareNumber = String.valueOf((int) (Double.parseDouble(matcher.group()) * 1000));
                }
            }
            else {
                if (totalShareText.contains("K")) {
                    Matcher matcher = numberPattern.matcher(totalShareText);
                    if (matcher.find()) {
                        shareNumber = String.valueOf(Integer.parseInt(matcher.group()) * 1000);
                    }
                } else {
                    Matcher matcher = numberPattern.matcher(totalShareText);
                    if (matcher.find()) {
                        shareNumber = matcher.group();
                    }
                }
            }
            if (totalLike.contains(".")) {
                Matcher matcher = floatPattern.matcher(totalLike);
                if (matcher.find()) {
                    likeNumber = String.valueOf((int) (Double.parseDouble(matcher.group()) * 1000));
                }
            } else if (totalLike.contains(",")) {
                totalLike = totalLike.replaceAll(",", ".");
                Matcher matcher = floatPattern.matcher(totalLike);
                if (matcher.find()) {
                    likeNumber = String.valueOf((int) (Double.parseDouble(matcher.group()) * 1000));
                }
            }
            else {
                if (totalLike.contains("K")) {
                    Matcher matcher = numberPattern.matcher(totalLike);
                    if (matcher.find()) {
                        likeNumber = String.valueOf(Integer.parseInt(matcher.group()) * 1000);
                    }
                } else {
                    Matcher matcher = numberPattern.matcher(totalLike);
                    if (matcher.find()) {
                        likeNumber = matcher.group();
                    }
                }
            }
            String idPost = "";
            if (urlPost.contains("posts")) {
                String subPostId = urlPost.substring(urlPost.indexOf("posts/") + 6, urlPost.lastIndexOf(""));
                idPost = String.join("_", pageId, subPostId);
            } else {
                String subPostId = urlPost.substring(urlPost.indexOf("videos/") + 7, urlPost.lastIndexOf(""));
                idPost = String.join("_", pageId, subPostId);
            }


            String splitUrl = urlPost.substring(urlPost.indexOf("posts/"), urlPost.lastIndexOf(""));
            String url = String.join("/", "https://www.facebook.com", pageId, splitUrl);

            Date timeCrawl = new Date();
            Date timeCreated = new Date(Long.parseLong(timePost) * 1000);

            JSONObject result = new JSONObject();

            JSONObject jsonNode = new JSONObject();
            jsonNode.put("name", namePage);
            jsonNode.put("id", pageId);
            jsonNode.put("type", "page");
            result.put("node", jsonNode);

            JSONObject jsonPost = new JSONObject();

            JSONObject jsonSummary = new JSONObject();
            jsonSummary.put("like", likeNumber);
            jsonSummary.put("comment", commentNumber);
            jsonSummary.put("share", shareNumber);
            jsonPost.put("summary", jsonSummary);
            jsonPost.put("id", idPost);

            result.put("post", jsonPost);

            JSONObject jsonFrom = new JSONObject();
            jsonFrom.put("id", pageId);
            jsonFrom.put("name", namePage);
            result.put("from", jsonFrom);

            result.put("title", namePage);
            result.put("content", contentPost);

            result.put("pub", Long.parseLong(timePost));
            result.put("crawl", timeCrawl.getTime() / 1000);

            String finalResult = String.join("\t", url, format.format(timeCrawl), format.format(timeCreated), result.toString());

            FileAppend.append(finalResult + "\n", outputFile);


            linkedList.add(namePage);
            linkedList.add(idPost);
            linkedList.add(contentPost);
            logger.info(String.format("Crawl post id %s successfully!", idPost));

        } catch (Exception e) {
            logger.error("Exception: ", e);
        }
        return linkedList;
    }

    private static void getDataComment(WebElement webElement, String pageId, String outputFile, LinkedList<String> linkedList) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        try {
            List<WebElement> elements = webElement.findElements(By.cssSelector("[data-testid=\"UFI2Comment/root_depth_0\"]"));
            if (elements.size() > 0) {
                for (WebElement element : elements) {
                    try {
                        String urlComment =
                                element.findElement(By.cssSelector("ul[data-testid=\"UFI2CommentActionLinks/root\"] a[class=\"_6qw7\"]")).getAttribute("href");

                        String splitUrl = urlComment.substring(urlComment.indexOf("posts/"), urlComment.lastIndexOf(""));
                        String url = String.join("/", "https://www.facebook.com", pageId, splitUrl);

                        String userId =
                                element.findElement(By.cssSelector("a[aria-hidden=\"true\"]")).getAttribute("data-hovercard");
                        String subUserId = userId.substring(userId.indexOf("?id=") + 4, userId.lastIndexOf(""));
                        String userName =
                                element.findElement(By.cssSelector("a[aria-hidden=\"true\"] img")).getAttribute("alt");
                        String contentComment = "";
                        int size = element.findElements(By.cssSelector("[data-testid=\"UFI2Comment/body\"] span span")).size();
                        if (size != 0) {
                            contentComment =
                                    element.findElement(By.cssSelector("[data-testid=\"UFI2Comment/body\"] span span")).getText();
                        }
                        String idPostFromUrlComment = urlComment.substring(urlComment.indexOf("posts/") + 6, urlComment.indexOf("?comment_id"));
                        String idCommentFromUrlComment = urlComment.substring(urlComment.indexOf("_id=") + 4, urlComment.lastIndexOf(""));
                        String commentId = String.join("_", idPostFromUrlComment, idCommentFromUrlComment);


                        String timeComment =
                                element.findElement(By.cssSelector("ul[data-testid=\"UFI2CommentActionLinks/root\"] a[class=\"_6qw7\"] abbr")).getAttribute("data-utime");

                        Date timeCrawl = new Date();
                        Date timCreate = new Date(Long.parseLong(timeComment) * 1000);

                        JSONObject jsonResult = new JSONObject();

                        String pageName = linkedList.get(0);
                        String postId = linkedList.get(1);
                        String contentPost = linkedList.get(2);

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

                        String finalResult = String.join("\t", url, format.format(timeCrawl), format.format(timCreate), jsonResult.toString());

                        FileAppend.append(finalResult + "\n", outputFile);
                        logger.info(String.format("Crawl comment id: %s successfully", commentId));
                    } catch (Exception e) {
                        logger.error("Exception when get data comment: ", e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
        }

    }

    private static void getSubComment(WebElement webElement, LinkedList<String> linkedList, String pageId, String outputFile, Set<String> set) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        try {
            List<WebElement> elements = webElement.findElements(By.cssSelector("[data-testid=\"UFI2Comment/root_depth_1\"]"));
            if (elements.size() > 0) {
                for (WebElement element : elements) {
                    try {
                        String urlComment = element.findElement(By.cssSelector("[data-testid=\"UFI2CommentActionLinks/root\"] a._6qw7")).getAttribute("href");
                        if (set.contains(urlComment)) {
                            continue;
                        }
                        String splitUrl = urlComment.substring(urlComment.indexOf("posts/"), urlComment.lastIndexOf(""));
                        String url = String.join("/", "https://www.facebook.com", pageId, splitUrl);

                        String userName = element.findElement(By.cssSelector("[data-testid=\"UFI2Comment/body\"] ._72vr a")).getText();
                        String userUrlId = element.findElement(By.cssSelector("[data-testid=\"UFI2Comment/body\"] ._72vr a")).getAttribute("data-hovercard");
                        String userId = userUrlId.substring(userUrlId.indexOf("?id") + 4, userUrlId.indexOf("&extra"));
                        String contentComment = "";
                        int size = element.findElements(By.cssSelector("[data-testid=\"UFI2Comment/body\"] ._72vr span")).size();
                        if (size > 0) {
                            contentComment = element.findElement(By.cssSelector("[data-testid=\"UFI2Comment/body\"] ._72vr span")).getText();
                        }

                        String timeComment = element.findElement(By.cssSelector("[data-testid=\"UFI2CommentActionLinks/root\"] a._6qw7 abbr")).getAttribute("data-utime");
                        set.add(urlComment);


                        String idComment = urlComment.substring(urlComment.indexOf("?comment_id=") + 12, urlComment.indexOf("&reply_comment_id="));
                        String idReply = urlComment.substring(urlComment.indexOf("&reply_comment_id=") + 18, urlComment.lastIndexOf(""));
                        String commentId = String.join("_", idComment, idReply);
                        // write data
                        Date timeCrawl = new Date();
                        Date timCreate = new Date(Long.parseLong(timeComment) * 1000);

                        JSONObject jsonResult = new JSONObject();

                        String pageName = linkedList.get(0);
                        String postId = linkedList.get(1);
                        String contentPost = linkedList.get(2);

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
                        jsonFrom.put("id", userId);
                        jsonResult.put("from", jsonFrom);

                        jsonResult.put("title", contentPost);
                        jsonResult.put("pub", Long.parseLong(timeComment));
                        jsonResult.put("content", contentComment);
                        jsonResult.put("crawl", timeCrawl.getTime() / 1000);

                        String finalResult = String.join("\t", url, format.format(timeCrawl), format.format(timCreate), jsonResult.toString());

                        FileAppend.append(finalResult + "\n", outputFile);
                        logger.info(String.format("Crawl reply comment id: %s successfully", commentId));
                    } catch (Exception e) {
                        logger.error("Exception when get data sub comment: ", e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
        }

    }

//    public static void switchToNewTab(WebDriver driver) {
//        try {
//            ArrayList<String> winHandles = new ArrayList<>(driver.getWindowHandles());
//            Thread.sleep(500);
//            int newTabIndex = winHandles.size();
//            driver.switchTo().window(winHandles.get(newTabIndex - 1));
//        } catch (Exception e) {
//            logger.error("Switch to new tab error: ", e);
//        }
//    }
//
//    public static void openNewTab() {
//        try {
//            Robot rob = new Robot();
//            rob.keyPress(KeyEvent.VK_CONTROL);
//            rob.keyPress(KeyEvent.VK_T);
//            rob.keyRelease(KeyEvent.VK_CONTROL);
//            rob.keyRelease(KeyEvent.VK_T);
//            Thread.sleep(100);
//
//        } catch (Exception e) {
//            logger.error("Open new tab error: ", e);
//        }
//    }

    private static void loginFacebook(WebDriver webDriver, String emai, String password, String chromePath) {
        // login

        while (true) {
            try {
                int size = webDriver.findElements(By.cssSelector("#email")).size();
                if (size > 0) {
                    webDriver.findElement(By.cssSelector("#email")).sendKeys(emai);
                    TimeUnit.SECONDS.sleep(1);
                    webDriver.findElement(By.cssSelector("#pass")).sendKeys(password);

                    WebElement element = webDriver.findElement(By.cssSelector(".login_form_login_button"));
                    if (element.isDisplayed()) {
                        element.click();
                    }
                    break;
                } else {
                    webDriver.quit();
                    ChromeOptions ops = new ChromeOptions();
                    ops.addArguments("--disable-notifications");
                    ops.addArguments("--start-maximized");
                    System.setProperty("webdriver.chrome.driver", chromePath);
                    WebDriver driver = new ChromeDriver(ops);
                    driver.navigate().to("https://www.facebook.com");
                }
            } catch (InterruptedException e) {
                logger.error("InterruptedException: ", e);
            }
        }
    }

    private static void scroll() {
        Robot robot = null;
        try {
            robot = new Robot();
            robot.keyPress(KeyEvent.VK_PAGE_DOWN);
            robot.keyRelease(KeyEvent.VK_PAGE_DOWN);
        } catch (AWTException e) {
            logger.error("Exception when scroll: ", e);
        }
    }

    private static void moveToElement(WebElement webElement, WebDriver driver) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", webElement);
            Thread.sleep(500);
        } catch (InterruptedException e) {
            logger.error("InterruptedException: ", e);
        }
    }
}
