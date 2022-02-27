package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.listener.async.jda.ButtonClickedListener;
import dev.sheldan.abstracto.core.models.listener.ButtonClickedListenerModel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface ExceptionService {
    CommandResult reportExceptionToContext(Throwable exception, CommandContext context, Command command);
    void reportExceptionToInteraction(Throwable exception, ButtonClickedListenerModel interActionContext, ButtonClickedListener executedListener);
    void reportExceptionToGuildMessageReceivedContext(Throwable exception, MessageReceivedEvent event);
    void reportExceptionToPrivateMessageReceivedContext(Throwable exception, MessageReceivedEvent event);
    void reportExceptionToChannel(Throwable exception, MessageChannel channel, Member member);
}
