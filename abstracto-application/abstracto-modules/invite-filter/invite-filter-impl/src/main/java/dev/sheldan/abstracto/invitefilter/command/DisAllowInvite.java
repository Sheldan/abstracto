package dev.sheldan.abstracto.invitefilter.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.invitefilter.config.InviteFilterFeatureDefinition;
import dev.sheldan.abstracto.invitefilter.config.InviteFilterModerationModuleDefinition;
import dev.sheldan.abstracto.invitefilter.service.InviteLinkFilterServiceBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class DisAllowInvite extends AbstractConditionableCommand {

    @Autowired
    private InviteLinkFilterServiceBean inviteLinkFilterServiceBean;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        String inviteLink = (String) commandContext.getParameters().getParameters().get(0);
        return inviteLinkFilterServiceBean.disAllowInvite(inviteLink, commandContext.getGuild().getIdLong(), commandContext.getJda())
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Parameter inviteParameter = Parameter
                .builder()
                .name("invite")
                .type(String.class)
                .templated(true)
                .build();
        parameters.add(inviteParameter);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("disAllowInvite")
                .module(InviteFilterModerationModuleDefinition.MODERATION)
                .templated(true)
                .messageCommandOnly(true)
                .async(true)
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
}
