package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.condition.ConditionResult;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.models.database.ACommand;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;

public interface CommandService {
    ACommand createCommand(String name, String moduleName, FeatureEnum featureEnum);
    boolean doesCommandExist(String name);
    void allowCommandForRole(ACommand aCommand, ARole role);
    void allowFeatureForRole(FeatureEnum featureEnum, ARole role);
    void makeRoleImmuneForCommand(ACommand aCommand, ARole role);
    void makeRoleAffectedByCommand(ACommand aCommand, ARole role);
    void restrictCommand(ACommand aCommand, AServer server);
    String generateUsage(Command command);
    void unRestrictCommand(ACommand aCommand, AServer server);
    void disAllowCommandForRole(ACommand aCommand, ARole role);
    void disAllowFeatureForRole(FeatureEnum featureEnum, ARole role);
    ConditionResult isCommandExecutable(Command command, CommandContext commandContext);
}
