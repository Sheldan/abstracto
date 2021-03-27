package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.model.database.ACommandInServerAlias;

import java.util.List;
import java.util.Optional;

public interface CommandInServerAliasService {
    ACommandInServerAlias createAliasForCommandInServer(Long serverId, String commandName, String alias);
    Optional<ACommandInServerAlias> getCommandInServerAlias(Long serverId, String text);
    void deleteCommandInServerAlias(Long serverId, String name);
    List<String> getAliasesForCommand(Long serverId, String commandName);
}
