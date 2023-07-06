package dev.sheldan.abstracto.core.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class RoleImmunityId implements Serializable {
    @Column(name = "role_id")
    private Long roleId;
    @Column(name = "effect_id")
    private Long effectId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleImmunityId that = (RoleImmunityId) o;
        return Objects.equals(roleId, that.roleId) &&
                Objects.equals(effectId, that.effectId);
    }

    public RoleImmunityId() {
    }

    public RoleImmunityId(Long roleId, Long effectId) {
        this.roleId = roleId;
        this.effectId = effectId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId, effectId);
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getEffectId() {
        return effectId;
    }

    public void setEffectId(Long effectId) {
        this.effectId = effectId;
    }
}
