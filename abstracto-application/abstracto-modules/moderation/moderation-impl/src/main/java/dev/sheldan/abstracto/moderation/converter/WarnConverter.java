package dev.sheldan.abstracto.moderation.converter;

import dev.sheldan.abstracto.core.utils.AbstractoDateUtils;
import dev.sheldan.abstracto.moderation.models.database.Warning;
import dev.sheldan.abstracto.moderation.models.dto.WarnDto;
import dev.sheldan.abstracto.moderation.models.template.commands.WarnModel;
import org.springframework.stereotype.Component;

@Component
public class WarnConverter {
    public WarnDto convertFromAWarn(Warning warning) {
        return WarnDto.builder()
                .id(warning.getId())
                .decayed(warning.getDecayed())
                .reason(warning.getReason())
                .decayDate(AbstractoDateUtils.convertInstant(warning.getDecayDate()))
                .warnDate(AbstractoDateUtils.convertInstant(warning.getWarnDate()))
                .build();
    }

    public WarnModel convertFromWarnDto(WarnDto warnDto) {
        return WarnModel
                .builder()
                .decayDate(warnDto.getDecayDate())
                .decayed(warnDto.getDecayed())
                .warnDate(warnDto.getWarnDate())
                .reason(warnDto.getReason())
                .build();
    }
}
