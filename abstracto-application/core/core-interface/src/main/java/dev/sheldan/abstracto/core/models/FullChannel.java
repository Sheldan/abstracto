package dev.sheldan.abstracto.core.models;

import dev.sheldan.abstracto.core.models.database.AChannel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class FullChannel implements Serializable {
    private AChannel channel;
    private transient TextChannel serverChannel;

    public String getChannelRepr() {
        if(serverChannel != null) {
            return serverChannel.getAsMention();
        } else {
            return channel.getId().toString();
        }
    }
}
