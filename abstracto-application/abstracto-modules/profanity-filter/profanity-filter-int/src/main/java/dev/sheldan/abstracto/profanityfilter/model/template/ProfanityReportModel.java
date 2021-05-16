package dev.sheldan.abstracto.profanityfilter.model.template;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Message;

@Getter
@Setter
@Builder
public class ProfanityReportModel {
    private String profanityGroupKey;
    private String profanityRegexName;
    private Message profaneMessage;
}
