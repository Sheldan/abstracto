package dev.sheldan.abstracto.modmail.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Member;

@Getter
@Setter
@Builder
public class ClosingContext {
    private Boolean notifyUser;
    private Boolean log;
    private Member closingMember;
    private Channel channel;
    private String note;
}
