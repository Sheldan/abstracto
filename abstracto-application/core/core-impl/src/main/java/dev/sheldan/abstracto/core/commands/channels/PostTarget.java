package dev.sheldan.abstracto.core.commands.channels;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.*;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.config.features.CoreFeatures;
import dev.sheldan.abstracto.core.models.template.commands.PostTargetErrorModel;
import dev.sheldan.abstracto.core.service.PostTargetService;
import dev.sheldan.abstracto.core.service.management.PostTargetManagement;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class PostTarget extends AbstractConditionableCommand {

    public static final String POST_TARGET_NO_TARGET_TEMPLATE = "posttarget_no_target";
    public static final String POST_TARGET_INVALID_TARGET_TEMPLATE = "posttarget_invalid_target";

    @Autowired
    private PostTargetManagement postTargetManagement;

    @Autowired
    private PostTargetService postTargetService;

    @Autowired
    private TemplateService templateService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        if(commandContext.getParameters().getParameters().isEmpty()) {
            PostTargetErrorModel postTargetErrorModel = (PostTargetErrorModel) ContextConverter.fromCommandContext(commandContext, PostTargetErrorModel.class);
            postTargetErrorModel.setValidPostTargets(postTargetService.getAvailablePostTargets());
            String errorMessage = templateService.renderTemplate(POST_TARGET_NO_TARGET_TEMPLATE, postTargetErrorModel);
            return CommandResult.fromError(errorMessage);
        }
        String targetName = (String) commandContext.getParameters().getParameters().get(0);
        if(!postTargetService.validPostTarget(targetName)) {
            PostTargetErrorModel postTargetErrorModel = (PostTargetErrorModel) ContextConverter.fromCommandContext(commandContext, PostTargetErrorModel.class);
            postTargetErrorModel.setValidPostTargets(postTargetService.getAvailablePostTargets());
            String errorMessage = templateService.renderTemplate(POST_TARGET_INVALID_TARGET_TEMPLATE, postTargetErrorModel);
            return CommandResult.fromError(errorMessage);
        }
        GuildChannel channel = (GuildChannel) commandContext.getParameters().getParameters().get(1);
        Guild guild = channel.getGuild();
        postTargetManagement.createOrUpdate(targetName, guild.getIdLong(), channel.getIdLong());
        log.info("Setting posttarget {} in {} to {}", targetName, guild.getIdLong(), channel.getId());
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter postTargetName = Parameter.builder().name("name").type(String.class).optional(true).templated(true).build();
        Parameter channel = Parameter.builder().name("channel").type(TextChannel.class).optional(true).templated(true).build();
        List<Parameter> parameters = Arrays.asList(postTargetName, channel);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("posttarget")
                .module(ChannelsModuleInterface.CHANNELS)
                .parameters(parameters)
                .help(helpInfo)
                .templated(true)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return CoreFeatures.CORE_FEATURE;
    }
}
