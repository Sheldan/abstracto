package dev.sheldan.abstracto.modmail.model.template;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

@Getter
@Setter
@Builder
public class ModmailLoggingThreadMessages {
    private List<Message> messages;
    private List<User> authors;
}
