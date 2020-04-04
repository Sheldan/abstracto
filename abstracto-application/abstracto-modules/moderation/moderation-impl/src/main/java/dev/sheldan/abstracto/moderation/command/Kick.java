package dev.sheldan.abstracto.moderation.command;

import dev.sheldan.abstracto.command.AbstractFeatureFlaggedCommand;
import dev.sheldan.abstracto.command.HelpInfo;
import dev.sheldan.abstracto.command.execution.*;
import dev.sheldan.abstracto.moderation.Moderation;
import dev.sheldan.abstracto.moderation.config.ModerationFeatures;
import dev.sheldan.abstracto.moderation.models.template.KickLogModel;
import dev.sheldan.abstracto.moderation.service.KickServiceBean;
import dev.sheldan.abstracto.templating.TemplateService;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Kick extends AbstractFeatureFlaggedCommand {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private KickServiceBean kickService;

    @Override
    public CommandResult execute(CommandContext commandContext) {

        List<Object> parameters = commandContext.getParameters().getParameters();
        Member member = (Member) parameters.get(0);
        String defaultReason = templateService.renderTemplate("ban_default_reason", null);
        String reason = parameters.size() == 2 ? (String) parameters.get(1) : defaultReason;

        KickLogModel kickLogModel = (KickLogModel) ContextConverter.fromCommandContext(commandContext, KickLogModel.class);
        kickLogModel.setKickedUser(member);
        kickLogModel.setKickingUser(commandContext.getAuthor());
        kickLogModel.setReason(reason);
        kickService.kickMember(member, reason, kickLogModel);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("user").type(Member.class).optional(false).build());
        parameters.add(Parameter.builder().name("reason").type(String.class).optional(true).remainder(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("kick")
                .module(Moderation.MODERATION)
                .templated(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public String getFeature() {
        return ModerationFeatures.MODERATION;
    }
}
