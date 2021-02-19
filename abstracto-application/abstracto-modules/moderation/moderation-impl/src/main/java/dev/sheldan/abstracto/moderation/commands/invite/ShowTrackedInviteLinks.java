package dev.sheldan.abstracto.moderation.commands.invite;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.config.ModerationModule;
import dev.sheldan.abstracto.moderation.config.features.ModerationFeatures;
import dev.sheldan.abstracto.moderation.config.features.mode.InviteFilterMode;
import dev.sheldan.abstracto.moderation.models.database.FilteredInviteLink;
import dev.sheldan.abstracto.moderation.models.template.commands.TrackedInviteLinksModel;
import dev.sheldan.abstracto.moderation.service.InviteLinkFilterService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class ShowTrackedInviteLinks extends AbstractConditionableCommand {

    @Autowired
    private InviteLinkFilterService inviteLinkFilterService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private TemplateService templateService;

    public static final String TRACKED_INVITE_LINKS_EMBED_TEMPLATE_KEY = "showTrackedInviteLinks_response";

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        TrackedInviteLinksModel model = (TrackedInviteLinksModel) ContextConverter.slimFromCommandContext(commandContext, TrackedInviteLinksModel.class);
        List<Object> parameters = commandContext.getParameters().getParameters();
        List<FilteredInviteLink> inviteLinks;
        if(!parameters.isEmpty()) {
            Integer count = (Integer) parameters.get(0);
            inviteLinks = inviteLinkFilterService.getTopFilteredInviteLinks(commandContext.getGuild().getIdLong(), count);
        } else {
            inviteLinks = inviteLinkFilterService.getTopFilteredInviteLinks(commandContext.getGuild().getIdLong());
        }
        model.setInviteLinks(inviteLinks);
        MessageToSend messageToSend = templateService.renderEmbedTemplate(TRACKED_INVITE_LINKS_EMBED_TEMPLATE_KEY, model, commandContext.getGuild().getIdLong());
        return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, commandContext.getChannel()))
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("amount").type(Integer.class).optional(true).templated(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("showTrackedInviteLinks")
                .module(ModerationModule.MODERATION)
                .templated(true)
                .async(true)
                .supportsEmbedException(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return ModerationFeatures.INVITE_FILTER;
    }

    @Override
    public List<FeatureMode> getFeatureModeLimitations() {
        return Arrays.asList(InviteFilterMode.TRACK_USES);
    }
}
