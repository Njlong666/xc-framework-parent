package com.xuecheng.manage_media.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class MediaUploadService {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    String routingkey_media_video;

    @Autowired
    MediaFileRepository mediaFileRepository;

    @Value("${xc-service-manage-media.upload-location}")
    String upload_location;

    //获取文件路径
    private String getFilePath(String fileMd5, String fileExt) {
        return upload_location + fileMd5.substring(0,1) + "/" +fileMd5.substring(1,2) + "/" +fileMd5 + "/" + fileMd5 + "." + fileExt;
    }

    //获取文件所属的路径
    private String getFolderPath(String fileMd5) {
        return upload_location + fileMd5.substring(0,1) + "/" +fileMd5.substring(1,2) + "/" +fileMd5 + "/";
    }
    //获取分块文件所属的路径
    private String getChunkFolderPath(String fileMd5) {
        return upload_location + fileMd5.substring(0,1) + "/" +fileMd5.substring(1,2) + "/" +fileMd5 + "/chunk/";
    }
    /****
     * 文件上传注册
     * @param fileMd5 MD5值
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @param mimetype 文件类型
     * @param fileExt 文件扩展名
     * @return
     */
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        // 1.检查文件是否在磁盘上存在
        //获取文件所属的路径
        String fileFolderPath = this.getFolderPath(fileMd5);
        //获取文件路径
        String filePath = this.getFilePath(fileMd5,fileExt);
        //检查是否存在
        File file = new File(filePath);
        boolean exists = file.exists();

        // 2. 检查文件是否在MongoDB上存在
        Optional<MediaFile> mediaFileOptional = mediaFileRepository.findById(fileMd5);
        if (exists && mediaFileOptional.isPresent()){
            //文件存在
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }
        //检查文件所在的目录是否存在,不存在,则创建
        File fileFolder = new File(fileFolderPath);
        if (!fileFolder.exists()){
            fileFolder.mkdirs();//创建文件所在目录
        }

        return new ResponseResult(CommonCode.SUCCESS);
    }


    /****
     *   检查文件分块
     * @param fileMd5 MD5值
     * @param chunk 块文件下标
     * @param chunkSize 块文件大小
     * @return 是否存在 t f
     */
    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize) {
        //检查文件分块是否存在
        //获取文件分块的所属路径
        String chunkFolderPath = this.getChunkFolderPath(fileMd5);
        File chunkFile = new File(chunkFolderPath + chunk);
        if (chunkFile.exists()){
            //块文件存在
            return new CheckChunkResult(CommonCode.SUCCESS,true);
        }else {
            //块文件不存在
            return new CheckChunkResult(CommonCode.SUCCESS,false);
        }
    }

    /****
     *    上传分块文件
     * @param file 分块文件
     * @param chunk 分块下标
     * @param fileMd5 MD5
     * @return
     */
    public ResponseResult uploadChunk(MultipartFile file, Integer chunk, String fileMd5) {
        //检查是否有分块文件
        String chunkFolderPath = this.getChunkFolderPath(fileMd5);
        //获取分块的文件路径
        String chunkFilePath = chunkFolderPath + chunk;
        File chunkFileFolder = new File(chunkFolderPath);
        if (!chunkFileFolder.exists()){
            chunkFileFolder.mkdirs();
        }
        //获取上传输出流
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            inputStream = file.getInputStream();
            fileOutputStream = new FileOutputStream(new File(chunkFilePath));
            IOUtils.copy(inputStream,fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /*****
     *   合并分块
     * @param fileMd5 MD5值
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @param mimetype 文件类型
     * @param fileExt 文件扩展名
     * @return
     */
    public ResponseResult mergeChunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        // 1.合并所有分块
        //获取分块的所属路径
        String chunkFileFolderPath = this.getChunkFolderPath(fileMd5);
        File chunkFileFolder = new File(chunkFileFolderPath);
        //分块文件列表
        File[] files = chunkFileFolder.listFiles();
        List<File> fileList = Arrays.asList(files);
        //创建合并文件
        String filePath = this.getFilePath(fileMd5, fileExt);
        File mergeFile = new File(filePath);

        //执行合并
        mergeFile = this.mergeFile(fileList, mergeFile);
        if (mergeFile == null){
            //合并失败
            ExceptionCast.cast(MediaCode.MERGE_FILE_FAIL);
        }

        // 2.检验文件的MD5值是否和前端的一样
        boolean checkFileMD5 = this.chunkFileMd5(mergeFile, fileMd5);
        if (!checkFileMD5){
            //文件校验失败
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }

        // 3.将文件信息存入到mongoDb
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileOriginalName(fileName);
        mediaFile.setFileName(fileMd5+"."+fileExt);
        //文件保存的相对路径
        String filePath1 =  fileMd5.substring(0,1) + "/" +fileMd5.substring(1,2) + "/" +fileMd5 + "/";
        mediaFile.setFilePath(filePath1);
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimetype);
        mediaFile.setFileType(fileExt);
        //设置状态
        mediaFile.setFileStatus("301002");
        mediaFileRepository.save(mediaFile);
        //向MQ发送消息
        this.sendProcessVideoMsg(mediaFile.getFileId());
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /***
     * 向mq 发送消息
     * @param mediaId 视频id
     * @return
     */
    private ResponseResult sendProcessVideoMsg(String mediaId){
        //查询数据库是否有这个文件
        Optional<MediaFile> mediaFileOptional = mediaFileRepository.findById(mediaId);
        if (!mediaFileOptional.isPresent()){
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //构建消息
        Map<String,String> map = new HashMap<>();
        map.put("mediaId",mediaId);
        String toJSONString = JSON.toJSONString(map);
        //向mq发送消息
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK,routingkey_media_video,toJSONString);
        } catch (AmqpException e) {
            e.printStackTrace();
            ExceptionCast.cast(CommonCode.FAIL);
        }

        return new ResponseResult(CommonCode.SUCCESS);
    }

    // 文件校验
    private boolean chunkFileMd5(File mergeFile,String md5){
        try {
            //创建文件输入流
            FileInputStream fileInputStream = new FileInputStream(mergeFile);
            //获取文件的MD5
            String md5Hex = DigestUtils.md5Hex(fileInputStream);
            //和传入的MD5比较
            if (md5.equalsIgnoreCase(md5Hex)){
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /***
     * 合并文件 私有方法
     * @param chunkFileList 文件列表
     * @param mergeFile 原始文件
     * @return 文件
     */
    private File mergeFile(List<File> chunkFileList,File mergeFile){

        try {
            //判断原始空文件是否存在,不存在则创建
            if (mergeFile.exists()){
                mergeFile.delete();
            }else {
                //创建一个新文件
                mergeFile.createNewFile();
            }
            //对文件进行排序
            Collections.sort(chunkFileList, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if (Integer.parseInt(o1.getName()) > Integer.parseInt(o2.getName())){
                        return 1;
                    }
                    return -1;
                }
            });
            //创建一个写对象
            RandomAccessFile raf_write = new RandomAccessFile(mergeFile,"rw");
            byte[] b = new byte[1024];
            for (File chunkFile : chunkFileList) {
                RandomAccessFile raf_read = new RandomAccessFile(chunkFile,"r");
                int len = -1;
                while ((len = raf_read.read(b)) != -1){
                    raf_write.write(b,0,len);
                }
                raf_read.close();
            }
            raf_write.close();
            return mergeFile;
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }
}
