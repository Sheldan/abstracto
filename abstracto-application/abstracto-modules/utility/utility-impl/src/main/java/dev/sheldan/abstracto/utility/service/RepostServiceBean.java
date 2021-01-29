package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.models.AServerAChannelAUser;
import dev.sheldan.abstracto.core.models.ServerChannelMessageUser;
import dev.sheldan.abstracto.core.models.cache.CachedAttachment;
import dev.sheldan.abstracto.core.models.cache.CachedEmbed;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.*;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.FileUtils;
import dev.sheldan.abstracto.utility.config.features.RepostDetectionFeatureMode;
import dev.sheldan.abstracto.utility.config.features.UtilityFeature;
import dev.sheldan.abstracto.utility.converter.RepostLeaderBoardConverter;
import dev.sheldan.abstracto.utility.models.RepostLeaderboardEntryModel;
import dev.sheldan.abstracto.utility.models.database.PostedImage;
import dev.sheldan.abstracto.utility.models.database.Repost;
import dev.sheldan.abstracto.utility.models.database.result.RepostLeaderboardResult;
import dev.sheldan.abstracto.utility.service.management.PostedImageManagement;
import dev.sheldan.abstracto.utility.service.management.RepostManagementService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class RepostServiceBean implements RepostService {

    public static final Integer LEADER_BOARD_PAGE_SIZE = 5;
    @Autowired
    private HttpService httpService;

    @Autowired
    private HashService hashService;

    @Autowired
    private FileUtils fileUtils;

    @Autowired
    private PostedImageManagement postedImageManagement;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private ReactionService reactionService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private RepostManagementService repostManagementService;

    @Autowired
    private RepostLeaderBoardConverter leaderBoardConverter;

    @Autowired
    private RepostServiceBean self;

    public static final List<String> NUMBER_EMOJI = Arrays.asList("\u0031\u20e3", "\u0032\u20e3", "\u0033\u20e3",
            "\u0034\u20e3", "\u0035\u20e3", "\u0036\u20e3",
            "\u0037\u20e3", "\u0038\u20e3", "\u0039\u20e3");

    public static final String REPOST_CHECK_CHANNEL_GROUP_TYPE = "repostDetection";

    // any embedded post will create an repost instance with a position higher than this
    public static final Integer EMBEDDED_LINK_POSITION_START_INDEX = 1000;
    public static final String REPOST_MARKER_EMOTE_KEY = "repostMarker";

    @Override
    public boolean isRepost(CachedMessage message, CachedEmbed messageEmbed, Integer index) {
        return getRepostFor(message, messageEmbed, index).isPresent();
    }

    @Override
    public Optional<PostedImage> getRepostFor(CachedMessage message, CachedEmbed messageEmbed, Integer embedIndex) {
        if(messageEmbed.getCachedThumbnail() == null && messageEmbed.getCachedImageInfo() == null) {
            return Optional.empty();
        }
        String urlToUse = null;
        if(messageEmbed.getCachedThumbnail() != null) {
            urlToUse = messageEmbed.getCachedThumbnail().getProxyUrl();
        } else if (messageEmbed.getCachedImageInfo() != null) {
            urlToUse = messageEmbed.getCachedImageInfo().getProxyUrl();
        }
        ServerChannelMessageUser serverChannelMessageUser = ServerChannelMessageUser
                .builder()
                .serverId(message.getServerId())
                .channelId(message.getChannelId())
                .userId(message.getAuthor().getAuthorId())
                .messageId(message.getMessageId())
                .build();
        return checkForDuplicates(serverChannelMessageUser, EMBEDDED_LINK_POSITION_START_INDEX + embedIndex, urlToUse);
    }

    @Override
    public Optional<PostedImage> getRepostFor(Message message, MessageEmbed messageEmbed, Integer embedIndex) {
        if(messageEmbed.getThumbnail() == null && messageEmbed.getImage() == null) {
            return Optional.empty();
        }
        String urlToUse = null;
        if(messageEmbed.getThumbnail() != null) {
            urlToUse = messageEmbed.getThumbnail().getProxyUrl();
        } else if (messageEmbed.getImage() != null) {
            urlToUse = messageEmbed.getImage().getProxyUrl();
        }
        ServerChannelMessageUser serverChannelMessageUser = ServerChannelMessageUser
                .builder()
                .serverId(message.getGuild().getIdLong())
                .channelId(message.getChannel().getIdLong())
                .userId(message.getAuthor().getIdLong())
                .messageId(message.getIdLong())
                .build();
        return checkForDuplicates(serverChannelMessageUser, EMBEDDED_LINK_POSITION_START_INDEX + embedIndex, urlToUse);
    }

    private Optional<PostedImage> checkForDuplicates(ServerChannelMessageUser serverChannelMessageUser, Integer index, String fileUrl)  {
        String fileHash = calculateHashForPost(fileUrl, serverChannelMessageUser.getServerId());
        AServer aServer = serverManagementService.loadServer(serverChannelMessageUser.getServerId());
        Optional<PostedImage> potentialRepost = postedImageManagement.getPostWithHash(fileHash, aServer);
        if(potentialRepost.isPresent()) {
            PostedImage existingRepost = potentialRepost.get();
            return !existingRepost.getPostId().getMessageId().equals(serverChannelMessageUser.getMessageId()) ? Optional.of(existingRepost) : Optional.empty();
        } else {
            AUserInAServer aUserInAServer = userInServerManagementService.loadOrCreateUser(serverChannelMessageUser.getServerId(), serverChannelMessageUser.getUserId());
            AServerAChannelAUser cause = AServerAChannelAUser
                    .builder()
                    .aUserInAServer(aUserInAServer)
                    .channel(channelManagementService.loadChannel(serverChannelMessageUser.getChannelId()))
                    .guild(aServer)
                    .user(aUserInAServer.getUserReference())
                    .build();
            postedImageManagement.createPost(cause, serverChannelMessageUser.getMessageId(), fileHash, index);
            return Optional.empty();
        }
    }

    @Override
    public boolean isRepost(CachedMessage message, CachedAttachment attachment, Integer index) {
       return getRepostFor(message, attachment, index).isPresent();
    }

    @Override
    public Optional<PostedImage> getRepostFor(CachedMessage message, CachedAttachment attachment, Integer index) {
        ServerChannelMessageUser serverChannelMessageUser = ServerChannelMessageUser
                .builder()
                .serverId(message.getServerId())
                .channelId(message.getChannelId())
                .userId(message.getAuthor().getAuthorId())
                .messageId(message.getMessageId())
                .build();
        return checkForDuplicates(serverChannelMessageUser, index, attachment.getProxyUrl());
    }

    @Override
    public String calculateHashForPost(String url, Long serverId) {
        File downloadedFile = null;
        try {
            if(featureModeService.featureModeActive(UtilityFeature.REPOST_DETECTION, serverId, RepostDetectionFeatureMode.DOWNLOAD)) {
                downloadedFile = httpService.downloadFileToTempFile(url);
                return hashService.sha256HashFileContent(downloadedFile);
            } else {
                return hashService.sha256HashString(url);
            }
        } catch (IOException e) {
            log.error("Failed to download attachment for repost check.", e);
        } finally {
            if(downloadedFile != null) {
                try {
                    fileUtils.safeDelete(downloadedFile);
                } catch (IOException e) {
                    log.error("Failed to delete downloaded repost check file.", e);
                }
            }
        }
        return null;
    }

    @Override
    public void processMessageAttachmentRepostCheck(CachedMessage message) {
        boolean canThereBeMultipleReposts = message.getAttachments().size() > 1;
        for (int imageIndex = 0; imageIndex < message.getAttachments().size(); imageIndex++) {
            executeRepostCheckForAttachment(message, message.getAttachments().get(imageIndex), imageIndex, canThereBeMultipleReposts);
        }
    }

    private void executeRepostCheckForAttachment(CachedMessage message, CachedAttachment attachment, Integer index, boolean moreRepostsPossible) {
        Optional<PostedImage> originalPostOptional = getRepostFor(message, attachment, index);
        ServerChannelMessageUser serverChannelMessageUser = ServerChannelMessageUser
                .builder()
                .serverId(message.getServerId())
                .channelId(message.getChannelId())
                .userId(message.getAuthor().getAuthorId())
                .messageId(message.getMessageId())
                .build();
        originalPostOptional.ifPresent(postedImage -> markMessageAndPersist(serverChannelMessageUser, index, moreRepostsPossible, postedImage));
    }

    private void markMessageAndPersist(ServerChannelMessageUser messageUser, Integer index, boolean moreRepostsPossible, PostedImage originalPost) {
        log.info("Detected repost in message embed {} of message {} in channel {} in server {}.", index, messageUser.getMessageId(), messageUser.getChannelId(), messageUser.getServerId());
        CompletableFuture<Void> markerFuture = reactionService.addReactionToMessageAsync(REPOST_MARKER_EMOTE_KEY, messageUser.getServerId(), messageUser.getChannelId(), messageUser.getMessageId());
        CompletableFuture<Void> counterFuture;
        if (moreRepostsPossible) {
            counterFuture = reactionService.addDefaultReactionToMessageAsync(NUMBER_EMOJI.get(index), messageUser.getServerId(), messageUser.getChannelId(), messageUser.getMessageId());
        } else {
            counterFuture = CompletableFuture.completedFuture(null);
        }
        Long messageId = originalPost.getPostId().getMessageId();
        Integer position = originalPost.getPostId().getPosition();
        Long serverId = messageUser.getServerId();
        Long userId = messageUser.getUserId();
        CompletableFuture.allOf(markerFuture, counterFuture).thenAccept(unused ->
            self.persistRepost(messageId, position, serverId, userId)
        );
    }

    @Transactional
    public void persistRepost(Long messageId, Integer position, Long serverId, Long userId) {
        PostedImage postedImage = postedImageManagement.getPostFromMessageAndPosition(messageId, position);
        AUserInAServer userInAServer = userInServerManagementService.loadOrCreateUser(serverId, userId);
        Optional<Repost> existingPost = repostManagementService.findRepostOptional(postedImage, userInAServer);
        if(existingPost.isPresent()) {
            Repost previousRepost = existingPost.get();
            existingPost.get().setCount(previousRepost.getCount() + 1);
        } else {
            repostManagementService.createRepost(postedImage, userInAServer);
        }
    }

    @Override
    public void processMessageEmbedsRepostCheck(List<CachedEmbed> embeds, CachedMessage message) {
        boolean canThereBeMultipleReposts = embeds.size() > 1 || !message.getAttachments().isEmpty();
        for (int imageIndex = 0; imageIndex < embeds.size(); imageIndex++) {
            executeRepostCheckForMessageEmbed(message, embeds.get(imageIndex), imageIndex + message.getAttachments().size(), canThereBeMultipleReposts);
        }
    }

    @Override
    public void processMessageEmbedsRepostCheck(List<MessageEmbed> embeds, Message message) {
        boolean canThereBeMultipleReposts = embeds.size() > 1 || !message.getAttachments().isEmpty();
        for (int imageIndex = 0; imageIndex < embeds.size(); imageIndex++) {
            executeRepostCheckForMessageEmbed(message, embeds.get(imageIndex), imageIndex + message.getAttachments().size(), canThereBeMultipleReposts);
        }
    }

    @Override
    public CompletableFuture<List<RepostLeaderboardEntryModel>> retrieveRepostLeaderboard(Guild guild, Integer page) {
        AServer server = serverManagementService.loadServer(guild.getIdLong());
        List<RepostLeaderboardResult> topRepostingUsersOfServer = repostManagementService.findTopRepostingUsersOfServer(server, page, LEADER_BOARD_PAGE_SIZE);
        return leaderBoardConverter.fromLeaderBoardResults(topRepostingUsersOfServer);
    }

    @Override
    public void purgeReposts(AUserInAServer userInAServer) {
        repostManagementService.deleteRepostsFromUser(userInAServer);
    }

    @Override
    public void purgeReposts(Guild guild) {
        AServer server = serverManagementService.loadServer(guild.getIdLong());
        repostManagementService.deleteRepostsFromServer(server);
    }

    private void executeRepostCheckForMessageEmbed(CachedMessage message, CachedEmbed messageEmbed, Integer index, boolean moreRepostsPossible) {
        Optional<PostedImage> originalPostOptional = getRepostFor(message, messageEmbed, index);
        ServerChannelMessageUser serverChannelMessageUser = ServerChannelMessageUser
                .builder()
                .serverId(message.getServerId())
                .channelId(message.getChannelId())
                .userId(message.getAuthor().getAuthorId())
                .messageId(message.getMessageId())
                .build();
        originalPostOptional.ifPresent(postedImage -> markMessageAndPersist(serverChannelMessageUser, index, moreRepostsPossible, postedImage));
    }

    private void executeRepostCheckForMessageEmbed(Message message, MessageEmbed messageEmbed, Integer index, boolean moreRepostsPossible) {
        Optional<PostedImage> originalPostOptional = getRepostFor(message, messageEmbed, index);
        ServerChannelMessageUser serverChannelMessageUser = ServerChannelMessageUser
                .builder()
                .serverId(message.getGuild().getIdLong())
                .channelId(message.getChannel().getIdLong())
                .userId(message.getAuthor().getIdLong())
                .messageId(message.getIdLong())
                .build();
        originalPostOptional.ifPresent(postedImage -> markMessageAndPersist(serverChannelMessageUser, index, moreRepostsPossible, postedImage));
    }
}
