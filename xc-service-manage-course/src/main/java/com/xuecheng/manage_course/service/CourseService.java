package com.xuecheng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.request.QueryResponseResult;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.dao.*;
import com.xuecheng.manage_course.feign.CmsPageClient;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    CourseBaseRepository courseBaseRepository;

    @Autowired
    TeachplanRepository teachplanRepository;

    @Autowired
    CoursePicRepository coursePicRepository;

    @Autowired
    CourseMarketRepository courseMarketRepository;

    @Value("${course-publish.dataUrlPre}")
    private String publish_dataUrlPre;
    @Value("${course-publish.pagePhysicalPath}")
    private String publish_page_physicalpath;
    @Value("${course-publish.pageWebPath}")
    private String publish_page_webpath;
    @Value("${course-publish.siteId}")
    private String publish_siteId;
    @Value("${course-publish.templateId}")
    private String publish_templateId;
    @Value("${course-publish.previewUrl}")
    private String previewUrl;

    @Autowired
    CmsPageClient cmsPageClient;

    @Autowired
    CoursePubRepository coursePubRepository;

    @Autowired
    TeachplanMediaRepository teachplanMediaRepository;

    @Autowired
    TeachplanMediaPubRepository teachplanMediaPubRepository;

    //查询课程列表
    public TeachplanNode findTeachplanList(String courseId){
       return teachplanMapper.selectist(courseId);
    }




    @Transactional
    //添加课程计划
    public ResponseResult addTeachplan(Teachplan teachplan) {
        if (teachplan == null || StringUtils.isEmpty(teachplan.getCourseid()) || StringUtils.isEmpty(teachplan.getPname())){
            ExceptionCast.cast(CommonCode.INVALID_PAPAM);
        }
        //取出课程id
        String courseid = teachplan.getCourseid();
        //取出parentId
        String parentid = teachplan.getParentid();
        if (StringUtils.isEmpty(parentid)){
            parentid = this.getTeachplanRoot(courseid);
        }

        Optional<Teachplan> optional = teachplanRepository.findById(parentid);
        Teachplan parentNode = optional.get();
        //取出父节点
        String grade = parentNode.getGrade();
        //新节点
        Teachplan teachplanNew = new Teachplan();
        //将teachplan拷贝到teachplanNew
        BeanUtils.copyProperties(teachplan,teachplanNew);
        teachplanNew.setCourseid(courseid);
        teachplanNew.setParentid(parentid);
        if (grade.equals("1")){ //级别根据父节点 设置
            teachplanNew.setGrade("2");
        }else {
            teachplanNew.setGrade("3");
        }
        teachplanRepository.save(teachplanNew);


        return new ResponseResult(CommonCode.SUCCESS);
    }



    //查询课程的根节点 如果查询不到 需要自动创建根节点
    private String getTeachplanRoot(String coureseId){
        Optional<CourseBase> optional = courseBaseRepository.findById(coureseId);
        if (!optional.isPresent()){
            return null;
        }
        //课程信息
        CourseBase courseBase = optional.get();
        //查询课程根节点
        List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndParentid(coureseId, "0");
        if (teachplanList == null || teachplanList.size()<=0){
            //查询不到根节点 需要自动创建
            Teachplan teachplan = new Teachplan();
            teachplan.setParentid("0");
            teachplan.setGrade("1");
            teachplan.setPname(courseBase.getName());
            teachplan.setCourseid(coureseId);
            teachplan.setStatus("0");
            teachplanRepository.save(teachplan);
            return teachplan.getId();
        }

        return teachplanList.get(0).getId();
    }


    @Autowired
    CourseMapper courseMapper;
    //查询我的课程列表
    public QueryResponseResult<CourseInfo> findCourseList(String companyId ,int page, int size , CourseListRequest courseListRequest){
        if (courseListRequest == null){
            courseListRequest = new CourseListRequest();
        }
        //将查询条件存入CourseListRequest
        courseListRequest.setCompanyId(companyId);
        if (page <=0 ){
            page = 1;
        }
        if (size <= 0){
            size = 10;
        }
        PageHelper.startPage(page,size);
        Page<CourseInfo> courseListPage = courseMapper.findCourseListPage(courseListRequest);
        //查询列表
        List<CourseInfo> list = courseListPage.getResult();
        //查询总记录数
        long total = courseListPage.getTotal();
        //查询结果集
        QueryResult<CourseInfo> courseInfoQueryResult = new QueryResult<>();
        courseInfoQueryResult.setList(list);
        courseInfoQueryResult.setTotal(total);
        return new QueryResponseResult<CourseInfo>(CommonCode.SUCCESS,courseInfoQueryResult);
    }

    //添加课程基础信息
    public AddCourseResult addCourseResult(CourseBase courseBase){
        //课程状态默认为未发布
        courseBase.setStatus("202001");
        courseBaseRepository.save(courseBase);
        return new AddCourseResult(CommonCode.SUCCESS,courseBase.getId());
    }

    //根据课程id查询课程
    public CourseBase findById(String courseId){
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (optional.isPresent()){
            CourseBase courseBase = optional.get();
            return courseBase;
        }
        return null;
    }

    //修改课程信息
    public ResponseResult update(String id,CourseBase courseBase){
        CourseBase course = this.findById(id);
        if (course == null){
            ExceptionCast.cast(CommonCode.INVALID_PAPAM);
        }
        //修改课程信息
        course.setName(courseBase.getName());
        course.setSt(courseBase.getSt());
        course.setMt(courseBase.getMt());
        course.setGrade(courseBase.getGrade());
        course.setStudymodel(courseBase.getStudymodel());
        course.setUsers(courseBase.getUsers());
        course.setDescription(courseBase.getDescription());
        courseBaseRepository.save(course);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /*****
     *   添加课程与图片的信息
     * @param courseId 课程ID
     * @param pic 课程图片
     * @return 返回状态码
     */
    @Transactional
    public ResponseResult addCoursePic(String courseId, String pic) {
        CoursePic coursePic = null;
                //查询是否存在
        Optional<CoursePic> op = coursePicRepository.findById(courseId);
        if (op.isPresent()){
            coursePic = op.get();
        }
        //没有课程图片则新建
        if (coursePic == null){
            coursePic = new CoursePic();
        }


        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);
        coursePicRepository.save(coursePic);


        return new ResponseResult(CommonCode.SUCCESS);
    }

    /****
     * 查询课程图片
     * @param courseId 课程id
     * @return CoursePic信息
     */
    public CoursePic findPic(String courseId) {
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        if(optional.isPresent()){
            CoursePic coursePic = optional.get();
            return coursePic;
        }
        return null;
    }

    /****
     * 删除课程图片
     * @param courseId 课程id
     * @return 影响数据的行数
     */
    @Transactional
    public ResponseResult deleteCoursePic(String courseId) {
        long count = coursePicRepository.deleteByCourseid(courseId);
        if (count > 0){
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    /*****
     * 查询课程视图信息
     * @param id 课程id
     * @return 课程视图全部信息
     */
    public CourseView getCourseView(String id) {
        /****
         *  private CoursePic coursePic; //课程图片
         *     private CourseBase courseBase; //课程基础信息
         *     private CourseMarket courseMarket; //课程营销信息
         *     private TeachplanNode teachplanNode; //课程教学计划
         *
         */
        CourseView courseView = new CourseView();
        //课程图片 信息
        Optional<CoursePic> coursePicOptional = coursePicRepository.findById(id);
        if (coursePicOptional.isPresent()){
            CoursePic coursePic = coursePicOptional.get();
            courseView.setCoursePic(coursePic);
        }

        //课程基础信息
        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(id);
        if (courseBaseOptional.isPresent()){
            CourseBase courseBase = courseBaseOptional.get();
            courseView.setCourseBase(courseBase);
        }

        //课程营销信息
        Optional<CourseMarket> courseMarketOptional = courseMarketRepository.findById(id);
        if (courseMarketOptional.isPresent()){
            CourseMarket courseMarket = courseMarketOptional.get();
            courseView.setCourseMarket(courseMarket);
        }

        //课程教学计划信息
        TeachplanNode teachplanNode = teachplanMapper.selectist(id);
        courseView.setTeachplanNode(teachplanNode);
        return courseView;
    }

    /****
     * 课程预览
     * @param courseId 课程ID
     * @return
     */
    public CoursePublishResult preview(String courseId) {
        //查询课程信息
        CourseBase courseBase = this.findCourse(courseId);
        //请求cms远程调用
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);//站点ID
        cmsPage.setPageName(courseId+".html");//页面名称
        cmsPage.setPageAliase(courseBase.getName());//课程名称
        cmsPage.setPageWebPath(publish_page_webpath);//页面物理路径
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);//页面webPath
        cmsPage.setDataUrl(publish_dataUrlPre+courseId);//页面数据模型url
        cmsPage.setTemplateId(publish_templateId);//模板ID

        CmsPageResult cmsPageResult = cmsPageClient.save(cmsPage);
        if (!cmsPageResult.isSuccess()){
            //抛出异常
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        //拼接url
        CmsPage cmsPage1 = cmsPageResult.getCmsPage();
        String pageId = cmsPage1.getPageId();
        String url = previewUrl+pageId;


        return new CoursePublishResult(CommonCode.SUCCESS,url);
    }


    /****
     * 根据课程id查询课程信息
     * @param courseId
     * @return
     */
    public CourseBase findCourse(String courseId){
        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(courseId);
        if (courseBaseOptional.isPresent()){
            CourseBase courseBase = courseBaseOptional.get();
            return courseBase;
        }
        //找不到课程
        ExceptionCast.cast(CourseCode.COURSE_GET_NOTEXISTS);
        return null;
    }

    /****
     * 课程一键发布
     * @param id
     * @return
     */
    @Transactional
    public CoursePublishResult publish(String id) {
        //查询课程信息
        CourseBase courseBase = this.findCourse(id);
        //远程调用cms 一键发布
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);//站点ID
        cmsPage.setPageName(id+".html");//页面名称
        cmsPage.setPageAliase(courseBase.getName());//课程名称
        cmsPage.setPageWebPath(publish_page_webpath);//页面物理路径
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);//页面webPath
        cmsPage.setDataUrl(publish_dataUrlPre+id);//页面数据模型url
        cmsPage.setTemplateId(publish_templateId);//模板ID

        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
        if (!cmsPostPageResult.isSuccess()){
            //抛出异常
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        //更新课程的状态为已发布
        CourseBase courseBaseSave = this.saveCoursePubState(id);
        if (courseBaseSave == null){
            //抛出异常
            return new CoursePublishResult(CommonCode.FAIL,null);
        }

        //保存课程索引信息
        //创建CoursePub对象
        CoursePub coursePub = createCoursePub(id);
        //将coursePub对象保存到数据库
        saveCoursePub(id,coursePub);

        //保存课程计划媒资信息到索引表
        this.saveTeachplanMediaPub(id);
        //得到页面的URL
        String pageUrl = cmsPostPageResult.getPageUrl();
        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);
    }

    // 保存课程计划媒资信息
    private void saveTeachplanMediaPub(String id) {
        //删除teachplanMediaPub信息
        teachplanMediaPubRepository.deleteByCourseId(id);
        //查询课程媒资信息
        List<TeachplanMedia> teachplanMediaList = teachplanMediaRepository.findByCourseId(id);
        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
        for (TeachplanMedia teachplanMedia : teachplanMediaList) {
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            BeanUtils.copyProperties(teachplanMedia,teachplanMediaPub);
            //添加时间戳
            teachplanMediaPub.setTimestamp(new Date());
            teachplanMediaPubList.add(teachplanMediaPub);
        }
        //存储到 teachplanPub数据库中
        teachplanMediaPubRepository.saveAll(teachplanMediaPubList);
    }

    //将coursePub保存到数据库
    private CoursePub saveCoursePub(String id,CoursePub coursePub){
        CoursePub coursePubNew =null;
        //查询coursePub
        Optional<CoursePub> coursePubOptional = coursePubRepository.findById(id);
        if (coursePubOptional.isPresent()){
            coursePubNew = coursePubOptional.get();
        }else {
            coursePubNew = new CoursePub();
        }
        //保存到数据库
        BeanUtils.copyProperties(coursePub,coursePubNew);
        coursePubNew.setId(id);
        //时间戳
        coursePubNew.setTimestamp(new Date());
        //发布时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        String format = simpleDateFormat.format(new Date());
        coursePubNew.setPubTime(format);
        coursePubRepository.save(coursePubNew);

        return coursePubNew;
    }


    //创建coursePub对象
    private CoursePub createCoursePub(String id){
        CoursePub coursePub = new CoursePub();

        //查询课程信息
        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(id);
        if (courseBaseOptional.isPresent()){
            CourseBase courseBase = courseBaseOptional.get();
            //将courseBase属性拷贝到coursePub
            BeanUtils.copyProperties(courseBase,coursePub);
        }

        //查询课程图片信息
        Optional<CoursePic> coursePicOptional = coursePicRepository.findById(id);
        if (coursePicOptional.isPresent()){
            CoursePic coursePic = coursePicOptional.get();
            //将coursePic属性拷贝到coursePub
            BeanUtils.copyProperties(coursePic,coursePub);
        }

        ////课程营销信息
        Optional<CourseMarket> courseMarketOptional = courseMarketRepository.findById(id);
        if (courseMarketOptional.isPresent()){
            CourseMarket courseMarket = courseMarketOptional.get();
            //将courseMarket属性拷贝到coursePub
            BeanUtils.copyProperties(courseMarket,coursePub);
        }


        //课程计划
        TeachplanNode teachplanNode = teachplanMapper.selectist(id);
        String toJSONString = JSON.toJSONString(teachplanNode);
        coursePub.setTeachplan(toJSONString);
        return coursePub;
    }

    //更新课程的状态
    private CourseBase saveCoursePubState(String courseId){
        //查询出课程信息
        CourseBase courseBase = this.findCourse(courseId);
        courseBase.setStatus("202002");//更新状态
        courseBaseRepository.save(courseBase);
        return courseBase;
    }

    /*****
     * 保存课程计划与媒资信息
     * @param teachplanMedia
     * @return
     */
    public ResponseResult saveMedia(TeachplanMedia teachplanMedia) {
        if (teachplanMedia== null || StringUtils.isEmpty(teachplanMedia.getTeachplanId())){
            ExceptionCast.cast(CommonCode.INVALID_PAPAM);
        }
        //获取计划ID
        String teachplanId = teachplanMedia.getTeachplanId();
        //查询课程计划是否是3级
        Optional<Teachplan> teachplanOptional = teachplanRepository.findById(teachplanId);
        if (!teachplanOptional.isPresent()){
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_ISNULL);
        }
        //查询出课程计划
        Teachplan teachplan= teachplanOptional.get();
        //获取课程计划分类等级
        String grade = teachplan.getGrade();
        //判断课程等级是否为3级
        if (grade == null || !"3".equals(grade)){
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_GRADEERROR);
        }
        //查询课程计划和媒资表中
        Optional<TeachplanMedia> teachplanMediaOptional = teachplanMediaRepository.findById(teachplanId);
        TeachplanMedia teachplanMedia1 = null;
        if (teachplanMediaOptional.isPresent()){
             teachplanMedia1 = teachplanMediaOptional.get();
        }else {
            teachplanMedia1 = new TeachplanMedia();
        }
        //向teachplanMedia表中,更新或者添加数据
        teachplanMedia1.setCourseId(teachplan.getCourseid()); //课程Id
        //媒资文件的原始名称
        teachplanMedia1.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());
        //媒资文件id
        teachplanMedia1.setMediaId(teachplanMedia.getMediaId());
        //媒资文件访问地址
        teachplanMedia1.setMediaUrl(teachplanMedia.getMediaUrl());
        //课程计划id
        teachplanMedia1.setTeachplanId(teachplanId);

        teachplanMediaRepository.save(teachplanMedia1);
        return new ResponseResult(CommonCode.SUCCESS);
    }
}

