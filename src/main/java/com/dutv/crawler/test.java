package com.dutv.crawler;

import com.dutv.utils.FileAppend;
import com.dutv.utils.TimeUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class test {
    public static void main(String[] args) throws IOException, ParseException {
//        String test = "2.6K";
//        Pattern floatPattern = Pattern.compile("[0-9]+[.][0-9]+");
//        if (test.contains(".")) {
//            Matcher matcher = floatPattern.matcher(test);
//            if (matcher.find()) {
//                System.out.println((int)(Double.parseDouble(matcher.group()) * 1000));
//            }
//        }
//        LocalDate now = LocalDate.now();
//        LocalDate yesterday = now.minusDays(1);
//        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
//        String unixTime = "1575907355";
//        Date date = new Date(Long.parseLong(unixTime) * 1000);
//        String dateFormat = format.format(date);
//        LocalDate localDate = LocalDate.parse(dateFormat, TimeUtils.format);
//        if (localDate.isEqual(now)) {
//            System.out.println("Fail");
//        }
//        System.out.println("HIHI" +localDate);

//        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//        DateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
//        String time = "2019/12/10 09:34:55";
//        System.out.println(dateFormat.parse(time).getTime()/1000);
////        Date date = dateFormat.parse(time);
////        Date yesterday = new Date(date.getTime() - 24*60*60*1000);
////        System.out.println(formatter.format(yesterday));
//
//        List<String> list = new ArrayList<>();
//        list.add("a");
//        list.add("b");
//        if (list.contains("a")) {
//            System.out.println("true");
//        }
        int port = 9300;
        String host = "127.0.0.1";
        String clusterName = "dutv";

        Settings settingClient = Settings.settingsBuilder().put("cluster.name", clusterName).build();
        Client client = TransportClient.builder().settings(settingClient).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
    }
}
