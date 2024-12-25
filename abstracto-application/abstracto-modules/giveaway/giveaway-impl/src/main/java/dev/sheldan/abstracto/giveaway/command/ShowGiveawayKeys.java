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
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.service.PaginatorService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.giveaway.config.GiveawayFeatureDefinition;
import dev.sheldan.abstracto.giveaway.config.GiveawayMode;
import dev.sheldan.abstracto.giveaway.config.GiveawaySlashCommandNames;
import dev.sheldan.abstracto.giveaway.model.database.GiveawayKey;
import dev.sheldan.abstracto.giveaway.model.template.GiveawayKeyDisplayModel;
import dev.sheldan.abstracto.giveaway.model.template.GiveawayKeysDisplayModel;
import dev.sheldan.abstracto.giveaway.service.management.GiveawayKeyManagementService;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShowGiveawayKeys extends AbstractConditionableCommand {

  public static final String SHOW_ALL_PARAMETER_NAME = "all";


  private static final String SHOW_GIVEAWAY_KEYS_RESPONSE_TEMPLATE = "showGiveawayKeys_response";
  private static final String SHOW_GIVEAWAY_NO_KEYS_FOUND_TEMPLATE = "showGiveawayKeys_no_keys_found";

  @Autowired
  private PaginatorService paginatorService;

  @Autowired
  private SlashCommandParameterService slashCommandParameterService;

  @Autowired
  private GiveawayKeyManagementService giveawayKeyManagementService;

  @Autowired
  private TemplateService templateService;

  @Autowired
  private InteractionService interactionService;

  @Override
  public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
    boolean showAll;
    if(slashCommandParameterService.hasCommandOption(SHOW_ALL_PARAMETER_NAME, event)) {
      showAll = slashCommandParameterService.getCommandOption(SHOW_ALL_PARAMETER_NAME, event, Boolean.class);
    } else {
      showAll = false;
    }
    List<GiveawayKey> giveawayKeys = giveawayKeyManagementService.getGiveawayKeys(event.getGuild().getIdLong(), showAll);
    if(giveawayKeys.isEmpty()) {
      MessageToSend messageToSend = templateService.renderEmbedTemplate(SHOW_GIVEAWAY_NO_KEYS_FOUND_TEMPLATE, new Object(), event.getGuild().getIdLong());
      return interactionService.replyMessageToSend(messageToSend, event)
          .thenApply(interactionHook -> CommandResult.fromSuccess());
    }
    List<GiveawayKeyDisplayModel> models = giveawayKeys
        .stream()
        .map(giveawayKey -> GiveawayKeyDisplayModel
            .builder()
            .key(giveawayKey.getKey())
            .id(giveawayKey.getId().getKeyId())
            .used(giveawayKey.getUsed())
            .description(giveawayKey.getDescription())
            .name(giveawayKey.getName())
            .winner(giveawayKey.getWinner() != null ? MemberDisplay.fromAUserInAServer(giveawayKey.getWinner()) : null)
            .creator(MemberDisplay.fromAUserInAServer(giveawayKey.getCreator()))
            .benefactor(giveawayKey.getBenefactor() != null ? MemberDisplay.fromAUserInAServer(giveawayKey.getBenefactor()) : null)
            .build())
        .toList();
    GiveawayKeysDisplayModel model = GiveawayKeysDisplayModel
        .builder()
        .keys(models)
        .build();
    return paginatorService.createPaginatorFromTemplate(SHOW_GIVEAWAY_KEYS_RESPONSE_TEMPLATE, model, event)
        .thenApply(unused -> CommandResult.fromSuccess());
  }

  @Override
  public CommandConfiguration getConfiguration() {

    Parameter showAllParameter = Parameter
        .builder()
        .templated(true)
        .name(SHOW_ALL_PARAMETER_NAME)
        .type(Boolean.class)
        .optional(true)
        .build();

    List<Parameter> parameters = Arrays.asList(showAllParameter);
    HelpInfo helpInfo = HelpInfo
        .builder()
        .templated(true)
        .build();

    SlashCommandConfig slashCommandConfig = SlashCommandConfig
        .builder()
        .enabled(true)
        .rootCommandName(GiveawaySlashCommandNames.GIVEAWAY)
        .groupName("keys")
        .commandName("show")
        .build();

    return CommandConfiguration.builder()
        .name("showGiveawayKeys")
        .module(UtilityModuleDefinition.UTILITY)
        .templated(true)
        .slashCommandConfig(slashCommandConfig)
        .async(true)
        .slashCommandOnly(true)
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
