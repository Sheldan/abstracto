package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.config.FeatureConfig;
import dev.sheldan.abstracto.core.listener.async.MessageContextCommandListener;
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

        List<MessageContextCommandListener> validListener = filterFeatureAwareListener(listenerList, model);
        Optional<MessageContextCommandListener> listenerOptional = findListener(validListener, model);
        if(listenerOptional.isPresent()) {
            MessageContextCommandListener listener = listenerOptional.get();
            listener.execute(model);
        } else {
            log.info("No listener found for event {}", event.getClass().getSimpleName());
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
