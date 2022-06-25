package dev.sheldan.abstracto.moderation.model.template.listener;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ReportInputModalModel {
    private String modalId;
    private String inputComponentId;
}
