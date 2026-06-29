package dev.sheldan.abstracto.modmail.repository;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.modmail.model.database.QuickReply;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuickReplyRepository extends JpaRepository<QuickReply, Long> {
    Optional<QuickReply> getByNameIgnoreCaseAndServer(String name, AServer server);
    void deleteByNameAndServer(String name, AServer server);
    List<QuickReply> findByServer(AServer server);
    List<QuickReply> findByNameStartsWithIgnoreCaseAndServer(String prefix, AServer server);
    List<QuickReply> findByNameContainingIgnoreCaseAndServer(String name, AServer server);
}
