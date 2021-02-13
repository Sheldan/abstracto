package dev.sheldan.abstracto.scheduling.repository;

import dev.sheldan.abstracto.scheduling.model.database.SchedulerJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


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
    boolean existsByName(String name);

    /**
     * Finds a job identified by the name
     * @param name The name of the job to search for
     * @return The found {@link SchedulerJob} instance by the name
     */
    SchedulerJob findByName(String name);
}
