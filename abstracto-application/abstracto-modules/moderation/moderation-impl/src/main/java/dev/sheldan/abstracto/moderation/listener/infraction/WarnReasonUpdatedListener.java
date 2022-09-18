package dev.sheldan.abstracto.moderation.listener.infraction;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.GuildService;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.listener.InfractionUpdatedDescriptionListener;
import dev.sheldan.abstracto.moderation.model.database.Infraction;
import dev.sheldan.abstracto.moderation.model.database.Warning;
import dev.sheldan.abstracto.moderation.model.listener.InfractionDescriptionEventModel;
import dev.sheldan.abstracto.moderation.model.template.command.WarnContext;
import dev.sheldan.abstracto.moderation.service.WarnServiceBean;
import dev.sheldan.abstracto.moderation.service.management.InfractionManagementService;
import dev.sheldan.abstracto.moderation.service.management.WarnManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class WarnReasonUpdatedListener implements InfractionUpdatedDescriptionListener {

    @Autowired
    private WarnManagementService warnManagementService;

    @Autowired
    private WarnServiceBean warnService;

    @Autowired
    private InfractionManagementService infractionManagementService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private GuildService guildService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private WarnReasonUpdatedListener self;

    @Override
    public CompletableFuture<DefaultListenerResult> execute(InfractionDescriptionEventModel model) {
        Optional<Warning> potentialWarning = warnManagementService.findWarnByInfraction(model.getInfractionId());
        if(potentialWarning.isPresent()) {
            Warning warning = potentialWarning.get();
            Long warnId = warning.getWarnId().getId();
            CompletableFuture<Member> warnedUser = memberService.retrieveMemberInServer(ServerUser.fromAUserInAServer(warning.getWarnedUser()));
            CompletableFuture<Member> warningUser = memberService.retrieveMemberInServer(ServerUser.fromAUserInAServer(warning.getWarningUser()));
            CompletableFuture<DefaultListenerResult> returningFuture = new CompletableFuture<>();
            CompletableFuture.allOf(warnedUser, warningUser)
                    .whenComplete((unused, throwable) -> {
                        if(throwable != null) {
                            log.warn("Failed to load members for infraction update of warning {} in server {}.", warnId, model.getServerId(), throwable);
                        }
                        self.handleWarnUpdate(model, warnId, warnedUser, warningUser, returningFuture);
                    });
            return returningFuture;
        } else {
            return CompletableFuture.completedFuture(DefaultListenerResult.IGNORED);
        }
    }

    private void handleWarnUpdate(InfractionDescriptionEventModel model, Long warnId, CompletableFuture<Member> warnedUser, CompletableFuture<Member> warningUser, CompletableFuture<DefaultListenerResult> returningFuture) {
        Guild guild = guildService.getGuildById(model.getServerId());
        Infraction infraction = infractionManagementService.loadInfraction(model.getInfractionId());
        GuildMessageChannel messageChannel = channelService.getMessageChannelFromServer(model.getServerId(), infraction.getLogChannel().getId());
        WarnContext context = WarnContext
                .builder()
                .warnedMember(warnedUser.isCompletedExceptionally() ? null : warnedUser.join())
                .member(warningUser.isCompletedExceptionally() ? null : warningUser.join())
                .reason(model.getNewDescription())
                .warnId(warnId)
                .guild(guild)
                .build();
        MessageToSend message = warnService.renderMessageModel(context);
        messageService.editMessageInChannel(messageChannel, message, infraction.getLogMessageId())
                .thenAccept(unused1 -> returningFuture.complete(DefaultListenerResult.PROCESSED))
                .exceptionally(throwable1 -> {
                    returningFuture.complete(DefaultListenerResult.PROCESSED);
                    return null;
                });
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.WARNING;
    }

    @Override
    public Boolean handlesEvent(InfractionDescriptionEventModel model) {
        return model.getType().equals(WarnServiceBean.WARN_INFRACTION_TYPE);
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.MEDIUM;
    }
}
