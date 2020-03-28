package dev.sheldan.abstracto.scheduling.factory;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.TimeZone;

import static org.quartz.SimpleScheduleBuilder.*;
import static org.quartz.CronScheduleBuilder.*;
import static org.quartz.TriggerBuilder.*;

@Component
@Slf4j
public class QuartzConfigFactory {

    public JobDetail createJob(Class<? extends QuartzJobBean> jobClass, boolean isDurable,
                               ApplicationContext context, String jobName, String jobGroup, boolean requestsRecovery) {

        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(jobClass);
        factoryBean.setDurability(isDurable);
        factoryBean.setApplicationContext(context);
        factoryBean.setRequestsRecovery(requestsRecovery);
        factoryBean.setName(jobName);
        factoryBean.setGroup(jobGroup);

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(jobName + jobGroup, jobClass.getName());
        factoryBean.setJobDataMap(jobDataMap);

        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }

    public CronTrigger createBasicCronTrigger(Date startTime, String cronExpression) {
              return newTrigger()
                .withSchedule(cronSchedule(cronExpression).inTimeZone(TimeZone.getTimeZone("UTC")).withMisfireHandlingInstructionIgnoreMisfires())
                .startAt(startTime)
                .build();
    }

    public Trigger createSimpleOnceOnlyTrigger(String triggerName, Date startTime) {
        return newTrigger()
                .startAt(startTime)
                .withSchedule(simpleSchedule())
                .build();
    }

    public Trigger createOnceOnlyTriggerForJob(String jobName, String jobGroup, Date startTime, JobDataMap jobDataMap) {
        return newTrigger()
                .startAt(startTime)
                .forJob(jobName, jobGroup)
                .withSchedule(simpleSchedule())
                .usingJobData(jobDataMap)
                .build();
    }
}


