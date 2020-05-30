package dev.sheldan.abstracto.test;

import dev.sheldan.abstracto.core.models.database.*;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;

public class MockUtils {

    private MockUtils() {

    }

    public static AUserInAServer getUserObject(Long id, AServer server) {
        AUser user = AUser.builder().id(id).build();
        AUserInAServer createdUser = AUserInAServer.builder().userReference(user).serverReference(server).build();
        server.getUsers().add(createdUser);
        return createdUser;
    }

    public static AServer getServer(Long id){
        return AServer.builder().id(id).build();
    }

    public static AServer getServer() {
        return getServer(2L);
    }

    public static GuildImpl getGuild(AServer serverToUse, JDAImpl jda) {
        return new GuildImpl(jda, serverToUse.getId());
    }

    public static MemberImpl getMockedMember(AServer serverToUse, AUserInAServer user, JDAImpl jda) {
        UserImpl jdaUser = new UserImpl(user.getUserReference().getId(), jda);
        GuildImpl jdaGuild = new GuildImpl(jda, serverToUse.getId());
        return new MemberImpl(jdaGuild, jdaUser);
    }

    public static AChannel getTextChannel(AServer server, Long id) {
        return AChannel.builder().id(id).server(server).deleted(false).type(AChannelType.TEXT).build();
    }

    public static TextChannelImpl getMockedTextChannel(Long id, GuildImpl guild) {
        return new TextChannelImpl(id, guild);
    }

    public static ARole getRole(Long id, AServer server) {
        return ARole.builder().server(server).id(id).build();
    }

    public static ReceivedMessage buildMockedMessage(long messageId, String text, MemberImpl member) {
        Instant dateObj = Instant.ofEpochSecond(1590615937);
        OffsetDateTime messageDate = OffsetDateTime.ofInstant(dateObj, ZoneId.systemDefault());
        User user = member != null ? member.getUser() : null;
        return new ReceivedMessage(messageId, null, MessageType.DEFAULT, false, false
                , null, null, false, false, text, "nonce", user, member, null, messageDate,
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 0);
    }
}
