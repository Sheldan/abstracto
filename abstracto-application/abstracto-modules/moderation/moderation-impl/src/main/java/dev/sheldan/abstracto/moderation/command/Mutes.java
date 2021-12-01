package dev.sheldan.abstracto.moderation.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.EntityGuildMismatchException;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.PaginatorService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.config.ModerationModuleDefinition;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.converter.MuteEntryConverter;
import dev.sheldan.abstracto.moderation.model.template.command.MuteEntry;
import dev.sheldan.abstracto.moderation.model.template.command.MutesModel;
import dev.sheldan.abstracto.moderation.service.management.MuteManagementService;
import net.dv8tion.jda.api.entities.Member;
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
    @Autowired
    private MuteManagementService muteManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private MuteEntryConverter muteEntryConverter;

    @Autowired
    private Mutes self;

    @Autowired
    private PaginatorService paginatorService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<dev.sheldan.abstracto.moderation.model.database.Mute> mutesToDisplay;
        if(commandContext.getParameters().getParameters().isEmpty()) {
            AServer server = serverManagementService.loadServer(commandContext.getGuild().getIdLong());
            mutesToDisplay = muteManagementService.getAllMutes(server);
        } else {
            Member member = (Member) commandContext.getParameters().getParameters().get(0);
            if(!member.getGuild().equals(commandContext.getGuild())) {
                throw new EntityGuildMismatchException();
            }
            mutesToDisplay = muteManagementService.getAllMutesOf(userInServerManagementService.loadOrCreateUser(member));
        }
        if(mutesToDisplay.isEmpty()) {
            MessageToSend messageToSend = templateService.renderEmbedTemplate(NO_MUTES_TEMPLATE_KEY, new Object(), commandContext.getGuild().getIdLong());
            return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()))
                    .thenApply(unused -> CommandResult.fromSuccess());
        } else {
            return muteEntryConverter.fromMutes(mutesToDisplay)
                    .thenCompose(muteEntries -> self.renderMutes(commandContext, muteEntries)
                    .thenApply(unused -> CommandResult.fromIgnored()));
        }
    }

    @Transactional
    public CompletableFuture<Void> renderMutes(CommandContext commandContext, List<MuteEntry> mutes) {
        MutesModel model = (MutesModel) ContextConverter.slimFromCommandContext(commandContext, MutesModel.class);
        model.setMutes(mutes);
        return paginatorService.createPaginatorFromTemplate(MUTES_DISPLAY_TEMPLATE_KEY, model, commandContext.getChannel(), commandContext.getAuthor().getIdLong());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("member").templated(true).type(Member.class).optional(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("mutes")
                .module(ModerationModuleDefinition.MODERATION)
                .templated(true)
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
