package dev.sheldan.abstracto.vccontext.repository;

import dev.sheldan.abstracto.vccontext.model.VoiceChannelContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoiceChannelContextRepository extends JpaRepository<VoiceChannelContext, Long> {
}
