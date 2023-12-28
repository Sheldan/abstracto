package dev.sheldan.abstracto.webservices.openeweathermap.command;

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
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.webservices.config.WebServicesSlashCommandNames;
import dev.sheldan.abstracto.webservices.config.WebserviceFeatureDefinition;
import dev.sheldan.abstracto.webservices.openweathermap.config.OpenWeatherMapConfig;
import dev.sheldan.abstracto.webservices.openweathermap.exception.LocationNotFoundException;
import dev.sheldan.abstracto.webservices.openweathermap.model.*;
import dev.sheldan.abstracto.webservices.openweathermap.service.OpenWeatherMapService;
import dev.sheldan.abstracto.webservices.openweathermap.service.WeatherService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class OpenWeatherMap extends AbstractConditionableCommand {

    private static final String OPEN_WEATHER_MAP_COMMAND = "openWeatherMap";
    private static final String SEARCH_QUERY_PARAMETER = "searchQuery";
    private static final String OPEN_WEATHER_MAP_RESPONSE_TEMPLATE_KEY = "openWeatherMap_command_response";

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private OpenWeatherMapService openWeatherMapService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private WeatherService weatherService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        String parameter = (String) commandContext.getParameters().getParameters().get(0);
        MessageToSend message = getMessageToSend(commandContext.getGuild().getIdLong(), parameter);
        return FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(message, commandContext.getChannel()))
                .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        String query = slashCommandParameterService.getCommandOption(SEARCH_QUERY_PARAMETER, event, String.class);
        MessageToSend messageToSend = getMessageToSend(event.getGuild().getIdLong(), query);
        return interactionService.replyMessageToSend(messageToSend, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
    }

    private MessageToSend getMessageToSend(Long serverId, String parameter) {
        try {
            GeoCodingResult geoCodingResult = openWeatherMapService.searchForLocation(parameter);
            if(geoCodingResult.getResults().isEmpty()) {
                throw new LocationNotFoundException();
            }
            String languageKey = configService.getStringValueOrConfigDefault(OpenWeatherMapConfig.OPEN_WEATHER_MAP_LANGUAGE_KEY_SYSTEM_CONFIG_KEY, serverId);
            GeoCodingLocation chosenLocation = geoCodingResult.getResults().get(0);
            WeatherResult weatherResult = openWeatherMapService.retrieveWeatherForLocation(chosenLocation, languageKey);
            WeatherResponseModel.WeatherResponseModelBuilder builder = WeatherResponseModel
                    .builder()
                    .description(weatherResult.getWeathers() != null && !weatherResult.getWeathers().isEmpty()
                            ? weatherResult.getWeathers().get(0).getDescription() : null)
                    .mainWeather(weatherResult.getWeathers() != null && !weatherResult.getWeathers().isEmpty()
                            ? weatherResult.getWeathers().get(0).getMain() : null)
                    .clouds(weatherResult.getCloudInfo() != null ? weatherResult.getCloudInfo().getAll() : null)
                    .rain1H(weatherResult.getRainInfo() != null ? weatherResult.getRainInfo().getRain1H() : null)
                    .rain3H(weatherResult.getRainInfo() != null ? weatherResult.getRainInfo().getRain3H() : null)
                    .snow1H(weatherResult.getSnowInfo() != null ? weatherResult.getSnowInfo().getSnow1H() : null)
                    .snow3H(weatherResult.getSnowInfo() != null ? weatherResult.getSnowInfo().getSnow3H() : null)
                    .windSpeed(weatherResult.getWind() != null ? weatherResult.getWind().getSpeed() : null)
                    .visibility(weatherResult.getVisibility())
                    .locationName(chosenLocation.getName())
                    .countryKey(chosenLocation.getCountryKey())
                    .dataCalculationTime(weatherResult.getDayTime() != null ? Instant.ofEpochSecond(weatherResult.getDayTime()) : null);
            Color embedColor = null;
            WeatherResultMain mainWeather = weatherResult.getMainWeather();
            if(mainWeather != null) {
                builder.feelsLikeTemperature(mainWeather.getFeelsLikeTemperature())
                        .temperature(mainWeather.getTemperature())
                        .maxTemperature(mainWeather.getMaxTemperature())
                        .minTemperature(mainWeather.getMinTemperature())
                        .pressure(mainWeather.getPressure())
                        .seaLevelPressure(mainWeather.getSeaLevelPressure())
                        .groundLevelPressure(mainWeather.getGroundLevelPressure())
                        .humidity(mainWeather.getHumidity());
                embedColor = weatherService.getColorForTemperature(mainWeather.getFeelsLikeTemperature());
            }
            builder.embedColor(embedColor);
            WeatherResultSystem systemInfo = weatherResult.getSystemInfo();
            if(systemInfo != null) {
                builder.sunset(systemInfo.getSunset() != null ? Instant.ofEpochSecond(systemInfo.getSunset()) : null);
                builder.sunset(systemInfo.getSunrise() != null ? Instant.ofEpochSecond(systemInfo.getSunrise()) : null);
            }
            builder.locationId(weatherResult.getLocationId());
            return templateService.renderEmbedTemplate(OPEN_WEATHER_MAP_RESPONSE_TEMPLATE_KEY, builder.build(), serverId);
        } catch (IOException e) {
            log.warn("Failed to load weather in server {}", serverId, e);
            throw new AbstractoRunTimeException(e);
        }
    }


    @Override
    public FeatureDefinition getFeature() {
        return WebserviceFeatureDefinition.OPEN_WEATHER_MAP;
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
        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(WebServicesSlashCommandNames.WEATHER)
                .commandName("search")
                .build();

        return CommandConfiguration.builder()
                .name(OPEN_WEATHER_MAP_COMMAND)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .slashCommandConfig(slashCommandConfig)
                .async(true)
                .supportsEmbedException(true)
                .causesReaction(false)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }
}
