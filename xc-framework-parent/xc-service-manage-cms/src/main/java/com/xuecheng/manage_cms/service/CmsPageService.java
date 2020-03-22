package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class CmsPageService {

    @Autowired
    CmsPageRepository cmsPageRepository;

    @Autowired
    CmsConfigRepository cmsConfigRepository;

    @Autowired
    CmsSiteRepository cmsSiteRepository;


    public QueryResponseResult findCmsPageList(int page, int size, QueryPageRequest queryPageRequest){
        if (queryPageRequest == null){
            queryPageRequest = new QueryPageRequest();
        }
        //定义条件匹配器 模糊查询页面别名
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withMatcher("pageAliase",ExampleMatcher.GenericPropertyMatchers.contains());
        //条件值对象
        CmsPage cmsPage = new CmsPage();
        //设置条件查询 站点id
        if (StringUtils.isNotEmpty(queryPageRequest.getSiteId())){ //判断是否有站点id查询条件
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        //设置条件查询 模板id
        if (StringUtils.isNotEmpty(queryPageRequest.getTemplateId())){ //判断是否有模板id查询条件
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }
        //设置条件查询 页面别名
        if (StringUtils.isNotEmpty(queryPageRequest.getPageAliase())){ //判断是否有页面别名查询条件
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }

        Example<CmsPage> example = Example.of(cmsPage,exampleMatcher);
        if (page<=0) {
            page = 1;
        }
        if (size<=0){
            size=10;
        }
        page = page-1;
        Pageable pageable = PageRequest.of(page,size);
        Page<CmsPage> all = cmsPageRepository.findAll(example,pageable);

        QueryResult queryResult = new QueryResult();
        queryResult.setList(all.getContent());
        queryResult.setTotal(all.getTotalElements());
        return new QueryResponseResult(CommonCode.SUCCESS,queryResult);
    }


    //新增页面
    public CmsPageResult add(CmsPage cmsPage){
        if (cmsPage == null){
            //页面已经存在
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }

        CmsPage cmsPage1 = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());

        if (cmsPage1 != null){
            //页面已经存在
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }
        if (cmsPage1 == null){
            //添加页面
            cmsPage.setPageId(null);
            cmsPageRepository.save(cmsPage);
            return new CmsPageResult(CommonCode.SUCCESS,cmsPage);
        }

        return new CmsPageResult(CommonCode.FAIL,null);
    }


    //根据id查询页面
    public CmsPage findById(String id){
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        if (optional.isPresent()){
            CmsPage cmsPage = optional.get();
            return cmsPage;
        }
        return null;
    }

    //修改页面信息
    public CmsPageResult update(String id,CmsPage cmsPage){

        CmsPage cmsPage1 = this.findById(id);
        if (cmsPage1 != null){
            //更新页面信息
            //更新模板id
            cmsPage1.setTemplateId(cmsPage.getTemplateId());
            //更新站点id
            cmsPage1.setSiteId(cmsPage.getSiteId());
            //页面名称
            cmsPage1.setPageName(cmsPage.getPageName());
            //页面别名
            cmsPage1.setPageAliase(cmsPage.getPageAliase());
            //访问路径
            cmsPage1.setPageWebPath(cmsPage.getPageWebPath());
            //物理路径
            cmsPage1.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            //创建时间
            cmsPage1.setPageCreateTime(cmsPage.getPageCreateTime());
            //数据 dataURL
            cmsPage1.setDataUrl(cmsPage.getDataUrl());

            CmsPage save = cmsPageRepository.save(cmsPage1);
            if (save != null){

                return new CmsPageResult(CommonCode.SUCCESS,save);
            }
        }
        return new CmsPageResult(CommonCode.FAIL,null);
    }

    //删除页面
    public ResponseResult delete(String id){
        //查询页面是否存在
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        if (optional.isPresent()){
            cmsPageRepository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }


    //根据id查询CmsConfig
    public CmsConfig findConfigById(String id){
        Optional<CmsConfig> optional = cmsConfigRepository.findById(id);
        if (optional.isPresent()){
            CmsConfig cmsConfig = optional.get();
            return cmsConfig;
        }
        return null;
    }




    @Autowired
    RestTemplate restTemplate;

    @Autowired
    CmsTemplateRepository cmsTemplateRepository;

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;

    /****
     * 2、静态化程序获取页面的DataUrl
     *
     * 3、静态化程序远程请求DataUrl获取数据模型。
     *
     * 4、静态化程序获取页面的模板信息
     *
     * 5、执行页面静态化
     */
    public String getPageHtml(String pageId){
        //获取模型数据
        Map model = this.getModelByPageId(pageId);
        if (model == null){
            //获取不到数据模型
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }


        //取出模板内容
        String template = this.getTemplateByPageId(pageId);
        if (StringUtils.isEmpty(template)){
            //页面模板为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }


        //执行静态化
        String html = this.generateHTML(template, model);
        return html;
    }




    //执行静态化
    private String generateHTML(String templateContent,Map model){

        //创建配置对象
        Configuration configuration = new Configuration(Configuration.getVersion());
        //创建模板加载器
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template",templateContent);
        //向configuration配置模板加载器
        configuration.setTemplateLoader(stringTemplateLoader);
        //获取模板信息
        try {
            Template template = configuration.getTemplate("template");
            //调用API进行静态化
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            return content;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }






    //获取页面的模板
    private String getTemplateByPageId(String pageId){
        //获取页面信息
        CmsPage cmsPage = this.findById(pageId);
        if (cmsPage == null){
            //页面找不到
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //获取页面的模板id
        String templateId = cmsPage.getTemplateId();
        if (StringUtils.isEmpty(templateId)){
            //页面模板为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }

        //查询模板信息
        Optional<CmsTemplate> optional = cmsTemplateRepository.findById(templateId);
        if (optional.isPresent()){
            CmsTemplate cmsTemplate = optional.get();
            //获取模板文件id
            String templateFileId = cmsTemplate.getTemplateFileId();
            //从GridFS中取出文件
            //根据文件id查询对象
            GridFSFile file = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            //打开下载流对象
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(file.getObjectId());

            //创建  对象获取流
            GridFsResource gridFsResource = new GridFsResource(file,gridFSDownloadStream);
            try {
                String toString = IOUtils.toString(gridFsResource.getInputStream(),"UTF-8");
                return toString;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //获取模型数据
    private Map getModelByPageId(String pageId){
        //获取页面信息
        CmsPage cmsPage = this.findById(pageId);
        if (cmsPage == null){
            //页面找不到
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //取出页面的dataURL
        String dataUrl = cmsPage.getDataUrl();
        if (StringUtils.isEmpty(dataUrl)){
            //页面dataUR为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        //通过RestTemplate请求dataURL获取数据
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        Map body = forEntity.getBody();
        return body;
    }

    //页面发布
    public ResponseResult post(String pageId){

        //1. 执行页面静态化
        String pageHtml = this.getPageHtml(pageId);
        //2. 将静态化的文件存储到GridFS中
        CmsPage cmsPage = this.saveHtml(pageId, pageHtml);
        //3. 向MQ发送消息
        sendPagePost(pageId);
        return new ResponseResult(CommonCode.SUCCESS);
    }


    @Autowired
    RabbitTemplate rabbitTemplate;

    //向MQ发送消息
    private  void sendPagePost(String pageId){
        //得到页面信息
        //得到页面信息
        CmsPage cmsPage = this.findById(pageId);
        if (cmsPage == null){
            ExceptionCast.cast(CommonCode.INVALID_PAPAM);
        }

        //创建消息对象
        Map<String,String> msg = new HashMap<>();
        msg.put("pageId",pageId);
        //转换JSON串
        String toJSONString = JSON.toJSONString(msg);
        //获取站点id
        String siteId = cmsPage.getSiteId();
        //发送mq
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE,siteId,toJSONString);

    }

    //保存HTML到GridFS中
    private CmsPage saveHtml(String pageId ,String htmlContent){
        //得到页面信息
        CmsPage cmsPage = this.findById(pageId);
        if (cmsPage == null){
            ExceptionCast.cast(CommonCode.INVALID_PAPAM);
        }
        ObjectId objectId = null;
        //htmlContent转换成输入流
        try {
            InputStream inputStream = IOUtils.toInputStream(htmlContent, "UTF-8");
            //将Html内容保存到GridFS中
            objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //将html文件id更新到cmsPage中
        cmsPage.setHtmlFileId(objectId.toHexString());
        cmsPageRepository.save(cmsPage);
        return cmsPage;
    }

    /****
     * 保存页面
     * @param cmsPage
     * @return
     */
    public CmsPageResult save(CmsPage cmsPage) {
        CmsPage cmsPage1 = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if (cmsPage1 != null){
            return this.update(cmsPage1.getPageId(),cmsPage);
        }
        return this.add(cmsPage);
    }


    /****
     * 页面一键发布
     * @param cmsPage
     * @return
     */
    public CmsPostPageResult postPageQuick(CmsPage cmsPage) {
        //将页面信息 保存到cms_page中
        CmsPageResult cmsPageResult = this.save(cmsPage);
        if (!cmsPageResult.isSuccess()) {
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //得到页面id
        CmsPage cmsPageSave = cmsPageResult.getCmsPage();
        String pageId = cmsPageSave.getPageId();
        //执行页面发布
        ResponseResult post = this.post(pageId);
        if (!post.isSuccess()) {
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //拼接页面URL
        String siteId = cmsPageSave.getSiteId();//站点ID
        CmsSite cmsSite = this.findSiteById(siteId);
        //url
        String url = cmsSite.getSiteDomain() + cmsSite.getSiteWebPath() + cmsPageSave.getPageWebPath() + cmsPageSave.getPageName();
        return new CmsPostPageResult(CommonCode.SUCCESS,url);
    }

    //获取站点信息
    private CmsSite findSiteById(String siteId) {
        Optional<CmsSite> cmsSiteOptional = cmsSiteRepository.findById(siteId);
        if (cmsSiteOptional.isPresent()){
            return cmsSiteOptional.get();
        }
        return null;
    }


}
