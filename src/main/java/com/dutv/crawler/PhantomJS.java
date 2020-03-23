package com.dutv.crawler;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;

public class PhantomJS {
    public static void main(String[] args) {
        String pathPhantom = "/home/dutv/Desktop/Selenium/phantomjs-2.1.1-linux-x86_64/bin/phantomjs";

        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setJavascriptEnabled(true);
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, pathPhantom);
        WebDriver driver = new PhantomJSDriver(caps);
        driver.navigate().to("https://www.facebook.com");
        System.out.println(driver.getTitle());
    }
}
