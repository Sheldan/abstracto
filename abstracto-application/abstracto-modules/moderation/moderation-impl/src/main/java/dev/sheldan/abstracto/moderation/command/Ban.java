package dev.sheldan.abstracto.moderation.command;

import dev.sheldan.abstracto.command.AbstractFeatureFlaggedCommand;
import dev.sheldan.abstracto.command.HelpInfo;
import dev.sheldan.abstracto.command.execution.*;
import dev.sheldan.abstracto.moderation.Moderation;
import dev.sheldan.abstracto.moderation.config.ModerationFeatures;
import dev.sheldan.abstracto.moderation.models.template.BanLog;
import dev.sheldan.abstracto.moderation.service.BanService;
import dev.sheldan.abstracto.templating.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class Ban extends AbstractFeatureFlaggedCommand {

    @Autowired
    private BanService banService;

    @Autowired
    private TemplateService templateService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Member member = (Member) parameters.get(0);
        String defaultReason = templateService.renderTemplate("ban_default_reason", null);
        String reason = parameters.size() == 2 ? (String) parameters.get(1) : defaultReason;

        BanLog banLogModel = (BanLog) ContextConverter.fromCommandContext(commandContext, BanLog.class);
        banLogModel.setBannedUser(member);
        banLogModel.setBanningUser(commandContext.getAuthor());
        banLogModel.setReason(reason);
        banService.banMember(member, reason, banLogModel);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("user").type(Member.class).optional(false).build());
        parameters.add(Parameter.builder().name("reason").type(String.class).optional(true).remainder(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("ban")
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
