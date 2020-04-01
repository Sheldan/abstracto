package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.listener.MessageTextUpdatedListener;
import dev.sheldan.abstracto.core.models.CachedMessage;
import dev.sheldan.abstracto.core.models.embed.MessageToSend;
import dev.sheldan.abstracto.core.service.MessageCache;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.moderation.models.template.listener.MessageEditedLog;
import dev.sheldan.abstracto.templating.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class MessageEditedListener implements MessageTextUpdatedListener {

    private static final String MESSAGE_EDITED_TEMPLATE = "message_edited";
    private static final String EDIT_LOG_TARGET = "editLog";

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private MessageCache messageCache;

    @Override
    @Transactional
    public void execute(CachedMessage messageBefore, Message messageAfter) {
        if(messageBefore.getContent().equals(messageAfter.getContentRaw())){
            log.debug("Message content was the same. Possible reason was: message was not in cache.");
            return;
        }
        MessageEditedLog log = MessageEditedLog.
                builder().
                messageAfter(messageAfter)
                .messageBefore(messageBefore)
                .textChannel(messageAfter.getTextChannel())
                .guild(messageAfter.getGuild())
                .member(messageAfter.getMember()).build();
        String simpleMessageUpdatedMessage = templateService.renderTemplate(MESSAGE_EDITED_TEMPLATE, log);
        postTargetService.sendTextInPostTarget(simpleMessageUpdatedMessage, EDIT_LOG_TARGET, messageAfter.getGuild().getIdLong());
        MessageToSend message = templateService.renderEmbedTemplate(MESSAGE_EDITED_TEMPLATE, log);
        postTargetService.sendEmbedInPostTarget(message, EDIT_LOG_TARGET, messageBefore.getServerId());

    }
}
