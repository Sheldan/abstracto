package dev.sheldan.abstracto.core.interaction.button.listener;

import com.google.gson.Gson;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.interaction.InteractionResult;
import dev.sheldan.abstracto.core.interaction.button.ButtonPostInteractionExecution;
import dev.sheldan.abstracto.core.models.database.ComponentPayload;
import dev.sheldan.abstracto.core.interaction.button.ButtonPayload;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.interaction.ComponentPayloadManagementService;
import dev.sheldan.abstracto.core.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SyncButtonClickedListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<ButtonClickedListener> listenerList;

    @Autowired(required = false)
    private List<ButtonPostInteractionExecution> postInteractionExecutions;

    @Autowired
    @Qualifier("buttonClickedExecutor")
    private TaskExecutor buttonClickedExecutor;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private SyncButtonClickedListenerBean self;

    @Autowired
    private ComponentPayloadManagementService componentPayloadManagementService;

    @Autowired
    private Gson gson;

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if(listenerList == null) return;
        // TODO remove this and make this configurable
        event.deferEdit().queue();
        CompletableFuture.runAsync(() ->  self.executeListenerLogic(event), buttonClickedExecutor).exceptionally(throwable -> {
            log.error("Failed to execute listener logic in async button event.", throwable);
            return null;
        });
    }

    @Transactional
    public void executeListenerLogic(@NotNull ButtonInteractionEvent event) {
        ButtonClickedListenerModel model = null;
        ButtonClickedListener listener = null;
        try {
            Optional<ComponentPayload> callbackInformation = componentPayloadManagementService.findPayload(event.getComponentId());
            if(callbackInformation.isPresent()) {
                model = getModel(event, callbackInformation.get());
                List<ButtonClickedListener> validListener = filterFeatureAwareListener(listenerList, model);
                Optional<ButtonClickedListener> listenerOptional = findListener(validListener, model);
                if(listenerOptional.isPresent()) {
                    listener = listenerOptional.get();
                    log.info("Executing button listener {} for event for id {}.", listener.getClass().getSimpleName(), event.getComponentId());
                    listener.execute(model);
                    InteractionResult result = InteractionResult.fromSuccess();
                    for (ButtonPostInteractionExecution postInteractionExecution : postInteractionExecutions) {
                        postInteractionExecution.execute(model, result, listener);
                    }
                } else {
                    log.warn("No listener found for button event for id {}.", event.getComponentId());
                }
            } else {
                log.warn("No callback found for id {}.", event.getComponentId());
            }
        } catch (Exception exception) {
            if(event.isFromGuild()) {
                log.error("Button clicked listener failed with exception in server {} and channel {}.", event.getGuild().getIdLong(),
                        event.getGuildChannel().getIdLong(), exception);
            } else {
                log.error("Button clicked listener failed with exception outside of a guild.", exception);
            }
            if(model != null && listener != null) {
                InteractionResult result = InteractionResult.fromError("Failed to execute interaction.", exception);
                if(postInteractionExecutions != null) {
                    for (ButtonPostInteractionExecution postInteractionExecution : postInteractionExecutions) {
                        postInteractionExecution.execute(model, result, listener);
                    }
                }
            }
        }
    }


    private Optional<ButtonClickedListener> findListener(List<ButtonClickedListener> featureAwareListeners, ButtonClickedListenerModel model) {
        return featureAwareListeners.stream().filter(asyncButtonClickedListener -> asyncButtonClickedListener.handlesEvent(model)).findFirst();
    }

    private List<ButtonClickedListener> filterFeatureAwareListener(List<ButtonClickedListener> featureAwareListeners, ButtonClickedListenerModel model) {
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

    private ButtonClickedListenerModel getModel(ButtonInteractionEvent event, ComponentPayload componentPayload) throws ClassNotFoundException {
        ButtonPayload payload = null;
        if(componentPayload.getPayloadType() != null && componentPayload.getPayload() != null) {
            payload = (ButtonPayload) gson.fromJson(componentPayload.getPayload(), Class.forName(componentPayload.getPayloadType()));
        }
        return ButtonClickedListenerModel
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
