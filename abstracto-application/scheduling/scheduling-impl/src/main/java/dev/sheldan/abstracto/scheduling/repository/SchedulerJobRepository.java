package dev.sheldan.abstracto.scheduling.repository;

import dev.sheldan.abstracto.scheduling.model.database.SchedulerJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;

/**
 * Repository responsible to access the stored job configuration in the database
 */
@Repository
public interface SchedulerJobRepository extends JpaRepository<SchedulerJob, Long> {

    /**
     * Finds whether or not the job identified by the name exists in the database
     * @param name The name of the job to check for existence
     * @return Boolean variable representing whether or not the job identified by the name exists.
     */
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    boolean existsByName(String name);

    /**
     * Finds a job identified by the name
     * @param name The name of the job to search for
     * @return The found {@link SchedulerJob} instance by the name
     */
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.CACHEABLE, value = "true"))
    SchedulerJob findByName(String name);
}
