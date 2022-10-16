package dev.sheldan.abstracto.core.repository;

import dev.sheldan.abstracto.core.models.database.AChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChannelRepository extends JpaRepository<AChannel, Long> {

    @Override
    Optional<AChannel> findById(@NonNull Long aLong);

    @Override
    boolean existsById(@NonNull Long aLong);
}
