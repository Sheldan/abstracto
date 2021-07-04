package dev.sheldan.abstracto.modmail.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.condition.CommandCondition;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.service.management.ChannelManagementService;
import dev.sheldan.abstracto.modmail.condition.ModMailContextCondition;
import dev.sheldan.abstracto.modmail.config.ModMailFeatureDefinition;
import dev.sheldan.abstracto.modmail.model.ClosingContext;
import dev.sheldan.abstracto.modmail.model.database.ModMailThread;
import dev.sheldan.abstracto.modmail.service.ModMailThreadService;
import dev.sheldan.abstracto.modmail.service.management.ModMailThreadManagementService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Closes the mod mail thread: logs the messages to the log post target, if the feature has the appropriate
 * {@link dev.sheldan.abstracto.core.config.FeatureMode}, deletes the {@link net.dv8tion.jda.api.entities.MessageChannel}.
 * This command takes an optional parameter, the note, which will be replaced with a default value, if not present
 */
@Component
@Slf4j
public class Close extends AbstractConditionableCommand {

    public static final String MODMAIL_CLOSE_DEFAULT_NOTE_TEMPLATE_KEY = "modmail_close_default_note";
    @Autowired
    private ModMailContextCondition requiresModMailCondition;

    @Autowired
    private ModMailThreadManagementService modMailThreadManagementService;

    @Autowired
    private ModMailThreadService modMailThreadService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelManagementService channelManagementService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        // the default value of the note is configurable via template
        String note = parameters.size() == 1 ? (String) parameters.get(0) : templateService.renderTemplate(MODMAIL_CLOSE_DEFAULT_NOTE_TEMPLATE_KEY, new Object());
        AChannel channel = channelManagementService.loadChannel(commandContext.getChannel());
        ModMailThread thread = modMailThreadManagementService.getByChannel(channel);
        ClosingContext context = ClosingContext
                .builder()
                .closingMember(commandContext.getAuthor())
                .notifyUser(true)
                .log(true)
                .note(note)
                .build();
        return modMailThreadService.closeModMailThread(thread, context, commandContext.getUndoActions())
                .thenApply(aVoid -> CommandResult.fromIgnored());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter note = Parameter.builder().name("note").type(String.class).remainder(true).optional(true).templated(true).build();
        List<Parameter> parameters = Arrays.asList(note);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("close")
                .module(ModMailModuleDefinition.MODMAIL)
                .parameters(parameters)
                .help(helpInfo)
                .async(true)
                .supportsEmbedException(true)
                .templated(true)
                .causesReaction(false)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModMailFeatureDefinition.MOD_MAIL;
    }

    @Override
    public List<CommandCondition> getConditions() {
        List<CommandCondition> conditions = super.getConditions();
        conditions.add(requiresModMailCondition);
        return conditions;
    }
}
