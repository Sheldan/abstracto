package dev.sheldan.abstracto.webservices.currencyconversion.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandAutoCompleteService;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.webservices.config.WebServicesSlashCommandNames;
import dev.sheldan.abstracto.webservices.config.WebserviceFeatureDefinition;
import dev.sheldan.abstracto.webservices.currencyconversion.model.ConvertCurrencyResponseModel;
import dev.sheldan.abstracto.webservices.currencyconversion.model.Currency;
import dev.sheldan.abstracto.webservices.currencyconversion.service.CurrencyConversionApiService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConvertCurrency extends AbstractConditionableCommand {

    private static final String SOURCE_CURRENCY_PARAMETER = "sourceCurrency";
    private static final String TARGET_CURRENCY_PARAMETER = "targetCurrency";
    private static final String VALUE_PARAMETER = "value";
    private static final String CONVERT_CURRENCY_RESPONSE_TEMPLATE = "convertCurrency_response";

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private CurrencyConversionApiService currencyConversionApiService;

    @Autowired
    private SlashCommandAutoCompleteService slashCommandAutoCompleteService;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String sourceCurrencyText = slashCommandParameterService.getCommandOption(SOURCE_CURRENCY_PARAMETER, event, String.class);
        String targetCurrencyText = slashCommandParameterService.getCommandOption(TARGET_CURRENCY_PARAMETER, event, String.class);
        Double value = slashCommandParameterService.getCommandOption(VALUE_PARAMETER, event, Double.class);
        Currency sourceCurrency = currencyConversionApiService.getCurrencyForString(sourceCurrencyText);
        Currency targetCurrency = currencyConversionApiService.getCurrencyForString(targetCurrencyText);
        Double convertedValue = currencyConversionApiService.convertCurrency(sourceCurrency, targetCurrency, value);
        ConvertCurrencyResponseModel responseModel = ConvertCurrencyResponseModel
            .builder()
            .sourceCurrency(sourceCurrency)
            .targetCurrency(targetCurrency)
            .sourceValue(value)
            .targetValue(convertedValue)
            .build();
        return interactionService.replyEmbed(CONVERT_CURRENCY_RESPONSE_TEMPLATE, responseModel, event)
            .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    @Override
    public List<String> performAutoComplete(CommandAutoCompleteInteractionEvent event) {
        if(slashCommandAutoCompleteService.matchesParameter(event.getFocusedOption(), SOURCE_CURRENCY_PARAMETER)
            || slashCommandAutoCompleteService.matchesParameter(event.getFocusedOption(), TARGET_CURRENCY_PARAMETER)) {
            String input = event.getFocusedOption().getValue().toLowerCase();
            List<Currency> supportedCurrencies = currencyConversionApiService.getSupportedCurrencies();
            Set<String> currencies = new HashSet<>();
            supportedCurrencies.forEach(currency -> {
                currencies.add(currency.getCode().toLowerCase());
                currencies.add(currency.getName().toLowerCase());
                currencies.add(currency.getSymbol().toLowerCase());
                if(currency.getSymbolNative() != null) {
                    currencies.add(currency.getSymbolNative().toLowerCase());
                }
            });
            if(!input.isEmpty()) {
                return currencies.stream().filter(s -> s.contains(input)).toList();
            } else {
                return currencies.stream().toList();
            }
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Parameter sourceCurrencyParameter = Parameter
            .builder()
            .name(SOURCE_CURRENCY_PARAMETER)
            .type(String.class)
            .supportsAutoComplete(true)
            .templated(true)
            .build();
        parameters.add(sourceCurrencyParameter);
        Parameter targetCurrencyParameter = Parameter
            .builder()
            .name(TARGET_CURRENCY_PARAMETER)
            .type(String.class)
            .supportsAutoComplete(true)
            .templated(true)
            .build();
        parameters.add(targetCurrencyParameter);
        Parameter valueParameter = Parameter
            .builder()
            .name(VALUE_PARAMETER)
            .type(Double.class)
            .templated(true)
            .build();
        parameters.add(valueParameter);
        HelpInfo helpInfo = HelpInfo
            .builder()
            .templated(true)
            .build();


        SlashCommandConfig slashCommandConfig = SlashCommandConfig
            .builder()
            .enabled(true)
            .rootCommandName(WebServicesSlashCommandNames.CONVERSION)
            .commandName("currency")
            .build();

        return CommandConfiguration.builder()
            .name("convertCurrency")
            .module(UtilityModuleDefinition.UTILITY)
            .templated(true)
            .slashCommandConfig(slashCommandConfig)
            .async(true)
            .supportsEmbedException(true)
            .slashCommandOnly(true)
            .causesReaction(false)
            .parameters(parameters)
            .help(helpInfo)
            .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return WebserviceFeatureDefinition.CURRENCY_CONVERSION;
    }
}
