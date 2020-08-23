package com.example.quartz.config;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
public class QuartzConfig {
    @Autowired
    DataSource dataSource;

    @Autowired
    TaskJobFactory jobFactory;

    @Bean(name = "SchedulerFactory")
    public SchedulerFactoryBean schedulerFactoryBean() throws IOException {
        //获取配置属性
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));
        //在quartz.properties中的属性被读取并注入后再初始化对象
        propertiesFactoryBean.afterPropertiesSet();
        //创建SchedulerFactoryBean
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        //将配置文件中的信息添加到调度工厂中
        //对应java应用程序中的 new StdSchedulerFactory(properties)
        factory.setQuartzProperties(propertiesFactoryBean.getObject());
        factory.setJobFactory(jobFactory);
        // 默认的自动执行调度,这里设置为不自动执行调度，
        // 为了方便扩展集群分布式调度任务，这个服务只配置调度，另外启动一个或多个服务执行调度
        // 若调度不随这项目的启动而启动，则需要将自动启动设为false
        factory.setAutoStartup(false);
        return factory;
    }

    /*
     * 通过SchedulerFactoryBean获取Scheduler的实例
     */
    @Bean(name = "scheduler")
    public Scheduler scheduler() throws IOException, SchedulerException {
        Scheduler scheduler = schedulerFactoryBean().getScheduler();
        return scheduler;
    }
}
