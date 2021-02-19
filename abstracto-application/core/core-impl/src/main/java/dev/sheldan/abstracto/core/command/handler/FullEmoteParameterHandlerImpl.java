package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.CommandConstants;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.command.handler.provided.EmoteParameterHandler;
import dev.sheldan.abstracto.core.command.handler.provided.FullEmoteParameterHandler;
import dev.sheldan.abstracto.core.models.FullEmote;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.service.EmoteService;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FullEmoteParameterHandlerImpl implements FullEmoteParameterHandler {

    @Autowired
    private EmoteParameterHandler emoteParameterHandler;

    @Autowired
    private EmoteService emoteService;

    @Override
    public boolean handles(Class clazz) {
        return clazz.equals(FullEmote.class);
    }

    @Override
    public Object handle(UnparsedCommandParameterPiece input, CommandParameterIterators iterators, Class clazz, Message context) {
        Emote emote = (Emote) emoteParameterHandler.handle(input, iterators, Emote.class, context);
        AEmote aEmote;
        if(emote != null) {
            aEmote = emoteService.getFakeEmoteFromEmote(emote);
        } else {
            aEmote = emoteService.getFakeEmote(input.getValue());
        }
        return FullEmote.builder().emote(emote).fakeEmote(aEmote).build();
    }

    @Override
    public Integer getPriority() {
        return CommandConstants.CORE_HANDLER_PRIORITY;
    }
}
