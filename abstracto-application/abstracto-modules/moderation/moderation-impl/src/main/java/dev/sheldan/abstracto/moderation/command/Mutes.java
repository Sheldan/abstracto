package dev.sheldan.abstracto.moderation.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.EntityGuildMismatchException;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.PaginatorService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.config.ModerationModuleDefinition;
import dev.sheldan.abstracto.moderation.config.ModerationSlashCommandNames;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.converter.MuteEntryConverter;
import dev.sheldan.abstracto.moderation.model.template.command.MuteEntry;
import dev.sheldan.abstracto.moderation.model.template.command.MutesModel;
import dev.sheldan.abstracto.moderation.service.management.MuteManagementService;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class Mutes extends AbstractConditionableCommand {

    private static final String NO_MUTES_TEMPLATE_KEY = "mutes_no_mutes_found";
    private static final String MUTES_DISPLAY_TEMPLATE_KEY = "mutes_display_response";
    public static final String MUTES_COMMAND = "mutes";
    public static final String MEMBER_PARAMETER = "member";
    @Autowired
    private MuteManagementService muteManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private MuteEntryConverter muteEntryConverter;

    @Autowired
    private Mutes self;

    @Autowired
    private PaginatorService paginatorService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Transactional
    public CompletableFuture<Void> renderMutes(InteractionHook event, List<MuteEntry> mutes) {
        MutesModel model = MutesModel
                .builder()
                .mutes(mutes)
                .build();
        return paginatorService.sendPaginatorToInteraction(MUTES_DISPLAY_TEMPLATE_KEY, model, event);
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        return event.deferReply().submit()
            .thenCompose((hook) -> self.loadAndRenderMutes(event, hook))
            .thenApply(u -> CommandResult.fromSuccess());
    }

    @Transactional
    public CompletableFuture<Void> loadAndRenderMutes(SlashCommandInteractionEvent event, InteractionHook hook) {
        List<dev.sheldan.abstracto.moderation.model.database.Mute> mutesToDisplay;
        if(!slashCommandParameterService.hasCommandOption(MEMBER_PARAMETER, event)) {
            AServer server = serverManagementService.loadServer(event.getGuild().getIdLong());
            mutesToDisplay = muteManagementService.getAllMutes(server);
        } else {
            Member memberParameter = slashCommandParameterService.getCommandOption(MEMBER_PARAMETER, event, Member.class, Member.class);
            if(!memberParameter.getGuild().equals(event.getGuild())) {
                throw new EntityGuildMismatchException();
            }
            mutesToDisplay = muteManagementService.getAllMutesOf(userInServerManagementService.loadOrCreateUser(memberParameter));
        }
        if(mutesToDisplay.isEmpty()) {
            return FutureUtils.toSingleFutureGeneric(interactionService.sendMessageToInteraction(NO_MUTES_TEMPLATE_KEY, new Object(), hook));
        } else {
            return muteEntryConverter.fromMutes(mutesToDisplay)
                    .thenCompose(muteEntries -> self.renderMutes(hook, muteEntries));
        }
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Parameter memberParameter = Parameter
                .builder()
                .name(MEMBER_PARAMETER)
                .templated(true)
                .type(Member.class)
                .optional(true)
                .build();
        parameters.add(memberParameter);

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(ModerationSlashCommandNames.MUTE)
                .defaultPrivilege(SlashCommandPrivilegeLevels.INVITER)
                .commandName("list")
                .build();

        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();
        return CommandConfiguration.builder()
                .name(MUTES_COMMAND)
                .module(ModerationModuleDefinition.MODERATION)
                .templated(true)
                .slashCommandOnly(true)
                .slashCommandConfig(slashCommandConfig)
                .supportsEmbedException(true)
                .async(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.MUTING;
    }
}
