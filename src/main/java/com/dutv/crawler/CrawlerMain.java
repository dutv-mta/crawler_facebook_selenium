package com.dutv.crawler;


import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

public class CrawlerMain {
    public static void main(String[] args) {
        // create the command line parser
        CommandLineParser parser = new DefaultParser();

        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();

        // create the Options
        Options options = new Options();
        options.addOption("t", "type", true, "ID or ARTICLE or ALL");
    }
}
