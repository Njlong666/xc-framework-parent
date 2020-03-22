package com.xuecheng.cms_client.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.cms_client.dao.CmsPageRepository;
import com.xuecheng.cms_client.dao.CmsSiteRepository;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Optional;

@Service
public class PageService {

    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger(PageService.class);


    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;

    @Autowired
    CmsPageRepository cmsPageRepository;

    @Autowired
    CmsSiteRepository cmsSiteRepository;


    //保存HTML頁面到服務器物理路徑
    public void savePageToServerPath(String pageId){

        //根据pageId查询到cmsPage
        CmsPage cmsPage = this.findById(pageId);
        //获取到html文件的id 从cmsPage中获取HtmlFileId内容
        String htmlFileId = cmsPage.getHtmlFileId();
        //从GridFS中查询html文件
        InputStream inputStream = this.getFileById(htmlFileId);
        if (inputStream == null){
            LOGGER.error("getFileById  InputStream is null ,htmlFileId:{}",htmlFileId);
            return;
        }

        //获取站点id
        String siteId = cmsPage.getSiteId();
        //获取站点信息
        CmsSite cmsSite = this.findCmsSiteById(siteId);
        //获取站点物理路径
        String sitePhysicaPath = cmsSite.getSitePhysicaPath();
        //拼装页面物理路径
        String pagePath = sitePhysicaPath+cmsPage.getPagePhysicalPath()+cmsPage.getPageName();
        //将html文件保存到服务器的物理路径上
        FileOutputStream fileOutputStream =null;
        try {
            fileOutputStream = new FileOutputStream(new File(pagePath));
            IOUtils.copy(inputStream,fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                fileOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    //根据htmlFileId 从GridFS查询文件内容
    public InputStream getFileById(String fileId){
        //文件对象
        GridFSFile file = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(fileId)));
        //打开下载流
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(file.getObjectId());
        //定义GridFsResource
        GridFsResource gridFsResource = new GridFsResource(file, gridFSDownloadStream);
        try {
            return gridFsResource.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //根据页面id查询页面信息
    public CmsPage findById(String pageId){
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if (optional.isPresent()){
            CmsPage cmsPage = optional.get();
            return cmsPage;
        }
        return null;
    }


    //根据页面id查询页面信息
    public CmsSite findCmsSiteById(String siteId){
        Optional<CmsSite> optional = cmsSiteRepository.findById(siteId);
        if (optional.isPresent()){
            CmsSite cmsSite = optional.get();
            return cmsSite;
        }
        return null;
    }
}
