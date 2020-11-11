package dev.sheldan.abstracto.statistic.emotes.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.statistic.emotes.model.PersistingEmote;
import dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emotes.model.database.embed.TrackedEmoteServer;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Guild;

import java.util.List;
import java.util.Optional;

public interface TrackedEmoteManagementService {
    TrackedEmote createTrackedEmote(Long emoteId, String emoteName, Boolean animated, AServer server);
    TrackedEmote createTrackedEmote(Emote emote, Guild guild);
    TrackedEmote createTrackedEmote(Emote emote, Guild guild, boolean external);
    TrackedEmote createTrackedEmote(Long emoteId, String emoteName, Boolean animated, Boolean tracked, AServer server);
    TrackedEmote createExternalEmote(Long emoteId, String emoteName, String externalUrl, Boolean animated, AServer server);
    TrackedEmote createNotTrackedEmote(Long emoteId, String emoteName, Boolean animated, AServer server);
    TrackedEmote createExternalEmote(PersistingEmote persistingEmote);
    TrackedEmote createExternalEmote(Emote emote, Guild guild);
    void markAsDeleted(Long serverId, Long emoteId);
    void markAsDeleted(TrackedEmote trackedemote);
    TrackedEmote loadByEmoteId(Long emoteId, Long serverId);
    TrackedEmote loadByEmote(Emote emote);
    boolean trackedEmoteExists(Long emoteId, Long serverId);
    TrackedEmote loadByTrackedEmoteServer(TrackedEmoteServer trackedEmoteServer);
    Optional<TrackedEmote> loadByEmoteIdOptional(Long emoteId, Long serverId);
    List<TrackedEmote> getAllActiveTrackedEmoteForServer(AServer server);
    List<TrackedEmote> getAllActiveTrackedEmoteForServer(Long serverId);
    List<TrackedEmote> getTrackedEmoteForServer(Long serverId, Boolean showTrackingDisabledEmotes);
    void changeName(TrackedEmote emote, String name);
    void disableTrackedEmote(TrackedEmote emote);
    void enableTrackedEmote(TrackedEmote emote);
    void deleteTrackedEmote(TrackedEmote emote);
}
