package dev.sheldan.abstracto.moderation.listener.infraction;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.models.template.display.UserDisplay;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.service.UserService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.listener.InfractionUpdatedDescriptionListener;
import dev.sheldan.abstracto.moderation.model.database.Infraction;
import dev.sheldan.abstracto.moderation.model.listener.InfractionDescriptionEventModel;
import dev.sheldan.abstracto.moderation.model.template.listener.UserBannedLogModel;
import dev.sheldan.abstracto.moderation.service.BanService;
import dev.sheldan.abstracto.moderation.service.BanServiceBean;
import dev.sheldan.abstracto.moderation.service.management.InfractionManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;


@Component
@Slf4j
public class BanReasonUpdatedListener implements InfractionUpdatedDescriptionListener {

    @Autowired
    private InfractionManagementService infractionManagementService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private UserService userService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private BanReasonUpdatedListener self;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ChannelService channelService;

    @Override
    public CompletableFuture<DefaultListenerResult> execute(InfractionDescriptionEventModel model) {
        Infraction infraction = infractionManagementService.loadInfraction(model.getInfractionId());
        CompletableFuture<User> infractionUser = userService.retrieveUserForId(infraction.getUser().getUserReference().getId());
        CompletableFuture<User> creatorUser = userService.retrieveUserForId(infraction.getInfractionCreator().getUserReference().getId());
        CompletableFuture<DefaultListenerResult> returningFuture = new CompletableFuture<>();
        Long infractionId = infraction.getId();
        CompletableFuture.allOf(infractionUser, creatorUser)
                .whenComplete((unused, throwable) -> {
                    if(throwable != null) {
                        log.warn("Failed to load members for infraction update of ban {} in server {}.", infractionId, model.getServerId(), throwable);
                    }
                    self.handleBanUpdate(model, infractionUser, creatorUser, returningFuture);
                });
        return returningFuture;
    }

    @Transactional
    public void handleBanUpdate(InfractionDescriptionEventModel model, CompletableFuture<User> infractionUser, CompletableFuture<User> infractionCreator, CompletableFuture<DefaultListenerResult> returningFuture) {
        Infraction infraction = infractionManagementService.loadInfraction(model.getInfractionId());
        GuildMessageChannel messageChannel = channelService.getMessageChannelFromServer(model.getServerId(), infraction.getLogChannel().getId());
        UserBannedLogModel banLog = UserBannedLogModel
                .builder()
                .bannedUser(infractionUser.isCompletedExceptionally() ? null : UserDisplay.fromUser(infractionUser.join()))
                .banningUser(infractionCreator.isCompletedExceptionally() ? null : UserDisplay.fromUser(infractionCreator.join()))
                .reason(model.getNewDescription())
                .build();

        MessageToSend message = templateService.renderEmbedTemplate(BanServiceBean.USER_BANNED_NOTIFICATION_TEMPLATE, banLog, model.getServerId());
        messageService.editMessageInChannel(messageChannel, message, infraction.getLogMessageId())
                .thenAccept(unused1 -> returningFuture.complete(DefaultListenerResult.PROCESSED))
                .exceptionally(throwable1 -> {
                    returningFuture.complete(DefaultListenerResult.PROCESSED);
                    return null;
                });
    }


        @Override
    public Boolean handlesEvent(InfractionDescriptionEventModel model) {
        return model.getType().equals(BanService.BAN_INFRACTION_TYPE);
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.MODERATION;
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.MEDIUM;
    }
}
