package dev.sheldan.abstracto.moderation.service;

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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
@Slf4j
public class PurgeServiceBean implements PurgeService {

    @Autowired
    private MessageService messageService;

    @Autowired
    private TemplateService templateService;

    @Override
    public CompletableFuture<Void> purgeMessagesInChannel(Integer amountToDelete, TextChannel channel, Long startId, Member purgedMember) {
        return purgeMessages(amountToDelete, channel, startId, purgedMember, amountToDelete, 0, 0L);
    }

    private CompletableFuture<Void> purgeMessages(Integer amountToDelete, TextChannel channel, Long startId, Member purgedMember, Integer totalCount, Integer currentCount, Long statusMessageId) {

        int toDeleteInThisIteration;
        int messageLimit = 100;
        if(amountToDelete >= messageLimit){
            toDeleteInThisIteration = messageLimit;
        } else {
            toDeleteInThisIteration = amountToDelete % messageLimit;
        }

        CompletableFuture<MessageHistory> historyFuture = channel.getHistoryBefore(startId, toDeleteInThisIteration).submit();
        CompletableFuture<Long> statusMessageFuture = getOrCreatedStatusMessage(channel, totalCount, statusMessageId);

        CompletableFuture<Void> deletionFuture = new CompletableFuture<>();
        CompletableFuture<Void> retrievalFuture = CompletableFuture.allOf(historyFuture, statusMessageFuture);
        retrievalFuture.thenAccept(voidParam -> {
            try {
                List<Message> retrievedHistory = historyFuture.get().getRetrievedHistory();
                List<Message> messagesToDeleteNow = filterMessagesToDelete(retrievedHistory, purgedMember);
                Long currentStatusMessageId = statusMessageFuture.get();
                if(messagesToDeleteNow.size() == 0) {
                    deletionFuture.completeExceptionally(new NoMessageFoundException());
                    channel.deleteMessageById(currentStatusMessageId).queueAfter(5, TimeUnit.SECONDS);
                    return;
                }
                Message latestMessage = messagesToDeleteNow.get(messagesToDeleteNow.size() - 1);
                log.trace("Deleting {} messages directly", messagesToDeleteNow.size());
                int newCurrentCount = currentCount + messagesToDeleteNow.size();
                int newAmountToDelete = amountToDelete - messageLimit;
                Consumer<Void> consumer = deletionConsumer(newAmountToDelete, channel, purgedMember, totalCount, newCurrentCount, deletionFuture, currentStatusMessageId, latestMessage);
                if (messagesToDeleteNow.size() > 1) {
                    bulkDeleteMessages(channel, deletionFuture, messagesToDeleteNow, consumer);
                } else if (messagesToDeleteNow.size() == 1) {
                    latestMessage.delete().queue(consumer, deletionFuture::completeExceptionally);
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
            channel.deleteMessages(messagesToDeleteNow).queue(consumer, deletionFuture::completeExceptionally);
        } catch (IllegalArgumentException e) {
            channel.sendMessage(e.getMessage()).queue();
            log.warn("Failed to bulk delete, message was most likely too old to delete by bulk.", e);
            deletionFuture.complete(null);
        }
    }

    private CompletableFuture<Long> getOrCreatedStatusMessage(TextChannel channel, Integer totalCount, Long statusMessageId) {
        CompletableFuture<Long> statusMessageFuture;
        if(statusMessageId == 0) {
            PurgeStatusUpdateModel model = PurgeStatusUpdateModel.builder().currentlyDeleted(0).totalToDelete(totalCount).build();
            MessageToSend messageToSend = templateService.renderTemplateToMessageToSend("purge_status_update", model);
            statusMessageFuture = messageService.createStatusMessageId(messageToSend, channel);
        } else {
            statusMessageFuture = CompletableFuture.completedFuture(statusMessageId);
        }
        return statusMessageFuture;
    }

    private List<Message> filterMessagesToDelete(List<Message> retrievedHistory, Member purgedMember) {
        long twoWeeksAgo = TimeUtil.getDiscordTimestamp((System.currentTimeMillis() - (14 * 24 * 60 * 60 * 1000)));
        List<Message> messagesToDeleteNow = new ArrayList<>();
        for (Message messageObj : retrievedHistory) {
            if (MiscUtil.parseSnowflake(messageObj.getId()) > twoWeeksAgo) {
                if(purgedMember != null) {
                    if(messageObj.getAuthor().getIdLong() == purgedMember.getIdLong()) {
                        messagesToDeleteNow.add(messageObj);
                    }
                } else {
                    messagesToDeleteNow.add(messageObj);
                }
            }
        }
        return messagesToDeleteNow;
    }

    private Consumer<Void> deletionConsumer(Integer amountToDelete, TextChannel channel, Member purgedMember, Integer totalCount, Integer currentCount, CompletableFuture<Void> deletionFuture, Long currentStatusMessageId, Message earliestMessage) {
        return aVoid -> {
            if (amountToDelete > 1) {
                purgeMessages(amountToDelete, channel, earliestMessage.getIdLong(), purgedMember, totalCount, currentCount, currentStatusMessageId).whenComplete((avoid, throwable) -> {
                            if (throwable != null) {
                                deletionFuture.completeExceptionally(throwable);
                            } else {
                                deletionFuture.complete(null);
                            }
                        }
                );
            } else {
                channel.deleteMessageById(currentStatusMessageId).queueAfter(5, TimeUnit.SECONDS);
                deletionFuture.complete(null);
            }
            log.info("Setting status for {} out of {}", currentCount, totalCount);
            PurgeStatusUpdateModel finalUpdateModel = PurgeStatusUpdateModel.builder().currentlyDeleted(currentCount).totalToDelete(totalCount).build();
            MessageToSend finalUpdateMessage = templateService.renderTemplateToMessageToSend("purge_status_update", finalUpdateModel);
            messageService.updateStatusMessage(channel, currentStatusMessageId, finalUpdateMessage);
        };
    }

    @Override
    public CompletableFuture<Void> purgeMessagesInChannel(Integer count, TextChannel channel, Message origin, Member purgingRestriction) {
        return purgeMessagesInChannel(count, channel, origin.getIdLong(), purgingRestriction);
    }
}
