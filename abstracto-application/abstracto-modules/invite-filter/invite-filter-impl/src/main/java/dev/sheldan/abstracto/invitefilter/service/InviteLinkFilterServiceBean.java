package dev.sheldan.abstracto.invitefilter.service;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.invitefilter.exception.InvalidInviteException;
import dev.sheldan.abstracto.invitefilter.model.database.FilteredInviteLink;
import dev.sheldan.abstracto.invitefilter.service.management.AllowedInviteLinkManagement;
import dev.sheldan.abstracto.invitefilter.service.management.FilteredInviteLinkManagement;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final Pattern INVITE_CODE_PATTERN = Pattern.compile("(?<code>[a-z0-9-]+)", Pattern.CASE_INSENSITIVE);

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
}
