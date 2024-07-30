package dev.sheldan.abstracto.experience.listener;

import com.google.gson.Gson;
import dev.sheldan.abstracto.core.exception.InputFormatException;
import dev.sheldan.abstracto.core.models.ServerUser;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.MemberService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.core.utils.ParseUtils;
import dev.sheldan.abstracto.experience.model.LevelActionPayload;
import dev.sheldan.abstracto.experience.model.SendMessageToChannelLevelActionMessageModel;
import dev.sheldan.abstracto.experience.model.SendMessageToChannelLevelActionPayload;
import dev.sheldan.abstracto.experience.model.database.AUserExperience;
import dev.sheldan.abstracto.experience.model.database.LevelAction;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SendMessageToChannelLevelAction implements LevelActionListener {

    public static final String ACTION_NAME = "send_message_to_channel_above_level";
    private static final String LEVEL_ACTION_SEND_MESSAGE_TEMPLATE_KEY = "levelAction_sendMessageToChannel_template";

    @Autowired
    private Gson gson;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private MemberService memberService;

    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    public void apply(AUserExperience userExperience, LevelAction levelAction, MemberActionModification container) {
        SendMessageToChannelLevelActionPayload payload = (SendMessageToChannelLevelActionPayload) levelAction.getLoadedPayload();
        SendMessageToChannelLevelActionMessageModel.SendMessageToChannelLevelActionMessageModelBuilder messageModelBuilder = SendMessageToChannelLevelActionMessageModel
                .builder()
                .level(userExperience.getLevelOrDefault())
                .templateKey(payload.getTemplateKey())
                .experience(userExperience.getExperience());
        ServerUser serverUser = ServerUser.fromAUserInAServer(userExperience.getUser());
        memberService.getMemberInServerAsync(serverUser).thenAccept(member -> {
            messageModelBuilder.memberDisplay(MemberDisplay.fromMember(member));
            SendMessageToChannelLevelActionMessageModel model = messageModelBuilder.build();
            MessageToSend messageToSend = templateService.renderEmbedTemplate(LEVEL_ACTION_SEND_MESSAGE_TEMPLATE_KEY, model, serverUser.getServerId());
            GuildMessageChannel targetChannel = channelService.getMessageChannelFromServer(serverUser.getServerId(), payload.getChannelId());
            FutureUtils.toSingleFutureGeneric(channelService.sendMessageToSendToChannel(messageToSend, targetChannel)).thenAccept(unused -> {
                log.info("Send message to channel action sent a message to channel {} for user {} in server {}.", payload.getChannelId(), serverUser.getUserId(), serverUser.getServerId());
            }).exceptionally(throwable -> {
                log.warn("Send message to channel action failed to send a message to channel {} for user {} in server {}.", payload.getChannelId(), serverUser.getUserId(), serverUser.getServerId(), throwable);
                return null;
            });
        }).exceptionally(throwable -> {
            log.warn("Failed to load member {} in server {} for send message level action towards channel {}.", serverUser.getUserId(), serverUser.getServerId(), payload.getChannelId());
            return null;
        });
    }

    @Override
    public boolean shouldExecute(AUserExperience aUserExperience, Integer oldLevel, LevelAction levelAction) {
        if(!oldLevel.equals(aUserExperience.getLevelOrDefault())) { // this means the user changed level now, this is the path from gaining a lot of experience
            boolean jumpedLevelToMatch = oldLevel < levelAction.getLevel().getLevel() && aUserExperience.getLevelOrDefault() >= levelAction.getLevel().getLevel();
            // this boolean means that the user did NOT have the action earlier, but does now (and more than that)
            return jumpedLevelToMatch || aUserExperience.getLevelOrDefault().equals(levelAction.getLevel().getLevel()); // or the user matches the level _exactly_, this is the path from normally gaining experience
        } else {
            // This case is useful for re-joining, because this means, that the user did _not_ change level, and already is somewhere way above
            return aUserExperience.getLevelOrDefault() >= levelAction.getLevel().getLevel();
        }
    }

    @Override
    public void prepareAction(LevelAction levelAction) {
        levelAction.setLoadedPayload(gson.fromJson(levelAction.getPayload(), SendMessageToChannelLevelActionPayload.class));
    }

    @Override
    public LevelActionPayload createPayload(Guild guild, String input) {
        if(!input.contains(";")) {
            throw new InputFormatException(input, "<#channel>;template_key");
        }
        String channelPart = input.substring(0, input.indexOf(";"));
        GuildChannel channel = ParseUtils.parseGuildChannelFromText(channelPart, guild);
        String templateKey = input.substring(input.indexOf(";") + 1);
        return SendMessageToChannelLevelActionPayload
                .builder()
                .channelId(channel.getIdLong())
                .templateKey(templateKey)
                .build();
    }
}
