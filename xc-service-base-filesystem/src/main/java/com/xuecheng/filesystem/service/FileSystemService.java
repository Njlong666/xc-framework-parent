package com.xuecheng.filesystem.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.filesystem.dao.FileSystemRepository;
import com.xuecheng.framework.domain.filesystem.FileSystem;
import com.xuecheng.framework.domain.filesystem.response.FileSystemCode;
import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;

import org.apache.commons.lang3.StringUtils;
import org.csource.fastdfs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class FileSystemService {

    @Value("${xuecheng.fastdfs.connect_timeout_in_seconds}")
    int connect_timeout_in_seconds;

    @Value("${xuecheng.fastdfs.network_timeout_in_seconds}")
    int network_timeout_in_seconds;

    @Value("${xuecheng.fastdfs.charset}")
    String charset;

    @Value("${xuecheng.fastdfs.tracker_servers}")
    String tracker_servers;

    @Autowired
    FileSystemRepository fileSystemRepository;


    public UploadFileResult upload(MultipartFile multipartFile,
                                   String filetag,
                                   String businesskey,
                                   String metadata){
        if (multipartFile == null){
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_FILEISNULL);
        }
        //1.向FastDFS中存储数据
        String filelId = this.fafsUpload(multipartFile);
        if (StringUtils.isEmpty(filelId)){
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_SERVERFAIL);
        }

        //2.将FastDFS返回的数据存储到 mongoDB中
        FileSystem fileSystem = new FileSystem();
        //文件请求路径
        fileSystem.setFilePath(filelId);
        fileSystem.setFileId(filelId);
        //文件名称
        fileSystem.setFileName(multipartFile.getOriginalFilename());
        //文件类型
        fileSystem.setFileType(multipartFile.getContentType());
        //业务标签
        fileSystem.setFiletag(filetag);
        //业务key
        fileSystem.setBusinesskey(businesskey);
        //文件大小
        fileSystem.setFileSize(multipartFile.getSize());
        if (StringUtils.isNotEmpty(metadata)){

            try{

                Map map = JSON.parseObject(metadata, Map.class);
                fileSystem.setMetadata(map);
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        FileSystem system = fileSystemRepository.save(fileSystem);
        return new UploadFileResult(CommonCode.SUCCESS,system);

    }

    /***
     * 上传文件
     *
     * @param multipartFile 文件
     * @return id
     */
    private String fafsUpload(MultipartFile multipartFile){
        //初始化FastDFS环境
        this.initFastConfig();
        //创建trackerClient
        TrackerClient trackerClient= new TrackerClient();
        try {
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取srotStorage
            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
            StorageClient1 storageClient1 = new StorageClient1(trackerServer,storeStorage);
            //上传文件
            //获取文件字节
            byte[] bytes = multipartFile.getBytes();
            //获取文件名,拿到后缀
            String originalFilename = multipartFile.getOriginalFilename();
            String substring = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            String file1Id = storageClient1.upload_file1(bytes, substring, null);
            return file1Id;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    private void initFastConfig(){
        try {
            //初始化 trackerServer
            ClientGlobal.initByTrackers(tracker_servers);
            ClientGlobal.setG_charset(charset);
            ClientGlobal.setG_network_timeout(network_timeout_in_seconds);
            ClientGlobal.setG_connect_timeout(connect_timeout_in_seconds);
        } catch (Exception e) {
            e.printStackTrace();
            //抛出异常
            ExceptionCast.cast(FileSystemCode.FS_INITFDFSERROR);
        }
    }

}
