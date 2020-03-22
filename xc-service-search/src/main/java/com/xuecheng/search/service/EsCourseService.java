package com.xuecheng.search.service;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class EsCourseService {

    @Value("${xuecheng.elasticsearch.course.index}")
    String index;
    @Value("${xuecheng.elasticsearch.media.index}")
    String media_index;

    @Value("${xuecheng.elasticsearch.course.type}")
    String type;

    @Value("${xuecheng.elasticsearch.media.type}")
    String media_type;

    @Value("${xuecheng.elasticsearch.course.source_field}")
    String source_field;

    @Value("${xuecheng.elasticsearch.media.source_field}")
    String media_source_field;


    @Autowired
    RestHighLevelClient restHighLevelClient;

    public QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam) {
        if (courseSearchParam == null){
            courseSearchParam = new CourseSearchParam();
        }
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest(index);
        //设置类型
        searchRequest.types(type);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //过滤源字段
        String[] course_field_array = source_field.split(",");
        searchSourceBuilder.fetchSource(course_field_array,new  String[]{});
        //创建布尔查询对象
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //搜索条件
        if (StringUtils.isNotEmpty(courseSearchParam.getKeyword())){// multiMatchQuery参数 :根据哪个字段来搜
            MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(courseSearchParam.getKeyword(), "name","description" , "teachplan")
                    .minimumShouldMatch("70%") //匹配的比例
                    .field("name", 10);//配置权重比例 name域增加10倍
            boolQueryBuilder.must(multiMatchQueryBuilder);
        }
        //分类调价
        if (StringUtils.isNotEmpty(courseSearchParam.getMt())){
            //根据一级分类查询
            boolQueryBuilder.filter(QueryBuilders.termQuery("mt",courseSearchParam.getMt()));
        }
        if (StringUtils.isNotEmpty(courseSearchParam.getSt())){
            //根据二级分类查询
            boolQueryBuilder.filter(QueryBuilders.termQuery("st",courseSearchParam.getSt()));
        }

        if (StringUtils.isNotEmpty(courseSearchParam.getGrade())){
            //根据难度等级查询
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade",courseSearchParam.getGrade()));
        }


        //设置boolQueryBuilder到searchSourceBuilder中
        searchSourceBuilder.query(boolQueryBuilder);
        //设置分页参数
        if (page<0){
            page = 1;
        }
        if (size<0){
            size = 1;
        }
        //起始记录的下标
        int from = (page-1)*size;
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        //设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<font class= 'eslight'>");
        highlightBuilder.postTags("</font>");
        //设置哪个字段高亮
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        searchSourceBuilder.highlighter(highlightBuilder);
        searchRequest.source(searchSourceBuilder);

        QueryResult<CoursePub> queryResult = new QueryResult<>();
        List<CoursePub> coursePubList = new ArrayList<>();
        try {
            //执行搜索
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
            SearchHits hits = searchResponse.getHits();
            long totalHits = hits.totalHits; //匹配的总记录数
            queryResult.setTotal(totalHits);
            SearchHit[] searchHits = hits.getHits(); //匹配数高的
            for (SearchHit hit : searchHits) {
                CoursePub coursePub = new CoursePub();
                //源文档
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                //取出id
                String id = (String) sourceAsMap.get("id");
                coursePub.setId(id);
                //获取name
                String name = (String) sourceAsMap.get("name");
                //获取高亮字段
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                if (highlightFields != null){
                    HighlightField highlighFieldsName = highlightFields.get("name");
                    if (highlighFieldsName !=null){
                        Text[] fragments = highlighFieldsName.fragments();
                        StringBuffer stringBuffer = new StringBuffer();
                        for (Text text : fragments) {
                            stringBuffer.append(text);
                        }
                        name = stringBuffer.toString();
                    }
                }
                coursePub.setName(name);
                //获取图片
                String pic = (String) sourceAsMap.get("pic");
                coursePub.setPic(pic);

                //获取价格
                Double price = null;
                if (sourceAsMap.get("price") != null){
                    price = ((Double) sourceAsMap.get("price"));
                }
                coursePub.setPrice(price);

                //获取旧价格
                Double price_old = null;
                if (sourceAsMap.get("price_old") != null){
                    price_old = ((Double) sourceAsMap.get("price_old"));
                }
                coursePub.setPrice_old(price_old);

                //将coursePub对象放入list中
                coursePubList.add(coursePub);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        queryResult.setList(coursePubList);
        QueryResponseResult<CoursePub> queryResponseResult = new QueryResponseResult<>(CommonCode.SUCCESS, queryResult);
        return queryResponseResult;
    }

    /*****
     *   根据id查询课程信息
     * @param id 课程id
     * @return
     */
    public Map<String, CoursePub> getAll(String id) {

        //创建搜索对象
        SearchRequest searchRequest = new SearchRequest(index);
        //设置类型
        searchRequest.types(type);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("id",id));
        searchRequest.source(searchSourceBuilder);
        //封住返回数据
        Map<String, CoursePub> map = new HashMap<>();
        try {
            SearchResponse search = restHighLevelClient.search(searchRequest);
            SearchHits hits = search.getHits();
            //匹配到的数据
            SearchHit[] searchHits = hits.getHits();
            for (SearchHit searchHit : searchHits) {
                CoursePub coursePub = new CoursePub();
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                //课程id
                String courseId = (String) sourceAsMap.get("id");
                coursePub.setId(courseId);
                //课程计划
                String teachplan = (String) sourceAsMap.get("teachplan");
                coursePub.setTeachplan(teachplan);
                //课程图片
                String pic = (String) sourceAsMap.get("pic");
                coursePub.setPic(pic);
                //课程等级
                String grade = (String) sourceAsMap.get("grade");
                coursePub.setGrade(grade);
                //课程介绍
                String description = (String) sourceAsMap.get("description");
                coursePub.setDescription(description);
                //封装到map 返回
                map.put(courseId,coursePub);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }

    /*****
     * 根据课程计划查询媒资信息
     * @param teachplanIds
     * @return
     */
    public QueryResponseResult<TeachplanMediaPub> getMedia(String[] teachplanIds) {

        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest(media_index);
        //设置类型
        searchRequest.types(media_type);
        //定义SearchSourceBuilder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termsQuery("teachplan_id",teachplanIds));
        //过滤源字段
        String[] split = media_source_field.split(",");
        searchSourceBuilder.fetchSource(split,new String[]{});
        searchRequest.source(searchSourceBuilder);
        //返回的结果集
        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
        long total = 0;
        try {
            //搜索请求客户端
            SearchResponse search = restHighLevelClient.search(searchRequest);
            SearchHits hits = search.getHits();
            //总记录数
            total = hits.getTotalHits();
            SearchHit[] searchHits = hits.getHits();
            for (SearchHit searchHit : searchHits) {
                TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();

                //课程id
                String courseid = (String) sourceAsMap.get("courseid");
                teachplanMediaPub.setCourseId(courseid);

                //媒资文件别名
                String media_fileoriginalname = (String) sourceAsMap.get("media_fileoriginalname");
                teachplanMediaPub.setMediaFileOriginalName(media_fileoriginalname);

                //媒资文件访问地址
                String media_url = (String) sourceAsMap.get("media_url");
                teachplanMediaPub.setMediaUrl(media_url);

                //媒资文件id
                String media_id = (String) sourceAsMap.get("media_id");
                teachplanMediaPub.setMediaId(media_id);

                //媒资文件id
                String teachplan_id = (String) sourceAsMap.get("teachplan_id");
                teachplanMediaPub.setTeachplanId(teachplan_id);

                //封装返回结果集
                teachplanMediaPubList.add(teachplanMediaPub);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //封装结果集
        QueryResult<TeachplanMediaPub> queryResult = new QueryResult<>();
        queryResult.setList(teachplanMediaPubList); //数据
        queryResult.setTotal(total); //总记录数
        QueryResponseResult<TeachplanMediaPub> queryResponseResult = new QueryResponseResult<>(CommonCode.SUCCESS,queryResult);

        return queryResponseResult;
    }
}
