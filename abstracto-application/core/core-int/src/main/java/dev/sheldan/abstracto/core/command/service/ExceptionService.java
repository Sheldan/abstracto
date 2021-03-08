package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public interface ExceptionService {
    CommandResult reportExceptionToContext(Throwable exception, CommandContext context, Command command);
    void reportExceptionToGuildMessageReceivedContext(Throwable exception, GuildMessageReceivedEvent event);
    void reportExceptionToPrivateMessageReceivedContext(Throwable exception, PrivateMessageReceivedEvent event);
    void reportExceptionToChannel(Throwable exception, MessageChannel channel, Member member);
}
