package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.CommandConstants;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.service.EmoteService;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AEmoteParameterHandler implements CommandParameterHandler {

    @Autowired
    private EmoteParameterHandler emoteParameterHandler;

    @Autowired
    private EmoteService emoteService;

    @Override
    public boolean handles(Class clazz) {
        return clazz.equals(AEmote.class);
    }

    @Override
    public Object handle(String input, CommandParameterIterators iterators, Class clazz, Message context) {
        Emote emote = (Emote) emoteParameterHandler.handle(input, iterators, Emote.class, context);
        return emoteService.getFakeEmoteFromEmote(emote);
    }

    @Override
    public Integer getPriority() {
        return CommandConstants.CORE_HANDLER_PRIORITY;
    }
}
