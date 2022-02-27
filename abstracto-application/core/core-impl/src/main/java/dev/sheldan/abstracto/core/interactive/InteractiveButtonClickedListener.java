package dev.sheldan.abstracto.core.interactive;

import com.google.gson.Gson;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.interactive.setup.payload.SetupConfirmationPayload;
import dev.sheldan.abstracto.core.listener.ButtonClickedListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.ButtonClickedListener;
import dev.sheldan.abstracto.core.models.listener.ButtonClickedListenerModel;
import dev.sheldan.abstracto.core.service.DelayedActionService;
import dev.sheldan.abstracto.core.service.FeatureSetupServiceBean;
import dev.sheldan.abstracto.core.service.management.ComponentPayloadManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class InteractiveButtonClickedListener implements ButtonClickedListener {

    @Autowired
    private InteractiveButtonClickedListener self;

    @Autowired
    private DelayedActionService delayedActionService;

    @Autowired
    private ComponentPayloadManagementService componentPayloadManagementService;

    @Autowired
    private FeatureSetupServiceBean featureSetupServiceBean;

    @Autowired
    private Gson gson;

    @Override
    public ButtonClickedListenerResult execute(ButtonClickedListenerModel model) {
        SetupConfirmationPayload payload = (SetupConfirmationPayload) model.getDeserializedPayload();
        try {
            if(payload.getAction().equals(SetupConfirmationPayload.SetupConfirmationAction.CONFIRM)) {
                self.executeDelayedSteps(payload.getActions());
                featureSetupServiceBean.notifyAboutCompletion(payload.getOrigin(), payload.getFeatureKey(), SetupStepResult.fromSuccess());
                return ButtonClickedListenerResult.ACKNOWLEDGED;
            } else {
                featureSetupServiceBean.notifyAboutCompletion(payload.getOrigin(), payload.getFeatureKey(), SetupStepResult.fromCancelled());
                return ButtonClickedListenerResult.IGNORED;
            }
        } finally {
            cleanup(model, payload);
        }
    }

    private void cleanup(ButtonClickedListenerModel model, SetupConfirmationPayload payload) {
        log.debug("Cleaning up component {} and {}.", payload.getOtherButtonComponentId(), model.getEvent().getComponentId());
        componentPayloadManagementService.deletePayloads(Arrays.asList(payload.getOtherButtonComponentId(), model.getEvent().getComponentId()));
    }

    @Transactional
    public void executeDelayedSteps(List<DelayedActionConfigContainer> actions) {
        List<DelayedActionConfig> delayedActionConfigs = new ArrayList<>();
        actions.forEach(container -> delayedActionConfigs.add(container.getObject()));
        delayedActionService.executeDelayedActions(delayedActionConfigs);

    }

    @Override
    public Boolean handlesEvent(ButtonClickedListenerModel model) {
        return model.getDeserializedPayload() instanceof SetupConfirmationPayload && model.getEvent().isFromGuild();
    }

    @Override
    public Integer getPriority() {
        return ListenerPriority.MEDIUM;
    }

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }
}
