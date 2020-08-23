package com.example.quartz.controller;

import com.example.quartz.jobs.OrderTimeOutJob;
import com.example.quartz.service.QuartzService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class JobController {

    @Autowired
    QuartzService service;

    /**
     * 添加新任务
     *
     * @return
     */
    @RequestMapping("/addJob")
    public Object addJob(@RequestParam String groupName, @RequestParam String jobName) {
        Map<String, Object> resultMap = new HashMap<>();
        //任务组名
//        String groupName = "order3";
        //任务名
//        String jobName = "20190724120322389224";
        //CRON表达式
        String cronExp = "* * * * * ? *";
        service.addJob(OrderTimeOutJob.class, jobName, groupName, cronExp, null);
        resultMap.put("groupName", groupName);
        resultMap.put("jobName", jobName);
        resultMap.put("cronExp", cronExp);
        return resultMap;
    }

    /**
     * 启动调度
     *
     * @return
     */
    @RequestMapping("/startAllJobs")
    public String startAllJobs() {
        service.startAllJobs();
        return "true";
    }

    /**
     * 关闭调度
     *
     * @return
     */
    @RequestMapping("/shutdownAllJobs")
    public String shutdownAllJobs() {
        service.shutdownAllJobs();
        return "true";
    }

    /**
     * 暂停某个调度
     *
     * @param groupName
     * @param jobName
     * @return
     */
    @RequestMapping("/pauseJob")
    public String pauseJob(@RequestParam String groupName, @RequestParam String jobName) {
        service.pauseJob(jobName, groupName);
        return "true";
    }

    /**
     * 恢复调度某个暂停的任务
     *
     * @param groupName
     * @param jobName
     * @return
     */
    @RequestMapping("/resumeJob")
    public String resumeJob(@RequestParam String groupName, @RequestParam String jobName) {
        service.resumeJob(jobName, groupName);
        return "true";
    }

    /**
     * 删除任务
     *
     * @return
     */
    @RequestMapping("/delJob")
    public Object delJob(@RequestParam String groupName, @RequestParam String jobName) {
        Map<String, Object> resultMap = new HashMap<>();
        //任务组名
        //任务名
        service.deleteJob(jobName, groupName);
        resultMap.put("groupName", groupName);
        resultMap.put("jobName", jobName);
        return resultMap;
    }
}
