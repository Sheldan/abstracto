package dev.sheldan.abstracto.core.command.config;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter @Builder
public class CommandConfiguration {

    private String name;
    private String module;
    private String description;

    @Builder.Default
    private boolean async = false;

    @Builder.Default
    private boolean supportsEmbedException = false;

    @Builder.Default
    private List<Parameter> parameters = new ArrayList<>();

    @Builder.Default
    private List<String> aliases = new ArrayList<>();

    @Builder.Default
    private boolean causesReaction = false;

    @Builder.Default
    private boolean templated = false;
    private HelpInfo help;
    @Builder.Default
    private List<EffectConfig> effects = new ArrayList<>();

    private CommandCoolDownConfig coolDownConfig;

    public int getNecessaryParameterCount(){
        return (int) parameters.stream().filter(parameter -> !parameter.isOptional()).count();
    }
}
