package dev.sheldan.abstracto.core.interaction.modal.listener;

import com.google.gson.Gson;
import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.interaction.ComponentPayloadManagementService;
import dev.sheldan.abstracto.core.interaction.InteractionResult;
import dev.sheldan.abstracto.core.interaction.modal.ModalPayload;
import dev.sheldan.abstracto.core.interaction.modal.ModalPostInteractionExecution;
import dev.sheldan.abstracto.core.models.database.ComponentPayload;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
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
public class ModalInteractionListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<ModalInteractionListener> listenerList;

    @Autowired(required = false)
    private List<ModalPostInteractionExecution> postInteractionExecutions;

    @Autowired
    @Qualifier("modalInteractionExecutor")
    private TaskExecutor modalInteractionExecutor;

    @Autowired
    private ModalInteractionListenerBean self;

    @Autowired
    private Gson gson;

    @Autowired
    private ComponentPayloadManagementService componentPayloadManagementService;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private FeatureModeService featureModeService;

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if(listenerList == null) return;
        // TODO remove this and make this configurable
        event.deferEdit().queue();
        CompletableFuture.runAsync(() ->  self.executeListenerLogic(event), modalInteractionExecutor).exceptionally(throwable -> {
            log.error("Failed to execute listener logic in modal interaction event.", throwable);
            return null;
        });
    }

    @Transactional
    public void executeListenerLogic(@NotNull ModalInteractionEvent event) {
        ModalInteractionListenerModel model = null;
        ModalInteractionListener listener = null;
        try {
            Optional<ComponentPayload> callbackInformation = componentPayloadManagementService.findPayload(event.getModalId());
            if(callbackInformation.isPresent()) {
                model = getModel(event, callbackInformation.get());
                List<ModalInteractionListener> validListener = filterFeatureAwareListener(listenerList, model);
                Optional<ModalInteractionListener> listenerOptional = findListener(validListener, model);
                if(listenerOptional.isPresent()) {
                    listener = listenerOptional.get();
                    log.info("Executing modal listener {} for event for id {}.", listener.getClass().getSimpleName(), event.getModalId());
                    listener.execute(model);
                    InteractionResult result = InteractionResult.fromSuccess();
                    for (ModalPostInteractionExecution postInteractionExecution : postInteractionExecutions) {
                        postInteractionExecution.execute(model, result, listener);
                    }
                } else {
                    log.warn("No listener found for button event for id {}.", event.getModalId());
                }
            } else {
                log.warn("No callback found for id {}.", event.getModalId());
            }
        } catch (Exception exception) {
            if(event.isFromGuild()) {
                log.error("Modal interaction listener failed with exception in server {} and channel {}.", event.getGuild().getIdLong(),
                        event.getGuildChannel().getIdLong(), exception);
            } else {
                log.error("Modal interaction clicked listener failed with exception outside of a guild.", exception);
            }
            if(model != null && listener != null) {
                InteractionResult result = InteractionResult.fromError("Failed to execute interaction.", exception);
                if(postInteractionExecutions != null) {
                    for (ModalPostInteractionExecution postInteractionExecution : postInteractionExecutions) {
                        postInteractionExecution.execute(model, result, listener);
                    }
                }
            }
        }
    }

    private Optional<ModalInteractionListener> findListener(List<ModalInteractionListener> featureAwareListeners, ModalInteractionListenerModel model) {
        return featureAwareListeners.stream().filter(modelInteractionListener -> modelInteractionListener.handlesEvent(model)).findFirst();
    }

    private List<ModalInteractionListener> filterFeatureAwareListener(List<ModalInteractionListener> featureAwareListeners, ModalInteractionListenerModel model) {
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

    private ModalInteractionListenerModel getModel(ModalInteractionEvent event, ComponentPayload componentPayload) throws ClassNotFoundException {
        ModalPayload payload = null;
        if(componentPayload.getPayloadType() != null && componentPayload.getPayload() != null) {
            payload = (ModalPayload) gson.fromJson(componentPayload.getPayload(), Class.forName(componentPayload.getPayloadType()));
        }
        return ModalInteractionListenerModel
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
