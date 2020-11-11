package dev.sheldan.abstracto.statistic.emotes.command.parameter.handler;

import dev.sheldan.abstracto.core.command.handler.CommandParameterHandler;
import dev.sheldan.abstracto.core.command.handler.CommandParameterIterators;
import dev.sheldan.abstracto.core.command.handler.provided.EmoteParameterHandler;
import dev.sheldan.abstracto.statistic.emotes.command.parameter.TrackEmoteParameter;
import dev.sheldan.abstracto.statistic.emotes.service.TrackedEmoteService;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TrackedEmoteParameterParameterHandler implements CommandParameterHandler {

    @Autowired
    private EmoteParameterHandler emoteParameterHandler;

    @Autowired
    private TrackedEmoteService trackedEmoteService;

    @Override
    public boolean handles(Class clazz) {
        return clazz.equals(TrackEmoteParameter.class);
    }

    @Override
    public Object handle(String input, CommandParameterIterators iterators, Class clazz, Message context) {
        TrackEmoteParameter parameter = TrackEmoteParameter.builder().build();
        Emote emote = (Emote) emoteParameterHandler.handle(input, iterators, Emote.class, context);
        if(emote != null) {
            parameter.setEmote(emote);
            parameter.setTrackedEmote(trackedEmoteService.getFakeTrackedEmote(emote, context.getGuild()));
        } else {
            long trackedEmoteId = Long.parseLong(input);
            parameter.setTrackedEmote(trackedEmoteService.getFakeTrackedEmote(trackedEmoteId, context.getGuild()));
        }
        return parameter;
    }

    @Override
    public Integer getPriority() {
        return 51;
    }
}
