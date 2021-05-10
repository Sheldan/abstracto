package dev.sheldan.abstracto.core.service;


import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.EffectType;
import dev.sheldan.abstracto.core.models.database.RoleImmunity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.Optional;

public interface RoleImmunityService {
    boolean isImmune(Member member, String effectTypeKey);
    boolean isImmune(Member member, EffectType effectType);
    Optional<RoleImmunity> getRoleImmunity(Member member, String effectType);
    RoleImmunity makeRoleImmune(Role role, String effectType);
    RoleImmunity makeRoleImmune(Role role, EffectType effectType);
    void makeRoleAffected(Role role, String effectType);
    void makeRoleAffected(ARole role, String effectType);
    void makeRoleAffected(Role role, EffectType effectType);
    void makeRoleAffected(ARole role, EffectType effectType);
}
