package dev.sheldan.abstracto.statistic.emotes.command.parameter.handler;

import dev.sheldan.abstracto.core.command.handler.CommandParameterHandler;
import dev.sheldan.abstracto.core.command.handler.CommandParameterIterators;
import dev.sheldan.abstracto.core.command.handler.provided.EmoteParameterHandler;
import dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emotes.service.TrackedEmoteService;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TrackedEmoteParameterHandler implements CommandParameterHandler {

    @Autowired
    private EmoteParameterHandler emoteParameterHandler;

    @Autowired
    private TrackedEmoteService trackedEmoteService;

    @Override
    public boolean handles(Class clazz) {
        return clazz.equals(TrackedEmote.class);
    }

    @Override
    public Object handle(String input, CommandParameterIterators iterators, Class clazz, Message context) {
        Emote emote = (Emote) emoteParameterHandler.handle(input, iterators, Emote.class, context);
        if(emote != null) {
            return trackedEmoteService.getFakeTrackedEmote(emote, context.getGuild());
        } else {
            long trackedEmoteId = Long.parseLong(input);
            return trackedEmoteService.getFakeTrackedEmote(trackedEmoteId, context.getGuild());
        }
    }

    @Override
    public Integer getPriority() {
        return 51;
    }
}
