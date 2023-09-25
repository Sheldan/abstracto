package dev.sheldan.abstracto.moderation.listener.infraction;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.service.UserService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.listener.InfractionUpdatedDescriptionListener;
import dev.sheldan.abstracto.moderation.model.database.Infraction;
import dev.sheldan.abstracto.moderation.model.database.InfractionParameter;
import dev.sheldan.abstracto.moderation.model.listener.InfractionDescriptionEventModel;
import dev.sheldan.abstracto.moderation.model.template.command.BanLog;
import dev.sheldan.abstracto.moderation.service.BanService;
import dev.sheldan.abstracto.moderation.service.BanServiceBean;
import dev.sheldan.abstracto.moderation.service.management.InfractionManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
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
    private BanServiceBean banServiceBean;

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
        CompletableFuture<Member> creatorUser = memberService.retrieveMemberInServer(ServerUser.fromAUserInAServer(infraction.getInfractionCreator()));
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
    public void handleBanUpdate(InfractionDescriptionEventModel model, CompletableFuture<User> infractionUser, CompletableFuture<Member> infractionCreator, CompletableFuture<DefaultListenerResult> returningFuture) {
        Infraction infraction = infractionManagementService.loadInfraction(model.getInfractionId());
        GuildMessageChannel messageChannel = channelService.getMessageChannelFromServer(model.getServerId(), infraction.getLogChannel().getId());
        Duration deletionDuration = infraction
                .getParameters()
                .stream()
                .filter(infractionParameter -> infractionParameter.getInfractionParameterId().getName().equals(BanService.INFRACTION_PARAMETER_DELETION_DURATION_KEY))
                .findAny()
                .map(InfractionParameter::getValue)
                .map(Duration::parse)
                .orElse(Duration.ZERO);
        BanLog banLog = BanLog
                .builder()
                .bannedUser(infractionUser.isCompletedExceptionally() ? null : infractionUser.join())
                .banningMember(infractionCreator.isCompletedExceptionally() ? null : infractionCreator.join())
                .deletionDuration(deletionDuration)
                .reason(model.getNewDescription())
                .build();

        MessageToSend message = banServiceBean.renderBanMessage(banLog, model.getServerId());
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
