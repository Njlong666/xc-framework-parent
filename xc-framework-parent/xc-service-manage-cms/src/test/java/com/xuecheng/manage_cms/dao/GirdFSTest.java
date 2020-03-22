package com.xuecheng.manage_cms.dao;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GirdFSTest {

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;

    @Test
    public void gridTest2() throws FileNotFoundException {
        //存储模板文件
        File file = new File("d:/course.ftl");
        FileInputStream fileInputStream = new FileInputStream(file);
        ObjectId store = gridFsTemplate.store(fileInputStream, "course.ftl");

        System.out.println(store);//5e59bd1a254cf02fa0940ee7
    }



    @Test
    public void gridTest() throws FileNotFoundException {
        //存储模板文件
        File file = new File("d:/index_banner.ftl");
        FileInputStream fileInputStream = new FileInputStream(file);
        ObjectId store = gridFsTemplate.store(fileInputStream, "index_banner.ftl");

        System.out.println(store);
    }


    //取文件
    @Test
    public void getGridFiles() throws IOException {
        //根据文件id查询对象
        GridFSFile file = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is("5e510e16254cf039bc3cb58d"))); //5e510e16254cf039bc3cb58d
        //打开下载流对象
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(file.getObjectId());

        //创建  对象获取流
        GridFsResource gridFsResource = new GridFsResource(file,gridFSDownloadStream);
        String toString = IOUtils.toString(gridFsResource.getInputStream());
        System.out.println(toString);

    }
}
