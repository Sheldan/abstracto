package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.EffectType;
import dev.sheldan.abstracto.core.models.database.RoleImmunity;
import dev.sheldan.abstracto.core.service.management.EffectTypeManagementService;
import dev.sheldan.abstracto.core.service.management.RoleImmunityManagementService;
import dev.sheldan.abstracto.core.service.management.RoleManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class RoleImmunityServiceBean implements RoleImmunityService {

    @Autowired
    private RoleImmunityManagementService roleImmunityManagementService;

    @Autowired
    private RoleManagementService roleManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private EffectTypeManagementService effectTypeManagementService;

    @Override
    public boolean isImmune(Member member, String effectTypeKey) {
        EffectType effectType = effectTypeManagementService.loadEffectTypeByKey(effectTypeKey);
        return isImmune(member, effectType);
    }

    @Override
    public boolean isImmune(Member member, EffectType effectType) {
        AServer server = serverManagementService.loadServer(member.getGuild());
        List<RoleImmunity> immuneRoles = roleImmunityManagementService.getRolesImmuneAgainst(server, effectType);
        if(immuneRoles.isEmpty()) {
            return false;
        }
        List<ARole> roles = immuneRoles
                .stream()
                .map(RoleImmunity::getRole)
                .collect(Collectors.toList());
        return roleService.hasAnyOfTheRoles(member, roles);
    }

    @Override
    public Optional<RoleImmunity> getRoleImmunity(Member member, String effectType) {
        AServer server = serverManagementService.loadServer(member.getGuild());
        List<RoleImmunity> immuneRoles = roleImmunityManagementService.getRolesImmuneAgainst(server, effectType);
        if(immuneRoles.isEmpty()) {
            return Optional.empty();
        }
        return  immuneRoles
                .stream()
                .filter(role -> member
                        .getRoles()
                        .stream()
                        .anyMatch(role1 -> role1.getIdLong() == role.getRole().getId()))
                .findFirst();
    }

    @Override
    public RoleImmunity makeRoleImmune(Role role, String effectTypeKey) {
        EffectType type = effectTypeManagementService.loadEffectTypeByKey(effectTypeKey);
        return makeRoleImmune(role, type);
    }

    @Override
    public RoleImmunity makeRoleImmune(Role role, EffectType effectType) {
        ARole aRole = roleManagementService.findRole(role.getIdLong());
        return roleImmunityManagementService.makeRoleImmune(aRole, effectType);
    }

    @Override
    public void makeRoleAffected(Role role, String effectType) {
        EffectType type = effectTypeManagementService.loadEffectTypeByKey(effectType);
        makeRoleAffected(role, type);
    }

    @Override
    public void makeRoleAffected(ARole role, String effectType) {
        EffectType type = effectTypeManagementService.loadEffectTypeByKey(effectType);
        makeRoleAffected(role, type);
    }

    @Override
    public void makeRoleAffected(Role role, EffectType effectType) {
        ARole aRole = roleManagementService.findRole(role.getIdLong());
        makeRoleAffected(aRole, effectType);
    }

    @Override
    public void makeRoleAffected(ARole role, EffectType effectType) {
        roleImmunityManagementService.makeRoleAffected(role, effectType);
    }
}
