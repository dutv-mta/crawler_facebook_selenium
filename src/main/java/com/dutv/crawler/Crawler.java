package com.dutv.crawler;

import com.dutv.utils.FileAppend;
import com.dutv.utils.TimeUtils;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crawler {
    static Logger logger = LoggerFactory.getLogger(Crawler.class);
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private static final SimpleDateFormat localFormatter = new SimpleDateFormat("yyyyMMdd");
    public static void main(String[] args) throws InterruptedException, IOException {
        Set<String> set = new HashSet<>();

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd.HH0000");
        Date now = new Date();
        String pageId = "402546526524902";
        String path = "/home/dutv/Desktop/test/selenium/";
        String outputFile = path + format.format(now) + ".tsv";
        String chromePath = "/home/dutv/Desktop/Selenium/chromedriver";
        String email = "dangchiengi5955@69postix.info";
        String password = "dangchien123";
        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--disable-notifications");
        System.setProperty("webdriver.chrome.driver", chromePath);
        WebDriver driver = new ChromeDriver(ops);
//        String pathPhantom = "/home/dutv/Desktop/Selenium/phantomjs-2.1.1-linux-x86_64/bin/phantomjs";
//        String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.97 Safari/537.11";
//        DesiredCapabilities capabilities = new DesiredCapabilities();
//        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent", userAgent);
//        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "loadImages", false);
//        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "javascriptEnabled", true);
//        capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, pathPhantom);
//        WebDriver driver = new PhantomJSDriver(capabilities);
        driver.navigate().to("https://www.facebook.com/login");
        driver.manage().window().maximize();
        loginFacebook(driver, email, password);
        TimeUnit.SECONDS.sleep(5);
        visitPage(driver, pageId);
        TimeUnit.SECONDS.sleep(5);
        JavascriptExecutor jse = (JavascriptExecutor)driver;
        do {
//            for (int i = 0; i < 7; i++) {
//
//                TimeUnit.SECONDS.sleep(2);
//                jse.executeScript("window.scrollTo(0, document.body.scrollHeight)");
////                scroll();
//            }
        driver.navigate().to("https://www.facebook.com/402546526524902/posts/2492399434206257");
            List<WebElement> webElementList = driver.findElements(By.cssSelector(".userContentWrapper"));
            if (webElementList.size() > 0) {
                System.out.println("Checkout post");
                for (WebElement webElement : webElementList) {
                    String timePost = webElement.findElement(By.cssSelector(".userContentWrapper div[data-testid=\"story-subtitle\"] abbr")).getAttribute("data-utime");
                    LocalDate nowLocalDate = LocalDate.now();
                    Date datePost = new Date (Long.parseLong(timePost) * 1000);
                    String strLocalTimePost = localFormatter.format(datePost);
                    LocalDate localTimePost = LocalDate.parse(strLocalTimePost, TimeUtils.format);
                    if (localTimePost == nowLocalDate.minusDays(2)) {
                        break;
                    }
                }
                getTotal(webElementList, driver, set, pageId, outputFile);
            }
        } while (true);


    }

    private static void getTotal(List<WebElement> webElementList, WebDriver driver, Set<String> set, String pageId, String outputFile) {
        try {
            for (WebElement webElement : webElementList) {
                int size = webElement.findElements(By.cssSelector("[data-testid=\"UFI2CommentsCount/root\"]")).size();
                if (size == 0) {
                    continue;
                }
                String urlPost = webElement.findElement(By.cssSelector("[data-testid=\"UFI2CommentsCount/root\"]")).getAttribute("href");
                if (set.contains(urlPost)) {
                    continue;
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
                getDataComment(webElement, pageId, outputFile, linkedList);


                // Check ReplyComment
                checkReply(webElement, driver, linkedList, pageId, outputFile);


                // add link post to set
                set.add(urlPost);

            }
        } catch (InterruptedException e) {
            logger.error("InterruptedException: ", e);
        }
    }

    private static LinkedList<String> getDataPost(WebElement element, WebDriver driver, String urlPost, String pageId, String outputFile) {
        System.out.println("Prepare parse data post");
        LinkedList<String> linkedList = new LinkedList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        SimpleDateFormat localFormatter = new SimpleDateFormat("yyyyMMdd");
        try {
            Pattern numberPattern = Pattern.compile("\\d+");
            Pattern floatPattern = Pattern.compile("[0-9]+[.][0-9]+");
            String commentNumber = "";
            String shareNumber = "";
            String likeNumber = "";

            String namePage = driver.findElement(By.cssSelector("#seo_h1_tag > a > span")).getText();
            String timePost = element.findElement(By.cssSelector(".userContentWrapper div[data-testid=\"story-subtitle\"] abbr")).getAttribute("data-utime");

            String contentPost = "";
            int size = element.findElements(By.cssSelector("[data-testid=\"post_message\"]")).size();
            if (size > 0) {
                contentPost = element.findElement(By.cssSelector("[data-testid=\"post_message\"]")).getText();
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
                    commentNumber = String.valueOf((int) Double.parseDouble(matcher.group()) * 1000);
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
                    shareNumber = String.valueOf((int) Double.parseDouble(matcher.group()) * 1000);
                }
            } else {
                Matcher matcher = numberPattern.matcher(totalCommentText);
                if (matcher.find()) {
                    shareNumber = matcher.group();
                }
            }
            if (totalLike.contains(".")) {
                Matcher matcher = floatPattern.matcher(totalLike);
                if (matcher.find()) {
                    likeNumber = String.valueOf((int) Double.parseDouble(matcher.group()) * 1000);
                }
            } else {
                Matcher matcher = numberPattern.matcher(totalLike);
                if (matcher.find()) {
                    likeNumber = matcher.group();
                }
            }
            String idPost = "";
            if (urlPost.contains("posts")) {
                String subPostId = urlPost.substring(urlPost.indexOf("posts/") + 6, urlPost.lastIndexOf(""));
                idPost = String.join("_", pageId, subPostId);
            } else {
                String subPostId = urlPost.substring(urlPost.indexOf("videos/") + 7, urlPost.lastIndexOf(""));;
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
                }
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
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
                moveToElement(webElement, driver);
                element.click();

                // get Comment 1
                getSubComment(webElement, linkedList, pageId, outputFile, set);
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
                }

            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
        }

    }

    private static void checkSeeMore(WebElement webElement) {
        boolean checkSeeMore = checkElement(webElement, ".see_more_link_inner");
        if (checkSeeMore) {
            WebElement element = webElement.findElement(By.cssSelector(".see_more_link_inner"));
            element.click();
        }

    }

    private static void checkViewMore(WebElement webElement) {
        try {
            while (true) {
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
            }
        } catch (InterruptedException e) {
            logger.error("InterruptedException: ", e);
        }
    }

    private static boolean checkElement(WebElement element, String css) {
        try {
            int size = element.findElements(By.cssSelector(css)).size();
            if (size != 0) {
                return true;
            }
        } catch (NoSuchElementException e) {
            System.out.println("Not found Element");
        }
        return false;
    }

    private static void moveToElement(WebElement webElement, WebDriver driver) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", webElement);
            Thread.sleep(500);
        } catch (InterruptedException e) {
            logger.error("InterruptedException: ", e);
        }
    }

    private static void visitPage(WebDriver driver, String pageId) {
        String url = "https://www.facebook.com/";
        driver.navigate().to(url + pageId);
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
                } else {
//                    String pathPhantom = "/home/dutv/Desktop/Selenium/phantomjs-2.1.1-linux-x86_64/bin/phantomjs";
//
//                    DesiredCapabilities caps = new DesiredCapabilities();
//                    caps.setJavascriptEnabled(true);
//                    caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, pathPhantom);
//                    WebDriver driver = new PhantomJSDriver(caps);
//                    driver.navigate().to("https://www.facebook.com");
                }
            }
        } catch (InterruptedException e) {
            logger.error("InterruptedException: ", e);
        }

    }
}
