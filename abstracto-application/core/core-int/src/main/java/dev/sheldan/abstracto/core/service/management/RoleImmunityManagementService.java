package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.EffectType;
import dev.sheldan.abstracto.core.models.database.RoleImmunity;

import java.util.List;
import java.util.Optional;

public interface RoleImmunityManagementService {
    RoleImmunity makeRoleImmune(ARole role, EffectType effectType);
    void makeRoleAffected(ARole role, EffectType effectType);
    Optional<RoleImmunity> getRoleImmunity(ARole role, EffectType effectType);
    Optional<RoleImmunity> getRoleImmunity(ARole role, String effectType);
    List<RoleImmunity> getRolesImmuneAgainst(AServer server, String effectType);
    List<RoleImmunity> getRolesImmuneAgainst(AServer server, EffectType effectType);
}
