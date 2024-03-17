package dev.sheldan.abstracto.profanityfilter.model.template;

import dev.sheldan.abstracto.moderation.model.ModerationActionButton;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

@Getter
@Setter
@Builder
public class ProfanityReportModel {
    private String profanityGroupKey;
    private String profanityRegexName;
    private Message profaneMessage;
    private List<ModerationActionButton> moderationActionComponents;
}
