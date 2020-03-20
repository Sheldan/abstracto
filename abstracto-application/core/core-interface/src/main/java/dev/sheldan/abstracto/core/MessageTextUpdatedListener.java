package dev.sheldan.abstracto.core;

import net.dv8tion.jda.api.entities.Message;

public interface MessageTextUpdatedListener {
    void execute(Message messageBefore, Message messageAfter);
}
