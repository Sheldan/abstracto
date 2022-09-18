package dev.sheldan.abstracto.core.command.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;

import java.util.Iterator;

@Getter
@AllArgsConstructor
public class CommandParameterIterators {
    private Iterator<TextChannel> channelIterator;
    private Iterator<CustomEmoji> emoteIterator;
    private Iterator<Member> memberIterator;
    private Iterator<Role> roleIterator;
}
