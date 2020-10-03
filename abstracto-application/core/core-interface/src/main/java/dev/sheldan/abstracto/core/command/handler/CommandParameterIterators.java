package dev.sheldan.abstracto.core.command.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.Iterator;

@Getter
@AllArgsConstructor
public class CommandParameterIterators {
    private Iterator<TextChannel> channelIterator;
    private Iterator<Emote> emoteIterator;
    private Iterator<Member> memberIterator;
    private Iterator<Role> roleIterator;
}
