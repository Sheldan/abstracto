package dev.sheldan.abstracto.moderation.listener;

import dev.sheldan.abstracto.core.MessageTextUpdatedListener;
import dev.sheldan.abstracto.core.models.database.PostTarget;
import dev.sheldan.abstracto.core.service.MessageCache;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.moderation.models.template.listener.MessageEditedLog;
import dev.sheldan.abstracto.templating.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class MessageEditedListener implements MessageTextUpdatedListener {

    private static final String MESSAGE_EDITED_TEMPLATE = "message_edited";

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private MessageCache messageCache;

    @Override
    @Transactional
    public void execute(Message messageBefore, Message messageAfter) {
        if(messageBefore.getContentRaw().equals(messageAfter.getContentRaw())){
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
        postTargetService.sendTextInPostTarget(simpleMessageUpdatedMessage, PostTarget.EDIT_LOG, messageAfter.getGuild().getIdLong());
        MessageEmbed embed = templateService.renderEmbedTemplate(MESSAGE_EDITED_TEMPLATE, log);
        postTargetService.sendEmbedInPostTarget(embed, PostTarget.DELETE_LOG, messageBefore.getGuild().getIdLong());

    }
}
