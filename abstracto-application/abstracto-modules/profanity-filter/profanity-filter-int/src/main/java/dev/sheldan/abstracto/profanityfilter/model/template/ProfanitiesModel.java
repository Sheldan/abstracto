package dev.sheldan.abstracto.profanityfilter.model.template;

import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

@Getter
@Setter
@Builder
public class ProfanitiesModel {
    private Member member;
    private Long falsePositives;
    private Long truePositives;
    private List<ServerChannelMessage> recentPositiveReports;
}
