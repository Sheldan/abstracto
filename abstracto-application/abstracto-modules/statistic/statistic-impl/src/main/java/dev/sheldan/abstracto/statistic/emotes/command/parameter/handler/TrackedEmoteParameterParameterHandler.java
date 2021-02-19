package dev.sheldan.abstracto.statistic.emotes.command.parameter.handler;

import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.command.handler.CommandParameterHandler;
import dev.sheldan.abstracto.core.command.handler.CommandParameterIterators;
import dev.sheldan.abstracto.core.command.handler.provided.EmoteParameterHandler;
import dev.sheldan.abstracto.statistic.emotes.command.parameter.TrackEmoteParameter;
import dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emotes.service.TrackedEmoteService;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@link CommandParameterHandler} for the {@link TrackEmoteParameter} class. It will call the
 * {@link EmoteParameterHandler} and use the returned {@link Emote} if one is available. Otherwise it will only use the
 * {@link Long} which was passed. This handler will create a fake instance for the {@link dev.sheldan.abstracto.statistic.emotes.model.database.TrackedEmote}
 * and only make the {@link Emote} available in the result, if it was passed as such. This handler has a slightly higher priority
 * than medium.
 */
@Component
public class TrackedEmoteParameterParameterHandler implements CommandParameterHandler {

    @Autowired
    private EmoteParameterHandler emoteParameterHandler;

    @Autowired
    private TrackedEmoteService trackedEmoteService;

    /**
     * This {@link CommandParameterHandler} only handles {@link TrackEmoteParameter}
     * @param clazz The desired {@link Class} of a parameter
     * @return Whether or not the given {@link Class} will be handled by this {@link CommandParameterHandler}
     */
    @Override
    public boolean handles(Class clazz) {
        return clazz.equals(TrackEmoteParameter.class);
    }

    /**
     * This tries to parse the input and extract an {@link Emote} or just an {@link Long}. It uses a {@link EmoteParameterHandler} at first,
     * and if nothing is found tries to parse the {@link Long} directly from the input. In case an {@link Emote} was used, this will populate the
     * respective member variable in {@link TrackEmoteParameter}.
     * @param input The {@link String} input at the current position
     * @param iterators The {@link CommandParameterIterators} containing all available iterators to directly retrieve JDA related
     *                  entities from
     * @param clazz The {@link Class} which this type should handle
     * @param context The {@link Message} which caused the command to be executed
     * @return An instance of {@link TrackEmoteParameter} which contains the available instances. This is an {@link Emote} in case it was
     * used directly. In every successful case, it will contain a faked {@link TrackedEmote}.
     */
    @Override
    public Object handle(UnparsedCommandParameterPiece input, CommandParameterIterators iterators, Class clazz, Message context) {
        TrackEmoteParameter parameter = TrackEmoteParameter.builder().build();
        Emote emote = (Emote) emoteParameterHandler.handle(input, iterators, Emote.class, context);
        if(emote != null) {
            parameter.setEmote(emote);
            parameter.setTrackedEmote(trackedEmoteService.getFakeTrackedEmote(emote, context.getGuild()));
        } else {
            long trackedEmoteId = Long.parseLong((String) input.getValue());
            parameter.setTrackedEmote(trackedEmoteService.getFakeTrackedEmote(trackedEmoteId, context.getGuild()));
        }
        return parameter;
    }

    @Override
    public Integer getPriority() {
        return 51;
    }
}
