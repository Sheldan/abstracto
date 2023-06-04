package dev.sheldan.abstracto.core.templating.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class SelectionMenuConfig {
    private String id;
    private SelectionMenuType type;
    private List<SelectionMenuTarget> targets;
    private List<SelectionMenuChannelType> channelTypes;
    private List<SelectionMenuEntry> menuEntries;
    private Integer position;
    private Integer minValues;
    private Integer maxValues;
    private Boolean disabled;
    private String placeholder;
}
