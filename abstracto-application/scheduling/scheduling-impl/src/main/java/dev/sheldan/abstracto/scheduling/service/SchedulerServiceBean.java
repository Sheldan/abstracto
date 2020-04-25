package dev.sheldan.abstracto.scheduling.service;

import dev.sheldan.abstracto.scheduling.factory.QuartzConfigFactory;
import dev.sheldan.abstracto.scheduling.model.database.SchedulerJob;
import dev.sheldan.abstracto.scheduling.service.management.SchedulerJobManagementServiceBean;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class SchedulerServiceBean implements SchedulerService {

    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private QuartzConfigFactory scheduleCreator;

    @Autowired
    private SchedulerJobManagementServiceBean schedulerJobManagementServiceBean;

    @Override
    public void startScheduledJobs() {
        List<SchedulerJob> jobs = schedulerJobManagementServiceBean.findAll();
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        jobs.forEach(schedulerJob -> {
            if(schedulerJob.isActive()) {
                scheduleNewJob(scheduler, schedulerJob);
            }
        });
    }

    private boolean isRecurringJob(SchedulerJob job) {
        return job.getCronExpression() != null && CronExpression.isValidExpression(job.getCronExpression());
    }

    private void scheduleNewJob(Scheduler scheduler, SchedulerJob schedulerJob) {
        if(!schedulerJob.isActive()) {
            return;
        }
        try {
            JobDetail jobDetail = JobBuilder.newJob((Class<? extends QuartzJobBean>) Class.forName(schedulerJob.getClazz()))
                    .withIdentity(schedulerJob.getName(), schedulerJob.getGroupName()).build();
            if (!scheduler.checkExists(jobDetail.getKey())) {
                // if its only started by triggers, it needs to be durable
                boolean recurringJob = isRecurringJob(schedulerJob);
                jobDetail = scheduleCreator.createJob((Class<? extends QuartzJobBean>) Class.forName(schedulerJob.getClazz()),
                        !recurringJob, context, schedulerJob.getName(), schedulerJob.getGroupName(), schedulerJob.isRecovery());
                if(recurringJob) {
                    Trigger trigger = scheduleCreator.createBasicCronTrigger(new Date(),
                            schedulerJob.getCronExpression());
                    scheduler.scheduleJob(jobDetail, trigger);
                } else {
                    scheduler.addJob(jobDetail, true);
                }
            } else {
                log.info("Not scheduling job {}, because it was already scheduled.", schedulerJob.getName());
            }
        } catch (ClassNotFoundException | SchedulerException e) {
            log.error("Failed to schedule job", e);
        }
    }

    @Override
    public void scheduleJob(SchedulerJob job) {
        log.info("Scheduling job {}", job.getName());
        this.scheduleNewJob(schedulerFactoryBean.getScheduler(), job);
    }

    @Override
    public void updateJob(SchedulerJob job, Date startDate) {
        Trigger newTrigger;
        if (job.getCronExpression() != null) {
            newTrigger = scheduleCreator.createBasicCronTrigger(startDate, job.getCronExpression());
        } else {
            newTrigger = scheduleCreator.createSimpleOnceOnlyTrigger(job.getName(), startDate);
        }
        try {
            schedulerFactoryBean.getScheduler().rescheduleJob(TriggerKey.triggerKey(job.getName()), newTrigger);
            schedulerJobManagementServiceBean.save(job);
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean unScheduleJob(String jobName) {
        try {
            return schedulerFactoryBean.getScheduler().unscheduleJob(new TriggerKey(jobName));
        } catch (SchedulerException e) {
            log.error("Failed to un-schedule job - {}", jobName, e);
            return false;
        }
    }

    @Override
    public boolean deleteJob(SchedulerJob job) {
        try {
            return schedulerFactoryBean.getScheduler().deleteJob(new JobKey(job.getName(), job.getGroupName()));
        } catch (SchedulerException e) {
            log.error("Failed to delete job - {}", job.getName(), e);
            return false;
        }
    }

    @Override
    public boolean pauseJob(SchedulerJob job) {
        try {
            schedulerFactoryBean.getScheduler().pauseJob(new JobKey(job.getName(), job.getGroupName()));
            return true;
        } catch (SchedulerException e) {
            log.error("Failed to pause job - {}", job.getName(), e);
            return false;
        }
    }

    @Override
    public boolean continueJob(SchedulerJob job) {
        try {
            schedulerFactoryBean.getScheduler().resumeJob(new JobKey(job.getName(), job.getGroupName()));
            return true;
        } catch (SchedulerException e) {
            log.error("Failed to resume job - {}", job.getName(), e);
            return false;
        }
    }

    @Override
    public boolean executeJob(SchedulerJob job) {
        try {
            schedulerFactoryBean.getScheduler().triggerJob(new JobKey(job.getName(), job.getGroupName()));
            return true;
        } catch (SchedulerException e) {
            log.error("Failed to start new job - {}", job.getName(), e);
            return false;
        }
    }

    @Override
    public String executeJobWithParametersOnce(String name, String group, JobDataMap dataMap, Date date) {
        Trigger onceOnlyTriggerForJob = scheduleCreator.createOnceOnlyTriggerForJob(name, group, date, dataMap);
        try {
            schedulerFactoryBean.getScheduler().scheduleJob(onceOnlyTriggerForJob);
            return onceOnlyTriggerForJob.getKey().getName();
        } catch (SchedulerException e) {
            log.error("Failed to start new job - {}", name, e);
            return null;
        }
    }

    @Override
    public void stopTrigger(String triggerKey) {
        try {
            schedulerFactoryBean.getScheduler().unscheduleJob(TriggerKey.triggerKey(triggerKey));
        } catch (SchedulerException e) {
            log.error("Failed to cancel job job - {}", triggerKey, e);
        }
    }

    @Override
    public void startScheduler() {
        try {
            schedulerFactoryBean.getScheduler().start();
        } catch (SchedulerException e) {
            log.error("Failed to start scheduler.", e);
        }
    }
}
