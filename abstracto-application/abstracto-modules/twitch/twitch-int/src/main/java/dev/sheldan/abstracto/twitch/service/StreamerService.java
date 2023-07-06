package dev.sheldan.abstracto.twitch.service;

import com.github.twitch4j.helix.domain.Stream;
import com.github.twitch4j.helix.domain.User;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.twitch.model.database.Streamer;
import dev.sheldan.abstracto.twitch.model.template.ListTwitchStreamerResponseModel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public interface StreamerService {
    void createStreamer(String name, GuildMessageChannel targetChannel, Member creator, Member streamerMember);
    void removeStreamer(String name, Guild guild);
    CompletableFutureList<Message> notifyAboutOnlineStream(Stream stream, Streamer streamer, User streamerUser);
    void changeStreamerNotificationToChannel(Streamer streamer, Long channelId);
    void disableNotificationsForStreamer(Streamer streamer, Boolean newState);
    void changeStreamerMemberToUserId(Streamer streamer, Long userId);
    void changeTemplateKeyTo(Streamer streamer, String templateKey);
    ListTwitchStreamerResponseModel getStreamersFromServer(Guild guild);
    void checkAndNotifyAboutOnlineStreamers();
}
