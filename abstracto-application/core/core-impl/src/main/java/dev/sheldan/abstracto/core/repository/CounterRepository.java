package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.CounterId;
import dev.sheldan.abstracto.core.models.database.Counter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CounterRepository extends JpaRepository<Counter, CounterId> {
    @Query(value = "SELECT next_counter(:counterKey, :serverId)", nativeQuery = true)
    Long getNewCounterForKey(@Param("serverId") Long serverId, @Param("counterKey") String counterKey);
}
