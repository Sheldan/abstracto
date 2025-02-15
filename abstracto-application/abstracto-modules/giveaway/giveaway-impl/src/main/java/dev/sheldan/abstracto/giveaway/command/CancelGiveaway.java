package dev.sheldan.abstracto.giveaway.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.giveaway.config.GiveawayFeatureDefinition;
import dev.sheldan.abstracto.giveaway.config.GiveawaySlashCommandNames;
import dev.sheldan.abstracto.giveaway.service.GiveawayService;
import jakarta.transaction.Transactional;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class CancelGiveaway extends AbstractConditionableCommand {

    private static final String COMMAND_NAME = "cancelGiveaway";
    private static final String ID_PARAMETER = "id";
    private static final String CANCEL_GIVEAWAY_RESPONSE_TEMPLATE_KEY = "cancelGiveaway_response";

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private GiveawayService giveawayService;

    @Autowired
    private CancelGiveaway self;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        return event.deferReply().submit().thenAccept(interactionHook -> {
            self.cancelGiveaway(event);
        }).thenCompose(unused -> {
                List<CompletableFuture<Message>> futures = interactionService.sendMessageToInteraction(CANCEL_GIVEAWAY_RESPONSE_TEMPLATE_KEY, new Object(), event.getHook());
                return new CompletableFutureList<>(futures).getMainFuture();
        })
        .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Transactional
    public void cancelGiveaway(SlashCommandInteractionEvent event) {
        Long giveawayId = slashCommandParameterService.getCommandOption(ID_PARAMETER, event, Integer.class).longValue();
        giveawayService.cancelGiveaway(giveawayId, event.getGuild().getIdLong());
    }

    @Override
    public CommandConfiguration getConfiguration() {

        Parameter giveawayIdParameter = Parameter
                .builder()
                .templated(true)
                .name(ID_PARAMETER)
                .type(Integer.class)
                .build();

        List<Parameter> parameters = Arrays.asList(giveawayIdParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(GiveawaySlashCommandNames.GIVEAWAY)
                .defaultPrivilege(SlashCommandPrivilegeLevels.INVITER)
                .groupName("management")
                .commandName("cancel")
                .build();

        return CommandConfiguration.builder()
                .name(COMMAND_NAME)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
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
}
