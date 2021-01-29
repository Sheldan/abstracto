package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.metrics.service.CounterMetric;
import dev.sheldan.abstracto.core.metrics.service.MetricService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.moderation.exception.NoMessageFoundException;
import dev.sheldan.abstracto.moderation.models.template.commands.PurgeStatusUpdateModel;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
@Slf4j
public class PurgeServiceBean implements PurgeService {

    public static final int PURGE_MAX_MESSAGES = 100;
    @Autowired
    private MessageService messageService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private MetricService metricService;

    @Autowired
    private ChannelService channelService;

    public static final String MODERATION_PURGE_METRIC = "moderation.purge";

    private static final CounterMetric PURGE_MESSAGE_COUNTER_METRIC =
            CounterMetric
                    .builder()
                    .name(MODERATION_PURGE_METRIC)
                    .build();

    @Override
    public CompletableFuture<Void> purgeMessagesInChannel(Integer amountToDelete, TextChannel channel, Long startId, Member purgedMember) {
        return purgeMessages(amountToDelete, channel, startId, purgedMember, amountToDelete, 0, 0L);
    }

    private CompletableFuture<Void> purgeMessages(Integer amountToDelete, TextChannel channel, Long startId, Member purgedMember, Integer totalCount, Integer currentCount, Long statusMessageId) {

        int toDeleteInThisIteration;
        if(amountToDelete >= PURGE_MAX_MESSAGES){
            toDeleteInThisIteration = PURGE_MAX_MESSAGES;
        } else {
            toDeleteInThisIteration = amountToDelete % PURGE_MAX_MESSAGES;
        }
        metricService.incrementCounter(PURGE_MESSAGE_COUNTER_METRIC, (long) toDeleteInThisIteration);
        log.info("Purging {} this iteration ({}/{}) messages in channel {} in server {}.", toDeleteInThisIteration, currentCount, totalCount, channel.getId(), channel.getGuild().getId());

        CompletableFuture<MessageHistory> historyFuture = channelService.getHistoryOfChannel(channel, startId, toDeleteInThisIteration);
        CompletableFuture<Long> statusMessageFuture = getOrCreatedStatusMessage(channel, totalCount, statusMessageId);

        CompletableFuture<Void> deletionFuture = new CompletableFuture<>();
        CompletableFuture<Void> retrievalFuture = CompletableFuture.allOf(historyFuture, statusMessageFuture);
        retrievalFuture.thenAccept(voidParam -> {
            try {
                List<Message> retrievedHistory = historyFuture.get().getRetrievedHistory();
                List<Message> messagesToDeleteNow = filterMessagesToDelete(retrievedHistory, purgedMember);
                Long currentStatusMessageId = statusMessageFuture.get();
                if(messagesToDeleteNow.size() == 0) {
                    log.warn("No messages found to delete, all were filtered.");
                    deletionFuture.completeExceptionally(new NoMessageFoundException());
                    // TODO move to message service
                    channel.deleteMessageById(currentStatusMessageId).queueAfter(5, TimeUnit.SECONDS);
                    return;
                }
                Message latestMessage = messagesToDeleteNow.get(messagesToDeleteNow.size() - 1);
                log.trace("Deleting {} messages directly", messagesToDeleteNow.size());
                int newCurrentCount = currentCount + messagesToDeleteNow.size();
                int newAmountToDelete = amountToDelete - PURGE_MAX_MESSAGES;
                Consumer<Void> consumer = deletionConsumer(newAmountToDelete, channel, purgedMember, totalCount, newCurrentCount, deletionFuture, currentStatusMessageId, latestMessage);
                if (messagesToDeleteNow.size() > 1) {
                    bulkDeleteMessages(channel, deletionFuture, messagesToDeleteNow, consumer);
                } else if (messagesToDeleteNow.size() == 1) {
                    messageService.deleteMessage(latestMessage).queue(consumer, deletionFuture::completeExceptionally);
                }

            } catch (Exception e) {
                log.warn("Failed to purge messages.", e);
                deletionFuture.completeExceptionally(e);
            }
        }).exceptionally(throwable -> {
            log.warn("Failed to fetch messages.", throwable);
            return null;
        });

        return CompletableFuture.allOf(retrievalFuture, deletionFuture);
    }

    private void bulkDeleteMessages(TextChannel channel, CompletableFuture<Void> deletionFuture, List<Message> messagesToDeleteNow, Consumer<Void> consumer) {
        try {
            channelService.deleteMessagesInChannel(channel, messagesToDeleteNow).queue(consumer, deletionFuture::completeExceptionally);
        } catch (IllegalArgumentException e) {
            channelService.sendTextToChannel(e.getMessage(), channel);
            log.warn("Failed to bulk delete, message was most likely too old to delete by bulk.", e);
            deletionFuture.complete(null);
        }
    }

    private CompletableFuture<Long> getOrCreatedStatusMessage(TextChannel channel, Integer totalCount, Long statusMessageId) {
        CompletableFuture<Long> statusMessageFuture;
        if(statusMessageId == 0) {
            log.trace("Creating new status message in channel {} in server {} because of puring.", channel.getIdLong(), channel.getGuild().getId());
            PurgeStatusUpdateModel model = PurgeStatusUpdateModel.builder().currentlyDeleted(0).totalToDelete(totalCount).build();
            MessageToSend messageToSend = templateService.renderTemplateToMessageToSend("purge_status_update", model);
            statusMessageFuture = messageService.createStatusMessageId(messageToSend, channel);
        } else {
            log.trace("Using existing status message {}.", statusMessageId);
            statusMessageFuture = CompletableFuture.completedFuture(statusMessageId);
        }
        return statusMessageFuture;
    }

    private List<Message> filterMessagesToDelete(List<Message> retrievedHistory, Member purgedMember) {
        long twoWeeksAgo = TimeUtil.getDiscordTimestamp((System.currentTimeMillis() - (14 * 24 * 60 * 60 * 1000)));
        log.trace("Filtering messages older than {}.", twoWeeksAgo);
        List<Message> messagesToDeleteNow = new ArrayList<>();
        for (Message messageObj : retrievedHistory) {
            if (MiscUtil.parseSnowflake(messageObj.getId()) > twoWeeksAgo) {
                if(purgedMember != null) {
                    if(messageObj.getAuthor().getIdLong() == purgedMember.getIdLong()) {
                        log.trace("Message {} is from filtered user {}. Purging.", messageObj.getId(), purgedMember.getIdLong());
                        messagesToDeleteNow.add(messageObj);
                    }
                } else {
                    messagesToDeleteNow.add(messageObj);
                }
            } else {
                log.trace("Message {} was older than {}. Not purging.", messageObj.getId(), twoWeeksAgo);
            }
        }
        return messagesToDeleteNow;
    }

    private Consumer<Void> deletionConsumer(Integer amountToDelete, TextChannel channel, Member purgedMember, Integer totalCount, Integer currentCount, CompletableFuture<Void> deletionFuture, Long currentStatusMessageId, Message earliestMessage) {
        return aVoid -> {
            if (amountToDelete >= 1) {
                log.trace("Still more than 1 message to delete. Continuing.");
                purgeMessages(amountToDelete, channel, earliestMessage.getIdLong(), purgedMember, totalCount, currentCount, currentStatusMessageId).whenComplete((avoid, throwable) -> {
                            if (throwable != null) {
                                deletionFuture.completeExceptionally(throwable);
                            } else {
                                deletionFuture.complete(null);
                            }
                        }
                );
            } else {
                log.trace("Completed purging of {} messages.", totalCount);
                // Todo Move to message service
                channel.deleteMessageById(currentStatusMessageId).queueAfter(5, TimeUnit.SECONDS);
                deletionFuture.complete(null);
            }
            log.trace("Setting status for {} out of {}", currentCount, totalCount);
            PurgeStatusUpdateModel finalUpdateModel = PurgeStatusUpdateModel.builder().currentlyDeleted(currentCount).totalToDelete(totalCount).build();
            MessageToSend finalUpdateMessage = templateService.renderTemplateToMessageToSend("purge_status_update", finalUpdateModel);
            messageService.updateStatusMessage(channel, currentStatusMessageId, finalUpdateMessage);
        };
    }

    @Override
    public CompletableFuture<Void> purgeMessagesInChannel(Integer count, TextChannel channel, Message origin, Member purgingRestriction) {
        return purgeMessagesInChannel(count, channel, origin.getIdLong(), purgingRestriction);
    }

    @PostConstruct
    public void postConstruct() {
        metricService.registerCounter(PURGE_MESSAGE_COUNTER_METRIC, "Amount of messages deleted by purge.");
    }
}
