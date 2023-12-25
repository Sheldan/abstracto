package dev.sheldan.abstracto.twitch.service;

import com.github.twitch4j.helix.domain.Stream;
import com.github.twitch4j.helix.domain.User;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.display.ChannelDisplay;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.CompletableFutureList;
import dev.sheldan.abstracto.twitch.config.TwitchFeatureConfig;
import dev.sheldan.abstracto.twitch.config.TwitchFeatureDefinition;
import dev.sheldan.abstracto.twitch.config.TwitchFeatureMode;
import dev.sheldan.abstracto.twitch.config.TwitchPostTarget;
import dev.sheldan.abstracto.twitch.exception.StreamerExistsException;
import dev.sheldan.abstracto.twitch.exception.StreamerNotFoundInServerException;
import dev.sheldan.abstracto.twitch.model.database.StreamSessionSection;
import dev.sheldan.abstracto.twitch.model.database.Streamer;
import dev.sheldan.abstracto.twitch.model.database.StreamSession;
import dev.sheldan.abstracto.twitch.model.template.*;
import dev.sheldan.abstracto.twitch.service.management.StreamSessionManagementService;
import dev.sheldan.abstracto.twitch.service.management.StreamSessionSectionManagementService;
import dev.sheldan.abstracto.twitch.service.management.StreamerManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class StreamerServiceBean implements StreamerService {

    @Autowired
    private TwitchService twitchService;

    @Autowired
    private StreamerManagementService streamerManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private TwitchFeatureConfig twitchFeatureConfig;

    @Autowired
    private StreamSessionSectionService streamSessionSectionService;

    @Autowired
    private StreamSessionManagementService streamSessionManagementService;

    @Autowired
    private StreamSessionSectionManagementService streamSessionSectionManagementService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private StreamerServiceBean self;

    private static final String DEFAULT_NOTIFICATION_TEMPLATE = "twitch_streamer_go_live_notification";
    private static final String WENT_OFF_LINE_MESSAGE_TEMPLATE = "twitch_streamer_went_offline_message";

    @Override
    public void createStreamer(String name, GuildMessageChannel targetChannel, Member creator, Member streamerMember) {
        User streamerByName = twitchService.getStreamerByName(name);
        Optional<Stream> optionalStream = twitchService.getStreamOfUser(streamerByName.getId());
        AServer server = serverManagementService.loadServer(creator.getGuild().getIdLong());
        if(streamerManagementService.streamerExistsInServerByID(streamerByName.getId(), server)) {
            throw new StreamerExistsException();
        }
        AChannel aTargetChannel = null;
        if(targetChannel != null) {
            aTargetChannel = channelManagementService.loadChannel(targetChannel);
        }
        AUserInAServer creatorUser = userInServerManagementService.loadOrCreateUser(creator);
        AUserInAServer streamerUser = null;
        if(streamerMember != null) {
            streamerUser = userInServerManagementService.loadOrCreateUser(streamerMember);
        }
        Streamer createdStreamer = streamerManagementService.createStreamer(streamerByName.getId(), name, aTargetChannel, creatorUser, streamerUser, optionalStream.isPresent());
        log.info("User {} created streamer {} in server {}.", creatorUser.getUserReference().getId(),
                createdStreamer.getId(), creatorUser.getServerReference().getId());
        optionalStream.ifPresent(stream -> createdStreamer.setCurrentGameId(stream.getGameId()));
    }

    @Override
    public void removeStreamer(String name, Guild guild) {
        AServer server = serverManagementService.loadServer(guild.getIdLong());
        Streamer streamerInServerByName = streamerManagementService.getStreamerInServerByName(name, server).orElseThrow(StreamerNotFoundInServerException::new);
        log.info("Removing streamer {} for server {}.", streamerInServerByName.getId(), guild.getIdLong());
        streamSessionSectionManagementService.deleteSectionsOfStreamer(streamerInServerByName);
        streamSessionManagementService.deleteSessionsOfStreamer(streamerInServerByName);
        streamerManagementService.removeStreamer(streamerInServerByName);
    }

    @Override
    public CompletableFutureList<Message> notifyAboutOnlineStream(Stream stream, Streamer streamer, User streamerUser) {
        GoLiveNotificationModel model = GoLiveNotificationModel
                .builder()
                .channelName(stream.getUserName())
                .mature(stream.isMature())
                .currentSection(StreamSectionDisplay.fromStream(stream))
                .streamerAvatarURL(streamerUser.getProfileImageUrl())
                .randomString(RandomStringUtils.randomAlphabetic(15))
                .streamURL(formatStreamUrl(stream.getUserName()))
                .build();
        MessageToSend messagetoSend;
        if(streamer.getTemplateKey() == null) {
            messagetoSend = templateService.renderEmbedTemplate(DEFAULT_NOTIFICATION_TEMPLATE, model, streamer.getServer().getId());
        } else {
            messagetoSend = templateService.renderEmbedTemplate(streamer.getTemplateKey(), model, streamer.getServer().getId());
        }
        if(Boolean.FALSE.equals(streamer.getShowNotifications())) {
            log.info("Not announcing streamer {} in server {}.", streamer.getId(), streamer.getServer().getId());
            return new CompletableFutureList<>(Arrays.asList(CompletableFuture.completedFuture(null)));
        }
        if(streamer.getNotificationChannel() != null) {
            log.info("Announcing streamer {} in server {} to channel {}.", streamer.getId(), streamer.getServer().getId(), streamer.getNotificationChannel().getId());
            List<CompletableFuture<Message>> futures = channelService.sendMessageEmbedToSendToAChannel(messagetoSend, streamer.getNotificationChannel());
            return new CompletableFutureList<>(futures);
        } else {
            log.info("Announcing streamer {} in server {}.", streamer.getId(), streamer.getServer().getId());
            List<CompletableFuture<Message>> futures = postTargetService.sendEmbedInPostTarget(messagetoSend, TwitchPostTarget.TWITCH_LIVE_NOTIFICATION, streamer.getServer().getId());
            return new CompletableFutureList<>(futures);
        }
    }

    public CompletableFuture<Void> updateExistingNotification(Stream stream, Streamer streamer, User streamerUser) {
        List<StreamSectionDisplay> pastSections = streamer
                .getCurrentSession()
                .getSections()
                .stream()
                .sorted(Comparator.comparing(StreamSessionSection::getId).reversed())
                .map(StreamSectionDisplay::fromSection)
                .toList();
        GoLiveNotificationModel model = GoLiveNotificationModel
                .builder()
                .channelName(stream.getUserName())
                .mature(stream.isMature())
                .randomString(RandomStringUtils.randomAlphabetic(15))
                .currentSection(StreamSectionDisplay.fromStream(stream))
                .streamerAvatarURL(streamerUser.getProfileImageUrl())
                .pastSections(pastSections)
                .streamURL(formatStreamUrl(stream.getUserName()))
                .build();
        MessageToSend messagetoSend;
        if(streamer.getTemplateKey() == null) {
            messagetoSend = templateService.renderEmbedTemplate(DEFAULT_NOTIFICATION_TEMPLATE, model, streamer.getServer().getId());
        } else {
            messagetoSend = templateService.renderEmbedTemplate(streamer.getTemplateKey(), model, streamer.getServer().getId());
        }
        if(Boolean.FALSE.equals(streamer.getShowNotifications())) {
            log.info("Not editing notification, because notifications are disabled for streamer {} in server {}.", streamer.getId(), streamer.getServer().getId());
            return CompletableFuture.completedFuture(null);
        }
        StreamSession currentSession = streamer.getCurrentSession();
        log.info("Updating notification {} for streamer {} in server {} in channel {}.", currentSession.getId(), streamer.getId(), streamer.getServer().getId(), currentSession.getChannel().getId());
        return channelService.editMessageInAChannelFuture(messagetoSend, streamer.getServer().getId(), currentSession.getChannel().getId(), currentSession.getId())
                .thenAccept(message -> {});
    }

    @Override
    public void changeStreamerNotificationToChannel(Streamer streamer, Long channelId) {
        log.info("Changing notification channel of streamer {} to channel {} in server {}.", streamer.getId(), channelId, streamer.getServer().getId());
        if(channelId != null) {
            AChannel channel = channelManagementService.loadChannel(channelId);
            streamer.setNotificationChannel(channel);
        } else {
            streamer.setNotificationChannel(null);
        }
    }

    @Override
    public void disableNotificationsForStreamer(Streamer streamer, Boolean newState) {
        log.info("Setting notifications of streamer {} to {} in server {}.", streamer.getId(), newState, streamer.getServer().getId());
        streamer.setShowNotifications(newState);
    }

    @Override
    public void changeStreamerMemberToUserId(Streamer streamer, Long userId) {
        log.info("Changing user id of streamer {} to channel {} in server {}.", streamer.getId(), userId, streamer.getServer().getId());
        if(userId != null) {
            AUserInAServer user = userInServerManagementService.loadOrCreateUser(streamer.getServer(), userId);
            streamer.setStreamerUser(user);
        } else {
            streamer.setStreamerUser(null);
        }
    }

    @Override
    public void changeTemplateKeyTo(Streamer streamer, String templateKey) {
        log.info("Changing template key of streamer {} in server {}.", streamer.getId(), streamer.getServer().getId());
        streamer.setTemplateKey(templateKey);
    }

    @Override
    public ListTwitchStreamerResponseModel getStreamersFromServer(Guild guild) {
        log.info("Loading streamers into a model for server {}.", guild.getIdLong());
        AServer server = serverManagementService.loadServer(guild.getIdLong());
        List<Streamer> streamers = streamerManagementService.getStreamersForServer(server);
        GuildMessageChannel postTargetChannel = postTargetService.getPostTargetChannel(TwitchPostTarget.TWITCH_LIVE_NOTIFICATION, server.getId()).orElse(null);
        List<TwitchStreamerDisplayModel> models = streamers
                .stream()
                .map(streamer -> {
            GuildMessageChannel notificationChannel;
            if(streamer.getNotificationChannel() != null) {
                notificationChannel = channelService.getMessageChannelFromServer(server.getId(), streamer.getNotificationChannel().getId());
            } else {
                notificationChannel = postTargetChannel;
            }
            return TwitchStreamerDisplayModel
                    .builder()
                    .name(streamer.getName())
                    .targetChannel(ChannelDisplay.fromChannel(notificationChannel))
                    .streamerURL(formatStreamUrl(streamer.getName()))
                    .showNotifications(streamer.getShowNotifications())
                    .build();

        }).collect(Collectors.toList());
        return ListTwitchStreamerResponseModel
                .builder()
                .streamers(models)
                .build();
    }

    private String formatStreamUrl(String name) {
        return String.format("https://twitch.tv/%s", name);
    }

    @Override
    @Transactional
    public void checkAndNotifyAboutOnlineStreamers() {
        log.info("Checking if stream notifications need to be sent.");
        List<AServer> servers = serverManagementService.getAllServers();
        List<AServer> serversWithEnabledFeature = servers
                .stream()
                .filter(server -> featureFlagService.isFeatureEnabled(twitchFeatureConfig, server))
                .toList();
        log.debug("Searching through {} servers for twitch notifications and {} have twitch feature enabled.", servers.size(), serversWithEnabledFeature.size());
        serversWithEnabledFeature.forEach(server -> {
            List<Streamer> streamersInServer = streamerManagementService.getStreamersForServer(server);
            Map<Long, Streamer> streamerIdMap = streamersInServer
                    .stream()
                    .collect(Collectors.toMap(Streamer::getId, Function.identity()));
            Map<String, Streamer> streamerMap = streamersInServer
                    .stream()
                    .collect(Collectors.toMap(Streamer::getUserId, Function.identity()));
            log.debug("Found {} streamers for server {}.", streamersInServer.size(), server.getId());
            if(streamersInServer.isEmpty()) {
                return;
            }
            List<String> userIds = streamersInServer
                    .stream()
                    .map(Streamer::getUserId)
                    .distinct()
                    .toList();
            List<Stream> streamsOfUsers = twitchService.getStreamsByUserIds(userIds);
            Set<Long> onlineStreamers = new HashSet<>();
            Map<Long, Boolean> updateNotificationFlagValues = new HashMap<>();
            streamsOfUsers.forEach(stream -> self.processOnlineStreamer(server, streamerMap, onlineStreamers, updateNotificationFlagValues, stream));
            Set<Long> allStreamersInServer = streamerIdMap.keySet();
            allStreamersInServer.removeAll(onlineStreamers); // then we have those that went offline
            Map<Long, Boolean> deleteFlagValues = new HashMap<>();
            allStreamersInServer.forEach(streamerId -> {
                Streamer streamer = streamerIdMap.get(streamerId);
                self.processOfflineStreamer(server, streamer, deleteFlagValues);
            });
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processOfflineStreamer(AServer server, Streamer streamer, Map<Long, Boolean> deleteFlagValues) {
        Long streamerId = streamer.getId();
        if(!streamer.getOnline()) {
            return;
        }
        log.info("Streamer {} went offline.", streamerId);
        if (deleteFlagValues.computeIfAbsent(streamer.getServer().getId(),
                aLong -> featureModeService.featureModeActive(TwitchFeatureDefinition.TWITCH, aLong, TwitchFeatureMode.DELETE_NOTIFICATION))) {
            Long channelId = streamer.getCurrentSession().getChannel().getId();
            Long messageId = streamer.getCurrentSession().getId();
            messageService.deleteMessageInChannelInServer(streamer.getServer().getId(), channelId, messageId).thenAccept(unused -> {
                log.info("Deleted notification message for streamer {}", streamerId);
            }).exceptionally(throwable -> {
                log.warn("Failed to delete notification message for streamer {}", streamerId, throwable);
                return null;
            });
        } else {
            User streamerUser = twitchService.getStreamerById(streamer.getUserId());
            if(streamer.getCurrentSession() == null) {
                log.warn("No session found for streamer {} - nothing to update or delete.", streamer.getId());
                streamer.setCurrentSession(null);
                streamer.setOnline(false);
                streamer.setCurrentGameId(null);
                streamerManagementService.saveStreamer(streamer);
                return;
            }
            List<StreamSectionDisplay> pastSections = streamer
                    .getCurrentSession()
                    .getSections()
                    .stream()
                    .sorted(Comparator.comparing(StreamSessionSection::getId).reversed())
                    .map(StreamSectionDisplay::fromSection)
                    .toList();
            String offlineImageURL = StringUtils.isBlank(streamerUser.getOfflineImageUrl()) ? null : streamerUser.getOfflineImageUrl();
            GoOfflineNotificationModel model = GoOfflineNotificationModel
                    .builder()
                    .channelName(streamer.getName())
                    .avatarURL(streamerUser.getProfileImageUrl())
                    .offlineImageURL(offlineImageURL)
                    .pastSections(pastSections)
                    .build();
            log.info("Updating existing notification for streamer {} in server {}.", streamer.getId(), server.getId());
            MessageToSend messageToSend = templateService.renderEmbedTemplate(WENT_OFF_LINE_MESSAGE_TEMPLATE, model, server.getId());
            Long channelId = streamer.getCurrentSession().getChannel().getId();
            Long messageId = streamer.getCurrentSession().getId();
            channelService.editMessageInAChannelFuture(messageToSend, server.getId(), channelId, messageId).thenAccept(message -> {
                log.debug("Successfully updated notification {}.", messageId);
            }).exceptionally(throwable -> {
                log.debug("Failed to update notification {}", messageId, throwable);
                return null;
            });
            if(!streamer.getName().equals(streamerUser.getLogin())) {
                streamer.setName(streamerUser.getLogin());
            }
        }
        streamer.setCurrentSession(null);
        streamer.setOnline(false);
        streamer.setCurrentGameId(null);
        streamerManagementService.saveStreamer(streamer);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processOnlineStreamer(AServer server, Map<String, Streamer> streamerMap, Set<Long> onlineStreamers, Map<Long, Boolean> updateNotificationFlagValues, Stream stream) {
        Streamer streamer = streamerMap.get(stream.getUserId());
        Long streamerId = streamer.getId();
        onlineStreamers.add(streamerId);
        // it could be that the streamer is already online, but we have no sessions yet
        // that is the case if you add an online streamer
        User streamerUser = twitchService.getStreamerById(stream.getUserId());
        StreamSession currentSession = streamer.getCurrentSession();
        if (Boolean.TRUE.equals(streamer.getOnline()) && currentSession != null) {
            // we already know that this streamer is online
            log.debug("Not notifying for streamer {} in server {} - streamer is already online.", streamerId, server.getId());
            if(!stream.getGameId().equals(streamer.getCurrentGameId())) {
                log.info("Streamer {} changed game from {} to {} - storing new section.", streamerId, streamer.getCurrentGameId(), stream.getGameId());
                streamSessionSectionService.createSectionFromStream(currentSession, stream);
                if (updateNotificationFlagValues.computeIfAbsent(streamer.getServer().getId(),
                        aLong -> featureModeService.featureModeActive(TwitchFeatureDefinition.TWITCH, aLong, TwitchFeatureMode.UPDATE_NOTIFICATION))) {
                    log.info("Updating notification is enabled - updating notification for streamer {}.", streamer.getId());
                    Long notificationMessageId = currentSession.getId();
                    Long notificationChannelId = currentSession.getChannel().getId();
                    Long serverId = streamer.getServer().getId();
                    updateExistingNotification(stream, streamer, streamerUser).thenAccept(unused -> {
                        log.info("Updating existing notification {} for server {} in channel {} about streamer {}.",
                                notificationMessageId, serverId, notificationChannelId, streamerId);
                    }).exceptionally(throwable -> {
                        log.error("Failed to update existing notification {} for server {} in channel {} about streamer {}.",
                                notificationMessageId, serverId, notificationChannelId, streamerId, throwable);
                        return null;
                    });
                }
                streamer.setCurrentGameId(stream.getGameId());
            }
        } else if(currentSession == null &&
                !postTargetService.postTargetUsableInServer(TwitchPostTarget.TWITCH_LIVE_NOTIFICATION, server.getId())) {
            // this is the case in which the streamer is online, and we should in theory notify about the online status
            // _but_ the difference is that there is no current session on going - as the sessions in our database are
            // bound to actual notifications sent, and this is the case in which the post target has been disabled.
            // In this case we only update current game if necessary
            // this only really serves as a shortcut to not evaluate and create a full MessageToSend object
            // just to not actually send it
            if(streamer.getCurrentGameId() == null || !streamer.getCurrentGameId().equals(stream.getGameId())) {
                log.info("Game for streamer {} has changed - updating game.", streamerId);
                streamer.setCurrentGameId(stream.getGameId());
            }
            streamer.setOnline(true);
            streamerManagementService.saveStreamer(streamer);
        } else {
            CompletableFutureList<Message> messages = notifyAboutOnlineStream(stream, streamer, streamerUser);
            messages.getMainFuture()
                    .thenAccept(unused -> {
                        Message message = messages.getFutures().get(0).join();
                        try {
                            if(message != null) {
                                self.storeStreamNotificationMessage(message, streamerId, stream);
                            }
                        } catch (Exception exception) {
                            log.error("Failed to update streamer {} in database.", streamerId, exception);
                        }
                    }).exceptionally(throwable -> {
                        log.error("Failed to notify about online stream of streamer {}.", streamerId, throwable);
                        return null;
                    });
        }
    }

    @Transactional
    public void storeStreamNotificationMessage(Message message, Long streamerId, Stream stream) {
        log.info("Storing notification for streamer {} in in server {} in channel {} using message {}.",
                streamerId, message.getGuild().getIdLong(), message.getChannel().getIdLong(), message.getIdLong());
        Optional<Streamer> streamerOptional = streamerManagementService.getStreamerById(streamerId);
        streamerOptional.
                ifPresent(streamer -> {
                    streamer.setCurrentGameId(stream.getGameId());
                    StreamSession session = streamSessionManagementService.startSession(streamer, message.getIdLong(), message.getChannel().getIdLong(), stream);
                    streamer.setCurrentSession(session);
                    streamer.setOnline(true);
                    if(!streamer.getName().equals(stream.getUserLogin())) {
                        streamer.setName(stream.getUserLogin());
                    }
                    streamSessionSectionService.createSectionFromStream(session, stream);
                    streamerManagementService.saveStreamer(streamer);
                });
    }
}
