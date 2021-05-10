package dev.sheldan.abstracto.core.models.template.commands;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ShowEffectsModel {
    private List<String> effects;
}
