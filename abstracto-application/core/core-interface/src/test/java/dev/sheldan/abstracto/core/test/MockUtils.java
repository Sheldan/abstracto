package dev.sheldan.abstracto.core.test;

import dev.sheldan.abstracto.core.models.database.*;

public class MockUtils {

    private MockUtils() {

    }

    public static AUserInAServer getUserObject(Long id, AServer server) {
        AUser user = AUser.builder().id(id).build();
        AUserInAServer createdUser = AUserInAServer.builder().userReference(user).serverReference(server).userInServerId(id).build();
        server.getUsers().add(createdUser);
        return createdUser;
    }

    public static AServer getServer(Long id){
        return AServer.builder().id(id).build();
    }

    public static AServer getServer() {
        return getServer(2L);
    }

    public static AChannel getTextChannel(AServer server, Long id) {
        return AChannel.builder().id(id).server(server).deleted(false).type(AChannelType.TEXT).build();
    }

    public static ARole getRole(Long id, AServer server) {
        return ARole.builder().server(server).id(id).build();
    }
}
