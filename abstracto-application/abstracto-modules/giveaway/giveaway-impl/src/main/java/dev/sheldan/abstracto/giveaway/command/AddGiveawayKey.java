package dev.sheldan.abstracto.giveaway.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.giveaway.config.GiveawayFeatureDefinition;
import dev.sheldan.abstracto.giveaway.config.GiveawayMode;
import dev.sheldan.abstracto.giveaway.config.GiveawaySlashCommandNames;
import dev.sheldan.abstracto.giveaway.service.management.GiveawayKeyManagementService;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AddGiveawayKey extends AbstractConditionableCommand {

    private static final String KEY_PARAMETER = "key";
    private static final String DESCRIPTION_PARAMETER = "description";
    private static final String BENEFACTOR_PARAMETER = "benefactor";
    private static final String NAME_PARAMETER = "name";
    private static final String ADD_GIVEAWAY_KEY_COMMAND_NAME = "addGiveawayKey";
    private static final String ADD_GIVEAWAY_KEY_RESPONSE = "addGiveawayKey_response";

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private GiveawayKeyManagementService giveawayKeyManagementService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String key = slashCommandParameterService.getCommandOption(KEY_PARAMETER, event, String.class);
        String name = slashCommandParameterService.getCommandOption(NAME_PARAMETER, event, String.class);
        String description;
        if (slashCommandParameterService.hasCommandOption(DESCRIPTION_PARAMETER, event)) {
            description = slashCommandParameterService.getCommandOption(DESCRIPTION_PARAMETER, event, String.class);
        } else {
            description = null;
        }
        Member benefactor;
        if (slashCommandParameterService.hasCommandOption(BENEFACTOR_PARAMETER, event)) {
            benefactor = slashCommandParameterService.getCommandOption(BENEFACTOR_PARAMETER, event, Member.class);
        } else {
            benefactor = null;
        }
        giveawayKeyManagementService.createGiveawayKey(event.getMember(), benefactor, key, description, name);
        return interactionService.replyEmbed(ADD_GIVEAWAY_KEY_RESPONSE, event)
            .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter giveawayKey = Parameter
            .builder()
            .templated(true)
            .name(KEY_PARAMETER)
            .type(String.class)
            .build();

        Parameter giveawayKeyDescription = Parameter
            .builder()
            .templated(true)
            .name(DESCRIPTION_PARAMETER)
            .type(String.class)
            .optional(true)
            .build();


        Parameter giveawayKeyBenefactor = Parameter
            .builder()
            .templated(true)
            .name(BENEFACTOR_PARAMETER)
            .type(Member.class)
            .optional(true)
            .build();

        Parameter giveawayKeyName = Parameter
            .builder()
            .templated(true)
            .name(NAME_PARAMETER)
            .type(String.class)
            .build();

        List<Parameter> parameters = Arrays.asList(giveawayKeyName, giveawayKey, giveawayKeyBenefactor, giveawayKeyDescription);
        HelpInfo helpInfo = HelpInfo
            .builder()
            .templated(true)
            .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
            .builder()
            .enabled(true)
            .rootCommandName(GiveawaySlashCommandNames.GIVEAWAY)
            .defaultPrivilege(SlashCommandPrivilegeLevels.INVITER)
            .groupName("keys")
            .commandName("add")
            .build();

        return CommandConfiguration.builder()
            .name(ADD_GIVEAWAY_KEY_COMMAND_NAME)
            .module(UtilityModuleDefinition.UTILITY)
            .templated(true)
            .slashCommandOnly(true)
            .slashCommandConfig(slashCommandConfig)
            .async(true)
            .supportsEmbedException(true)
            .causesReaction(false)
            .parameters(parameters)
            .help(helpInfo)
            .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return GiveawayFeatureDefinition.GIVEAWAY;
    }

    @Override
    public List<FeatureMode> getFeatureModeLimitations() {
        return Arrays.asList(GiveawayMode.KEY_GIVEAWAYS);
    }
}
