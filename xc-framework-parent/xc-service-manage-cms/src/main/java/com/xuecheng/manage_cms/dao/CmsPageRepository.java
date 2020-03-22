package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CmsPageRepository extends MongoRepository<CmsPage,String> {
    //根据 页面别名查询
    CmsPage findByPageAliase(String pageAliase);

    //添加根据页面名称、站点Id、页面webpath查询页面方法，此方法用于校验页面是否存在
    CmsPage findByPageNameAndSiteIdAndPageWebPath(String pageName,String SiteId,String webPath);
}
