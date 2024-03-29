package dev.sheldan.abstracto.statistic.emote.command.parameter.handler;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.ParameterPieceType;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.command.handler.CommandParameterHandler;
import dev.sheldan.abstracto.core.command.handler.CommandParameterIterators;
import dev.sheldan.abstracto.core.command.handler.provided.EmoteParameterHandler;
import dev.sheldan.abstracto.core.command.service.CommandService;
import dev.sheldan.abstracto.statistic.emote.command.parameter.TrackEmoteParameter;
import dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote;
import dev.sheldan.abstracto.statistic.emote.service.TrackedEmoteService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@link CommandParameterHandler} for the {@link TrackEmoteParameter} class. It will call the
 * {@link EmoteParameterHandler} and use the returned {@link CustomEmoji} if one is available. Otherwise it will only use the
 * {@link Long} which was passed. This handler will create a fake instance for the {@link dev.sheldan.abstracto.statistic.emote.model.database.TrackedEmote}
 * and only make the {@link CustomEmoji} available in the result, if it was passed as such. This handler has a slightly higher priority
 * than medium.
 */
@Component
public class TrackedEmoteParameterParameterHandler implements CommandParameterHandler {

    @Autowired
    private EmoteParameterHandler emoteParameterHandler;

    @Autowired
    private TrackedEmoteService trackedEmoteService;

    @Autowired
    private CommandService commandService;

    /**
     * This {@link CommandParameterHandler} only handles {@link TrackEmoteParameter}
     * @param clazz The desired {@link Class} of a parameter
     * @param value
     * @return Whether or not the given {@link Class} will be handled by this {@link CommandParameterHandler}
     */
    @Override
    public boolean handles(Class clazz, UnparsedCommandParameterPiece value) {
        return clazz.equals(TrackEmoteParameter.class) && value.getType().equals(ParameterPieceType.STRING);
    }

    /**
     * This tries to parse the input and extract an {@link CustomEmoji} or just an {@link Long}. It uses a {@link EmoteParameterHandler} at first,
     * and if nothing is found tries to parse the {@link Long} directly from the input. In case an {@link CustomEmoji} was used, this will populate the
     * respective member variable in {@link TrackEmoteParameter}.
     * @param input The {@link String} input at the current position
     * @param iterators The {@link CommandParameterIterators} containing all available iterators to directly retrieve JDA related
     *                  entities from
     * @param param The {@link Class} which this type should handle
     * @param context The {@link Message} which caused the command to be executed
     * @param command
     * @return An instance of {@link TrackEmoteParameter} which contains the available instances. This is an {@link CustomEmoji} in case it was
     * used directly. In every successful case, it will contain a faked {@link TrackedEmote}.
     */
    @Override
    public Object handle(UnparsedCommandParameterPiece input, CommandParameterIterators iterators, Parameter param, Message context, Command command) {
        TrackEmoteParameter parameter = TrackEmoteParameter.builder().build();
        Parameter cloned = commandService.cloneParameter(param);
        cloned.setType(CustomEmoji.class);
        CustomEmoji emote = (CustomEmoji) emoteParameterHandler.handle(input, iterators, cloned, context, command);
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
