package com.xuecheng.order.dao;

import com.xuecheng.framework.domain.task.XcTask;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;

public interface XcTaskRepository extends JpaRepository<XcTask,String> {
    //查询前一分钟的N条数据
    public Page<XcTask> findXcTaskByUpdateTimeBefore(Pageable pageable, Date updateTime);

    //更新updateTime
    @Modifying
    @Query("update XcTask t set t.updateTime=:updateTime where t.id=:id")
    public int updateTaskTime(@Param(value = "id") String id,@Param(value = "updateTime") Date updateTime);


    //使用乐观锁方式校验任务id和版本号是否匹配，匹配则版本号加1
    @Modifying
    @Query("update XcTask t set t.version=:version+1 where t.id=:id and t.version=:version")
    public int updateTaskVersion(@Param(value = "id") String id,@Param("version") int version);
}
