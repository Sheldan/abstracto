package dev.sheldan.abstracto.core.command.config;

import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter @Builder @EqualsAndHashCode
public class CommandConfiguration {

    private String name;
    private String module;
    private String description;

    @Builder.Default
    private boolean async = false;

    @Builder.Default
    private boolean supportsEmbedException = false;

    @Builder.Default
    private boolean requiresConfirmation = false;

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

    @Builder.Default
    private boolean supportsMessageCommand = true;

    private CommandCoolDownConfig coolDownConfig;

    @Builder.Default
    private SlashCommandConfig slashCommandConfig = SlashCommandConfig
            .builder()
            .enabled(false)
            .build();

    public int getNecessaryParameterCount(){
        return (int) parameters.stream().filter(parameter -> !parameter.isOptional()).count();
    }
}
