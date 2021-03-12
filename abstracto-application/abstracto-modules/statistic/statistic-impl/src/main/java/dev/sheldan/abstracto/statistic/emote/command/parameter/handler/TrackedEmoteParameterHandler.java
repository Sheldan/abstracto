package dev.sheldan.abstracto.statistic.emote.command.parameter.handler;

import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.command.handler.CommandParameterHandler;
import dev.sheldan.abstracto.core.command.handler.CommandParameterIterators;
import dev.sheldan.abstracto.core.command.handler.provided.EmoteParameterHandler;
import dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emote.service.TrackedEmoteService;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@link CommandParameterHandler} for the {@link TrackedEmote} class. This parameter handler will only create
 * fake {@link TrackedEmote} and it has a priority a bit higher than medium.
 */
@Component
public class TrackedEmoteParameterHandler implements CommandParameterHandler {

    @Autowired
    private EmoteParameterHandler emoteParameterHandler;

    @Autowired
    private TrackedEmoteService trackedEmoteService;

    /**
     * This {@link CommandParameterHandler} only handles {@link TrackedEmote}
     * @param clazz The desired {@link Class} of a parameter
     * @return Whether or not the given {@link Class} will be handled by this {@link CommandParameterHandler}
     */
    @Override
    public boolean handles(Class clazz) {
        return clazz.equals(TrackedEmote.class);
    }

    /**
     * This will parse the input for potential {@link TrackedEmote} and return a fake instance of such.
     * At first it will see if there are any {@link Emote} directly in the message. If there are none at the current position
     * it will try to parse the parameter to a {@link Long}. It is *not* guaranteed that a {@link TrackedEmote} with this ID
     * really exists for this server. So, any commands using this are required to do checks on their own.
     * @param input The {@link String} input at the current position
     * @param iterators The {@link CommandParameterIterators} containing all available iterators to directly retrieve JDA related
     *                  entities from
     * @param clazz The {@link Class} which this type should handle
     * @param context The {@link Message} which caused the command to be executed
     * @return A faked {@link TrackedEmote} based on the given input or from {@link CommandParameterIterators} directly. This {@link TrackedEmote}
     * does not need to actually exist.
     */
    @Override
    public Object handle(UnparsedCommandParameterPiece input, CommandParameterIterators iterators, Class clazz, Message context) {
        Emote emote = (Emote) emoteParameterHandler.handle(input, iterators, Emote.class, context);
        if(emote != null) {
            return trackedEmoteService.getFakeTrackedEmote(emote, context.getGuild());
        } else {
            long trackedEmoteId = Long.parseLong((String) input.getValue());
            return trackedEmoteService.getFakeTrackedEmote(trackedEmoteId, context.getGuild());
        }
    }

    @Override
    public Integer getPriority() {
        return 51;
    }
}
