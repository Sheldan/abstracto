package dev.sheldan.abstracto.scheduling.factory;

import dev.sheldan.abstracto.scheduling.model.JobParameters;
import dev.sheldan.abstracto.scheduling.model.database.SchedulerJob;
import dev.sheldan.abstracto.scheduling.service.SchedulerServiceBean;
import dev.sheldan.abstracto.scheduling.service.management.SchedulerJobManagementServiceBean;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.*;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SchedulerServiceBeanTest {

    public static final String JOB_CLASS = "dev.sheldan.abstracto.scheduling.factory.TestJob";
    public static final String GROUP_NAME = "groupName";
    public static final String JOB_NAME = "jobName";

    @InjectMocks
    private SchedulerServiceBean classToTest;

    @Mock
    private SchedulerFactoryBean schedulerFactoryBean;

    @Mock
    private ApplicationContext context;

    @Mock
    private QuartzConfigFactory scheduleCreator;

    @Mock
    private SchedulerJobManagementServiceBean schedulerJobManagementServiceBean;

    @Mock
    private Scheduler scheduler;

    @Mock
    private Trigger trigger;

    @Before
    public void setup() throws ClassNotFoundException {
        when(schedulerFactoryBean.getScheduler()).thenReturn(scheduler);
        when(scheduleCreator.createJob(eq(TestJob.class), anyBoolean(), any(), anyString(), anyString(), anyBoolean())).thenReturn(getJobDetail());
    }

    @Test
    public void testStartingAllJobs() throws SchedulerException {
        when(schedulerJobManagementServiceBean.findAll()).thenReturn(allCronJobsActive());
        classToTest.startScheduledJobs();
        verify(scheduler, times(2)).checkExists(eq(new JobKey(JOB_NAME, GROUP_NAME)));
        verify(scheduler, times(2)).addJob(any(JobDetail.class), eq(true));
    }

    @Test
    public void testStartSomeJobs() throws SchedulerException {
        when(schedulerJobManagementServiceBean.findAll()).thenReturn(someCronJobsActive());
        classToTest.startScheduledJobs();
        verify(scheduler, times(1)).checkExists(eq(new JobKey(JOB_NAME, GROUP_NAME)));
        verify(scheduler, times(1)).addJob(any(JobDetail.class), eq(true));
    }

    @Test
    public void testInvalidClass() throws SchedulerException {
        when(schedulerJobManagementServiceBean.findAll()).thenReturn(Arrays.asList(SchedulerJob.builder().active(true).cronExpression("*").clazz("invalidJob").groupName(GROUP_NAME).name(JOB_NAME).build()));
        classToTest.startScheduledJobs();
        verify(scheduler, times(0)).checkExists(eq(new JobKey(JOB_NAME, GROUP_NAME)));
        verify(scheduler, times(0)).addJob(any(JobDetail.class), eq(true));
    }

    @Test
    public void scheduleSingleJob() throws SchedulerException {
        classToTest.scheduleJob(activeJobCronJob());
        verify(scheduler, times(1)).checkExists(eq(new JobKey(JOB_NAME, GROUP_NAME)));
        verify(scheduler, times(1)).addJob(any(JobDetail.class), eq(true));
    }

    @Test
    public void unScheduleSingleJob() throws SchedulerException {
        classToTest.unScheduleJob(JOB_NAME);
        verify(scheduler, times(1)).unscheduleJob(any(TriggerKey.class));
    }

    @Test
    public void executeJobOnce() throws SchedulerException {
        when(scheduleCreator.createOnceOnlyTriggerForJob(eq(JOB_NAME), eq(GROUP_NAME), any(Date.class), any(JobDataMap.class))).thenReturn(trigger);
        when(trigger.getKey()).thenReturn(TriggerKey.triggerKey("random key"));
        classToTest.executeJobWithParametersOnce(JOB_NAME, GROUP_NAME, JobParameters.builder().build(), new Date());
        verify(scheduler, times(1)).scheduleJob(any(Trigger.class));
    }

    private List<SchedulerJob> allCronJobsActive() {
        List<SchedulerJob> jobs = new ArrayList<>();
        jobs.add(activeJobCronJob());
        jobs.add(activeJobCronJob());
        return jobs;
    }

    private List<SchedulerJob> someCronJobsActive() {
        List<SchedulerJob> jobs = new ArrayList<>();
        jobs.add(activeJobCronJob());
        jobs.add(inactiveCronJob());
        return jobs;
    }

    private SchedulerJob activeJobCronJob() {
        return SchedulerJob.builder().active(true).cronExpression("*").clazz(JOB_CLASS).groupName(GROUP_NAME).name(JOB_NAME).build();
    }

    private SchedulerJob inactiveCronJob() {
        return SchedulerJob.builder().active(false).cronExpression("*").clazz(JOB_CLASS).groupName(GROUP_NAME).name(JOB_NAME).build();
    }

    private JobDetail getJobDetail() throws ClassNotFoundException {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        Class<? extends Job> jobClass = (Class<? extends Job>) Class.forName(JOB_CLASS);
        factoryBean.setJobClass(jobClass);
        factoryBean.setDurability(true);
        factoryBean.setApplicationContext(context);
        factoryBean.setRequestsRecovery(false);
        factoryBean.setName(JOB_NAME);
        factoryBean.setGroup(GROUP_NAME);

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(JOB_NAME + GROUP_NAME, jobClass.getName());
        factoryBean.setJobDataMap(jobDataMap);

        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }

}
