package dev.sheldan.abstracto.core.interactive;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
@Slf4j
public class InteractiveServiceBean implements InteractiveService {

    @Autowired
    private ChannelService channelService;

    @Autowired
    private EventWaiter eventWaiter;

    @Autowired
    private MessageService messageService;

    @Autowired
    private EmoteService emoteService;

    @Override
    public void createMessageWithResponse(String messageText, AUserInAServer responder, AChannel channel, Long messageId, Consumer<MessageReceivedEvent> action, Runnable finalAction) {
        channelService.sendTextToAChannel(messageText, channel);
        Long channelId = channel.getId();
        eventWaiter.waitForEvent(MessageReceivedEvent.class, event -> {
            if(event != null) {
                return event.getAuthor().getIdLong() == responder.getUserReference().getId() && event.getMessage().getIdLong() != messageId && event.getMessage().getChannel().getIdLong() == channelId;
            }
            return false;
        }, action, 1, TimeUnit.MINUTES, finalAction);
    }

    @Override
    public void createMessageWithResponse(MessageToSend messageToSend, AUserInAServer responder, AChannel channel, Long messageId, Consumer<MessageReceivedEvent> action, Runnable finalAction) {
        channelService.sendMessageEmbedToSendToAChannel(messageToSend, channel);
        Long userId = responder.getUserReference().getId();
        eventWaiter.waitForEvent(MessageReceivedEvent.class, event -> {
            if(event != null) {
                return event.getAuthor().getIdLong() == userId && event.getMessage().getIdLong() != messageId;
            }
            return false;
        }, action, 1, TimeUnit.MINUTES, finalAction);
    }


    @Override
    public void createMessageWithConfirmation(String text, AUserInAServer responder, AChannel channel, Long messageId, Consumer<Void> confirmation, Consumer<Void> denial, Runnable finalAction) {
        Long serverId = responder.getServerReference().getId();
        ButtonMenu.Builder builder = new ButtonMenu.Builder();
        HashMap<String, Consumer<Void>> actions = new HashMap<>();

        addEmoteToBuilder("confirmation", confirmation, serverId, builder, actions);
        addEmoteToBuilder("denial", denial, serverId, builder, actions);

        ButtonMenu menu = builder
                .setEventWaiter(eventWaiter)
                .setDescription(text)
                .setAction(reactionEmote -> {
                    if(reactionEmote.isEmoji()) {
                        actions.get(reactionEmote.getEmoji()).accept(null);
                    } else {
                        actions.get(reactionEmote.getEmote().getId()).accept(null);
                    }
                })
                .build();
        Optional<TextChannel> textChannelInGuild = channelService.getTextChannelFromServerOptional(serverId, channel.getId());
        textChannelInGuild.ifPresent(menu::display);
    }

    private void addEmoteToBuilder(String key, Consumer<Void> consumer, Long serverId, ButtonMenu.Builder builder, HashMap<String, Consumer<Void>> actions) {
        AEmote emoteOrFakeEmote = emoteService.getEmoteOrDefaultEmote(key, serverId);
        if(Boolean.TRUE.equals(emoteOrFakeEmote.getCustom())){
            Optional<Emote> emote = emoteService.getEmote(serverId, emoteOrFakeEmote);
            emote.ifPresent(emote1 -> {
                builder.addChoice(emote1);
                actions.put(emote1.getId(), consumer);
            });
        } else {
            builder.addChoice(emoteOrFakeEmote.getEmoteKey());
            actions.put(emoteOrFakeEmote.getEmoteKey(), consumer);
        }
    }


}
