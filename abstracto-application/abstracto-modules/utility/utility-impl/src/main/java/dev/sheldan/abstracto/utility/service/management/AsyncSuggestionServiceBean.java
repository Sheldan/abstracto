package dev.sheldan.abstracto.utility.service.management;

import dev.sheldan.abstracto.core.models.database.PostTarget;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.templating.TemplateService;
import dev.sheldan.abstracto.utility.models.template.SuggestionLog;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static dev.sheldan.abstracto.utility.service.SuggestionServiceBean.SUGGESTION_LOG_TEMPLATE;

@Component
@Slf4j
public class AsyncSuggestionServiceBean {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PostTargetService postTargetService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateSuggestionMessageText(String text, SuggestionLog suggestionLog, Message message) {
        Optional<MessageEmbed> embedOptional = message.getEmbeds().stream().filter(embed -> embed.getDescription() != null).findFirst();
        if(embedOptional.isPresent()) {
            MessageEmbed suggestionEmbed = embedOptional.get();
            suggestionLog.setReason(text);
            suggestionLog.setText(suggestionEmbed.getDescription());
            MessageEmbed embed = templateService.renderEmbedTemplate(SUGGESTION_LOG_TEMPLATE, suggestionLog);
            postTargetService.sendEmbedInPostTarget(embed, PostTarget.SUGGESTIONS, suggestionLog.getServer().getId());
        }
    }

}
