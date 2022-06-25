package dev.sheldan.abstracto.core.interaction.context.message;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.interaction.InteractionResult;
import dev.sheldan.abstracto.core.interaction.context.message.listener.MessageContextCommandListener;
import dev.sheldan.abstracto.core.models.listener.interaction.MessageContextInteractionModel;
import dev.sheldan.abstracto.core.service.FeatureConfigService;
import dev.sheldan.abstracto.core.service.FeatureFlagService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MessageContextCommandListenerBean extends ListenerAdapter {

    @Autowired(required = false)
    private List<MessageContextCommandListener> listenerList;

    @Autowired(required = false)
    private List<MessageContextPostInteractionExecution> postInteractionExecutions;

    @Autowired
    @Qualifier("messageContextCommandExecutor")
    private TaskExecutor messageContextCommandExecutor;

    @Autowired
    private MessageContextCommandListenerBean self;

    @Autowired
    private FeatureConfigService featureConfigService;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private FeatureModeService featureModeService;

    @Override
    public void onMessageContextInteraction(@NotNull MessageContextInteractionEvent event) {
        if(listenerList == null) return;
        CompletableFuture.runAsync(() ->  self.executeListenerLogic(event), messageContextCommandExecutor).exceptionally(throwable -> {
            log.error("Failed to execute listener logic in async button event.", throwable);
            return null;
        });
    }

    @Transactional
    public void executeListenerLogic(MessageContextInteractionEvent event) {
        MessageContextInteractionModel model = MessageContextInteractionModel
                .builder()
                .event(event)
                .build();
        MessageContextCommandListener listener = null;
        try {
            List<MessageContextCommandListener> validListener = filterFeatureAwareListener(listenerList, model);
            Optional<MessageContextCommandListener> listenerOptional = findListener(validListener, model);
            if(listenerOptional.isPresent()) {
                listener = listenerOptional.get();
                listener.execute(model);
                InteractionResult result = InteractionResult.fromSuccess();
                for (MessageContextPostInteractionExecution postInteractionExecution : postInteractionExecutions) {
                    postInteractionExecution.execute(model, result, listener);
                }
            } else {
                log.info("No listener found for event {}", event.getClass().getSimpleName());
            }
        } catch (Exception exception) {
            if(event.isFromGuild()) {
                log.error("Message context listener failed with exception in server {} and channel {}.", event.getGuild().getIdLong(),
                        event.getGuildChannel().getIdLong(), exception);
            } else {
                log.error("Message context listener failed with exception outside of a guild.", exception);
            }
            if(model != null && listener != null) {
                InteractionResult result = InteractionResult.fromError("Failed to execute message context interaction.", exception);
                if(postInteractionExecutions != null) {
                    for (MessageContextPostInteractionExecution postInteractionExecution : postInteractionExecutions) {
                        postInteractionExecution.execute(model, result, listener);
                    }
                }
            }
        }
    }

    private Optional<MessageContextCommandListener> findListener(List<MessageContextCommandListener> featureAwareListeners, MessageContextInteractionModel model) {
        return featureAwareListeners.stream().filter(contextListener -> contextListener.handlesEvent(model)).findFirst();
    }

    private List<MessageContextCommandListener> filterFeatureAwareListener(List<MessageContextCommandListener> featureAwareListeners, MessageContextInteractionModel model) {
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

    public List<MessageContextCommandListener> getListenerList() {
        return listenerList;
    }

}
