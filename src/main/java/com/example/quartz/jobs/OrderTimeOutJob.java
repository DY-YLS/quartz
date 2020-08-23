package com.example.quartz.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

public class OrderTimeOutJob implements Job {
    @Override
    public void execute(JobExecutionContext context) {
        //获取任务名
        String taskName = context.getJobDetail().getKey().getName();
        String groupName=context.getJobDetail().getKey().getGroup();
        System.out.println(String.format("task--->%s,%s",taskName,groupName));
        //todo:处理执行任务时的业务代码
    }
}
