package dev.sheldan.abstracto.core.models.cache;

import dev.sheldan.abstracto.core.utils.MemberUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class CachedAuthor implements Serializable {
    private Long authorId;
    private Boolean isBot;

    public String getAsMention() {
        return MemberUtils.getUserAsMention(authorId);
    }
}
