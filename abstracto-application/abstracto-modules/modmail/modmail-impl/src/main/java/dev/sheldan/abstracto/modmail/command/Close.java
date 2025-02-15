package dev.sheldan.abstracto.modmail.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.condition.CommandCondition;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.modmail.condition.ModMailContextCondition;
import dev.sheldan.abstracto.modmail.config.ModMailFeatureDefinition;
import dev.sheldan.abstracto.modmail.config.ModMailSlashCommandNames;
import dev.sheldan.abstracto.modmail.exception.ModMailThreadClosedException;
import dev.sheldan.abstracto.modmail.model.ClosingContext;
import dev.sheldan.abstracto.modmail.model.database.ModMailThread;
import dev.sheldan.abstracto.modmail.model.database.ModMailThreadState;
import dev.sheldan.abstracto.modmail.service.ModMailThreadService;
import dev.sheldan.abstracto.modmail.service.management.ModMailThreadManagementService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Closes the mod mail thread: logs the messages to the log post target, if the feature has the appropriate
 * {@link dev.sheldan.abstracto.core.config.FeatureMode}, deletes the message channel.
 * This command takes an optional parameter, the note, which will be replaced with a default value, if not present
 */
@Component
@Slf4j
public class Close extends AbstractConditionableCommand {

    private static final String MODMAIL_CLOSE_DEFAULT_NOTE_TEMPLATE_KEY = "modmail_close_default_note";
    private static final String CLOSE_COMMAND = "close";
    private static final String NOTE_PARAMETER = "note";
    private static final String SILENTLY_PARAMETER = "silently";
    private static final String LOG_PARAMETER = "log";
    private static final String CLOSE_RESPONSE = "close_response";
    @Autowired
    private ModMailContextCondition requiresModMailCondition;

    @Autowired
    private ModMailThreadManagementService modMailThreadManagementService;

    @Autowired
    private ModMailThreadService modMailThreadService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private Close self;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String note;
        if(slashCommandParameterService.hasCommandOption(NOTE_PARAMETER, event)) {
            note = slashCommandParameterService.getCommandOption(NOTE_PARAMETER, event, String.class);
        } else {
            note = templateService.renderTemplate(MODMAIL_CLOSE_DEFAULT_NOTE_TEMPLATE_KEY, new Object(), event.getGuild().getIdLong());
        }
        Boolean silently;
        if(slashCommandParameterService.hasCommandOption(SILENTLY_PARAMETER, event)) {
            silently = slashCommandParameterService.getCommandOption(SILENTLY_PARAMETER, event, Boolean.class);
        } else {
            silently = false;
        }
        Boolean log;
        if(slashCommandParameterService.hasCommandOption(LOG_PARAMETER, event)) {
            log = slashCommandParameterService.getCommandOption(LOG_PARAMETER, event, Boolean.class);
        } else {
            log = true;
        }
        ClosingContext context = ClosingContext
                .builder()
                .closingMember(event.getMember())
                .notifyUser(!silently)
                .log(log)
                .note(note)
                .build();
        return interactionService.replyEmbed(CLOSE_RESPONSE, event)
                .thenCompose(interactionHook -> self.closeThread(context, event.getChannelIdLong()))
                .thenApply(aVoid -> CommandResult.fromIgnored());
    }

    @Transactional
    public CompletableFuture<Void> closeThread(ClosingContext closingContext, Long channelId) {
        ModMailThread modMailThread = modMailThreadManagementService.getByChannelId(channelId);
        if(ModMailThreadState.CLOSED.equals(modMailThread.getState()) || ModMailThreadState.CLOSING.equals(modMailThread.getState())) {
            throw new ModMailThreadClosedException();
        }
        return modMailThreadService.closeModMailThread(modMailThread, closingContext, new ArrayList<>());
    }

    @Override
    public CommandConfiguration getConfiguration() {
        Parameter noteParameter = Parameter
                .builder()
                .name(NOTE_PARAMETER)
                .type(String.class)
                .remainder(true)
                .optional(true)
                .templated(true)
                .build();

        Parameter silentlyParameter = Parameter
                .builder()
                .name(SILENTLY_PARAMETER)
                .type(Boolean.class)
                .remainder(true)
                .slashCommandOnly(true)
                .optional(true)
                .templated(true)
                .build();

        Parameter logParameter = Parameter
                .builder()
                .name(LOG_PARAMETER)
                .type(Boolean.class)
                .remainder(true)
                .slashCommandOnly(true)
                .optional(true)
                .templated(true)
                .build();

        List<Parameter> parameters = Arrays.asList(noteParameter, silentlyParameter, logParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(ModMailSlashCommandNames.MODMAIL)
                .defaultPrivilege(SlashCommandPrivilegeLevels.INVITER)
                .commandName(CLOSE_COMMAND)
                .build();

        return CommandConfiguration.builder()
                .name(CLOSE_COMMAND)
                .module(ModMailModuleDefinition.MODMAIL)
                .parameters(parameters)
                .help(helpInfo)
                .slashCommandConfig(slashCommandConfig)
                .async(true)
                .slashCommandOnly(true)
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
