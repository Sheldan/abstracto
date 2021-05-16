package dev.sheldan.abstracto.profanityfilter.listener;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncMessageReceivedListener;
import dev.sheldan.abstracto.core.metric.service.CounterMetric;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.metric.service.MetricTag;
import dev.sheldan.abstracto.core.models.database.ProfanityRegex;
import dev.sheldan.abstracto.core.models.listener.MessageReceivedModel;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.service.ProfanityService;
import dev.sheldan.abstracto.core.service.RoleImmunityService;
import dev.sheldan.abstracto.profanityfilter.config.ProfanityFilterFeatureDefinition;
import dev.sheldan.abstracto.profanityfilter.config.ProfanityFilterMode;
import dev.sheldan.abstracto.profanityfilter.service.ProfanityFilterService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Optional;

import static dev.sheldan.abstracto.profanityfilter.service.ProfanityFilterService.PROFANITY_FILTER_EFFECT_KEY;

@Component
@Slf4j
public class ProfanityDetectionListener implements AsyncMessageReceivedListener {

    @Autowired
    private ProfanityService profanityService;

    @Autowired
    private ProfanityFilterService profanityFilterService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private MetricService metricService;

    @Autowired
    private RoleImmunityService roleImmunityService;

    public static final String MODERATION_PURGE_METRIC = "profanity.filter";
    public static final String STEP = "step";

    private static final CounterMetric PROFANITIES_DETECTED_METRIC =
            CounterMetric
                    .builder()
                    .tagList(Arrays.asList(MetricTag.getTag(STEP, "detection")))
                    .name(MODERATION_PURGE_METRIC)
                    .build();

    @Override
    public DefaultListenerResult execute(MessageReceivedModel model) {
        Message message = model.getMessage();
        if(message.isWebhookMessage() || message.getType().isSystem() || !message.isFromGuild()) {
            return DefaultListenerResult.IGNORED;
        }

        if(roleImmunityService.isImmune(message.getMember(), PROFANITY_FILTER_EFFECT_KEY)) {
            log.info("Not checking for profanities in message, because author {} in channel {} in guild {} is immune against profanity filter.",
                    message.getMember().getIdLong(), message.getGuild().getIdLong(), message.getChannel().getIdLong());
            return DefaultListenerResult.IGNORED;
        }

        Long serverId = model.getServerId();
        Optional<ProfanityRegex> potentialProfanityGroup = profanityService.getProfanityRegex(message.getContentRaw(), serverId);
        if(potentialProfanityGroup.isPresent()) {
            metricService.incrementCounter(PROFANITIES_DETECTED_METRIC);
            if(featureModeService.featureModeActive(ProfanityFilterFeatureDefinition.PROFANITY_FILTER, serverId, ProfanityFilterMode.PROFANITY_REPORT)) {
                ProfanityRegex foundProfanityGroup = potentialProfanityGroup.get();
                profanityFilterService.createProfanityReport(message, foundProfanityGroup).exceptionally(throwable -> {
                    log.error("Failed to report or persist profanities in server {} for message {} in channel {}.",
                            serverId, message.getChannel().getIdLong(), message.getIdLong(), throwable);
                    return null;
                });
            }
            if(featureModeService.featureModeActive(ProfanityFilterFeatureDefinition.PROFANITY_FILTER, serverId, ProfanityFilterMode.AUTO_DELETE_PROFANITIES)) {
                messageService.deleteMessage(message).exceptionally(throwable -> {
                    log.error("Failed to delete profanity message with id {} in channel {} in server {}.",
                            message.getIdLong(), message.getChannel().getIdLong(), message.getGuild().getIdLong(), throwable);
                    return null;
                });
            }
            return DefaultListenerResult.PROCESSED;
        }
        return DefaultListenerResult.IGNORED;
    }

    @Override
    public FeatureDefinition getFeature() {
        return ProfanityFilterFeatureDefinition.PROFANITY_FILTER;
    }

    @PostConstruct
    public void postConstruct() {
        metricService.registerCounter(PROFANITIES_DETECTED_METRIC, "Amount of profanities detected");
    }
}
