package dev.sheldan.abstracto.statistic.emote.service.management;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.cache.CachedEmote;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.statistic.emote.exception.TrackedEmoteNotFoundException;
import dev.sheldan.abstracto.statistic.emote.model.PersistingEmote;
import dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emote.repository.TrackedEmoteRepository;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class TrackedEmoteManagementServiceBean implements TrackedEmoteManagementService {

    @Autowired
    private TrackedEmoteRepository repository;

    @Autowired
    private ServerManagementService serverManagementService;

    @Override
    public TrackedEmote createTrackedEmote(Long emoteId, String emoteName, Boolean animated, AServer server) {
        return createTrackedEmote(emoteId, emoteName, animated, true, server);
    }

    @Override
    public TrackedEmote createTrackedEmote(Emote emote, Guild guild) {
        AServer server = serverManagementService.loadServer(guild.getIdLong());
        return createTrackedEmote(emote.getIdLong(), emote.getName(), emote.isAnimated(), true, server);
    }

    @Override
    public TrackedEmote createTrackedEmote(CachedEmote emote) {
        AServer server = serverManagementService.loadServer(emote.getServerId());
        return createTrackedEmote(emote.getEmoteId(), emote.getEmoteName(), emote.getAnimated(), true, server);
    }

    @Override
    public TrackedEmote createTrackedEmote(Emote emote, Guild guild, boolean external) {
        if(external) {
            return createExternalTrackedEmote(emote, guild);
        } else {
            return createTrackedEmote(emote, guild);
        }
    }

    @Override
    public TrackedEmote createTrackedEmote(Long emoteId, String emoteName, Boolean animated, Boolean tracked, AServer server) {
        TrackedEmote emote = TrackedEmote
                .builder()
                .animated(animated)
                .trackedEmoteId(new ServerSpecificId(server.getId(), emoteId))
                .trackingEnabled(tracked)
                .emoteName(emoteName)
                .server(server)
                .external(false)
                .deleted(false)
                .build();
        log.info("Creating tracking emote with id {} and server {}.", emoteId, server.getId());
        return repository.save(emote);
    }

    @Override
    public TrackedEmote createExternalEmote(Long emoteId, String emoteName, String externalUrl, Boolean animated, AServer server, boolean trackingEnabled) {
        TrackedEmote emote = TrackedEmote
                .builder()
                .animated(animated)
                .trackedEmoteId(new ServerSpecificId(server.getId(), emoteId))
                .trackingEnabled(true)
                .deleted(false)
                .emoteName(emoteName)
                .server(server)
                .external(true)
                .externalUrl(externalUrl)
                .build();
        log.info("Creating external emote with id {} for server {}.", emoteId, server.getId());
        return repository.save(emote);
    }

    @Override
    public TrackedEmote createNotTrackedEmote(Long emoteId, String emoteName, Boolean animated, AServer server) {
        return createTrackedEmote(emoteId, emoteName, animated, false, server);
    }

    @Override
    public TrackedEmote createExternalTrackedEmote(PersistingEmote persistingEmote) {
        AServer server = serverManagementService.loadServer(persistingEmote.getServerId());
        return createExternalEmote(persistingEmote.getEmoteId(), persistingEmote.getEmoteName(), persistingEmote.getExternalUrl(), persistingEmote.getAnimated(), server, true);
    }

    @Override
    public TrackedEmote createExternalTrackedEmote(Emote emote, Guild guild) {
        AServer server = serverManagementService.loadServer(guild.getIdLong());
        return createExternalEmote(emote.getIdLong(), emote.getName(), emote.getImageUrl(), emote.isAnimated(), server, true);
    }

    @Override
    public void markAsDeleted(Long serverId, Long emoteId) {
        TrackedEmote emote = loadByEmoteId(emoteId, serverId);
        markAsDeleted(emote);
    }

    @Override
    public void markAsDeleted(TrackedEmote trackedemote) {
        log.info("Marking tracked emote {} in server {} as deleted.", trackedemote.getTrackedEmoteId().getId(), trackedemote.getTrackedEmoteId().getServerId());
        trackedemote.setDeleted(true);
    }

    @Override
    public TrackedEmote loadByEmoteId(Long emoteId, Long serverId) {
        return loadByEmoteIdOptional(emoteId, serverId).orElseThrow(() -> new TrackedEmoteNotFoundException(String.format("Tracked emote %s in server %s not found.", emoteId, serverId)));
    }

    @Override
    public TrackedEmote loadByEmote(Emote emote) {
        return loadByEmoteId(emote.getIdLong(), emote.getGuild().getIdLong());
    }

    @Override
    public boolean trackedEmoteExists(Long emoteId, Long serverId) {
        return loadByEmoteIdOptional(emoteId, serverId).isPresent();
    }

    @Override
    public TrackedEmote loadByTrackedEmoteServer(ServerSpecificId trackedEmoteServer) {
        return loadByEmoteId(trackedEmoteServer.getId(), trackedEmoteServer.getServerId());
    }

    @Override
    public Optional<TrackedEmote> loadByEmoteIdOptional(Long emoteId, Long serverId) {
        return repository.findById(new ServerSpecificId(serverId, emoteId));
    }

    @Override
    public List<TrackedEmote> getAllActiveTrackedEmoteForServer(AServer server) {
        return getAllActiveTrackedEmoteForServer(server.getId());
    }

    @Override
    public List<TrackedEmote> getAllActiveTrackedEmoteForServer(Long serverId) {
        return repository.findByTrackedEmoteId_ServerIdAndDeletedFalseAndExternalFalse(serverId);
    }

    @Override
    public List<TrackedEmote> getTrackedEmoteForServer(Long serverId, Boolean showTrackingDisabledEmotes) {
        if(showTrackingDisabledEmotes) {
            return repository.findByTrackedEmoteId_ServerId(serverId);
        } else {
            return repository.findByTrackedEmoteId_ServerIdAndTrackingEnabledTrue(serverId);
        }
    }

    @Override
    public void changeName(TrackedEmote emote, String newName) {
        log.info("Changing name of emote {} in server {}.", emote.getTrackedEmoteId().getId(), emote.getTrackedEmoteId().getServerId());
        emote.setEmoteName(newName);
    }

    @Override
    public void disableTrackedEmote(TrackedEmote emote) {
        log.info("Disabling tracking for tracked emote {} in server {}.", emote.getTrackedEmoteId().getId(), emote.getTrackedEmoteId().getServerId());
        emote.setTrackingEnabled(false);
    }

    @Override
    public void enableTrackedEmote(TrackedEmote emote) {
        log.info("Enabling tracking for tracked emote {} in server {}.", emote.getTrackedEmoteId().getId(), emote.getTrackedEmoteId().getServerId());
        emote.setTrackingEnabled(true);
    }

    @Override
    public void deleteTrackedEmote(TrackedEmote emote) {
        log.info("Deleting tracked emote {} in server {}.", emote.getTrackedEmoteId().getId(), emote.getTrackedEmoteId().getServerId());
        repository.delete(emote);
    }

}
