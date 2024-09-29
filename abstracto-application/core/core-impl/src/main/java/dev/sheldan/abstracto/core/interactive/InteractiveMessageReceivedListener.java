package dev.sheldan.abstracto.core.interactive;

import dev.sheldan.abstracto.core.command.config.features.CoreFeatureDefinition;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interactive.setup.callback.MessageInteractionCallback;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.listener.async.jda.AsyncMessageReceivedListener;
import dev.sheldan.abstracto.core.models.listener.MessageReceivedModel;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class InteractiveMessageReceivedListener implements AsyncMessageReceivedListener {

    // server -> channel -> user
    // TODO timeout
    private Map<Long, Map<Long, Map<Long, MessageInteractionCallback>>> callbacks = new HashMap<>();

    @Autowired
    private Tracer tracer;

    private static final Lock runTimeLock = new ReentrantLock();

    @Override
    public DefaultListenerResult execute(MessageReceivedModel model) {
        if(model.getServerId() == null) {
            return DefaultListenerResult.IGNORED;
        }
        if(executeCallback(model)) {
            return DefaultListenerResult.PROCESSED;
        }
        return DefaultListenerResult.IGNORED;
    }

    @Override
    public FeatureDefinition getFeature() {
        return CoreFeatureDefinition.CORE_FEATURE;
    }

    public boolean executeCallback(MessageReceivedModel model) {
        runTimeLock.lock();
        Span newSpan = tracer.nextSpan().name("interactive-message-tracker");
        try (Tracer.SpanInScope ws = this.tracer.withSpan(newSpan.start())){
            if(callbacks.containsKey(model.getServerId())) {
                Map<Long, Map<Long, MessageInteractionCallback>> channelMap = callbacks.get(model.getServerId());
                if(channelMap.containsKey(model.getMessage().getChannel().getIdLong())) {
                    Map<Long, MessageInteractionCallback> userMap = channelMap.get(model.getMessage().getChannel().getIdLong());
                    if(userMap.containsKey(model.getMessage().getAuthor().getIdLong())) {
                        MessageInteractionCallback foundCallback = userMap.get(model.getMessage().getAuthor().getIdLong());
                        if(foundCallback.getCondition() == null || foundCallback.getCondition().test(model)) {
                            userMap.remove(model.getMessage().getAuthor().getIdLong());
                            foundCallback.getAction().accept(model);
                            return true;
                        }
                    }
                }
            }
        } finally {
            newSpan.end();
            runTimeLock.unlock();
        }
        return false;
    }

    public void addCallback(MessageInteractionCallback interactiveCallBack) {
        runTimeLock.lock();
        try {
            Map<Long, Map<Long, MessageInteractionCallback>> channelMap = new HashMap<>();
            if(callbacks.containsKey(interactiveCallBack.getServerId())) {
                channelMap = callbacks.get(interactiveCallBack.getServerId());
            } else {
                callbacks.put(interactiveCallBack.getServerId(), channelMap);
            }
            Map<Long, MessageInteractionCallback> userMap = new HashMap<>();
            if(channelMap.containsKey(interactiveCallBack.getChannelId())) {
                userMap = channelMap.get(interactiveCallBack.getChannelId());
            } else {
                channelMap.put(interactiveCallBack.getChannelId(), userMap);
            }
            if(!userMap.containsKey(interactiveCallBack.getUserId())) {
                userMap.put(interactiveCallBack.getUserId(), interactiveCallBack);
            }
        } finally {
            runTimeLock.unlock();
        }
    }

}
