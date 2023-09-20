package dev.sheldan.abstracto.invitefilter.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureMode;
import dev.sheldan.abstracto.invitefilter.config.InviteFilterFeatureDefinition;
import dev.sheldan.abstracto.invitefilter.config.InviteFilterMode;
import dev.sheldan.abstracto.invitefilter.config.InviteFilterModerationModuleDefinition;
import dev.sheldan.abstracto.invitefilter.service.InviteLinkFilterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class RemoveTrackedInviteLinks extends AbstractConditionableCommand {

    @Autowired
    private InviteLinkFilterService inviteLinkFilterService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        if(!parameters.isEmpty()) {
            String invite = (String) parameters.get(0);
            return inviteLinkFilterService.clearAllUsedOfCode(invite, commandContext.getGuild().getIdLong(), commandContext.getJda())
                    .thenApply(unused -> CommandResult.fromSuccess());
        } else {
            inviteLinkFilterService.clearAllTrackedInviteCodes(commandContext.getGuild().getIdLong());
        }
        return CompletableFuture.completedFuture(CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Parameter inviteParameter = Parameter
                .builder()
                .name("invite")
                .type(String.class)
                .optional(true)
                .templated(true)
                .build();
        parameters.add(inviteParameter);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("removeTrackedInviteLinks")
                .module(InviteFilterModerationModuleDefinition.MODERATION)
                .templated(true)
                .async(true)
                .messageCommandOnly(true)
                .requiresConfirmation(true)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return InviteFilterFeatureDefinition.INVITE_FILTER;
    }

    @Override
    public List<FeatureMode> getFeatureModeLimitations() {
        return Arrays.asList(InviteFilterMode.TRACK_USES);
    }
}
