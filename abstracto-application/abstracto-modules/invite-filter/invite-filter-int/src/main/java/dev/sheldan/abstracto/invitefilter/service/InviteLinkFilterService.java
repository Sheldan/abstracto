package dev.sheldan.abstracto.invitefilter.service;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.invitefilter.model.database.FilteredInviteLink;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Invite;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface InviteLinkFilterService {
    String INVITE_FILTER_CHANNEL_GROUP_TYPE = "inviteFilter";
    String INVITE_FILTER_EFFECT_KEY = "inviteFilterDeletion";
    boolean isCodeFiltered(Long targetServerId, ServerUser serverUser);
    boolean isCodeFiltered(String code, ServerUser serverUser);
    boolean isCodeAllowed(Long targetServerId, ServerUser serverUser);
    boolean isCodeAllowed(String code, ServerUser serverUser);
    boolean isCodeAllowed(Long targetServerId, Long serverId);
    void storeFilteredInviteLinkUsage(Long targetServerId, String serverName, ServerUser serverUser);
    CompletableFuture<Void> allowInvite(String invite, Long serverId, JDA jda);
    CompletableFuture<Void> disAllowInvite(String invite, Long serverId, JDA jda);
    void clearAllTrackedInviteCodes(Long serverId);
    void clearAllUses(Long targetServerId, Long serverId);
    CompletableFuture<Void> clearAllUsedOfCode(String code, Long serverId, JDA jda);
    List<FilteredInviteLink> getTopFilteredInviteLinks(Long serverId, Integer count);
    List<FilteredInviteLink> getTopFilteredInviteLinks(Long serverId);
    CompletableFuture<Invite> resolveInvite(JDA jda, String code);
}
