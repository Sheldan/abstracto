package dev.sheldan.abstracto.webservices.wikipedia.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.UserCommandConfig;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.ContextUtils;
import dev.sheldan.abstracto.webservices.config.WebServicesSlashCommandNames;
import dev.sheldan.abstracto.webservices.config.WebserviceFeatureDefinition;
import dev.sheldan.abstracto.webservices.wikipedia.config.WikipediaFeatureConfig;
import dev.sheldan.abstracto.webservices.wikipedia.model.WikipediaArticleSummary;
import dev.sheldan.abstracto.webservices.wikipedia.model.template.WikipediaArticleSummaryModel;
import dev.sheldan.abstracto.webservices.wikipedia.service.WikipediaService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class WikipediaArticleSummaryCommand extends AbstractConditionableCommand {

    private static final String WIKIPEDIA_ARTICLE_SUMMARY_RESPONSE_MODEL_TEMPLATE_KEY = "wikipediaArticleSummary_response";
    private static final String WIKIPEDIA_ARTICLE_SUMMARY_COMMAND = "wikipediaArticleSummary";
    private static final String SEARCH_QUERY_PARAMETER = "searchQuery";
    private static final String LANGUAGE_PARAMETER = "language";

    @Autowired
    private WikipediaService wikipediaService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private ConfigService configService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String query = slashCommandParameterService.getCommandOption(SEARCH_QUERY_PARAMETER, event, String.class);
        String language;
        if(slashCommandParameterService.hasCommandOption(LANGUAGE_PARAMETER, event)) {
            language = slashCommandParameterService.getCommandOption(LANGUAGE_PARAMETER, event, String.class);
        } else {
            language = null;
        }
        try {
            MessageToSend messageToSend = getMessageToSend(ContextUtils.serverIdOrNull(event), query, language);
            return interactionService.replyMessageToSend(messageToSend, event)
                    .thenApply(interactionHook -> CommandResult.fromSuccess());
        } catch (IOException e) {
            throw new AbstractoRunTimeException(e);
        }
    }

    private MessageToSend getMessageToSend(Long serverId, String searchInput, String language) throws IOException {
        String languageKey = language != null ? language :
                configService.getStringValueOrConfigDefault(WikipediaFeatureConfig.WIKIPEDIA_LANGUAGE_KEY_SYSTEM_CONFIG_KEY, serverId);
        WikipediaArticleSummary definition = wikipediaService.getSummary(searchInput, languageKey);
        WikipediaArticleSummaryModel model = WikipediaArticleSummaryModel
                .builder()
                .summary(definition.getSummary())
                .fullURL(definition.getFullURL())
                .title(definition.getTitle())
                .build();
        return templateService.renderEmbedTemplate(WIKIPEDIA_ARTICLE_SUMMARY_RESPONSE_MODEL_TEMPLATE_KEY, model, serverId);
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Parameter searchQueryParameter = Parameter
                .builder()
                .name(SEARCH_QUERY_PARAMETER)
                .type(String.class)
                .remainder(true)
                .templated(true)
                .build();
        parameters.add(searchQueryParameter);
        Parameter languageParameter = Parameter
                .builder()
                .name(LANGUAGE_PARAMETER)
                .type(String.class)
                .slashCommandOnly(true)
                .optional(true)
                .templated(true)
                .build();
        parameters.add(languageParameter);
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        List<String> aliases = Arrays.asList("wiki");

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .userInstallable(true)
                .userCommandConfig(UserCommandConfig.all())
                .rootCommandName(WebServicesSlashCommandNames.WIKIPEDIA)
                .groupName("article")
                .commandName("summary")
                .build();

        return CommandConfiguration.builder()
                .name(WIKIPEDIA_ARTICLE_SUMMARY_COMMAND)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
                .async(true)
                .aliases(aliases)
                .supportsEmbedException(true)
                .slashCommandOnly(true)
                .causesReaction(false)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return WebserviceFeatureDefinition.WIKIPEDIA;
    }
}
