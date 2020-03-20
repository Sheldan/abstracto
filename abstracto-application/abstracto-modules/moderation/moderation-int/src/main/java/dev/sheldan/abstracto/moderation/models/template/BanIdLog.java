package dev.sheldan.abstracto.moderation.models.template;

import dev.sheldan.abstracto.core.models.UserInitiatedServerContext;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.entities.Member;

@Getter @SuperBuilder @Setter
public class BanIdLog extends UserInitiatedServerContext {
    private String reason;
    private Member banningUser;
    private Long bannedUserId;

    @Override
    public String getTemplateSuffix() {
        return "";
    }
}
