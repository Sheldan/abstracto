package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.CommandConstants;
import dev.sheldan.abstracto.core.command.handler.provided.DurationParameterHandler;
import dev.sheldan.abstracto.core.utils.ParseUtils;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class DurationParameterHandlerImpl implements DurationParameterHandler {
    @Override
    public boolean handles(Class clazz) {
        return clazz.equals(Duration.class);
    }

    @Override
    public Object handle(String input, CommandParameterIterators iterators, Class clazz, Message context) {
        return ParseUtils.parseDuration(input);
    }

    @Override
    public Integer getPriority() {
        return CommandConstants.CORE_HANDLER_PRIORITY;
    }
}
