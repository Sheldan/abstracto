package dev.sheldan.abstracto.experience.model.api;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ExperienceConfig {
    private List<ExperienceRoleDisplay> roles;
}
