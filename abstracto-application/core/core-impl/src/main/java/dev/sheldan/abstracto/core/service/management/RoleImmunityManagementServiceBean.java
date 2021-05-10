package dev.sheldan.abstracto.core.service.management;

import dev.sheldan.abstracto.core.models.RoleImmunityId;
import dev.sheldan.abstracto.core.models.database.ARole;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.EffectType;
import dev.sheldan.abstracto.core.models.database.RoleImmunity;
import dev.sheldan.abstracto.core.repository.RoleImmunityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class RoleImmunityManagementServiceBean implements RoleImmunityManagementService {

    @Autowired
    private RoleImmunityRepository repository;

    @Autowired
    private EffectTypeManagementService effectTypeManagementService;

    @Override
    public RoleImmunity makeRoleImmune(ARole role, EffectType effectType) {
        RoleImmunityId id = new RoleImmunityId(role.getId(), effectType.getId());
        RoleImmunity immunity = RoleImmunity
                .builder()
                .immunityId(id)
                .role(role)
                .server(role.getServer())
                .effect(effectType)
                .build();
        return repository.save(immunity);
    }

    @Override
    public void makeRoleAffected(ARole role, EffectType effectType) {
        repository.deleteById(new RoleImmunityId(role.getId(), effectType.getId()));
    }

    @Override
    public Optional<RoleImmunity> getRoleImmunity(ARole role, EffectType effectType) {
        return repository.findById(new RoleImmunityId(role.getId(), effectType.getId()));
    }

    @Override
    public Optional<RoleImmunity> getRoleImmunity(ARole role, String effectTypeKey) {
        EffectType effectType = effectTypeManagementService.loadEffectTypeByKey(effectTypeKey);
        return getRoleImmunity(role, effectType);
    }

    @Override
    public List<RoleImmunity> getRolesImmuneAgainst(AServer server, String effectTypeKey) {
        EffectType effectType = effectTypeManagementService.loadEffectTypeByKey(effectTypeKey);
        return getRolesImmuneAgainst(server, effectType);
    }

    @Override
    public List<RoleImmunity> getRolesImmuneAgainst(AServer server, EffectType effectType) {
        return repository.findByServerAndEffect(server, effectType);
    }
}
