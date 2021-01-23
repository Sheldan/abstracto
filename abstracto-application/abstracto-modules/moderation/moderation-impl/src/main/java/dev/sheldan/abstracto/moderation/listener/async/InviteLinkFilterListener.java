package dev.sheldan.abstracto.moderation.listener.async;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.execution.result.MessageReceivedListenerResult;
import dev.sheldan.abstracto.core.listener.sync.jda.MessageReceivedListener;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.config.features.ModerationFeatures;
import dev.sheldan.abstracto.moderation.config.features.mode.InviteFilterMode;
import dev.sheldan.abstracto.moderation.config.posttargets.InviteFilterPostTarget;
import dev.sheldan.abstracto.moderation.models.template.listener.InviteDeletedModel;
import dev.sheldan.abstracto.moderation.service.InviteLinkFilterService;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;

@Component
@Slf4j
public class InviteLinkFilterListener implements MessageReceivedListener {

    @Autowired
    private InviteLinkFilterService inviteLinkFilterService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private TemplateService templateService;

    public static final String INVITE_LINK_DELETED_NOTIFICATION_EMBED_TEMPLATE_KEY = "invite_link_deleted_notification";

    @Override
    public MessageReceivedListenerResult execute(Message message) {
        Long serverId = message.getGuild().getIdLong();
        Matcher matcher = Message.INVITE_PATTERN.matcher(message.getContentRaw());
        ServerUser author = ServerUser.builder().userId(message.getAuthor().getIdLong()).serverId(message.getGuild().getIdLong()).build();
        boolean toDelete = false;
        List<String> codesToTrack = new ArrayList<>();
        while(matcher.find()) {
            String code = matcher.group("code");
            boolean codeFiltered = inviteLinkFilterService.isCodeFiltered(code, author);
            if(codeFiltered) {
                codesToTrack.add(code);
                toDelete = true;
            }
        }

        if(toDelete) {
            message.delete().queue();
            boolean trackUsages = featureModeService.featureModeActive(ModerationFeatures.INVITE_FILTER, serverId, InviteFilterMode.TRACK_USES);
            if(trackUsages) {
                codesToTrack.forEach(s -> inviteLinkFilterService.storeFilteredInviteLinkUsage(s, author));
            }
            boolean sendNotification = featureModeService.featureModeActive(ModerationFeatures.INVITE_FILTER, serverId, InviteFilterMode.FILTER_NOTIFICATIONS);
            if(sendNotification) {
                sendDeletionNotification(codesToTrack, message);
            }
            return MessageReceivedListenerResult.DELETED;
        } else {
            return MessageReceivedListenerResult.PROCESSED;
        }
    }

    private void sendDeletionNotification(List<String> codes, Message message) {
        Long serverId = message.getGuild().getIdLong();
        if(!postTargetService.postTargetDefinedInServer(InviteFilterPostTarget.INVITE_DELETE_LOG, serverId)) {
            log.info("Post target {} not defined for server {} - not sending invite link deletion notification.", InviteFilterPostTarget.INVITE_DELETE_LOG.getKey(), serverId);
            return;
        }
        InviteDeletedModel model = InviteDeletedModel
                .builder()
                .author(message.getMember())
                .guild(message.getGuild())
                .message(message)
                .channel(message.getTextChannel())
                .invites(codes)
                .build();
        log.info("Sending notification about {} deleted invite links in guild {} from user {} in channel {} in message {}.",
                codes.size(), serverId, message.getAuthor().getIdLong(), message.getTextChannel().getIdLong(), message.getIdLong());
        MessageToSend messageToSend = templateService.renderEmbedTemplate(INVITE_LINK_DELETED_NOTIFICATION_EMBED_TEMPLATE_KEY, model);
        List<CompletableFuture<Message>> messageFutures = postTargetService.sendEmbedInPostTarget(messageToSend, InviteFilterPostTarget.INVITE_DELETE_LOG, serverId);
        FutureUtils.toSingleFutureGeneric(messageFutures).thenAccept(unused ->
            log.trace("Successfully send notification about deleted invite link in message {}.", message.getIdLong())
        ).exceptionally(throwable -> {
            log.error("Failed to send notification about deleted invite link in message {}.", message.getIdLong());
            return null;
        });

    }

    @Override
    public FeatureEnum getFeature() {
        return ModerationFeatures.INVITE_FILTER;
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.HIGH;
    }
}
