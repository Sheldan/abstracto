package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.model.database.ALock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

@Repository
public interface LockRepository extends JpaRepository<ALock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from ALock a where a.id = :id")
    ALock findALockForRead(@Param("id") Long id);
}
