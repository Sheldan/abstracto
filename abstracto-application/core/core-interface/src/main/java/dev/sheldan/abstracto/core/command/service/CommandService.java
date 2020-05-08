package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.models.database.ACommand;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;

public interface CommandService {
    ACommand createCommand(String name, String moduleName, FeatureEnum featureEnum);
    Boolean doesCommandExist(String name);
    void allowCommandForRole(ACommand aCommand, ARole role);
    void allowFeatureForRole(FeatureEnum featureEnum, ARole role);
    void makeRoleImmuneForCommand(ACommand aCommand, ARole role);
    void makeRoleAffectedByCommand(ACommand aCommand, ARole role);
    void restrictCommand(ACommand aCommand, AServer server);
    void unRestrictCommand(ACommand aCommand, AServer server);
    void disAllowCommandForRole(ACommand aCommand, ARole role);
}
