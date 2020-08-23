# springboot + quartz + mysql 实现持久化分布式调度
1. 官方文档：[http://www.quartz-scheduler.org/documentation/quartz-2.3.0/](http://www.quartz-scheduler.org/documentation/quartz-2.3.0/)
2. 中文文档：[https://www.w3cschool.cn/quartz_doc/quartz_doc-2put2clm.html](https://www.w3cschool.cn/quartz_doc/quartz_doc-2put2clm.html)
## 1. 获取 sql 脚本文件，创建数据库
在官网下载好包后，在 `quartz-2.3.0-SNAPSHOT\src\org\quartz\impl\jdbcjobstore` 目录下可以选择适合自己的数据库脚本文件   
我使用的数据库是mysql,因此我也将sql脚本放到了本项目的 `resources` 目录下
```$xslt
DROP TABLE IF EXISTS QRTZ_FIRED_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_PAUSED_TRIGGER_GRPS;
DROP TABLE IF EXISTS QRTZ_SCHEDULER_STATE;
DROP TABLE IF EXISTS QRTZ_LOCKS;
DROP TABLE IF EXISTS QRTZ_SIMPLE_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_SIMPROP_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_CRON_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_BLOB_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_JOB_DETAILS;
DROP TABLE IF EXISTS QRTZ_CALENDARS;

CREATE TABLE QRTZ_JOB_DETAILS(
SCHED_NAME VARCHAR(120) NOT NULL,
JOB_NAME VARCHAR(190) NOT NULL,
JOB_GROUP VARCHAR(190) NOT NULL,
DESCRIPTION VARCHAR(250) NULL,
JOB_CLASS_NAME VARCHAR(250) NOT NULL,
IS_DURABLE VARCHAR(1) NOT NULL,
IS_NONCONCURRENT VARCHAR(1) NOT NULL,
IS_UPDATE_DATA VARCHAR(1) NOT NULL,
REQUESTS_RECOVERY VARCHAR(1) NOT NULL,
JOB_DATA BLOB NULL,
PRIMARY KEY (SCHED_NAME,JOB_NAME,JOB_GROUP))
ENGINE=InnoDB;

CREATE TABLE QRTZ_TRIGGERS (
SCHED_NAME VARCHAR(120) NOT NULL,
TRIGGER_NAME VARCHAR(190) NOT NULL,
TRIGGER_GROUP VARCHAR(190) NOT NULL,
JOB_NAME VARCHAR(190) NOT NULL,
JOB_GROUP VARCHAR(190) NOT NULL,
DESCRIPTION VARCHAR(250) NULL,
NEXT_FIRE_TIME BIGINT(13) NULL,
PREV_FIRE_TIME BIGINT(13) NULL,
PRIORITY INTEGER NULL,
TRIGGER_STATE VARCHAR(16) NOT NULL,
TRIGGER_TYPE VARCHAR(8) NOT NULL,
START_TIME BIGINT(13) NOT NULL,
END_TIME BIGINT(13) NULL,
CALENDAR_NAME VARCHAR(190) NULL,
MISFIRE_INSTR SMALLINT(2) NULL,
JOB_DATA BLOB NULL,
PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
FOREIGN KEY (SCHED_NAME,JOB_NAME,JOB_GROUP)
REFERENCES QRTZ_JOB_DETAILS(SCHED_NAME,JOB_NAME,JOB_GROUP))
ENGINE=InnoDB;

CREATE TABLE QRTZ_SIMPLE_TRIGGERS (
SCHED_NAME VARCHAR(120) NOT NULL,
TRIGGER_NAME VARCHAR(190) NOT NULL,
TRIGGER_GROUP VARCHAR(190) NOT NULL,
REPEAT_COUNT BIGINT(7) NOT NULL,
REPEAT_INTERVAL BIGINT(12) NOT NULL,
TIMES_TRIGGERED BIGINT(10) NOT NULL,
PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP))
ENGINE=InnoDB;

CREATE TABLE QRTZ_CRON_TRIGGERS (
SCHED_NAME VARCHAR(120) NOT NULL,
TRIGGER_NAME VARCHAR(190) NOT NULL,
TRIGGER_GROUP VARCHAR(190) NOT NULL,
CRON_EXPRESSION VARCHAR(120) NOT NULL,
TIME_ZONE_ID VARCHAR(80),
PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP))
ENGINE=InnoDB;

CREATE TABLE QRTZ_SIMPROP_TRIGGERS
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(190) NOT NULL,
    TRIGGER_GROUP VARCHAR(190) NOT NULL,
    STR_PROP_1 VARCHAR(512) NULL,
    STR_PROP_2 VARCHAR(512) NULL,
    STR_PROP_3 VARCHAR(512) NULL,
    INT_PROP_1 INT NULL,
    INT_PROP_2 INT NULL,
    LONG_PROP_1 BIGINT NULL,
    LONG_PROP_2 BIGINT NULL,
    DEC_PROP_1 NUMERIC(13,4) NULL,
    DEC_PROP_2 NUMERIC(13,4) NULL,
    BOOL_PROP_1 VARCHAR(1) NULL,
    BOOL_PROP_2 VARCHAR(1) NULL,
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
    REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP))
ENGINE=InnoDB;

CREATE TABLE QRTZ_BLOB_TRIGGERS (
SCHED_NAME VARCHAR(120) NOT NULL,
TRIGGER_NAME VARCHAR(190) NOT NULL,
TRIGGER_GROUP VARCHAR(190) NOT NULL,
BLOB_DATA BLOB NULL,
PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
INDEX (SCHED_NAME,TRIGGER_NAME, TRIGGER_GROUP),
FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP))
ENGINE=InnoDB;

CREATE TABLE QRTZ_CALENDARS (
SCHED_NAME VARCHAR(120) NOT NULL,
CALENDAR_NAME VARCHAR(190) NOT NULL,
CALENDAR BLOB NOT NULL,
PRIMARY KEY (SCHED_NAME,CALENDAR_NAME))
ENGINE=InnoDB;

CREATE TABLE QRTZ_PAUSED_TRIGGER_GRPS (
SCHED_NAME VARCHAR(120) NOT NULL,
TRIGGER_GROUP VARCHAR(190) NOT NULL,
PRIMARY KEY (SCHED_NAME,TRIGGER_GROUP))
ENGINE=InnoDB;

CREATE TABLE QRTZ_FIRED_TRIGGERS (
SCHED_NAME VARCHAR(120) NOT NULL,
ENTRY_ID VARCHAR(95) NOT NULL,
TRIGGER_NAME VARCHAR(190) NOT NULL,
TRIGGER_GROUP VARCHAR(190) NOT NULL,
INSTANCE_NAME VARCHAR(190) NOT NULL,
FIRED_TIME BIGINT(13) NOT NULL,
SCHED_TIME BIGINT(13) NOT NULL,
PRIORITY INTEGER NOT NULL,
STATE VARCHAR(16) NOT NULL,
JOB_NAME VARCHAR(190) NULL,
JOB_GROUP VARCHAR(190) NULL,
IS_NONCONCURRENT VARCHAR(1) NULL,
REQUESTS_RECOVERY VARCHAR(1) NULL,
PRIMARY KEY (SCHED_NAME,ENTRY_ID))
ENGINE=InnoDB;

CREATE TABLE QRTZ_SCHEDULER_STATE (
SCHED_NAME VARCHAR(120) NOT NULL,
INSTANCE_NAME VARCHAR(190) NOT NULL,
LAST_CHECKIN_TIME BIGINT(13) NOT NULL,
CHECKIN_INTERVAL BIGINT(13) NOT NULL,
PRIMARY KEY (SCHED_NAME,INSTANCE_NAME))
ENGINE=InnoDB;

CREATE TABLE QRTZ_LOCKS (
SCHED_NAME VARCHAR(120) NOT NULL,
LOCK_NAME VARCHAR(40) NOT NULL,
PRIMARY KEY (SCHED_NAME,LOCK_NAME))
ENGINE=InnoDB;

CREATE INDEX IDX_QRTZ_J_REQ_RECOVERY ON QRTZ_JOB_DETAILS(SCHED_NAME,REQUESTS_RECOVERY);
CREATE INDEX IDX_QRTZ_J_GRP ON QRTZ_JOB_DETAILS(SCHED_NAME,JOB_GROUP);

CREATE INDEX IDX_QRTZ_T_J ON QRTZ_TRIGGERS(SCHED_NAME,JOB_NAME,JOB_GROUP);
CREATE INDEX IDX_QRTZ_T_JG ON QRTZ_TRIGGERS(SCHED_NAME,JOB_GROUP);
CREATE INDEX IDX_QRTZ_T_C ON QRTZ_TRIGGERS(SCHED_NAME,CALENDAR_NAME);
CREATE INDEX IDX_QRTZ_T_G ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_GROUP);
CREATE INDEX IDX_QRTZ_T_STATE ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_N_STATE ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP,TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_N_G_STATE ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_GROUP,TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_NEXT_FIRE_TIME ON QRTZ_TRIGGERS(SCHED_NAME,NEXT_FIRE_TIME);
CREATE INDEX IDX_QRTZ_T_NFT_ST ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_STATE,NEXT_FIRE_TIME);
CREATE INDEX IDX_QRTZ_T_NFT_MISFIRE ON QRTZ_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME);
CREATE INDEX IDX_QRTZ_T_NFT_ST_MISFIRE ON QRTZ_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_NFT_ST_MISFIRE_GRP ON QRTZ_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_GROUP,TRIGGER_STATE);

CREATE INDEX IDX_QRTZ_FT_TRIG_INST_NAME ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,INSTANCE_NAME);
CREATE INDEX IDX_QRTZ_FT_INST_JOB_REQ_RCVRY ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,INSTANCE_NAME,REQUESTS_RECOVERY);
CREATE INDEX IDX_QRTZ_FT_J_G ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,JOB_NAME,JOB_GROUP);
CREATE INDEX IDX_QRTZ_FT_JG ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,JOB_GROUP);
CREATE INDEX IDX_QRTZ_FT_T_G ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);
CREATE INDEX IDX_QRTZ_FT_TG ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,TRIGGER_GROUP);

commit;
```

## 2. 创建springboot项目，导入maven依赖包
```
        <dependency>
            <groupId>com.mchange</groupId>
            <artifactId>c3p0</artifactId>
            <version>0.9.5.4</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-quartz</artifactId>
            <version>2.3.3.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
        </dependency>
```

## 3. 在 `resources` 目录下创建 quartz 的配置文件 `quartz.properties`

```$xslt
#quartz集群配置
# ===========================================================================
# Configure Main Scheduler Properties 调度器属性
# ===========================================================================
#调度标识名 集群中每一个实例都必须使用相同的名称
org.quartz.scheduler.instanceName=DefaultQuartzScheduler
#ID设置为自动获取 每一个必须不同
org.quartz.scheduler.instanceid=AUTO
#============================================================================
# Configure ThreadPool
#============================================================================
#线程池的实现类（一般使用SimpleThreadPool即可满足几乎所有用户的需求）
org.quartz.threadPool.class=org.quartz.simpl.SimpleThreadPool
#指定线程数，至少为1（无默认值）(一般设置为1-100直接的整数合适)
org.quartz.threadPool.threadCount=25
#设置线程的优先级（最大为java.lang.Thread.MAX_PRIORITY 10，最小为Thread.MIN_PRIORITY 1，默认为5）
org.quartz.threadPool.threadPriority=5
#============================================================================
# Configure JobStore
#============================================================================
# 信息保存时间 默认值60秒
org.quartz.jobStore.misfireThreshold=60000
#数据保存方式为数据库持久化
org.quartz.jobStore.class=org.quartz.impl.jdbcjobstore.JobStoreTX
#数据库代理类，一般org.quartz.impl.jdbcjobstore.StdJDBCDelegate可以满足大部分数据库
org.quartz.jobStore.driverDelegateClass=org.quartz.impl.jdbcjobstore.StdJDBCDelegate
#JobDataMaps是否都为String类型
org.quartz.jobStore.useProperties=false
#数据库别名 随便取
org.quartz.jobStore.dataSource=myDS
#表的前缀，默认QRTZ_
org.quartz.jobStore.tablePrefix=QRTZ_
#是否加入集群
org.quartz.jobStore.isClustered=true
#调度实例失效的检查时间间隔
org.quartz.jobStore.clusterCheckinInterval=20000
#============================================================================
# Configure Datasources
#============================================================================
#数据库引擎
org.quartz.dataSource.myDS.driver=com.mysql.cj.jdbc.Driver
#数据库连接
org.quartz.dataSource.myDS.URL=jdbc:mysql://localhost:3306/quartz?characterEncoding=utf8&allowMultiQueries=true&useSSL=false&autoReconnect=true&serverTimezone=UTC
#数据库用户
org.quartz.dataSource.myDS.user=root
#数据库密码
org.quartz.dataSource.myDS.password=root
#允许最大连接
org.quartz.dataSource.myDS.maxConnections=5
#验证查询sql,可以不设置
org.quartz.dataSource.myDS.validationQuery=select 0 from dual
```

## 4. 注册Quartz任务工厂（TaskJobFactory）
```$xslt
@Component
public class TaskJobFactory extends AdaptableJobFactory {
    @Autowired
    AutowireCapableBeanFactory capableBeanFactory;

    @Override
    protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
        //调用父类的方法
        Object jobInstance = super.createJobInstance(bundle);
        //进行注入
        capableBeanFactory.autowireBean(jobInstance);
        return jobInstance;
    }
}
```
## 5. 注册调度工厂，以及生成调度实例
若要在该服务启动时，就执行调度任务，需要将 `factory.setAutoStartup(false);` 改为 `factory.setAutoStartup(true);`
```$xslt
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
```

## 6. Quartz服务层接口（QuartzService）
```$xslt
public interface QuartzService {
    /**
     * 添加任务可以传参数
     *
     * @param clazz
     * @param jobName
     * @param groupName
     * @param cronExp
     * @param param
     */
    void addJob(Class clazz, String jobName, String groupName, String cronExp, Map<String, Object> param);

    /**
     * 暂停任务
     *
     * @param name
     * @param groupName
     */
    void pauseJob(String name, String groupName);

    /**
     * 恢复任务
     *
     * @param name
     * @param groupName
     */
    void resumeJob(String name, String groupName);

    /**
     * 更新任务
     *
     * @param name
     * @param groupName
     * @param cronExp
     * @param param
     */
    void updateJob(String name, String groupName, String cronExp, Map<String, Object> param);

    /**
     * 删除任务
     *
     * @param name
     * @param groupName
     */
    void deleteJob(String name, String groupName);

    /**
     * 启动所有任务
     */
    void startAllJobs();

    /**
     * 关闭所有任务
     */
    void shutdownAllJobs();
}

```

## 7. 任务调度服务接口实现类（QuartzServiceImpl）
```$xslt
@Service
public class QuartzServiceImpl implements QuartzService {

    @Autowired
    Scheduler scheduler;


    /**
     * 创建job，可传参
     *
     * @param clazz     任务类
     * @param name      任务名称
     * @param groupName 任务所在组名称
     * @param cronExp   cron表达式
     * @param param     map形式参数
     */
    @Override
    public void addJob(Class clazz, String name, String groupName, String cronExp, Map<String, Object> param) {
        try {
            // 启动调度器
//            scheduler.start();
            //构建job信息
            //((Job) clazz.newInstance()).getClass()
            JobDetail jobDetail = JobBuilder.newJob(clazz).withIdentity(name, groupName).build();
            //表达式调度构建器(即任务执行的时间)
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExp);
            //按新的cronExpression表达式构建一个新的trigger
            CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(name, groupName).withSchedule(scheduleBuilder).build();
            //获得JobDataMap，写入数据
            if (param != null) {
                trigger.getJobDataMap().putAll(param);
            }
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 暂停job
     *
     * @param name      任务名称
     * @param groupName 任务所在组名称
     */
    @Override
    public void pauseJob(String name, String groupName) {
        try {
            scheduler.pauseJob(JobKey.jobKey(name, groupName));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    /**
     * 恢复job
     *
     * @param name      任务名称
     * @param groupName 任务所在组名称
     */
    @Override
    public void resumeJob(String name, String groupName) {
        try {
            scheduler.resumeJob(JobKey.jobKey(name, groupName));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    /**
     * job 更新,更新频率和参数
     *
     * @param name      任务名称
     * @param groupName 任务所在组名称
     * @param cronExp   cron表达式
     * @param param     参数
     */
    @Override
    public void updateJob(String name, String groupName, String cronExp, Map<String, Object> param) {
        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(name, groupName);
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            if (cronExp != null) {
                // 表达式调度构建器
                CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExp);
                // 按新的cronExpression表达式重新构建trigger
                trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
            }

            //修改map
            if (param != null) {
                trigger.getJobDataMap().putAll(param);
            }
            // 按新的trigger重新设置job执行
            scheduler.rescheduleJob(triggerKey, trigger);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * job 删除
     * <p>
     * 同时删除 调度中的触发器 Trigger 和任务 job
     *
     * @param name      任务名称
     * @param groupName 任务所在组名称
     */
    @Override
    public void deleteJob(String name, String groupName) {
        try {
            scheduler.pauseTrigger(TriggerKey.triggerKey(name, groupName));
            scheduler.unscheduleJob(TriggerKey.triggerKey(name, groupName));
            scheduler.deleteJob(JobKey.jobKey(name, groupName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 启动所有定时任务
     */
    @Override
    public void startAllJobs() {
        try {
            scheduler.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 关闭所有定时任务
     */
    @Override
    public void shutdownAllJobs() {
        try {
            if (!scheduler.isShutdown()) {
                // scheduler生命周期结束，无法再 start() 启动
                scheduler.shutdown();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
```

## 8. 需要执行的任务（OrderTimeOutJob ）
```$xslt
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
```

## 9. 编写接口类进行测试（JobController）
```$xslt
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
```

## 10. 最后添加配置文件 `application.yml`,启动项目
```$xslt
spring:
  application:
    name: quartz
  datasource:
    url: jdbc:mysql://localhost:3306/quartz?characterEncoding=utf8&allowMultiQueries=true&useSSL=false&autoReconnect=true&serverTimezone=UTC
    username: root
    password: root
```

## 总结
1. 若需要随着上面项目的启动，就执行调度任务，需要将步骤5中 `factory.setAutoStartup(false);` 改为 `factory.setAutoStartup(true);`，或者直接删除 `factory.setAutoStartup(false);`
2. 为了保证调度的高可用，重新创建一个或多个新项目用来执行调度，新项目中只需要配置上面的 `2~5` 步骤即可
，第五步将 `factory.setAutoStartup(false);` 改为 `factory.setAutoStartup(true);`，或者直接删除 `factory.setAutoStartup(false);`


代码仓库地址：[https://github.com/1612480331/quartz](https://github.com/1612480331/quartz)

