package dev.sheldan.abstracto.moderation.service;

import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.moderation.exception.NoMessageFoundException;
import dev.sheldan.abstracto.moderation.model.template.command.PurgeStatusUpdateModel;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
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

    @Autowired
    private InteractionService interactionService;

    public static final String MODERATION_PURGE_METRIC = "moderation.purge";

    private static final CounterMetric PURGE_MESSAGE_COUNTER_METRIC =
            CounterMetric
                    .builder()
                    .name(MODERATION_PURGE_METRIC)
                    .build();

    @Override
    public CompletableFuture<Void> purgeMessagesInChannel(Integer amountToDelete, GuildMessageChannel channel, Long startId, Member purgedMember) {
        return purgeMessages(amountToDelete, channel, startId, purgedMember, amountToDelete, 0, 0L);
    }

    private CompletableFuture<Void> purgeMessages(Integer amountToDelete, GuildMessageChannel channel, Long startId, Member purgedMember, Integer totalCount, Integer currentCount, Long statusMessageId) {

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
                if(messagesToDeleteNow.isEmpty()) {
                    log.warn("No messages found to delete, all were filtered.");
                    deletionFuture.completeExceptionally(new NoMessageFoundException());
                    // TODO move to message service
                    channel.deleteMessageById(currentStatusMessageId).queueAfter(5, TimeUnit.SECONDS);
                    return;
                }
                Message latestMessage = messagesToDeleteNow.get(messagesToDeleteNow.size() - 1);
                log.debug("Deleting {} messages directly", messagesToDeleteNow.size());
                int newCurrentCount = currentCount + messagesToDeleteNow.size();
                int newAmountToDelete = amountToDelete - PURGE_MAX_MESSAGES;
                Consumer<Void> consumer = deletionConsumer(newAmountToDelete, channel, purgedMember, totalCount, newCurrentCount, deletionFuture, currentStatusMessageId, latestMessage);
                if (messagesToDeleteNow.size() > 1) {
                    bulkDeleteMessages(channel, deletionFuture, messagesToDeleteNow, consumer);
                } else if (messagesToDeleteNow.size() == 1) {
                    messageService.deleteMessageWithAction(latestMessage).queue(consumer, deletionFuture::completeExceptionally);
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

    private void bulkDeleteMessages(GuildMessageChannel channel, CompletableFuture<Void> deletionFuture, List<Message> messagesToDeleteNow, Consumer<Void> consumer) {
        try {
            channelService.deleteMessagesInChannel(channel, messagesToDeleteNow).thenAccept(consumer).exceptionally(throwable -> {
                deletionFuture.completeExceptionally(throwable);
                return null;
            });
        } catch (IllegalArgumentException e) {
            channelService.sendTextToChannel(e.getMessage(), channel);
            log.warn("Failed to bulk delete, message was most likely too old to delete by bulk.", e);
            deletionFuture.complete(null);
        }
    }

    private CompletableFuture<Long> getOrCreatedStatusMessage(GuildMessageChannel channel, Integer totalCount, Long statusMessageId) {
        CompletableFuture<Long> statusMessageFuture;
        if(statusMessageId == 0) {
            Long serverId = channel.getGuild().getIdLong();
            log.debug("Creating new status message in channel {} in server {} because of puring.", channel.getIdLong(), channel.getGuild().getId());
            MessageToSend messageToSend = getStatusMessageToSend(totalCount, serverId, 0);
            statusMessageFuture = messageService.createStatusMessageId(messageToSend, channel);
        } else {
            log.debug("Using existing status message {}.", statusMessageId);
            statusMessageFuture = CompletableFuture.completedFuture(statusMessageId);
        }
        return statusMessageFuture;
    }

    private MessageToSend getStatusMessageToSend(Integer totalCount, Long serverId, Integer currentlyDeleted) {
        PurgeStatusUpdateModel model = PurgeStatusUpdateModel
                .builder()
                .currentlyDeleted(currentlyDeleted)
                .totalToDelete(totalCount)
                .build();
        return templateService.renderTemplateToMessageToSend("purge_status_update", model, serverId);
    }

    private List<Message> filterMessagesToDelete(List<Message> retrievedHistory, Member purgedMember) {
        long twoWeeksAgo = TimeUtil.getDiscordTimestamp((System.currentTimeMillis() - (14 * 24 * 60 * 60 * 1000)));
        log.debug("Filtering messages older than {}.", twoWeeksAgo);
        List<Message> messagesToDeleteNow = new ArrayList<>();
        for (Message messageObj : retrievedHistory) {
            if (MiscUtil.parseSnowflake(messageObj.getId()) > twoWeeksAgo) {
                if(purgedMember != null) {
                    if(messageObj.getAuthor().getIdLong() == purgedMember.getIdLong()) {
                        log.debug("Message {} is from filtered user {}. Purging.", messageObj.getId(), purgedMember.getIdLong());
                        messagesToDeleteNow.add(messageObj);
                    }
                } else {
                    messagesToDeleteNow.add(messageObj);
                }
            } else {
                log.debug("Message {} was older than {}. Not purging.", messageObj.getId(), twoWeeksAgo);
            }
        }
        return messagesToDeleteNow;
    }

    private Consumer<Void> deletionConsumer(Integer amountToDelete, GuildMessageChannel channel, Member purgedMember, Integer totalCount, Integer currentCount, CompletableFuture<Void> deletionFuture, Long currentStatusMessageId, Message earliestMessage) {
        return aVoid -> {
            if (amountToDelete >= 1) {
                log.debug("Still more than 1 message to delete. Continuing.");
                purgeMessages(amountToDelete, channel, earliestMessage.getIdLong(), purgedMember, totalCount, currentCount, currentStatusMessageId)
                        .whenComplete((avoid, throwable) -> {
                            if (throwable != null) {
                                deletionFuture.completeExceptionally(throwable);
                            } else {
                                deletionFuture.complete(null);
                            }
                        }
                ).exceptionally(throwable -> {
                    deletionFuture.completeExceptionally(throwable);
                    return null;
                });
            } else {
                log.debug("Completed purging of {} messages.", totalCount);
                // Todo Move to message service
                channel.deleteMessageById(currentStatusMessageId).queueAfter(5, TimeUnit.SECONDS);
                deletionFuture.complete(null);
            }
            log.debug("Setting status for {} out of {}", currentCount, totalCount);
            MessageToSend finalUpdateMessage = getStatusMessageToSend(totalCount, channel.getGuild().getIdLong(), currentCount);
            messageService.updateStatusMessage(channel, currentStatusMessageId, finalUpdateMessage);
        };
    }

    private Consumer<Void> deletionConsumer(Integer amountToDelete, GuildMessageChannel channel, Member purgedMember, Integer totalCount, Integer currentCount, CompletableFuture<Void> deletionFuture, InteractionHook interactionHook, Message earliestMessage) {
        return aVoid -> {
            if (amountToDelete >= 1) {
                log.debug("Still more than 1 message to delete. Continuing.");
                purgeMessages(amountToDelete, channel, earliestMessage.getIdLong(), purgedMember, totalCount, currentCount, interactionHook)
                        .whenComplete((avoid, throwable) -> {
                                    if (throwable != null) {
                                        deletionFuture.completeExceptionally(throwable);
                                    } else {
                                        deletionFuture.complete(null);
                                    }
                                }
                        ).exceptionally(throwable -> {
                            deletionFuture.completeExceptionally(throwable);
                            return null;
                        });
            } else {
                log.debug("Completed purging of {} messages.", totalCount);
                deletionFuture.complete(null);
            }
            log.debug("Setting status for {} out of {}", currentCount, totalCount);
            MessageToSend finalUpdateMessage = getStatusMessageToSend(totalCount, channel.getGuild().getIdLong(), currentCount);
            interactionService.replaceOriginal(finalUpdateMessage, interactionHook);
        };
    }

    @Override
    public CompletableFuture<Void> purgeMessagesInChannel(Integer count, GuildMessageChannel channel, Message origin, Member purgingRestriction) {
        return purgeMessagesInChannel(count, channel, origin.getIdLong(), purgingRestriction);
    }

    @Override
    public CompletionStage<Void> purgeMessagesInChannel(Integer amountOfMessages, GuildMessageChannel guildMessageChannel, Long startId, InteractionHook hook, Member memberToPurgeMessagesOf) {
        return purgeMessages(amountOfMessages, guildMessageChannel, startId, memberToPurgeMessagesOf, amountOfMessages, 0, hook);
    }

    private CompletableFuture<Void> purgeMessages(Integer amountToDelete, GuildMessageChannel channel, Long startId, Member purgedMember, Integer totalCount, Integer currentCount, InteractionHook interactionHook) {

        int toDeleteInThisIteration;
        if(amountToDelete >= PURGE_MAX_MESSAGES){
            toDeleteInThisIteration = PURGE_MAX_MESSAGES;
        } else {
            toDeleteInThisIteration = amountToDelete % PURGE_MAX_MESSAGES;
        }
        metricService.incrementCounter(PURGE_MESSAGE_COUNTER_METRIC, (long) toDeleteInThisIteration);
        log.info("Purging {} this iteration ({}/{}) messages in channel {} in server {}.", toDeleteInThisIteration, currentCount, totalCount, channel.getId(), channel.getGuild().getId());

        CompletableFuture<MessageHistory> historyFuture = channelService.getHistoryOfChannel(channel, startId, toDeleteInThisIteration);
        MessageToSend statusMessageToSend = getStatusMessageToSend(totalCount, channel.getGuild().getIdLong(), 0);
        CompletableFuture<Message> statusMessageFuture = interactionService.replaceOriginal(statusMessageToSend, interactionHook);

        CompletableFuture<Void> deletionFuture = new CompletableFuture<>();
        CompletableFuture<Void> retrievalFuture = CompletableFuture.allOf(historyFuture, statusMessageFuture);
        retrievalFuture.thenAccept(voidParam -> {
            try {
                List<Message> retrievedHistory = historyFuture.get().getRetrievedHistory();
                List<Message> messagesToDeleteNow = filterMessagesToDelete(retrievedHistory, purgedMember);
                if(messagesToDeleteNow.isEmpty()) {
                    log.warn("No messages found to delete, all were filtered.");
                    deletionFuture.completeExceptionally(new NoMessageFoundException());
                    return;
                }
                Message latestMessage = messagesToDeleteNow.get(messagesToDeleteNow.size() - 1);
                log.debug("Deleting {} messages directly", messagesToDeleteNow.size());
                int newCurrentCount = currentCount + messagesToDeleteNow.size();
                int newAmountToDelete = amountToDelete - PURGE_MAX_MESSAGES;
                Consumer<Void> consumer = deletionConsumer(newAmountToDelete, channel, purgedMember, totalCount, newCurrentCount, deletionFuture, interactionHook, latestMessage);
                if (messagesToDeleteNow.size() > 1) {
                    bulkDeleteMessages(channel, deletionFuture, messagesToDeleteNow, consumer);
                } else if (messagesToDeleteNow.size() == 1) {
                    messageService.deleteMessageWithAction(latestMessage).queue(consumer, deletionFuture::completeExceptionally);
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

    @PostConstruct
    public void postConstruct() {
        metricService.registerCounter(PURGE_MESSAGE_COUNTER_METRIC, "Amount of messages deleted by purge.");
    }
}
