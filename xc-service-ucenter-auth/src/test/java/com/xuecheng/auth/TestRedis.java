package com.xuecheng.auth;


import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import springfox.documentation.spring.web.json.Json;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestRedis {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Test
    public void testRedis(){
        //key
        String user_key = "user_token:7f59c939-c352-4ac1-9d34-28fc24547572";
        //定义map存储keyValue形式数据
        Map<String,String> value = new HashMap<>();
        value.put("id","101");
        value.put("name","itcast");
        //转换为JSON
        String valueString = JSON.toJSONString(value);
        //向Redis存储数据
        stringRedisTemplate.boundValueOps(user_key).set(valueString,60, TimeUnit.SECONDS);

        //根据key获取value
        String keyString = stringRedisTemplate.opsForValue().get(user_key);
        System.out.println(keyString);
    }
}
