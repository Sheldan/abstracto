package dev.sheldan.abstracto.scheduling.model;

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

    boolean executeJobWithParametersOnce(String name, String group, JobDataMap dataMap, Date date);
}
