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
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.giveaway.config.GiveawayFeatureDefinition;
import dev.sheldan.abstracto.giveaway.config.GiveawayMode;
import dev.sheldan.abstracto.giveaway.config.GiveawaySlashCommandNames;
import dev.sheldan.abstracto.giveaway.service.management.GiveawayKeyManagementService;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RemoveGiveawayKey extends AbstractConditionableCommand {


  private static final String ID_PARAMETER = "id";
  private static final String REMOVE_GIVEAWAY_KEY_COMMAND_NAME = "removeGiveawayKey";
  private static final String REMOVE_GIVEAWAY_KEY_RESPONSE = "removeGiveawayKey_response";

  @Autowired
  private GiveawayKeyManagementService giveawayKeyManagementService;

  @Autowired
  private SlashCommandParameterService slashCommandParameterService;

  @Autowired
  private InteractionService interactionService;

  @Override
  public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
    Long id = slashCommandParameterService.getCommandOption(ID_PARAMETER, event, Integer.class).longValue();
    giveawayKeyManagementService.deleteById(id, event.getGuild().getIdLong());
    return interactionService.replyEmbed(REMOVE_GIVEAWAY_KEY_RESPONSE, event)
        .thenApply(interactionHook -> CommandResult.fromSuccess());
  }

  @Override
  public CommandConfiguration getConfiguration() {
    Parameter giveawayKeyId = Parameter
        .builder()
        .templated(true)
        .name(ID_PARAMETER)
        .type(Long.class)
        .build();

    List<Parameter> parameters = Arrays.asList(giveawayKeyId);
    HelpInfo helpInfo = HelpInfo
        .builder()
        .templated(true)
        .build();

    SlashCommandConfig slashCommandConfig = SlashCommandConfig
        .builder()
        .enabled(true)
        .rootCommandName(GiveawaySlashCommandNames.GIVEAWAY)
        .groupName("keys")
        .commandName("remove")
        .build();

    return CommandConfiguration.builder()
        .name(REMOVE_GIVEAWAY_KEY_COMMAND_NAME)
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
