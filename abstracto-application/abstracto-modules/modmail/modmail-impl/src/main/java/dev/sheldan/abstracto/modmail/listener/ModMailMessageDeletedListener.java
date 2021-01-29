package dev.sheldan.abstracto.modmail.listener;

import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncMessageDeletedListener;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.modmail.config.ModMailFeatures;
import dev.sheldan.abstracto.modmail.models.database.ModMailMessage;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import dev.sheldan.abstracto.modmail.service.management.ModMailMessageManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class ModMailMessageDeletedListener implements AsyncMessageDeletedListener {

    @Autowired
    private ModMailMessageManagementService modMailMessageManagementService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ModMailMessageDeletedListener self;

    @Autowired
    private MemberService memberService;

    @Override
    public void execute(CachedMessage messageBefore) {
        Optional<ModMailMessage> messageOptional = modMailMessageManagementService.getByMessageIdOptional(messageBefore.getMessageId());
        messageOptional.ifPresent(modMailMessage -> {
            ModMailThread thread = modMailMessage.getThreadReference();
            Long dmMessageId = modMailMessage.getCreatedMessageInDM();
            boolean hasMessageInChannel = modMailMessage.getCreatedMessageInChannel() != null;
            Long channelMessage = modMailMessage.getCreatedMessageInChannel();
            Long channelId = thread.getChannel().getId();
            Long serverId = thread.getServer().getId();
            log.info("Deleting message for mod mail thread {} in channel {} in server {}.", thread.getId(), channelId, serverId);
            memberService.getMemberInServerAsync(messageBefore.getServerId(), modMailMessage.getThreadReference().getUser().getUserReference().getId()).thenAccept(member -> {
                CompletableFuture<Void> dmDeletePromise = messageService.deleteMessageInChannelWithUser(member.getUser(), dmMessageId);
                CompletableFuture<Void> channelDeletePromise;
                if(hasMessageInChannel) {
                    channelDeletePromise = messageService.deleteMessageInChannelInServer(serverId, channelId, channelMessage);
                } else {
                    channelDeletePromise = CompletableFuture.completedFuture(null);
                }
                CompletableFuture.allOf(dmDeletePromise, channelDeletePromise).whenComplete((unused, throwable) ->
                        self.removeMessageFromThread(messageBefore.getMessageId())
                );
            });
        });

    }

    @Transactional
    public void removeMessageFromThread(Long messageId) {
        Optional<ModMailMessage> messageOptional = modMailMessageManagementService.getByMessageIdOptional(messageId);
        messageOptional.ifPresent(modMailMessage ->
            modMailMessageManagementService.deleteMessageFromThread(modMailMessage)
        );
    }

    @Override
    public FeatureEnum getFeature() {
        return ModMailFeatures.MOD_MAIL;
    }

}
