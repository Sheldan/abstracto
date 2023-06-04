package dev.sheldan.abstracto.core.interaction.menu;

import com.google.gson.Gson;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.interaction.ComponentPayloadManagementService;
import dev.sheldan.abstracto.core.interaction.InteractionResult;
import dev.sheldan.abstracto.core.interaction.menu.listener.StringSelectMenuListener;
import dev.sheldan.abstracto.core.interaction.menu.listener.StringSelectMenuListenerModel;
import dev.sheldan.abstracto.core.models.database.ComponentPayload;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
public class StringSelectMenuListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<StringSelectMenuListener> listenerList;

    @Autowired
    @Qualifier("buttonClickedExecutor")
    private TaskExecutor buttonClickedExecutor;

    @Autowired
    private StringSelectMenuListenerBean self;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private ComponentPayloadManagementService componentPayloadManagementService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private Gson gson;

    @Override
    public void onStringSelectInteraction(@Nonnull StringSelectInteractionEvent event) {
        if(listenerList == null) return;
        event.deferEdit().queue();
        CompletableFuture.runAsync(() ->  self.executeListenerLogic(event), buttonClickedExecutor).exceptionally(throwable -> {
            log.error("Failed to execute listener logic in async button event.", throwable);
            return null;
        });
    }

    @Transactional
    public void executeListenerLogic(StringSelectInteractionEvent event) {
        StringSelectMenuListenerModel model = null;
        StringSelectMenuListener listener = null;
        try {
            Optional<ComponentPayload> callbackInformation = componentPayloadManagementService.findPayload(event.getComponentId());
            if(callbackInformation.isPresent()) {
                model = getModel(event, callbackInformation.get());
                List<StringSelectMenuListener> validListener = filterFeatureAwareListener(listenerList, model);
                Optional<StringSelectMenuListener> listenerOptional = findListener(validListener, model);
                if(listenerOptional.isPresent()) {
                    listener = listenerOptional.get();
                    log.info("Executing string select menu listener {} for event for id {}.", listener.getClass().getSimpleName(), event.getComponentId());
                    listener.execute(model);
                } else {
                    log.warn("No listener found for string select menu event for id {}.", event.getComponentId());
                }
            } else {
                log.warn("No callback found for id {}.", event.getComponentId());
            }
        } catch (Exception exception) {
            if(event.isFromGuild()) {
                log.error("String select menu listener failed with exception in server {} and channel {}.", event.getGuild().getIdLong(),
                        event.getGuildChannel().getIdLong(), exception);
            } else {
                log.error("String select menu listener failed with exception outside of a guild.", exception);
            }
        }
    }


    private Optional<StringSelectMenuListener> findListener(List<StringSelectMenuListener> featureAwareListeners, StringSelectMenuListenerModel model) {
        return featureAwareListeners.stream().filter(asyncButtonClickedListener -> asyncButtonClickedListener.handlesEvent(model)).findFirst();
    }

    private List<StringSelectMenuListener> filterFeatureAwareListener(List<StringSelectMenuListener> featureAwareListeners, StringSelectMenuListenerModel model) {
        return featureAwareListeners.stream().filter(trFeatureAwareListener -> {
            FeatureConfig feature = featureConfigService.getFeatureDisplayForFeature(trFeatureAwareListener.getFeature());
            if(!model.getEvent().isFromGuild()) {
                return true;
            }
            if (!featureFlagService.isFeatureEnabled(feature, model.getServerId())) {
                return false;
            }
            return featureModeService.necessaryFeatureModesMet(trFeatureAwareListener, model.getServerId());
        }).collect(Collectors.toList());
    }

    private StringSelectMenuListenerModel getModel(StringSelectInteractionEvent event, ComponentPayload componentPayload) throws ClassNotFoundException {
        SelectMenuPayload payload = null;
        if(componentPayload.getPayloadType() != null && componentPayload.getPayload() != null) {
            payload = (SelectMenuPayload) gson.fromJson(componentPayload.getPayload(), Class.forName(componentPayload.getPayloadType()));
        }
        return StringSelectMenuListenerModel
                .builder()
                .event(event)
                .deserializedPayload(payload)
                .payload(componentPayload.getPayload())
                .origin(componentPayload.getOrigin())
                .build();
    }

    @PostConstruct
    public void postConstruct() {
        BeanUtils.sortPrioritizedListeners(listenerList);
    }
}
