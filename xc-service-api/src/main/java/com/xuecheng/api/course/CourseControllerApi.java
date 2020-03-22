package com.xuecheng.api.course;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CoursePic;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.TeachplanMedia;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.request.QueryResponseResult;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "课程接口管理" ,description = "课程管理接口,提供页面的增,删,改,查")
public interface CourseControllerApi {

    @ApiOperation("课程计划查询")
    public TeachplanNode findTeachplanList(String courseId);

    @ApiOperation("添加课程计划")
    public ResponseResult  addTeachplan(Teachplan teachplan);

    @ApiOperation("查询课程列表" )
    public QueryResponseResult<CourseInfo> findCourseList(int page, int size , CourseListRequest courseListRequest);

    @ApiOperation("添加课程基础信息")
    public AddCourseResult addCourse(CourseBase courseBase);

    @ApiOperation(("根据id查询课程"))
    public CourseBase findById(String courseId);

    @ApiOperation("修改课程信息")
    public ResponseResult update(String id,CourseBase courseBase);

    @ApiOperation("添加课程与图片的信息")
    public ResponseResult addCoursePic(String courseId,String pic);

    @ApiOperation("查询课程图片")
    public CoursePic findPic(String courseId);


    @ApiOperation("删除课程图片")
    public ResponseResult  deleteCoursePic(String courseId);

    @ApiOperation("查询课程视图")
    public CourseView findCourseView(String id);

    @ApiOperation("课程预览")
    public CoursePublishResult preview(String courseId);

    @ApiOperation("课程一键发布")
    public CoursePublishResult  publish(String id);

    @ApiOperation("保存课程计划与媒资信息")
    public ResponseResult  saveMedia(TeachplanMedia teachplanMedia);

}
