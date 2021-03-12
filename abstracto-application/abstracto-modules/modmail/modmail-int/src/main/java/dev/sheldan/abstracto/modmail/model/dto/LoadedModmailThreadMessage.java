package dev.sheldan.abstracto.modmail.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.concurrent.CompletableFuture;

@Getter
@Setter
@Builder
public class LoadedModmailThreadMessage {
    private CompletableFuture<Message> messageFuture;
    private CompletableFuture<Member> memberFuture;
}
