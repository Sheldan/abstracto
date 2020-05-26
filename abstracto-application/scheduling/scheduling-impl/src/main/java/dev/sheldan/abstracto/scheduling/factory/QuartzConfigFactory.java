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

/**
 * Bean used to create the different types of jobs supported. The jobs include cron jobs and one-time jobs.
 */
@Component
@Slf4j
public class QuartzConfigFactory {

    /**
     * Creates a job according to the given configuration. This is the most detailed configuration. And is used when setting up the job on startup.
     * @param jobClass The class object of the job to be executed. Needs to extend {@link QuartzJobBean}.
     * @param isDurable Whether or not the job should be stored in the database, even though there are no triggers pointing to it. This is needed for
     *                  one time jobs, because they might not have any immediate execution scheduled.
     * @param context The spring application context for the job
     * @param jobName The name of the job to be used for triggers in order to find the job
     * @param jobGroup The group of the job to be used for triggers in order to find the job
     * @param requestsRecovery Whether or not the job should be executed again, if the scheduling application crashes.
     * @return The created description of the job according to the parameters
     */
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

    /**
     * Creates a trigger for a cron job according to a given cron expression and schedules the job to be started at the given time.
     * @param startTime The time the job should be active
     * @param cronExpression The cron expression used for the job.
     * @throws RuntimeException If the cron expression is not a valid expression.
     * @return The {@link CronTrigger} representing the cron expression
     */
    public CronTrigger createBasicCronTrigger(Date startTime, String cronExpression) {
              return newTrigger()
                .withSchedule(cronSchedule(cronExpression).inTimeZone(TimeZone.getTimeZone("UTC")).withMisfireHandlingInstructionDoNothing())
                .startAt(startTime)
                .build();
    }

    /**
     * Creates a detailed cron trigger with the given cron expression and start date for a job specifically.
     * It is also possible to directly provided parameters for the given job, which are then available within the job.
     * @param jobName The name of the job to schedule
     * @param jobGroup The group of the job to schedule
     * @param startTime The start time at which the job should start to execute
     * @param cronExpression The cron expression which represents at which times the job should execute
     * @param jobDataMap The {@link JobDataMap} containing parameters available to the job
     * @return The {@link CronTrigger} which can be used to directly schedule the job in the {@link Scheduler}
     */
    public CronTrigger createBasicCronTrigger(String jobName, String jobGroup, Date startTime, String cronExpression, JobDataMap jobDataMap) {
        return newTrigger()
                .withSchedule(cronSchedule(cronExpression).inTimeZone(TimeZone.getTimeZone("UTC")).withMisfireHandlingInstructionDoNothing())
                .startAt(startTime)
                .usingJobData(jobDataMap)
                .forJob(jobName, jobGroup)
                .build();
    }

    /**
     * Creates a simple trigger, which executes exactly once at the given time
     * @param startTime The {@link Date} object containing the time at which a job should execute at
     * @return The {@link Trigger} object necessary in order to schedule a job
     */
    public Trigger createSimpleOnceOnlyTrigger(Date startTime) {
        return newTrigger()
                .startAt(startTime)
                .withSchedule(simpleSchedule())
                .build();
    }

    /**
     * Creates a simple one time trigger for a specific job with the provided parameters
     * @param jobName The name of the job to execute
     * @param jobGroup The group of the job to execute
     * @param startTime The time at which the job should be executed
     * @param jobDataMap The {@link JobDataMap} containing the parameters which should be available for the job
     * @return The {@link Trigger} containing the given parameters, read to be scheduled with {@link Scheduler}
     */
    public Trigger createOnceOnlyTriggerForJob(String jobName, String jobGroup, Date startTime, JobDataMap jobDataMap) {
        return newTrigger()
                .startAt(startTime)
                .forJob(jobName, jobGroup)
                .withSchedule(simpleSchedule())
                .usingJobData(jobDataMap)
                .build();
    }
}


