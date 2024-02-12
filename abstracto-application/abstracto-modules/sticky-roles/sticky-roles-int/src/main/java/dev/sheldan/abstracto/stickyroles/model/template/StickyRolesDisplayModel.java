package dev.sheldan.abstracto.stickyroles.model.template;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class StickyRolesDisplayModel {
    private List<StickyRoleDisplayModel> roles;
}
