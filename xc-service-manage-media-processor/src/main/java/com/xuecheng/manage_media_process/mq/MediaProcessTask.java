package com.xuecheng.manage_media_process.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.MediaFileProcess_m3u8;
import com.xuecheng.framework.utils.HlsVideoUtil;
import com.xuecheng.framework.utils.Mp4VideoUtil;
import com.xuecheng.manage_media_process.dao.MediaFileRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class MediaProcessTask {

    @Autowired
    MediaFileRepository mediaFileRepository;

    @Value("${xc-service-manage-media.ffmpeg-path}")
    String ffmpeg;

    @Value("${xc-service-manage-media.video-location}")
    String serverPath;

    @RabbitListener(queues = "${xc-service-manage-media.mq.queue-media-video-processor}",containerFactory="customContainerFactory")
    public void receiveMediaProcessTask(String msg){
        //1.解析消息内容 获取到mediaId
        Map msgMap = JSON.parseObject(msg, Map.class);
        String mediaId = (String) msgMap.get("mediaId");
        //2.使用mediaId,从数据库查询
        Optional<MediaFile> mediaFileOptional = mediaFileRepository.findById(mediaId);
        if (!mediaFileOptional.isPresent()){
            return;
        }
        MediaFile mediaFile = mediaFileOptional.get();
        //获取文件类型
        String fileType = mediaFile.getFileType();
        if ( !fileType.equals("avi")){
            //无需处理
            mediaFile.setProcessStatus("303004");
            mediaFileRepository.save(mediaFile);
            return;
        }else {
            mediaFile.setProcessStatus("303001");
            mediaFileRepository.save(mediaFile);
        }
        //要处理的视频文件地址
        String video_path = serverPath + mediaFile.getFilePath() + mediaFile.getFileName();
        //视频名称
        String mp4_name = mediaFile.getFileId() + ".mp4";
        //处理完的视频路径
        String mp4folder_path = serverPath + mediaFile.getFilePath();
        //处理视频的工具类
        Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpeg, video_path, mp4_name, mp4folder_path);
        String result = mp4VideoUtil.generateMp4();
        if (result == null || !result.equals("success")){
            //处理失败
            mediaFile.setProcessStatus("303003");
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(result);
            //记录失败原因
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return;
        }
        //MP4视频的路径
        String mp4_video_path = serverPath + mediaFile.getFilePath() +mp4_name;
        //M3u8名称
        String m3u8_name = mediaFile.getFileId() + ".m3u8";
        //处理完的M3u8文件地址
        String m3u8folder_path = serverPath + mediaFile.getFilePath() + "hls/";
        //将mp4文件生成m3u8文件  String ffmpeg_path, String video_path, String m3u8_name,String m3u8folder_path
        HlsVideoUtil hlsVideoUtil = new HlsVideoUtil(ffmpeg,mp4_video_path,m3u8_name,m3u8folder_path);
        String m3u8Result = hlsVideoUtil.generateM3u8();
        if (m3u8Result == null || !m3u8Result.equals("success")){
            //处理失败
            mediaFile.setProcessStatus("303003");
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(result);
            //记录失败原因
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            mediaFileRepository.save(mediaFile);
            return;
        }
        //获取ts列表
        List<String> ts_list = hlsVideoUtil.get_ts_list();
        //处理成功
        mediaFile.setProcessStatus("303002");
        MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
        mediaFileProcess_m3u8.setTslist(ts_list);
        mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
        //保存 fileUrl
        String fileUrl = mediaFile.getFilePath() + "hls/" + m3u8_name;
        mediaFile.setFileUrl(fileUrl);
        mediaFileRepository.save(mediaFile);
    }

}
