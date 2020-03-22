package com.xuecheng.test.fastdfs;


import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestFastDFS {


    //上传文件
    @Test
    public void testFastDFS(){
        //加载配置类
        try {
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            //定义TrackerClient
            TrackerClient trackerClient = new TrackerClient();
            //连接Tracker
            TrackerServer connection = trackerClient.getConnection();
            //获取Stroage
            StorageServer storeStorage = trackerClient.getStoreStorage(connection);
            //创建stroageClient
            StorageClient1 storageClient1 = new StorageClient1(connection,storeStorage);
            //向stroage上传图片
            String pngPath="d:/logo.png";
            String file1 = storageClient1.upload_file1(pngPath, "png", null);
            System.out.println(file1);//group1/M00/00/00/wKjIgF5XQ32AWyD0AAM2lwNWvUE256.png
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //文件下载
    @Test
    public void test2(){
        try {
            ClientGlobal.initByProperties("config/fastdfs-client.properties");

            //定义TrackerClient
            TrackerClient trackerClient = new TrackerClient();
            //连接Tracker
            TrackerServer connection = trackerClient.getConnection();
            //获取Stroage
            StorageServer storeStorage = trackerClient.getStoreStorage(connection);
            //创建stroageClient
            StorageClient1 storageClient1 = new StorageClient1(connection,storeStorage);
            //下载文件
            //文件id
            String fileId = "group1/M00/00/00/wKjIgF5XQ32AWyD0AAM2lwNWvUE256.png";
            byte[] bytes = storageClient1.download_file1(fileId);
            FileOutputStream fileOutputStream = new FileOutputStream(new File("E:/logo.png"));
            fileOutputStream.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test3(){

        try {
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            //定义TrackerClient
            TrackerClient trackerClient = new TrackerClient();
            //连接Tracker
            TrackerServer connection = trackerClient.getConnection();
            //获取Stroage
            StorageServer storeStorage = trackerClient.getStoreStorage(connection);
            //创建stroageClient
            StorageClient1 storageClient1 = new StorageClient1(connection,storeStorage);
            //删除文件
            //文件id
            String fileId = "group1/M00/00/00/wKjIgF5XQ32AWyD0AAM2lwNWvUE256.png";
            int file1 = storageClient1.delete_file1(fileId);
            System.out.println(file1);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
