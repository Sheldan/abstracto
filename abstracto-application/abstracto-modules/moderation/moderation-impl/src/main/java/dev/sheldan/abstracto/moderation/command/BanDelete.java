package dev.sheldan.abstracto.moderation.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.condition.CommandCondition;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.EffectConfig;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.moderation.config.ModerationModuleDefinition;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.service.BanService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.moderation.command.Ban.BAN_NOTIFICATION_NOT_POSSIBLE;
import static dev.sheldan.abstracto.moderation.service.BanService.BAN_EFFECT_KEY;

@Component
@Slf4j
public class BanDelete extends AbstractConditionableCommand {

    @Autowired
    private BanService banService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        User user = (User) parameters.get(0);
        Integer delDays = (Integer) parameters.get(1);
        String reason = (String) parameters.get(2);
        Guild guild = commandContext.getGuild();
        Message message = commandContext.getMessage();
        Member banningMember = commandContext.getAuthor();
        return banService.banUserWithNotification(user, reason, commandContext.getAuthor(), delDays)
                .thenCompose(banResult -> {
                    String errorNotification = templateService.renderSimpleTemplate(BAN_NOTIFICATION_NOT_POSSIBLE, guild.getIdLong());
                    return channelService.sendTextToChannel(errorNotification, message.getChannel())
                            .thenAccept(message1 -> log.info("Notified about not being able to send ban notification in server {} and channel {} from user {}."
                                    , guild, message.getChannel().getIdLong(), banningMember.getIdLong()));
                })
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Parameter userParameter = Parameter
                .builder()
                .name("user")
                .templated(true)
                .type(User.class)
                .build();
        parameters.add(userParameter);
        Parameter delDaysParameter = Parameter
                .builder()
                .name("delDays")
                .templated(true)
                .type(Integer.class)
                .build();
        parameters.add(delDaysParameter);
        Parameter reasonParameter = Parameter
                .builder()
                .name("reason")
                .templated(true)
                .type(String.class)
                .remainder(true)
                .build();
        parameters.add(reasonParameter);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        List<EffectConfig> effectConfig = Arrays.asList(EffectConfig.builder().position(0).effectKey(BAN_EFFECT_KEY).build());
        return CommandConfiguration.builder()
                .name("banDelete")
                .module(ModerationModuleDefinition.MODERATION)
                .templated(true)
                .messageCommandOnly(true)
                .async(true)
                .effects(effectConfig)
                .supportsEmbedException(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.MODERATION;
    }

    @Override
    public List<CommandCondition> getConditions() {
        List<CommandCondition> conditions = super.getConditions();
        conditions.add(immuneUserCondition);
        return conditions;
    }
}
