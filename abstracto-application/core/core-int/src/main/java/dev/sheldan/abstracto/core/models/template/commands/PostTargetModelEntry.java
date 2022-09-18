package dev.sheldan.abstracto.core.models.template.commands;

import dev.sheldan.abstracto.core.models.database.PostTarget;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

@Getter
@Setter
@Builder
public class PostTargetModelEntry {
    private PostTarget postTarget;
    private MessageChannel channel;
    private Boolean disabled;
}
