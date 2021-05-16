package dev.sheldan.abstracto.invitefilter.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncMessageReceivedListener;
import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.listener.MessageReceivedModel;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.invitefilter.config.InviteFilterFeatureDefinition;
import dev.sheldan.abstracto.invitefilter.config.InviteFilterMode;
import dev.sheldan.abstracto.invitefilter.config.InviteFilterPostTarget;
import dev.sheldan.abstracto.invitefilter.model.template.listener.DeletedInvite;
import dev.sheldan.abstracto.invitefilter.model.template.listener.DeletedInvitesNotificationModel;
import dev.sheldan.abstracto.invitefilter.service.InviteLinkFilterService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static dev.sheldan.abstracto.invitefilter.service.InviteLinkFilterService.INVITE_FILTER_CHANNEL_GROUP_TYPE;
import static dev.sheldan.abstracto.invitefilter.service.InviteLinkFilterService.INVITE_FILTER_EFFECT_KEY;

@Component
@Slf4j
public class InviteLinkFilterListener implements AsyncMessageReceivedListener {

    @Autowired
    private InviteLinkFilterService inviteLinkFilterService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private MetricService metricService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ChannelGroupService channelGroupService;

    @Autowired
    private RoleImmunityService roleImmunityService;

    public static final String INVITE_FILTER_METRIC = "invite.filter";
    public static final String CONSEQUENCE = "consequence";

    private static final CounterMetric MESSAGE_INVITE_FILTERED =
            CounterMetric
                    .builder()
                    .tagList(Arrays.asList(MetricTag.getTag(CONSEQUENCE, "filtered")))
                    .name(INVITE_FILTER_METRIC)
                    .build();

    public static final String INVITE_LINK_DELETED_NOTIFICATION_EMBED_TEMPLATE_KEY = "invite_link_deleted_notification";

    private void sendDeletionNotification(List<String> codes, Message message) {
        Long serverId = message.getGuild().getIdLong();
        if(!postTargetService.postTargetDefinedInServer(InviteFilterPostTarget.INVITE_DELETE_LOG, serverId)) {
            log.info("Post target {} not defined for server {} - not sending invite link deletion notification.", InviteFilterPostTarget.INVITE_DELETE_LOG.getKey(), serverId);
            return;
        }
        DeletedInvitesNotificationModel model = DeletedInvitesNotificationModel
                .builder()
                .author(message.getMember())
                .guild(message.getGuild())
                .message(message)
                .channel(message.getChannel())
                .invites(groupInvites(codes))
                .build();
        log.info("Sending notification about {} deleted invite links in guild {} from user {} in channel {} in message {}.",
                codes.size(), serverId, message.getAuthor().getIdLong(), message.getChannel().getIdLong(), message.getIdLong());
        MessageToSend messageToSend = templateService.renderEmbedTemplate(INVITE_LINK_DELETED_NOTIFICATION_EMBED_TEMPLATE_KEY, model, message.getGuild().getIdLong());
        List<CompletableFuture<Message>> messageFutures = postTargetService.sendEmbedInPostTarget(messageToSend, InviteFilterPostTarget.INVITE_DELETE_LOG, serverId);
        FutureUtils.toSingleFutureGeneric(messageFutures).thenAccept(unused ->
            log.debug("Successfully send notification about deleted invite link in message {}.", message.getIdLong())
        ).exceptionally(throwable -> {
            log.error("Failed to send notification about deleted invite link in message {}.", message.getIdLong());
            return null;
        });
    }

    private List<DeletedInvite> groupInvites(List<String> codes) {
        return codes
                .stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .map(functionLongEntry -> new DeletedInvite(functionLongEntry.getKey(), functionLongEntry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public FeatureDefinition getFeature() {
        return InviteFilterFeatureDefinition.INVITE_FILTER;
    }

    @PostConstruct
    public void postConstruct() {
        metricService.registerCounter(MESSAGE_INVITE_FILTERED, "Amount of messages containing an invite filtered");
    }

    private boolean isInviteFilterActiveInChannel(MessageChannel channel) {
        return channelGroupService.isChannelInEnabledChannelGroupOfType(INVITE_FILTER_CHANNEL_GROUP_TYPE, channel.getIdLong());
    }

    @Override
    public DefaultListenerResult execute(MessageReceivedModel model) {
        Message message = model.getMessage();

        if(!message.isFromGuild() || message.isWebhookMessage() || message.getType().isSystem()) {
            return DefaultListenerResult.IGNORED;
        }

        List<String> foundInvites = new ArrayList<>();
        Matcher matcher = Message.INVITE_PATTERN.matcher(message.getContentRaw());
        while(matcher.find()) {
            foundInvites.add(matcher.group("code"));
        }

        if(foundInvites.isEmpty()){
            return DefaultListenerResult.IGNORED;
        }

        if(!isInviteFilterActiveInChannel(model.getMessage().getChannel())) {
            return DefaultListenerResult.IGNORED;
        }

        if(roleImmunityService.isImmune(message.getMember(), INVITE_FILTER_EFFECT_KEY)) {
            log.info("Not checking for invites in message, because author {} in channel {} in guild {} is immune against invite filter.",
                    message.getMember().getIdLong(), message.getGuild().getIdLong(), message.getChannel().getIdLong());
            return DefaultListenerResult.IGNORED;
        }

        List<CompletableFuture<Invite>> inviteList = new ArrayList<>();
        JDA jda = model.getMessage().getJDA();
        foundInvites.forEach(s -> inviteList.add(inviteLinkFilterService.resolveInvite(jda, s)));

        CompletableFutureList<Invite> list = new CompletableFutureList<>(inviteList);
        list.getMainFuture().whenComplete((unused, throwable) -> {
            List<Invite> invites = list.getObjects();
            Long serverId = message.getGuild().getIdLong();
            ServerUser author = ServerUser.builder().userId(message.getAuthor().getIdLong()).serverId(message.getGuild().getIdLong()).build();
            boolean toDelete = false;
            Map<Long, String> targetServers = new HashMap<>();
            List<String> deletedInvites = new ArrayList<>();
            for (Invite invite : invites) {
                if (invite.getType().equals(Invite.InviteType.GUILD)
                        && inviteLinkFilterService.isCodeFiltered(invite.getGuild().getIdLong(), author)) {
                    toDelete = true;
                    deletedInvites.add(invite.getCode());
                    targetServers.put(invite.getGuild().getIdLong(), invite.getGuild().getName());
                }
            }
            List<String> unResolvedInvites = new ArrayList<>();
            foundInvites.forEach(possibleUnresolvedInvite -> {
                if(invites.stream().noneMatch(invite -> invite.getCode().equals(possibleUnresolvedInvite))) {
                    unResolvedInvites.add(possibleUnresolvedInvite);
                }
            });

            for(String unresolvedInvite : unResolvedInvites) {
                if(inviteLinkFilterService.isCodeFiltered(unresolvedInvite, author)) {
                    toDelete = true;
                    deletedInvites.add(unresolvedInvite);
                }
            }
            if(toDelete) {
                metricService.incrementCounter(MESSAGE_INVITE_FILTERED);
                messageService.deleteMessage(message);
                boolean trackUsages = featureModeService.featureModeActive(InviteFilterFeatureDefinition.INVITE_FILTER, serverId, InviteFilterMode.TRACK_USES);
                if(trackUsages) {
                    targetServers.forEach((targetServerId, serverName) -> inviteLinkFilterService.storeFilteredInviteLinkUsage(targetServerId, serverName, author));
                }
                boolean sendNotification = featureModeService.featureModeActive(InviteFilterFeatureDefinition.INVITE_FILTER, serverId, InviteFilterMode.FILTER_NOTIFICATIONS);
                if(sendNotification) {
                    sendDeletionNotification(deletedInvites, message);
                }
            }
        });

        return DefaultListenerResult.PROCESSED;
    }
}
