package com.xuecheng.search;


import org.elasticsearch.action.search.SearchRequest;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


@SpringBootTest
@RunWith(SpringRunner.class)
public class TestSearch {

    @Autowired
    RestHighLevelClient client;

    @Autowired
    RestClient restClient;

    @Test //es搜索
    public void createESDtabses() throws IOException, ParseException {
        //创建搜索对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索方式 第一个参数为结果集包含哪些字段 第二个不要包含
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp"}, new String[]{});
        //设置搜索源
        searchRequest.source(searchSourceBuilder);
        //执行搜索,向ES发起http请求
        SearchResponse searchResponse = client.search(searchRequest);
        //搜索结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度较高的
        SearchHit[] searchHits = hits.getHits();
        //日期格式化对象
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hit : searchHits) {
            //文档主键
            String id = hit.getId();
            //整个源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            //获取name
            String name = (String) sourceAsMap.get("name");
            //获取学习模式
            String studymodel = (String) sourceAsMap.get("studymodel");
            //获取价格
            Double price = (Double) sourceAsMap.get("price");
            //时间
            Date timestamp = simpleDateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(price);
            System.out.println(timestamp);
        }
    }


     @Test //es搜索 分页查询
    public void searchPage() throws IOException, ParseException {
            //创建搜索对象
            SearchRequest searchRequest = new SearchRequest("xc_course");
            //指定类型
            searchRequest.types("doc");
            //搜索源构建对象
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            //设置分页
            int page =3;
            int size =1;
            int from = (page-1)*size;
            searchSourceBuilder.from(from);
            searchSourceBuilder.size(size);

            //搜索方式 第一个参数为结果集包含哪些字段 第二个不要包含
            searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp"}, new String[]{});
            //设置搜索源
            searchRequest.source(searchSourceBuilder);
            //执行搜索,向ES发起http请求
            SearchResponse searchResponse = client.search(searchRequest);
            //搜索结果
            SearchHits hits = searchResponse.getHits();
            //匹配到的总记录数
            long totalHits = hits.getTotalHits();
            //得到匹配度较高的
            SearchHit[] searchHits = hits.getHits();
            //日期格式化对象
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (SearchHit hit : searchHits) {
                //文档主键
                String id = hit.getId();
                //整个源文档内容
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                //获取name
                String name = (String) sourceAsMap.get("name");
                //获取学习模式
                String studymodel = (String) sourceAsMap.get("studymodel");
                //获取价格
                Double price = (Double) sourceAsMap.get("price");
                //时间
                Date timestamp = simpleDateFormat.parse((String) sourceAsMap.get("timestamp"));
                System.out.println(name);
                System.out.println(studymodel);
                System.out.println(price);
                System.out.println(timestamp);
            }
        }
    @Test //es搜索 term查询
    public void searchTerm() throws IOException, ParseException {
        //创建搜索对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //搜索方式
        searchSourceBuilder.query(QueryBuilders.termQuery("name","spring"));
        //搜索方式 第一个参数为结果集包含哪些字段 第二个不要包含
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp"}, new String[]{});
        //设置搜索源
        searchRequest.source(searchSourceBuilder);
        //执行搜索,向ES发起http请求
        SearchResponse searchResponse = client.search(searchRequest);
        //搜索结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度较高的
        SearchHit[] searchHits = hits.getHits();
        //日期格式化对象
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hit : searchHits) {
            //文档主键
            String id = hit.getId();
            //整个源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            //获取name
            String name = (String) sourceAsMap.get("name");
            //获取学习模式
            String studymodel = (String) sourceAsMap.get("studymodel");
            //获取价格
            Double price = (Double) sourceAsMap.get("price");
            //时间
            Date timestamp = simpleDateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(price);
            System.out.println(timestamp);
        }
    }


    @Test //es搜索 根据id精确查询
    public void searchTermById() throws IOException, ParseException {
        //创建搜索对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //定义id
        String[] ids = new String[]{"1","2"};
        //搜索方式
        searchSourceBuilder.query(QueryBuilders.termsQuery("_id",ids));
        //搜索方式 第一个参数为结果集包含哪些字段 第二个不要包含
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp"}, new String[]{});
        //设置搜索源
        searchRequest.source(searchSourceBuilder);
        //执行搜索,向ES发起http请求
        SearchResponse searchResponse = client.search(searchRequest);
        //搜索结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度较高的
        SearchHit[] searchHits = hits.getHits();
        //日期格式化对象
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hit : searchHits) {
            //文档主键
            String id = hit.getId();
            //整个源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            //获取name
            String name = (String) sourceAsMap.get("name");
            //获取学习模式
            String studymodel = (String) sourceAsMap.get("studymodel");
            //获取价格
            Double price = (Double) sourceAsMap.get("price");
            //时间
            Date timestamp = simpleDateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(price);
            System.out.println(timestamp);
        }
    }

    @Test //es搜索  Match查询
    public void searchTermByMatch() throws IOException, ParseException {
        //创建搜索对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //定义id
        String[] ids = new String[]{"1","2"};
        //搜索方式
        searchSourceBuilder.query(QueryBuilders.matchQuery("description","spring开发框架").minimumShouldMatch("90%"));
        //搜索方式 第一个参数为结果集包含哪些字段 第二个不要包含
        searchSourceBuilder.fetchSource(new String[]{"name", "studymodel", "price", "timestamp"}, new String[]{});
        //设置搜索源
        searchRequest.source(searchSourceBuilder);
        //执行搜索,向ES发起http请求
        SearchResponse searchResponse = client.search(searchRequest);
        //搜索结果
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度较高的
        SearchHit[] searchHits = hits.getHits();
        //日期格式化对象
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hit : searchHits) {
            //文档主键
            String id = hit.getId();
            //整个源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            //获取name
            String name = (String) sourceAsMap.get("name");
            //获取学习模式
            String studymodel = (String) sourceAsMap.get("studymodel");
            //获取价格
            Double price = (Double) sourceAsMap.get("price");
            //时间
            Date timestamp = simpleDateFormat.parse((String) sourceAsMap.get("timestamp"));
            System.out.println(name);
            System.out.println(studymodel);
            System.out.println(price);
            System.out.println(timestamp);
        }
    }
}
