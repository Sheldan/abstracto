package dev.sheldan.abstracto.webservices.dictionaryapi.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.webservices.config.WebServicesSlashCommandNames;
import dev.sheldan.abstracto.webservices.config.WebserviceFeatureDefinition;
import dev.sheldan.abstracto.webservices.dictionaryapi.model.template.DictionaryMeaning;
import dev.sheldan.abstracto.webservices.dictionaryapi.model.template.DictionaryDefinition;
import dev.sheldan.abstracto.webservices.dictionaryapi.model.WordMeaning;
import dev.sheldan.abstracto.webservices.dictionaryapi.service.DictionaryApiService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class DictionaryApiDefinitionCommand extends AbstractConditionableCommand {

    private static final String DICTIONARY_DEFINITION_RESPONSE_MODEL_TEMPLATE_KEY = "dictionaryDefinition_response";
    private static final String DICTIONARY_DEFINITION_COMMAND = "dictionaryDefinition";
    private static final String SEARCH_QUERY_PARAMETER = "searchQuery";

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private DictionaryApiService dictionaryApiService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        String parameter = (String) commandContext.getParameters().getParameters().get(0);
        try {
            MessageToSend message = getMessageToSend(commandContext.getGuild().getIdLong(), parameter);
            return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(message, commandContext.getChannel()))
                    .thenApply(unused -> CommandResult.fromSuccess());
        } catch (IOException e) {
            throw new AbstractoRunTimeException(e);
        }
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String query = slashCommandParameterService.getCommandOption(SEARCH_QUERY_PARAMETER, event, String.class);
        try {
            MessageToSend messageToSend = getMessageToSend(event.getGuild().getIdLong(), query);
            return interactionService.replyMessageToSend(messageToSend, event)
                    .thenApply(interactionHook -> CommandResult.fromSuccess());
        } catch (IOException e) {
            throw new AbstractoRunTimeException(e);
        }
    }

    private MessageToSend getMessageToSend(Long serverId, String searchInput) throws IOException {
        WordMeaning meaning = dictionaryApiService.getDefinitions(searchInput);
        List<DictionaryDefinition> definitions = meaning
                .getDefinitions()
                .stream().map(DictionaryDefinition::fromWordDefinition).toList();
        DictionaryMeaning model = DictionaryMeaning
                .builder()
                .definitions(definitions)
                .word(meaning.getWord())
                .build();
        return templateService.renderEmbedTemplate(DICTIONARY_DEFINITION_RESPONSE_MODEL_TEMPLATE_KEY, model, serverId);
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Parameter searchQueryParameter = Parameter
                .builder()
                .name(SEARCH_QUERY_PARAMETER)
                .type(String.class)
                .templated(true)
                .remainder(true)
                .build();
        parameters.add(searchQueryParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        List<String> aliases = Arrays.asList("dict");

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(WebServicesSlashCommandNames.DICTIONARY)
                .commandName("definition")
                .build();

        return CommandConfiguration.builder()
                .name(DICTIONARY_DEFINITION_COMMAND)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
                .async(true)
                .aliases(aliases)
                .supportsEmbedException(true)
                .causesReaction(false)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return WebserviceFeatureDefinition.DICTIONARY;
    }
}
