package com.xuecheng.manage_media;

import com.google.inject.internal.cglib.core.$Constants;
import org.junit.Test;
import org.omg.PortableInterceptor.INACTIVE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/***
 * 测试文件分块
 * 合并
 */
public class testFile {

    /***
     * 分块
     *
     * @throws IOException
     */
    @Test
    public void testChunk() throws IOException {
        //源文件
        File sourceFile = new File("D:\\dev\\FFmpeg\\ffmpegtest\\lucene.avi");
        //块文件目录
        String chunkFileFolder = "D:\\dev\\FFmpeg\\ffmpegtest\\chunks\\";

        //定义块文件的大小
        long chunkFileSize = 1 * 1024 * 1024;

        //块数
        long chunkFileNum  = (long) Math.ceil(sourceFile.length() * 1.0 / chunkFileSize);

        //创建读文件对象
        RandomAccessFile raf_read = new RandomAccessFile(sourceFile,"r");

        //缓冲区
        byte[] b = new byte[1024];
        for (int i=0; i<chunkFileNum; i++){

            //块文件
            File chunkFile = new File(chunkFileFolder + i);
            //创建向块文件的写对象
            RandomAccessFile raf_write = new RandomAccessFile(chunkFile ,"rw");
            int len = -1;
            while ((len=raf_read.read(b)) != -1){

                raf_write.write(b,0,len);
                if (chunkFile.length() >= chunkFileSize){
                    break;
                }
            }
            raf_write.close();
        }
        raf_read.close();

    }

    @Test
    public void testMergFile() throws IOException {
        //块文件目录
        String chunkFilePath = "D:\\dev\\FFmpeg\\ffmpegtest\\chunks\\";
        //块文件对象
        File chunkFileFolder = new File(chunkFilePath);
        //获取块文件列表
        File[] files = chunkFileFolder.listFiles();
        List<File> fileList= Arrays.asList(files);
        //文件名排序
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (Integer.parseInt(o1.getName()) > Integer.parseInt(o2.getName())){
                    return 1;
                }
                return -1;
            }
        });

        //合并文件
        File mergeFile = new File("D:\\dev\\FFmpeg\\ffmpegtest\\lucene_merge.avi");
        //创建新文件
        boolean newFile = mergeFile.createNewFile();
        //创建写对象
        //创建向块文件的写对象
        RandomAccessFile raf_write = new RandomAccessFile(mergeFile ,"rw");

        byte[] b = new  byte[1024];
        for (File chunkFile : fileList) {
            //创建一个读文件对象
            RandomAccessFile raf_read = new RandomAccessFile(chunkFile, "r");

            int len = -1;
            while ((len = raf_read.read(b)) != -1) {
                raf_write.write(b, 0, len);
            }
            raf_read.close();
        }
        raf_write.close();
    }
}
