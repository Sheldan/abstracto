package dev.sheldan.abstracto.invitefilter.service;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.invitefilter.config.InviteFilterFeatureDefinition;
import dev.sheldan.abstracto.invitefilter.config.InviteFilterMode;
import dev.sheldan.abstracto.invitefilter.config.InviteFilterPostTarget;
import dev.sheldan.abstracto.invitefilter.exception.InvalidInviteException;
import dev.sheldan.abstracto.invitefilter.model.database.FilteredInviteLink;
import dev.sheldan.abstracto.invitefilter.model.template.listener.DeletedInvite;
import dev.sheldan.abstracto.invitefilter.model.template.listener.DeletedInvitesNotificationModel;
import dev.sheldan.abstracto.invitefilter.service.management.AllowedInviteLinkManagement;
import dev.sheldan.abstracto.invitefilter.service.management.FilteredInviteLinkManagement;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InviteLinkFilterServiceBean implements InviteLinkFilterService {

    @Autowired
    private AllowedInviteLinkManagement allowedInviteLinkManagement;

    @Autowired
    private FilteredInviteLinkManagement filteredInviteLinkManagement;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private InviteLinkFilterServiceBean self;

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

    private static final Pattern INVITE_CODE_PATTERN = Pattern.compile("(?<code>[a-z0-9-]+)", Pattern.CASE_INSENSITIVE);

    public static final String INVITE_FILTER_METRIC = "invite.filter";
    public static final String CONSEQUENCE = "consequence";

    private static final CounterMetric MESSAGE_INVITE_FILTERED =
            CounterMetric
                    .builder()
                    .tagList(Arrays.asList(MetricTag.getTag(CONSEQUENCE, "filtered")))
                    .name(INVITE_FILTER_METRIC)
                    .build();

    public static final String INVITE_LINK_DELETED_NOTIFICATION_EMBED_TEMPLATE_KEY = "invite_link_deleted_notification";


    @Override
    public boolean isCodeFiltered(Long targetServerId, ServerUser serverUser) {
        return !isCodeAllowed(targetServerId, serverUser);
    }

    @Override
    public boolean isCodeFiltered(String code, ServerUser serverUser) {
        return !isCodeAllowed(code, serverUser);
    }

    @Override
    public boolean isCodeAllowed(Long targetServerId, ServerUser serverUser) {
        return allowedInviteLinkManagement.allowedInviteLinkExists(serverUser, targetServerId);
    }

    @Override
    public boolean isCodeAllowed(String code, ServerUser serverUser) {
        return allowedInviteLinkManagement.allowedInviteLinkExists(serverUser, code);
    }

    @Override
    public boolean isCodeAllowed(Long targetServerId, Long serverId) {
        return allowedInviteLinkManagement.allowedInviteLinkExists(serverId, targetServerId);
    }

    @Override
    public void storeFilteredInviteLinkUsage(Long targetServerId, String serverName, ServerUser serverUser) {
        Optional<FilteredInviteLink> inviteLinkOptional = filteredInviteLinkManagement.findInviteLinkViaTargetID(serverUser.getServerId(), targetServerId);
        if(inviteLinkOptional.isPresent()) {
            inviteLinkOptional.ifPresent(filteredInviteLink -> filteredInviteLink.setUses(filteredInviteLink.getUses() + 1));
        } else {
            AServer server = serverManagementService.loadServer(serverUser.getServerId());
            filteredInviteLinkManagement.createFilteredInviteLink(server, targetServerId, serverName);
        }
    }

    @Override
    public CompletableFuture<Void> allowInvite(String inviteLink, Long serverId, JDA jda) {
        String inviteCode = extractCode(inviteLink);
        return self.resolveInvite(jda, inviteCode)
                .thenAccept(invite -> self.allowInviteInServer(serverId, invite));
    }

    public void allowInviteInServer(Long serverId, Invite invite) {
        if(!invite.getType().equals(Invite.InviteType.GUILD)) {
            throw new AbstractoRunTimeException("Invite is not for a guild.");
        }
        Long targetServerId = invite.getGuild().getIdLong();
        if(self.isCodeAllowed(targetServerId, serverId)) {
            return;
        }
        AServer server = serverManagementService.loadServer(serverId);
        allowedInviteLinkManagement.createAllowedInviteLink(server, targetServerId, invite.getCode());
    }

    private String extractCode(String invite) {
        Matcher matcher = Message.INVITE_PATTERN.matcher(invite);
        String inviteCode;
        if(matcher.find()) {
            inviteCode = matcher.group("code");
        } else {
            Matcher codeOnlyMatcher = INVITE_CODE_PATTERN.matcher(invite);
            if(codeOnlyMatcher.find()) {
                inviteCode = codeOnlyMatcher.group("code");
            } else {
                throw new InvalidInviteException("Invalid invite was provided.");
            }
        }
        return inviteCode;
    }

    @Override
    public CompletableFuture<Void> disAllowInvite(String fullInvite, Long serverId, JDA jda) {
        String inviteCode = extractCode(fullInvite);
        return self.resolveInvite(jda, inviteCode)
                .thenAccept(resolvedInvite -> self.disallowInviteInServer(serverId, resolvedInvite));
    }
    @Transactional
    public void disallowInviteInServer(Long serverId, Invite resolvedInvite) {
        if(!resolvedInvite.getType().equals(Invite.InviteType.GUILD)) {
            throw new AbstractoRunTimeException("Invite is not for a guild.");
        }
        Long targetServerId = resolvedInvite.getGuild().getIdLong();
        AServer server = serverManagementService.loadServer(serverId);
        allowedInviteLinkManagement.removeAllowedInviteLink(server, targetServerId);
    }

    @Override
    public void clearAllTrackedInviteCodes(Long serverId) {
        filteredInviteLinkManagement.clearFilteredInviteLinks(serverId);
    }

    @Override
    public void clearAllUses(Long targetServerId, Long serverId) {
        filteredInviteLinkManagement.clearFilteredInviteLink(targetServerId, serverId);
    }

    @Override
    public CompletableFuture<Void> clearAllUsedOfCode(String invite, Long serverId, JDA jda) {
        return resolveInvite(jda, invite)
                .thenAccept(resolvedInvite -> self.clearUsesOfInvite(serverId, resolvedInvite))
                .exceptionally(throwable -> {
                    log.warn("Failed to to clear tracked invite uses via invite resolving.", throwable);
                    return null;
                });
    }

    @Transactional
    public void clearUsesOfInvite(Long serverId, Invite resolvedInvite) {
        if(resolvedInvite.getType().equals(Invite.InviteType.GUILD)) {
            self.clearAllUses(resolvedInvite.getGuild().getIdLong(), serverId);
        } else {
            throw new AbstractoRunTimeException("Given invite was from a group channel.");
        }
    }

    @Override
    public List<FilteredInviteLink> getTopFilteredInviteLinks(Long serverId, Integer count) {
        return filteredInviteLinkManagement.getTopFilteredInviteLink(serverId, count);
    }

    @Override
    public List<FilteredInviteLink> getTopFilteredInviteLinks(Long serverId) {
        return getTopFilteredInviteLinks(serverId, 5);
    }

    @Override
    public CompletableFuture<Invite> resolveInvite(JDA jda, String code) {
        return Invite.resolve(jda, extractCode(code)).submit();
    }

    private CompletableFuture<Void> sendDeletionNotification(List<InviteToDelete> codes, Message message) {
        Long serverId = message.getGuild().getIdLong();
        if(!postTargetService.postTargetDefinedInServer(InviteFilterPostTarget.INVITE_DELETE_LOG, serverId)) {
            log.info("Post target {} not defined for server {} - not sending invite link deletion notification.", InviteFilterPostTarget.INVITE_DELETE_LOG.getKey(), serverId);
            return CompletableFuture.completedFuture(null);
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
        return FutureUtils.toSingleFutureGeneric(messageFutures).thenAccept(unused ->
                log.debug("Successfully send notification about deleted invite link in message {}.", message.getIdLong())
        );
    }

    private List<DeletedInvite> groupInvites(List<InviteToDelete> codes) {

        Map<String, String> codeToGuildName = new HashMap<>();
        for (InviteToDelete invite: codes) {
            if(!codeToGuildName.containsKey(invite.getInviteCode())) {
                codeToGuildName.put(invite.getInviteCode(), invite.getGuildName());
            }
        }
        return codes
                .stream()
                .collect(Collectors.groupingBy(InviteToDelete::getInviteCode, Collectors.counting()))
                .entrySet()
                .stream()
                .map(functionLongEntry -> DeletedInvite
                        .builder()
                        .code(functionLongEntry.getKey())
                        .guildName(codeToGuildName.get(functionLongEntry.getKey()))
                        .count(functionLongEntry.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public boolean isInviteFilterActiveInChannel(MessageChannel channel) {
        return channelGroupService.isChannelInEnabledChannelGroupOfType(INVITE_FILTER_CHANNEL_GROUP_TYPE, channel.getIdLong());
    }

    @Override
    public boolean isMemberImmuneAgainstInviteFilter(Member member) {
        return roleImmunityService.isImmune(member, INVITE_FILTER_EFFECT_KEY);
    }

    @Override
    public List<String> findInvitesInMessage(Message message) {
        List<String> foundInvites;
        foundInvites = new ArrayList<>();
        Matcher matcher = Message.INVITE_PATTERN.matcher(message.getContentRaw());
        while(matcher.find()) {
            foundInvites.add(matcher.group("code"));
        }
        return foundInvites;
    }

    public void resolveAndCheckInvites(Message message, List<String> foundInvites) {
        List<CompletableFuture<Invite>> inviteList = new ArrayList<>();
        JDA jda = message.getJDA();
        foundInvites.forEach(s -> inviteList.add(resolveInvite(jda, s)));

        CompletableFutureList<Invite> list = new CompletableFutureList<>(inviteList);
        list.getMainFuture().whenComplete((unused, throwable) -> {
            List<Invite> invites = list.getObjects();
            Long serverId = message.getGuild().getIdLong();
            ServerUser author = ServerUser.builder().userId(message.getAuthor().getIdLong()).serverId(message.getGuild().getIdLong()).build();
            boolean toDelete = false;
            Map<Long, String> targetServers = new HashMap<>();
            List<InviteToDelete> deletedInvites = new ArrayList<>();
            for (Invite invite : invites) {
                if (invite.getType().equals(Invite.InviteType.GUILD)
                        && isCodeFiltered(invite.getGuild().getIdLong(), author)) {
                    toDelete = true;
                    InviteToDelete inviteToDelete = InviteToDelete
                            .builder()
                            .inviteCode(invite.getCode())
                            .guildName(invite.getGuild().getName())
                            .build();
                    deletedInvites.add(inviteToDelete);
                    targetServers.put(invite.getGuild().getIdLong(), invite.getGuild().getName());
                }
            }
            List<String> unResolvedInvites = new ArrayList<>();
            foundInvites.forEach(possibleUnresolvedInvite -> {
                if(invites.stream().noneMatch(invite -> invite.getCode().equalsIgnoreCase(possibleUnresolvedInvite))) {
                    unResolvedInvites.add(possibleUnresolvedInvite);
                }
            });

            for(String unresolvedInvite : unResolvedInvites) {
                if(isCodeFiltered(unresolvedInvite, author)) {
                    toDelete = true;
                    InviteToDelete inviteToDelete = InviteToDelete
                            .builder()
                            .inviteCode(unresolvedInvite)
                            .build();
                    deletedInvites.add(inviteToDelete);
                }
            }
            if(toDelete) {
                metricService.incrementCounter(MESSAGE_INVITE_FILTERED);
                messageService.deleteMessage(message);
                boolean trackUsages = featureModeService.featureModeActive(InviteFilterFeatureDefinition.INVITE_FILTER, serverId, InviteFilterMode.TRACK_USES);
                if(trackUsages) {
                    targetServers.forEach((targetServerId, serverName) -> storeFilteredInviteLinkUsage(targetServerId, serverName, author));
                }
                boolean sendNotification = featureModeService.featureModeActive(InviteFilterFeatureDefinition.INVITE_FILTER, serverId, InviteFilterMode.FILTER_NOTIFICATIONS);
                if(sendNotification) {
                    sendDeletionNotification(deletedInvites, message)
                    .thenAccept(unused1 -> log.info("Sent invite deletion notification.")).exceptionally(throwable1 -> {
                        log.error("Failed to send invite deletion notification.");
                        return null;
                    });
                }
            }
        }).exceptionally(throwable -> {
            log.error("Invite matching failed.", throwable);
            return null;
        });
    }

    @PostConstruct
    public void postConstruct() {
        metricService.registerCounter(MESSAGE_INVITE_FILTERED, "Amount of messages containing an invite filtered");
    }

    @Getter
    @Setter
    @Builder
    private static class InviteToDelete {
        private String guildName;
        private String inviteCode;
    }

}
