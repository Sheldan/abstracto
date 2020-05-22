package dev.sheldan.abstracto.core.interactive;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import dev.sheldan.abstracto.core.models.FullUser;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AEmote;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.BotService;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.MessageService;
import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
@Slf4j
public class InteractiveServiceBean implements InteractiveService {

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private EventWaiter eventWaiter;

    @Autowired
    private MessageService messageService;

    @Autowired
    private EmoteService emoteService;

    @Autowired
    private BotService botService;

    @Override
    public void createMessageWithResponse(String messageText, AUserInAServer responder, AChannel channel, Long messageId, Consumer<MessageReceivedEvent> action, Runnable finalAction) {
        channelService.sendTextToAChannel(messageText, channel);
        eventWaiter.waitForEvent(MessageReceivedEvent.class, event -> {
            if(event != null) {
                return event.getAuthor().getIdLong() == responder.getUserReference().getId() && event.getMessage().getIdLong() != messageId;
            }
            return false;
        }, action, 1, TimeUnit.MINUTES, finalAction);
    }

    @Override
    public void createMessageWithResponse(MessageToSend messageToSend, AUserInAServer responder, AChannel channel, Long messageId, Consumer<MessageReceivedEvent> action, Runnable finalAction) {
        channelService.sendMessageToSendToAChannel(messageToSend, channel);
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
        Optional<TextChannel> textChannelInGuild = channelService.getTextChannelInGuild(serverId, channel.getId());
        textChannelInGuild.ifPresent(menu::display);
    }

    private void addEmoteToBuilder(String key, Consumer<Void> consumer, Long serverId, ButtonMenu.Builder builder, HashMap<String, Consumer<Void>> actions) {
        AEmote emoteOrFakeEmote = emoteService.getEmoteOrFakeEmote(key, serverId);
        if(emoteOrFakeEmote.getCustom()){
            Optional<Emote> emote = botService.getEmote(serverId, emoteOrFakeEmote);
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
