package dev.sheldan.abstracto.customcommand.repository;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.customcommand.model.database.CustomCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomCommandRepository extends JpaRepository<CustomCommand, Long> {
    Optional<CustomCommand> getByNameIgnoreCaseAndServer(String name, AServer server);
    Optional<CustomCommand> getByNameIgnoreCaseAndCreatorUser(String name, AUser creator);
    Optional<CustomCommand> getByNameIgnoreCaseAndCreatorUser_IdAndUserSpecific(String name, Long userId, Boolean userSpecific);
    void deleteByNameAndServer(String name, AServer server);
    void deleteByNameAndCreatorUserAndUserSpecific(String name, AUser aUser, Boolean userSpecific);
    List<CustomCommand> findByServer(AServer server);
    List<CustomCommand> findByCreatorUserAndUserSpecific(AUser user, Boolean userSpecific);
    List<CustomCommand> findByNameStartsWithIgnoreCaseAndServer(String prefix, AServer server);
    List<CustomCommand> findByNameStartsWithIgnoreCaseAndCreatorUserAndUserSpecific(String prefix, AUser aUser, Boolean userSpecific);
}
