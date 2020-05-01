package dev.sheldan.abstracto.scheduling.repository;

import dev.sheldan.abstracto.scheduling.model.database.SchedulerJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;

import javax.persistence.QueryHint;

public interface SchedulerJobRepository extends JpaRepository<SchedulerJob, Long> {

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    boolean existsByName(String name);

    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    SchedulerJob findByName(String name);
}
