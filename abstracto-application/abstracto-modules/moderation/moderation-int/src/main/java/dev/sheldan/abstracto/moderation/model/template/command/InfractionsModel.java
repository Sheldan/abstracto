package dev.sheldan.abstracto.moderation.model.template.command;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class InfractionsModel {
    private List<InfractionEntry> entries;
}
