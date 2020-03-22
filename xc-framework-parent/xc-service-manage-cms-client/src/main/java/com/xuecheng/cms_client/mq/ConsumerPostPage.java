package com.xuecheng.cms_client.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.cms_client.service.PageService;
import com.xuecheng.framework.domain.cms.CmsPage;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/****
 * 监听消息队列,接收页面发布的消息
 *
 *
 */
@Component
public class ConsumerPostPage {
    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger(PageService.class);


    @Autowired
    PageService pageService;


    @RabbitListener(queues = {"${xuecheng.mq.queue}"})
    public void postPage(String message){
        //解析消息
        Map map = JSON.parseObject(message, Map.class);
        String pageId = (String) map.get("pageId");
        //校验页面是否合法
        CmsPage cmsPage = pageService.findById(pageId);
        if (cmsPage == null){
            LOGGER.error("reccive postpage message , cmsPage is null ,pageId:{} ",pageId);
            return;
        }

        //调用service方法 从GridFS中下载页面
        pageService.savePageToServerPath(pageId);
    }
}
