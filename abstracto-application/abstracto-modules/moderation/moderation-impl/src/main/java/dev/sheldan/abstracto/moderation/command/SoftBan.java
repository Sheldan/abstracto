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
import dev.sheldan.abstracto.moderation.config.ModerationModuleDefinition;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.service.BanService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static dev.sheldan.abstracto.moderation.service.BanService.BAN_EFFECT_KEY;

@Component
@Slf4j
public class SoftBan extends AbstractConditionableCommand {

    @Autowired
    private BanService banService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        User user = (User) parameters.get(0);
        Duration delDays = Duration.ofDays(7);
        if(parameters.size() >  1) {
            delDays = (Duration) parameters.get(1);
        }
        return banService.softBanUser(commandContext.getGuild(), user, delDays)
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(Parameter.builder().name("user").templated(true).type(User.class).build());
        parameters.add(Parameter.builder().name("delDays").templated(true).type(Duration.class).optional(true).build());
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        List<EffectConfig> effectConfig = Arrays.asList(EffectConfig.builder().position(0).effectKey(BAN_EFFECT_KEY).build());
        return CommandConfiguration.builder()
                .name("softBan")
                .module(ModerationModuleDefinition.MODERATION)
                .templated(true)
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
