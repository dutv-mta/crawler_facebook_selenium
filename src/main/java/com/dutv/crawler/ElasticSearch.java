package com.dutv.crawler;

import com.dutv.utils.FileAppend;
import com.google.common.base.Splitter;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ElasticSearch {
    private static final String PRE_INDICE_NAME = "indexer_";
    private Client client;

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public ElasticSearch(Client client) {
        this.client = client;
    }

    public static void main(String[] args) throws UnknownHostException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate now = LocalDate.now();
        String date_indexer = formatter.format(now.minusDays(1));

        //get indexer name
        String indexer = "indexer_20191205";
        int port = 9300;
        String host = "34.87.51.88";
        String clusterName = "dutv";
        Settings settingClient = Settings.settingsBuilder().put("cluster.name", clusterName).build();
        Client client = TransportClient.builder().settings(settingClient).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));

        String mustWord = "quảng cáo";
        String stopWord = "iphone";

//        BoolQueryBuilder boolQueryBuilder = makeQuery(mustWord, stopWord);
//        SearchResponse searchResponse = client.prepareSearch(indexer)
//                .setQuery(boolQueryBuilder)
//                .setSize(1000)
//                .execute()
//                .actionGet();
//        SearchHit[] results = searchResponse.getHits().getHits();
//        for (SearchHit searchHit : results) {
//            String sourceAsString = searchHit.getSourceAsString();
//            String url = new JSONObject(sourceAsString).getString("url");
//            if (url.contains("post") && !url.contains("comment")) {
//                System.out.println(url);
//            }
//        }

    }


    public static Set<Map<String, String>> getDataPost() throws UnknownHostException {

        Set<Map<String, String>> set = new HashSet<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate now = LocalDate.now();
        String date_indexer = formatter.format(now.minusDays(2));

        //get indexer name
        String indexer = PRE_INDICE_NAME + date_indexer;
        System.out.println(indexer);
        int port = 9300;
        String host = "34.87.51.88";
        String clusterName = "dutv";

        Settings settingClient = Settings.settingsBuilder().put("cluster.name", clusterName).build();
        Client client = TransportClient.builder().settings(settingClient).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
        SearchResponse searchResponse = client.prepareSearch(indexer)
                .setQuery(QueryBuilders.termQuery("edge_type", "post"))
                .setScroll(new TimeValue(60000))
                .setSize(1000)
                .execute()
                .actionGet();
        while (true) {
        SearchHit[] results = searchResponse.getHits().getHits();
        for (SearchHit searchHit : results) {

            String sourceAsString = searchHit.getSourceAsString();
            String url = new JSONObject(sourceAsString).getString("url");
            String source = new JSONObject(sourceAsString).getString("source");
            Map<String, String> map = new LinkedHashMap<>();
            map.put(source, url);

            // add to list
            set.add(map);
        }
            searchResponse = client.prepareSearchScroll(searchResponse.getScrollId()).setScroll(new TimeValue(60000))
                    .execute().actionGet();
            if (searchResponse.getHits().getHits().length == 0) {
                System.out.println("FINISH");
                break;
            }
        }
        return set;
    }

    public static Set<Map<String, String>> getDataPostTest() throws IOException {

        Set<Map<String, String>> set = new HashSet<>();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
//        LocalDate now = LocalDate.now();
//        String date_indexer = formatter.format(now.minusDays(2));

        String now = "20191207";

        //get indexer name
        String indexer = PRE_INDICE_NAME + now;
        System.out.println(indexer);
        int port = 9300;
        String host = "34.87.51.88";
        String clusterName = "dutv";

        Settings settingClient = Settings.settingsBuilder().put("cluster.name", clusterName).build();
        Client client = TransportClient.builder().settings(settingClient).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
        SearchResponse searchResponse = client.prepareSearch(indexer)
                .setQuery(QueryBuilders.termQuery("edge_type", "post"))
                .setScroll(new TimeValue(60000))
                .setSize(1000)
                .execute()
                .actionGet();
        while (true) {
            SearchHit[] results = searchResponse.getHits().getHits();
            for (SearchHit searchHit : results) {
                String today = "2019/12/24 18:03:29";
                String sourceAsString = searchHit.getSourceAsString();
                String url = new JSONObject(sourceAsString).getString("url");
                JSONObject node = new JSONObject(sourceAsString).getJSONObject("node");

                JSONObject post = new JSONObject(sourceAsString).getJSONObject("post");
                String pubDate = new JSONObject(sourceAsString).getString("pubDate");
                String title = new JSONObject(sourceAsString).getString("title");
                String content = new JSONObject(sourceAsString).getString("content");
                Date unixToday = new Date(today);
                Date unixPub = new Date(pubDate);
                JSONObject from = new JSONObject(sourceAsString).getJSONObject("publisher");
                String postId = new JSONObject(sourceAsString).getString("post_id");

                JSONObject json = new JSONObject();
                json.put("node", node);
                json.put("from", from);
                json.put("post", post);
                json.put("title", title);
                json.put("content", content);
                json.put("id", postId);
                json.put("pub", unixPub.getTime()/1000);
                json.put("crawl", unixToday.getTime()/1000);

                String result = String.join("\t", url, today, pubDate, json.toString());
                FileAppend.append(result + "\n", "/home/dutv/Desktop/test/2019120.tsv");

                String source = new JSONObject(sourceAsString).getString("source");
                Map<String, String> map = new LinkedHashMap<>();
                map.put(source, url);

                // add to list
                set.add(map);
            }
            searchResponse = client.prepareSearchScroll(searchResponse.getScrollId()).setScroll(new TimeValue(60000))
                    .execute().actionGet();
            if (searchResponse.getHits().getHits().length == 0) {
                System.out.println("FINISH");
                break;
            }
        }
        return set;
    }


    public static Set<Map<String, String>> getDataPost06() throws IOException {

        Set<Map<String, String>> set = new HashSet<>();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
//        LocalDate now = LocalDate.now();
//        String date_indexer = formatter.format(now.minusDays(2));

        String now = "20191222";

        //get indexer name
        String indexer = PRE_INDICE_NAME + now;
        System.out.println(indexer);
        int port = 9300;
        String host = "34.87.51.88";
        String clusterName = "dutv";

        Settings settingClient = Settings.settingsBuilder().put("cluster.name", clusterName).build();
        Client client = TransportClient.builder().settings(settingClient).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
        SearchResponse searchResponse = client.prepareSearch(indexer)
                .setQuery(QueryBuilders.termQuery("edge_type", "post"))
                .setScroll(new TimeValue(60000))
                .setSize(1000)
                .execute()
                .actionGet();
        while (true) {
            SearchHit[] results = searchResponse.getHits().getHits();
            for (SearchHit searchHit : results) {
                String sourceAsString = searchHit.getSourceAsString();
                String pubDate = new JSONObject(sourceAsString).getString("pubDate");
                if (pubDate.contains("2019/12/06")) {
                    String url = new JSONObject(sourceAsString).getString("url");
                    String id = searchHit.getId();
                    String result = String.join(",", id, pubDate, url);
                    System.out.println(result);
                }
            }
            searchResponse = client.prepareSearchScroll(searchResponse.getScrollId()).setScroll(new TimeValue(60000))
                    .execute().actionGet();
            if (searchResponse.getHits().getHits().length == 0) {
                System.out.println("FINISH");
                break;
            }
        }
        return set;
    }
}
