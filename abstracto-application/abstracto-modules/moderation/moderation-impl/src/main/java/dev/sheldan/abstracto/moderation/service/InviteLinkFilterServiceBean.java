package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.moderation.exception.InvalidInviteException;
import dev.sheldan.abstracto.moderation.models.database.FilteredInviteLink;
import dev.sheldan.abstracto.moderation.service.management.AllowedInviteLinkManagement;
import dev.sheldan.abstracto.moderation.service.management.FilteredInviteLinkManagement;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class InviteLinkFilterServiceBean implements InviteLinkFilterService {

    @Autowired
    private AllowedInviteLinkManagement allowedInviteLinkManagement;

    @Autowired
    private FilteredInviteLinkManagement filteredInviteLinkManagement;

    @Autowired
    private ServerManagementService serverManagementService;

    private static final Pattern INVITE_CODE_PATTERN = Pattern.compile("(?<code>[a-z0-9-]+)", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean isCodeFiltered(String code, ServerUser serverUser) {
        return !isCodeAllowed(code, serverUser);
    }

    @Override
    public boolean isCodeAllowed(String code, ServerUser serverUser) {
        return allowedInviteLinkManagement.allowedInviteLinkExists(serverUser, code);
    }

    @Override
    public boolean isCodeAllowed(String code, Long serverId) {
        return allowedInviteLinkManagement.allowedInviteLinkExists(serverId, code);
    }

    @Override
    public void storeFilteredInviteLinkUsage(String code, ServerUser serverUser) {
        Optional<FilteredInviteLink> inviteLinkOptional = filteredInviteLinkManagement.findInviteLinkViaCode(serverUser.getServerId(), code);
        if(inviteLinkOptional.isPresent()) {
            inviteLinkOptional.ifPresent(filteredInviteLink -> filteredInviteLink.setUses(filteredInviteLink.getUses() + 1));
        } else {
            AServer server = serverManagementService.loadServer(serverUser.getServerId());
            filteredInviteLinkManagement.createFilteredInviteLink(server, code);
        }
    }

    @Override
    public void allowInvite(String invite, Long serverId) {
        String inviteCode = extractCode(invite);
        if(isCodeAllowed(inviteCode, serverId)) {
            return;
        }
        AServer server = serverManagementService.loadServer(serverId);
        allowedInviteLinkManagement.createAllowedInviteLink(server, inviteCode);
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
    public void disAllowInvite(String invite, Long serverId) {
        String inviteCode = extractCode(invite);
        AServer server = serverManagementService.loadServer(serverId);
        allowedInviteLinkManagement.removeAllowedInviteLink(server, inviteCode);
    }

    @Override
    public void clearAllTrackedInviteCodes(Long serverId) {
        filteredInviteLinkManagement.clearFilteredInviteLinks(serverId);
    }

    @Override
    public void clearAllUses(String code, Long serverId) {
        String inviteCode = extractCode(code);
        filteredInviteLinkManagement.clearFilteredInviteLink(inviteCode, serverId);
    }

    @Override
    public List<FilteredInviteLink> getTopFilteredInviteLinks(Long serverId, Integer count) {
        return filteredInviteLinkManagement.getTopFilteredInviteLink(serverId, count);
    }

    @Override
    public List<FilteredInviteLink> getTopFilteredInviteLinks(Long serverId) {
        return getTopFilteredInviteLinks(serverId, 5);
    }
}
