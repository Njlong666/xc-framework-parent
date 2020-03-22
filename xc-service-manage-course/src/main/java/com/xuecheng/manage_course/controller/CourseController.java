package com.xuecheng.manage_course.controller;

import com.xuecheng.api.course.CourseControllerApi;
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
import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.XcOauth2Util;
import com.xuecheng.framework.web.BaseController;
import com.xuecheng.manage_course.service.CourseService;
import org.codehaus.jackson.map.Serializers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/course")
public class CourseController extends BaseController implements CourseControllerApi {

    @Autowired
    CourseService courseService;


    @PreAuthorize("hasAuthority('course_find_list')")
    @Override
    @GetMapping("/teachplan/list/{courseId}")
    public TeachplanNode findTeachplanList(@PathVariable("courseId") String courseId) {
        return courseService.findTeachplanList(courseId);
    }

    //添加课程计划
    @Override
    @PostMapping("/teachplan/add")
    public ResponseResult addTeachplan(@RequestBody Teachplan teachplan) {
        return courseService.addTeachplan(teachplan);
    }

    //查询课程列表 并加入细粒度授权 根据不同用户的公司ID查询对应课程数据
    @Override
    @GetMapping("/coursebase/list/{page}/{size}")
    public QueryResponseResult<CourseInfo> findCourseList(@PathVariable("page") int page, @PathVariable("size")int size,  CourseListRequest courseListRequest) {
        XcOauth2Util xcOauth2Util = new XcOauth2Util();
        XcOauth2Util.UserJwt userJwtFromHeader = xcOauth2Util.getUserJwtFromHeader(request);
        //动态获取 用户所属公司ID
        String companyId = userJwtFromHeader.getCompanyId();
        return courseService.findCourseList( companyId ,page,size,courseListRequest);
    }

    ///*添加课程基础信息*/
    @Override
    @PostMapping("/coursebase/add")
    public AddCourseResult addCourse(@RequestBody CourseBase courseBase) {
        AddCourseResult addCourseResult = courseService.addCourseResult(courseBase);
        return addCourseResult;
    }

    //根据课程id查询课程信息 修改信息 页面回显
    @Override
    @GetMapping("/find/course/{courseId}")
    public CourseBase findById(@PathVariable("courseId") String courseId) {
        return courseService.findById(courseId);
    }

    //修改课程信息
    @Override
    @PutMapping("/coursebase/update/{id}")
    public ResponseResult update(@PathVariable("id") String id,@RequestBody CourseBase courseBase) {
        ResponseResult responseResult = courseService.update(id, courseBase);
        return responseResult;
    }

    //添加课程与图片的信息
    @Override
    @PostMapping("/coursepic/add")
    public ResponseResult addCoursePic(@RequestParam("courseId") String courseId, @RequestParam("pic")String pic) {
        return courseService.addCoursePic(courseId,pic);
    }

    /****
     * 查询课程图片
     * @param courseId 课程id
     * @return CoursePic信息
     */

    @PreAuthorize("hasAuthority('course_find_pic')")
    @Override
    @GetMapping("/coursepic/list/{courseId}")
    public CoursePic findPic(@PathVariable("courseId") String courseId) {
        return courseService.findPic(courseId);
    }

    /****
     * 删除课程图片
     * @param courseId 课程ID
     * @return 数据库影响行数
     */
    @Override
    @DeleteMapping("/coursepic/delete")
    public ResponseResult deleteCoursePic(@RequestParam("courseId") String courseId) {
        return courseService.deleteCoursePic(courseId);
    }

    /****
     * 查询课程视图信息
     * @param id 课程id
     * @return 课程视图信息
     *
     */
    @Override
    @GetMapping("/courseview/{id}")
    public CourseView findCourseView(@PathVariable("id") String id) {
        return courseService.getCourseView(id);
    }

    /*****
     * 课程预览
     * @param courseId
     * @return
     */
    @Override
    @PostMapping("/preview/{courseId}")
    public CoursePublishResult preview(@PathVariable("courseId") String courseId) {
        return courseService.preview(courseId);
    }

    /****
     * 课程一键发布
     * @param id
     * @return
     */
    @Override
    @PostMapping("/publish/{id}")
    public CoursePublishResult publish(@PathVariable("id") String id) {
        return courseService.publish(id);
    }

    /*****
     * 保存课程计划与媒资信息
     * @param teachplanMedia
     * @return
     */
    @Override
    @PostMapping("/savemedia")
    public ResponseResult saveMedia(@RequestBody TeachplanMedia teachplanMedia) {
        return courseService.saveMedia(teachplanMedia);
    }
}
