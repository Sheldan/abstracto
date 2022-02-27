package dev.sheldan.abstracto.core.listener.sync.jda;

import dev.sheldan.abstracto.core.command.service.ExceptionService;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.utils.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.List;

@Component
@Slf4j
public class PrivateMessageReceivedListenerBean extends ListenerAdapter {

    @Autowired
    private BotService botService;

    @Autowired
    private ExceptionService exceptionService;

    @Autowired(required = false)
    private List<PrivateMessageReceivedListener> privateMessageReceivedListeners;

    @Autowired
    private PrivateMessageReceivedListenerBean self;

    @Override
    @Transactional
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if(!event.isFromType(ChannelType.PRIVATE)) return;
        if(privateMessageReceivedListeners == null) return;
        if(event.getAuthor().getId().equals(botService.getInstance().getSelfUser().getId())) {
            return;
        }
        privateMessageReceivedListeners.forEach(messageReceivedListener -> {
            try {
                self.executeIndividualPrivateMessageReceivedListener(event, messageReceivedListener);
            } catch (Exception e) {
                log.error("Listener {} had exception when executing.", messageReceivedListener, e);
                exceptionService.reportExceptionToPrivateMessageReceivedContext(e, event);
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public void executeIndividualPrivateMessageReceivedListener(@Nonnull MessageReceivedEvent event, PrivateMessageReceivedListener messageReceivedListener) {
        // no feature flag check, because we are in no server context
        log.debug("Executing private message listener {} for member {}.", messageReceivedListener.getClass().getName(), event.getAuthor().getId());
        messageReceivedListener.execute(event.getMessage());
    }

    @PostConstruct
    public void postConstruct() {
        BeanUtils.sortPrioritizedListeners(privateMessageReceivedListeners);
    }

}
