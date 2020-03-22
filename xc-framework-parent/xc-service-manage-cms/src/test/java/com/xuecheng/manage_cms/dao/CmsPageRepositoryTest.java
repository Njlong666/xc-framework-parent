package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsPageRepositoryTest {


    @Autowired
    CmsPageRepository cmsPageRepository;


    @Test
    public void findAllTest(){

        List<CmsPage> all = cmsPageRepository.findAll();
        System.out.println(all);
    }

    @Test
    public void findAllPageTest() {
        int page = 0;
        int size =10;
        Pageable pageable = PageRequest.of(page, size);
        Page<CmsPage> all = cmsPageRepository.findAll(pageable);
        System.out.println(all);
    }


    @Test
    public void updateTest(){
        //查询
        Optional<CmsPage> optional = cmsPageRepository.findById("5e033134fa6bbb3ff0d6c524");
        if (optional.isPresent()){
            CmsPage cmsPage = optional.get();
            cmsPage.setPageAliase("test8888");
            //修改
            CmsPage save = cmsPageRepository.save(cmsPage);
            System.out.println(save);
        }
    }

    @Test
    public void findByPageAliase(){
        CmsPage test8888 = cmsPageRepository.findByPageAliase("test8888");
        System.out.println(test8888);
    }



    //精确查询
    @Test
    public void findAllTest1(){
        int page = 0;
        int size =20;
        Pageable pageable = PageRequest.of(page, size);

        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId("5a751fab6abb5044e0d19ea1");
       cmsPage.setTemplateId("5a925be7b00ffc4b3c1578b5");

        ExampleMatcher exampleMatcher = ExampleMatcher.matching();
        Example<CmsPage> example = Example.of(cmsPage,exampleMatcher);
        Page<CmsPage> all = cmsPageRepository.findAll(example, pageable);
        System.out.println(all);
    }


    //模糊查询
    @Test
    public void findAllTest2(){
        int page = 0;
        int size =10;
        Pageable pageable = PageRequest.of(page, size);

        CmsPage cmsPage = new CmsPage();
        //cmsPage.setSiteId("5a751fab6abb5044e0d19ea111");
        cmsPage.setPageAliase("轮");

        ExampleMatcher exampleMatcher = ExampleMatcher.matching();
        exampleMatcher = exampleMatcher.withMatcher("pageAliase",ExampleMatcher.GenericPropertyMatchers.contains());
        Example<CmsPage> example = Example.of(cmsPage,exampleMatcher);

        Page<CmsPage> all = cmsPageRepository.findAll(example, pageable);
        List<CmsPage> content = all.getContent();
        System.out.println(content);

    }
}
