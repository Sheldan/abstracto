package dev.sheldan.abstracto.modmail.commands;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.condition.CommandCondition;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.modmail.condition.ModMailContextCondition;
import dev.sheldan.abstracto.modmail.config.ModMailFeatures;
import dev.sheldan.abstracto.modmail.models.database.ModMailThread;
import dev.sheldan.abstracto.modmail.service.ModMailSubscriptionService;
import dev.sheldan.abstracto.modmail.service.management.ModMailThreadManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * This command subscribes the member executing this command to the {@link ModMailThread} in which the command was executed in.
 * In case the member is already subscribed an error message is displayed.
 */
@Component
public class Subscribe extends AbstractConditionableCommand {

    @Autowired
    private ModMailContextCondition requiresModMailCondition;

    @Autowired
    private ModMailThreadManagementService modMailThreadManagementService;

    @Autowired
    private ModMailSubscriptionService modMailSubscriptionService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        ModMailThread modMailThread = modMailThreadManagementService.getByChannelId(commandContext.getChannel().getIdLong());
        modMailSubscriptionService.subscribeToThread(userInServerManagementService.loadOrCreateUser(commandContext.getAuthor()), modMailThread);
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("subscribe")
                .module(ModMailModuleInterface.MODMAIL)
                .help(helpInfo)
                .supportsEmbedException(true)
                .templated(true)
                .causesReaction(true)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return ModMailFeatures.MOD_MAIL;
    }

    @Override
    public List<CommandCondition> getConditions() {
        List<CommandCondition> conditions = super.getConditions();
        conditions.add(requiresModMailCondition);
        return conditions;
    }
}
