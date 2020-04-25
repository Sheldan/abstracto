package dev.sheldan.abstracto.moderation.commands.mute;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.moderation.Moderation;
import dev.sheldan.abstracto.moderation.config.ModerationFeatures;
import dev.sheldan.abstracto.moderation.models.template.commands.MuteLog;
import dev.sheldan.abstracto.moderation.service.MuteService;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class Mute extends AbstractConditionableCommand {

    @Autowired
    private MuteService muteService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Member member = (Member) parameters.get(0);
        Duration duration = (Duration) parameters.get(1);
        String reason = (String) parameters.get(2);
        MuteLog muteLogModel = (MuteLog) ContextConverter.fromCommandContext(commandContext, MuteLog.class);
        muteLogModel.setMessage(commandContext.getMessage());
        muteLogModel.setMutedUser(member);
        muteLogModel.setMutingUser(commandContext.getAuthor());
        muteService.muteMemberWithLog(member, commandContext.getAuthor(), reason, Instant.now().plus(duration), muteLogModel, commandContext.getMessage());
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("user").type(Member.class).build());
        parameters.add(Parameter.builder().name("duration").type(Duration.class).build());
        parameters.add(Parameter.builder().name("reason").type(String.class).optional(true).remainder(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(false).build();
        return CommandConfiguration.builder()
                .name("mute")
                .module(Moderation.MODERATION)
                .templated(false)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public String getFeature() {
        return ModerationFeatures.MUTING;
    }
}
