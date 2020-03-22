package com.xuecheng.manage_media_process;

import com.xuecheng.framework.utils.Mp4VideoUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


@SpringBootTest
@RunWith(SpringRunner.class)
public class TestProcessBuilder {

    /***
     * 使用processBuilder调用第三方的应用程序
     * @throws IOException
     */
    @Test
    public void testProcessBuilder() throws IOException {
        //创建processBuilder对象
        ProcessBuilder processBuilder = new ProcessBuilder("ping","127.0.0.1");
        //将标准输入流和错误流合并
        processBuilder.redirectErrorStream(true);
        //启动一个进程
        Process start = processBuilder.start();
        //通过标准输入流拿到错误或者正常的信息
        InputStream inputStream = start.getInputStream();
        //转换字符流
        InputStreamReader reader = new InputStreamReader(inputStream,"gbk");
        //缓存
        char[] c = new char[1024];
        int len = -1;
        while ((len = reader.read(c)) != -1){
            String string = new String(c,0,len);
            System.out.println(string);
        }
        inputStream.close();
        reader.close();

    }

    @Test
    public void testFFmpeg() throws IOException {
        //创建processBuilder对象
        ProcessBuilder processBuilder = new ProcessBuilder();
        //定义命令内容
        List<String> command = new ArrayList<>();
        command.add("D:\\dev\\FFmpeg\\ffmpeg-20180227-fa0c9d6-win64-static\\bin\\ffmpeg.exe");
        command.add("-i");
        command.add("D:\\dev\\FFmpeg\\ffmpegtest\\1.avi");
        command.add("-y");//覆盖输出文件
        command.add("-c:v");
        command.add("libx264");
        command.add("-s");
        command.add("1280x720");
        command.add("-pix_fmt");
        command.add("yuv420p");
        command.add("-b:a");
        command.add("63k");
        command.add("-b:v");
        command.add("753k");
        command.add("-r");
        command.add("18");
        command.add("D:\\dev\\FFmpeg\\ffmpegtest\\1.mp4");
        processBuilder.command(command);
        //将标准输入流和错误流合并
        processBuilder.redirectErrorStream(true);
        //启动一个进程
        Process start = processBuilder.start();
        //通过标准输入流拿到错误或者正常的信息
        InputStream inputStream = start.getInputStream();
        //转换字符流
        InputStreamReader reader = new InputStreamReader(inputStream,"gbk");
        //缓存
        char[] c = new char[1024];
        int len = -1;
        while ((len = reader.read(c)) != -1){
            String string = new String(c,0,len);
            System.out.println(string);
        }
        inputStream.close();
        reader.close();

    }

    //测试使用工具类将avi转成mp4
    @Test
    public void testMp4VideoUtil() {
        // String ffmpeg_path, String video_path, String mp4_name, String mp4folder_path
        String ffmpeg_path ="D:\\dev\\FFmpeg\\ffmpeg-20180227-fa0c9d6-win64-static\\bin\\ffmpeg.exe";
        String video_path ="D:\\dev\\FFmpeg\\ffmpegtest\\1.avi";
        String mp4_name ="2.mp4";
        String mp4folder_path ="D:\\dev\\FFmpeg\\ffmpegtest\\";

        Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpeg_path,video_path,mp4_name,mp4folder_path);
        String s = mp4VideoUtil.generateMp4();
        System.out.println(s);
    }
}
