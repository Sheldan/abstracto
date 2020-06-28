package dev.sheldan.abstracto.scheduling.service;

import dev.sheldan.abstracto.scheduling.model.database.SchedulerJob;
import org.quartz.JobDataMap;

import java.util.Date;

public interface SchedulerService {
    /**
     * Starts all the currently active and available jobs from the database with their respective configuration
     */
    void startScheduledJobs();

    /**
     * Schedules the given {@link SchedulerJob} instance directly
     * @param job The job to schedule
     */
    void scheduleJob(SchedulerJob job);

    /**
     * Updates an already scheduled job, with the same name and group, with the new {@link SchedulerJob} configuration
     * @param job The new configuration of the job to use. The name and the group of the job to update are taken from this object as well.
     * @param startDate The date at which this scheduled job should start executing
     */
    void updateJob(SchedulerJob job, Date startDate);

    /**
     * Removes a job from the scheduler.
     * @param triggerKey The key of the trigger to unSchedule
     * @return if the job was found and unscheduled
     */
    boolean unScheduleJob(String triggerKey);

    /**
     * Deletes the job from the scheduler.
     * @param job The {@link SchedulerJob} instance containing the configuration of the job to remove
     * @return fi the job was found and deleted
     */
    boolean deleteJob(SchedulerJob job);

    /**
     * Pauses the given job in the scheduler
     * @param job The {@link SchedulerJob} instance containing the configuration of the job to pause
     * @return fi the job was found and paused
     */
    boolean pauseJob(SchedulerJob job);

    /**
     * Continues the job in the scheduler.
     * @param job The {@link SchedulerJob} instance containing the configuration of the job to continue
     * @return fi the job was found and continued
     */
    boolean continueJob(SchedulerJob job);
    /**
     * Executes the job directly in the scheduler.
     * @param job The {@link SchedulerJob} instance containing the configuration of the job to execute directly
     * @return fi the job was found and executed directly
     */
    boolean executeJob(SchedulerJob job);

    /**
     * Executes the job identified by name and group with the given {@link JobDataMap} as parameters on the given {@link Date}
     * @param name The name of the job to execute
     * @param group The group of the job to execute
     * @param dataMap The {@link JobDataMap} made available to the group
     * @param date The {@link Date} at which the job should be execute at.
     * @return The trigger key which triggers the job at the given date
     */
    String executeJobWithParametersOnce(String name, String group, JobDataMap dataMap, Date date);

    /**
     * Stops the trigger identified by the trigger key.
     * @param triggerKey The key of the trigger to stop
     */
    void stopTrigger(String triggerKey);

    /**
     * Actually starts the scheduler.
     */
    void startScheduler();
}
