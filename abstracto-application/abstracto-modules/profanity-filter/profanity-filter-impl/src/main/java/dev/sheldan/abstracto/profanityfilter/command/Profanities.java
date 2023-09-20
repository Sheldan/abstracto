package dev.sheldan.abstracto.profanityfilter.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.core.models.ServerChannelMessage;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.profanityfilter.config.ProfanityFilterFeatureDefinition;
import dev.sheldan.abstracto.profanityfilter.config.ProfanityFilterMode;
import dev.sheldan.abstracto.profanityfilter.config.ProfanityFilterModerationModuleDefinition;
import dev.sheldan.abstracto.profanityfilter.model.database.ProfanityUserInAServer;
import dev.sheldan.abstracto.profanityfilter.model.template.ProfanitiesModel;
import dev.sheldan.abstracto.profanityfilter.service.ProfanityFilterService;
import dev.sheldan.abstracto.profanityfilter.service.management.ProfanityUserInServerManagementService;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class Profanities extends AbstractConditionableCommand {

    @Autowired
    private ProfanityFilterService profanityFilterService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private ProfanityUserInServerManagementService profanityUserInServerManagementService;

    @Autowired
    private ChannelService channelService;

    private static final String PROFANITIES_TEMPLATE_KEY = "profanities_response";

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        Member member = (Member) commandContext.getParameters().getParameters().get(0);
        AUserInAServer userInServer = userInServerManagementService.loadOrCreateUser(member);
        ProfanityUserInAServer profanityUser = profanityUserInServerManagementService.getProfanityUser(userInServer);
        Long positiveReports = profanityFilterService.getPositiveReportCountForUser(profanityUser);
        Long falsePositives = profanityFilterService.getFalseProfanityReportCountForUser(profanityUser);
        List<ServerChannelMessage> reports = profanityFilterService.getRecentPositiveReports(profanityUser, 3);
        ProfanitiesModel model =  ProfanitiesModel
                .builder()
                .member(member)
                .recentPositiveReports(reports)
                .falsePositives(falsePositives)
                .truePositives(positiveReports)
                .build();
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInTextChannelList(PROFANITIES_TEMPLATE_KEY, model, commandContext.getChannel()))
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter memberParameter = Parameter
                .builder()
                .templated(true)
                .name("member")
                .type(Member.class)
                .optional(true)
                .build();
        List<Parameter> parameters = Collections.singletonList(memberParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();
        return CommandConfiguration.builder()
                .name("profanities")
                .module(ProfanityFilterModerationModuleDefinition.MODERATION)
                .templated(true)
                .messageCommandOnly(true)
                .async(true)
                .causesReaction(false)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ProfanityFilterFeatureDefinition.PROFANITY_FILTER;
    }

    @Override
    public List<FeatureMode> getFeatureModeLimitations() {
        return Arrays.asList(ProfanityFilterMode.TRACK_PROFANITIES);
    }
}
