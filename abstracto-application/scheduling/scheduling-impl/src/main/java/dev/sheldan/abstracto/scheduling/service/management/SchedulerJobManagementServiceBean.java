package dev.sheldan.abstracto.scheduling.service.management;

import dev.sheldan.abstracto.scheduling.model.database.SchedulerJob;
import dev.sheldan.abstracto.scheduling.repository.SchedulerJobRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class SchedulerJobManagementServiceBean {

    @Autowired
    private SchedulerJobRepository repository;

    public SchedulerJob createOrUpdate(SchedulerJob job) {
        if(repository.existsByName(job.getName())) {
            SchedulerJob byName = repository.findByName(job.getName());
            byName.setActive(job.isActive());
            byName.setClazz(job.getClazz());
            byName.setCronExpression(job.getCronExpression());
            byName.setGroupName(job.getGroupName());
            return repository.save(byName);
        } else {
            return this.createJob(job);
        }
    }

    public SchedulerJob createJob(SchedulerJob job) {
        log.info("Creating job {}", job.getName());
        repository.save(job);
        return job;
    }

    public List<SchedulerJob> findAll() {
        return repository.findAll();
    }

    public SchedulerJob save(SchedulerJob job) {
        repository.save(job);
        return job;
    }

    public boolean doesJobExist(SchedulerJob schedulerJob) {
        return repository.existsByName(schedulerJob.getName());
    }

    public boolean isJobDefinitionTheSame(SchedulerJob job) {
        SchedulerJob old = repository.findByName(job.getName());
        if(old == null) {
            return false;
        }
        boolean cronExp;
        if(old.getCronExpression() == null && job.getCronExpression() != null) {
            cronExp = false;
        } else if(old.getCronExpression() != null && job.getCronExpression() == null) {
            cronExp = false;
        } else if(old.getCronExpression() == null && job.getCronExpression() == null) {
            cronExp = true;
        } else {
            cronExp = old.getCronExpression().equals(job.getCronExpression());
        }
        boolean active = old.isActive() == job.isActive();
        boolean classEqual = old.getClazz().equals(job.getClazz());
        boolean group = old.getGroupName().equals(job.getGroupName());
        return cronExp && active && classEqual && group;
    }

}
