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
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.utils.ParseUtils;
import dev.sheldan.abstracto.giveaway.config.GiveawayFeatureConfig;
import dev.sheldan.abstracto.giveaway.config.GiveawayFeatureDefinition;
import dev.sheldan.abstracto.giveaway.config.GiveawayMode;
import dev.sheldan.abstracto.giveaway.config.GiveawaySlashCommandNames;
import dev.sheldan.abstracto.giveaway.exception.GiveawayNotPossibleException;
import dev.sheldan.abstracto.giveaway.exception.GiveawayKeyNotFoundException;
import dev.sheldan.abstracto.giveaway.model.GiveawayCreationRequest;
import dev.sheldan.abstracto.giveaway.model.database.GiveawayKey;
import dev.sheldan.abstracto.giveaway.service.GiveawayService;
import dev.sheldan.abstracto.giveaway.service.management.GiveawayKeyManagementService;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GreateKeyGiveaway extends AbstractConditionableCommand {

    private static final String COMMAND_NAME = "createKeyGiveaway";
    private static final String KEY_ID_PARAMETER = "keyId";
    private static final String DURATION_PARAMETER = "duration";

    private static final String CREATE_KEY_GIVEAWAY_RESPONSE_TEMPLATE_KEY = "createKeyGiveaway_response";

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private GiveawayService giveawayService;

    @Autowired
    private GiveawayKeyManagementService giveawayKeyManagementService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private GreateKeyGiveaway self;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        Long id = slashCommandParameterService.getCommandOption(KEY_ID_PARAMETER, event, Integer.class).longValue();
        String durationString;
        if(slashCommandParameterService.hasCommandOption(DURATION_PARAMETER, event)) {
            durationString = slashCommandParameterService.getCommandOption(DURATION_PARAMETER, event, Duration.class, String.class);
        } else {
            durationString = configService.getStringValueOrConfigDefault(GiveawayFeatureConfig.KEY_GIVEAWAYS_DURATION, event.getGuild().getIdLong()).trim();
        }
        Duration duration = ParseUtils.parseDuration(durationString);
        return event.deferReply()
          .submit()
          .thenCompose(interactionHook -> self.createKeyGiveaway(id, interactionHook, duration))
          .thenApply(unused -> CommandResult.fromSuccess());

    }

    @Transactional
    public CompletableFuture<Void> createKeyGiveaway(Long giveawayKeyId, InteractionHook interactionHook, Duration duration) {
        Long serverId = interactionHook.getInteraction().getGuild().getIdLong();
        GiveawayKey giveawayKey = giveawayKeyManagementService.getById(giveawayKeyId, serverId)
          .orElseThrow(GiveawayKeyNotFoundException::new);
        if((giveawayKey.getGiveaway() != null && giveawayKey.getGiveaway().getTargetDate().isAfter(Instant.now())) || giveawayKey.getUsed()) {
            throw new GiveawayNotPossibleException();
        }
      GiveawayCreationRequest request = GiveawayCreationRequest
          .builder()
          .benefactorId(giveawayKey.getBenefactor() != null ? giveawayKey.getBenefactor().getUserReference().getId() : null)
          .creatorId(giveawayKey.getCreator().getUserReference().getId())
          .description(giveawayKey.getDescription())
          .duration(duration)
          .serverId(serverId)
          .giveawayKeyId(giveawayKeyId)
          .winnerCount(1)
          .title(giveawayKey.getName())
          .build();

      return giveawayService.createGiveaway(request)
          .thenAccept(giveawayId -> {
              interactionService.sendEmbed(CREATE_KEY_GIVEAWAY_RESPONSE_TEMPLATE_KEY, interactionHook);
          });
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter keyParameter = Parameter
            .builder()
            .templated(true)
            .name(KEY_ID_PARAMETER)
            .type(Long.class)
            .build();

        Parameter durationParameter = Parameter
            .builder()
            .templated(true)
            .name(DURATION_PARAMETER)
            .optional(true)
            .type(Duration.class)
            .build();

        List<Parameter> parameters = Arrays.asList(keyParameter, durationParameter);
        HelpInfo helpInfo = HelpInfo
            .builder()
            .templated(true)
            .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
            .builder()
            .enabled(true)
            .rootCommandName(GiveawaySlashCommandNames.GIVEAWAY)
            .defaultPrivilege(SlashCommandPrivilegeLevels.INVITER)
            .groupName("key")
            .commandName("creategiveaway")
            .build();

        return CommandConfiguration.builder()
            .name(COMMAND_NAME)
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
