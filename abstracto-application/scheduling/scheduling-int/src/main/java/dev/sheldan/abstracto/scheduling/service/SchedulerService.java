package dev.sheldan.abstracto.scheduling.service;

import dev.sheldan.abstracto.scheduling.model.database.SchedulerJob;
import org.quartz.JobDataMap;

import java.util.Date;

public interface SchedulerService {
    void startScheduledJobs();
    void scheduleJob(SchedulerJob job);
    void updateJob(SchedulerJob job, Date startDate);
    boolean unScheduleJob(String jobName);
    boolean deleteJob(SchedulerJob job);
    boolean pauseJob(SchedulerJob job);
    boolean continueJob(SchedulerJob job);
    boolean executeJob(SchedulerJob job);
    String executeJobWithParametersOnce(String name, String group, JobDataMap dataMap, Date date);
    String startCronJobWithParameters(String name, String group, JobDataMap dataMap, String cronExpression);
    void stopTrigger(String triggerKey);
    void startScheduler();
}
