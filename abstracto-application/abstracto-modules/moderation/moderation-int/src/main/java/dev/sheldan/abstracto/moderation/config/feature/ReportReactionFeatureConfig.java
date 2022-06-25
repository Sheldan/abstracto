package dev.sheldan.abstracto.moderation.config.feature;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.config.PostTargetEnum;
import dev.sheldan.abstracto.moderation.config.feature.mode.ReportReactionMode;
import dev.sheldan.abstracto.moderation.config.posttarget.ReactionReportPostTarget;
import dev.sheldan.abstracto.moderation.service.ReactionReportService;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;


@Component
public class ReportReactionFeatureConfig implements FeatureConfig {

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.REPORT_REACTIONS;
    }

    @Override
    public List<PostTargetEnum> getRequiredPostTargets() {
        return Arrays.asList(ReactionReportPostTarget.REACTION_REPORTS);
    }

    @Override
    public List<String> getRequiredEmotes() {
        return Arrays.asList(ReactionReportService.REACTION_REPORT_EMOTE_KEY);
    }

    @Override
    public List<String> getRequiredSystemConfigKeys() {
        return Arrays.asList(ReactionReportService.REACTION_REPORT_COOLDOWN);
    }

    @Override
    public List<FeatureMode> getAvailableModes() {
        return Arrays.asList(ReportReactionMode.SINGULAR_MESSAGE, ReportReactionMode.ANONYMOUS);
    }
}
