package com.xuecheng.order.service;


import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.order.dao.XcTaskHisRepository;
import com.xuecheng.order.dao.XcTaskRepository;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class XcTaskService {

    @Autowired
    XcTaskRepository xcTaskRepository;

    @Autowired
    XcTaskHisRepository xcTaskHisRepository;



    @Autowired
    RabbitTemplate rabbitTemplate;

    public List<XcTask> findXcTaskByUpdateTimeBefore(Date updateTime,int size){
        Pageable pageable = new PageRequest(0,size);
        Page<XcTask> all = xcTaskRepository.findXcTaskByUpdateTimeBefore(pageable, updateTime);
        List<XcTask> content = all.getContent();
        return content;
    }

    /*****
     *  发送消息
     * @param xcTask 任务对象
     * @param ex 交换机名称
     * @param routingKey routingKey
     */
    public void publish(XcTask xcTask ,String ex,String routingKey){
        //查询数据库是否有数据
        Optional<XcTask> xcTaskOptional = xcTaskRepository.findById(xcTask.getId());
        if (xcTaskOptional.isPresent()){
            XcTask task = xcTaskOptional.get();
            //发送MQ消息
            rabbitTemplate.convertAndSend(ex,routingKey,task);
            //新修改UpdateTime
            task.setUpdateTime(new Date());
            xcTaskRepository.save(task);
        }

    }

    /***
     * 使用乐观锁方法校验任务
     * @param id taskId
     * @param version 版本号
     * @return 影响数据库的行数
     */
    @Transactional
    public int getTask(String id,int version){
        int count = xcTaskRepository.updateTaskVersion(id, version);
        return count;
    }


    /****
     *
     *  删除当前任务
     * @param taskId
     */
    @Transactional
    public void finishTask(String taskId){
        Optional<XcTask> taskOptional = xcTaskRepository.findById(taskId);
        if (taskOptional.isPresent()){
            XcTask xcTask = taskOptional.get();
            //设置删除时间
            xcTask.setDeleteTime(new Date());
            //当前任务
            XcTaskHis xcTaskHis = new XcTaskHis();
            //将当前任务Beancopy到历史任务
            BeanUtils.copyProperties(xcTask,xcTaskHis);
            //保存历史任务
            xcTaskHisRepository.save(xcTaskHis);
            //删除当前任务
            xcTaskRepository.delete(xcTask);
        }

    }



}
