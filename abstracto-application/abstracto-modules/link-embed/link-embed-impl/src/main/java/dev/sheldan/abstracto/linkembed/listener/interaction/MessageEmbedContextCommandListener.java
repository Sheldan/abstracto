package dev.sheldan.abstracto.linkembed.listener.interaction;

import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.MessageContextConfig;
import dev.sheldan.abstracto.core.listener.DefaultListenerResult;
import dev.sheldan.abstracto.core.interaction.context.message.listener.MessageContextCommandListener;
import dev.sheldan.abstracto.core.models.GuildMemberMessageChannel;
import dev.sheldan.abstracto.core.models.cache.CachedMessage;
import dev.sheldan.abstracto.core.models.listener.interaction.MessageContextInteractionModel;
import dev.sheldan.abstracto.core.interaction.context.ContextCommandService;
import dev.sheldan.abstracto.core.service.MessageCache;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.linkembed.config.LinkEmbedFeatureDefinition;
import dev.sheldan.abstracto.linkembed.service.MessageEmbedService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class MessageEmbedContextCommandListener implements MessageContextCommandListener {

    @Autowired
    private MessageCache messageCache;

    @Autowired
    private MessageEmbedService messageEmbedService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private MessageEmbedContextCommandListener self;

    @Autowired
    private ContextCommandService contextCommandService;

    @Override
    public DefaultListenerResult execute(MessageContextInteractionModel model) {
        MessageContextInteractionEvent event = model.getEvent();
        event.deferReply().queue();
        Message targetMessage = event.getInteraction().getTarget();
        Member actor = model.getEvent().getMember();

        messageCache.getMessageFromCache(targetMessage)
                .thenAccept(cachedMessage -> self.embedMessage(model, actor, cachedMessage));
        return DefaultListenerResult.PROCESSED;
    }

    @Transactional
    public void embedMessage(MessageContextInteractionModel model, Member actor, CachedMessage cachedMessage) {
        Long userEmbeddingUserInServerId = userInServerManagementService.loadOrCreateUser(actor).getUserInServerId();
        GuildMemberMessageChannel context = GuildMemberMessageChannel
                .builder()
                .message(null)
                .guild(actor.getGuild())
                .member(actor)
                .guildChannel(model.getEvent().getGuildChannel())
                .build();
        messageEmbedService.embedLink(cachedMessage, model.getEvent().getGuildChannel(), userEmbeddingUserInServerId, context, model.getEvent().getInteraction());
    }

    @Override
    public FeatureDefinition getFeature() {
        return LinkEmbedFeatureDefinition.LINK_EMBEDS;
    }

    @Override
    public MessageContextConfig getConfig() {
        return MessageContextConfig
                .builder()
                .isTemplated(true)
                .name("embed_message")
                .templateKey("message_embed_message_context_menu_label")
                .build();
    }

    @Override
    public Boolean handlesEvent(MessageContextInteractionModel model) {
        return contextCommandService.matchesGuildContextName(model, getConfig(), model.getServerId());
    }
}
