package com.xuecheng.order.mq;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.config.RabbitMQConfig;
import com.xuecheng.order.service.XcTaskService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@Component
public class ChooseCourseTask {

    @Autowired
    XcTaskService xcTaskService;


    /***
     * 删除当前任务的监听MQ方法
     * @param xcTask
     */
    @RabbitListener(queues = RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE)
    public void receiveFinishChoosecourseTask(XcTask xcTask){
        //接收到的消息
        String taskId = xcTask.getId();
        //删除任务，添加历史任务
        xcTaskService.finishTask(taskId);
    }


    @Scheduled(cron = "0/3 * * * * *")
    public void  sendChoosecourseTask(){
        //取出当前时间1分钟之前的时间
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(GregorianCalendar.MINUTE,-1);
        Date time = calendar.getTime();
        List<XcTask> xcTask = xcTaskService.findXcTaskByUpdateTimeBefore(time, 10);
        System.out.println(xcTask);
        for (XcTask task:xcTask){
            //调用乐观锁方法校验任务是否可以执行
            if (xcTaskService.getTask(task.getId(),task.getVersion()) > 0 ){
                String ex = task.getMqExchange();
                String mqRoutingkey = task.getMqRoutingkey();
                xcTaskService.publish(task,ex,mqRoutingkey);
            }
        }
    }



}
