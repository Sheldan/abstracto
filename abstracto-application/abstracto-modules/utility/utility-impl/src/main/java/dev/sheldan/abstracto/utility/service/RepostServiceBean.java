package dev.sheldan.abstracto.utility.service;

import dev.sheldan.abstracto.core.models.AServerAChannelAUser;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.service.HashService;
import dev.sheldan.abstracto.core.service.HttpService;
import dev.sheldan.abstracto.core.service.MessageService;
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
import net.dv8tion.jda.api.entities.*;
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
    private MessageService messageService;

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
    public boolean isRepost(Message message, MessageEmbed messageEmbed, Integer index) {
        return getRepostFor(message, messageEmbed, index).isPresent();
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
        return checkForDuplicates(message, EMBEDDED_LINK_POSITION_START_INDEX + embedIndex, urlToUse);
    }

    private Optional<PostedImage> checkForDuplicates(Message message, Integer index, String fileUrl)  {
        String fileHash = calculateHashForPost(fileUrl, message.getGuild().getIdLong());
        AServer aServer = serverManagementService.loadServer(message.getGuild().getIdLong());
        Optional<PostedImage> potentialRepost = postedImageManagement.getPostWithHash(fileHash, aServer);
        if(potentialRepost.isPresent()) {
            PostedImage existingRepost = potentialRepost.get();
            return existingRepost.getPostId().getMessageId() != message.getIdLong() ? Optional.of(existingRepost) : Optional.empty();
        } else {
            AUserInAServer aUserInAServer = userInServerManagementService.loadUser(message.getMember());
            AServerAChannelAUser cause = AServerAChannelAUser
                    .builder()
                    .aUserInAServer(aUserInAServer)
                    .channel(channelManagementService.loadChannel(message.getTextChannel().getIdLong()))
                    .guild(aServer)
                    .user(aUserInAServer.getUserReference())
                    .build();
            postedImageManagement.createPost(cause, message, fileHash, index);
            return Optional.empty();
        }
    }

    @Override
    public boolean isRepost(Message message, Message.Attachment attachment, Integer index) {
       return getRepostFor(message, attachment, index).isPresent();
    }

    @Override
    public Optional<PostedImage> getRepostFor(Message message, Message.Attachment attachment, Integer index) {
        return checkForDuplicates(message, index, attachment.getProxyUrl());
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
    public void processMessageAttachmentRepostCheck(Message message) {
        boolean canThereBeMultipleReposts = message.getAttachments().size() > 1;
        for (int imageIndex = 0; imageIndex < message.getAttachments().size(); imageIndex++) {
            executeRepostCheckForAttachment(message, message.getAttachments().get(imageIndex), imageIndex, canThereBeMultipleReposts);
        }
    }

    private void executeRepostCheckForAttachment(Message message, Message.Attachment attachment, Integer index, boolean moreRepostsPossible) {
        Optional<PostedImage> originalPostOptional = getRepostFor(message, attachment, index);
        originalPostOptional.ifPresent(postedImage -> markMessageAndPersist(message, index, moreRepostsPossible, postedImage));
    }

    private void markMessageAndPersist(Message message, Integer index, boolean moreRepostsPossible, PostedImage originalPost) {
        log.info("Detected repost in message embed {} of message {} in channel {} in server {}.", index, message.getIdLong(), message.getTextChannel().getIdLong(), message.getGuild().getIdLong());
        CompletableFuture<Void> markerFuture = messageService.addReactionToMessageWithFuture(REPOST_MARKER_EMOTE_KEY, message.getGuild().getIdLong(), message);
        CompletableFuture<Void> counterFuture;
        if (moreRepostsPossible) {
            counterFuture = messageService.addDefaultReactionToMessageAsync(NUMBER_EMOJI.get(index), message);
        } else {
            counterFuture = CompletableFuture.completedFuture(null);
        }
        Long messageId = originalPost.getPostId().getMessageId();
        Integer position = originalPost.getPostId().getPosition();
        Long serverId = message.getGuild().getIdLong();
        Long userId = message.getAuthor().getIdLong();
        CompletableFuture.allOf(markerFuture, counterFuture).thenAccept(unused ->
            self.persistRepost(messageId, position, serverId, userId)
        );
    }

    @Transactional
    public void persistRepost(Long messageId, Integer position, Long serverId, Long userId) {
        PostedImage postedImage = postedImageManagement.getPostFromMessageAndPosition(messageId, position);
        AUserInAServer userInAServer = userInServerManagementService.loadUser(serverId, userId);
        Optional<Repost> existingPost = repostManagementService.findRepostOptional(postedImage, userInAServer);
        if(existingPost.isPresent()) {
            Repost previousRepost = existingPost.get();
            existingPost.get().setCount(previousRepost.getCount() + 1);
        } else {
            repostManagementService.createRepost(postedImage, userInAServer);
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

    private void executeRepostCheckForMessageEmbed(Message message, MessageEmbed messageEmbed, Integer index, boolean moreRepostsPossible) {
        Optional<PostedImage> originalPostOptional = getRepostFor(message, messageEmbed, index);
        originalPostOptional.ifPresent(postedImage -> markMessageAndPersist(message, index, moreRepostsPossible, postedImage));
    }
}
