package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.ApiOperation;

public interface CmsPageControllerApi{
    //分页查询CmsPage
    public QueryResponseResult findCmsPageList(int page, int size, QueryPageRequest queryPageRequest);

    //新增页面
    public CmsPageResult add(CmsPage cmsPage);


    //根据id查询页面
    public CmsPage findById(String id);

    //修改页面信息
    public CmsPageResult update(String id,CmsPage cmsPage);

    //删除页面
    public ResponseResult delete(String id);

    //页面发布
    public ResponseResult post(String pageId);

    //新增页面
    @ApiOperation("保存页面") //有就更新 没有添加
    public CmsPageResult save(CmsPage cmsPage);

    //新增页面
    @ApiOperation("课程一键发布") //有就更新 没有添加
    public CmsPostPageResult postPageQuick(CmsPage cmsPage);
}
