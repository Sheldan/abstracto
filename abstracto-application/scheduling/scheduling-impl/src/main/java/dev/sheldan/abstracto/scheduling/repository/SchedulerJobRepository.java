package dev.sheldan.abstracto.scheduling.repository;

import dev.sheldan.abstracto.scheduling.model.SchedulerJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchedulerJobRepository extends JpaRepository<SchedulerJob, Long> {
    boolean existsByName(String name);
    SchedulerJob findByName(String name);
}
