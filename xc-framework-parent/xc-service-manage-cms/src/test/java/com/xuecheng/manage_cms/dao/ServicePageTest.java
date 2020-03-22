package com.xuecheng.manage_cms.dao;

import com.xuecheng.manage_cms.service.CmsPageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ServicePageTest {


    @Autowired
    CmsPageService cmsPageService;


    @Test
    public void restTemplateTest(){

        String pageHtml = cmsPageService.getPageHtml("5e51d627254cf04668647fe4");
        System.out.println(pageHtml);
    }
}
