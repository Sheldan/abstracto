package dev.sheldan.abstracto.core.test;

import dev.sheldan.abstracto.core.models.database.*;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

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

    public static void mockQueueDoubleVoidConsumer(RestAction action) {
        doAnswer(invocationOnMock -> {
            Object consumerObj = invocationOnMock.getArguments()[0];
            if(consumerObj instanceof Consumer) {
                Consumer<Void> consumer = (Consumer) consumerObj;
                consumer.accept(null);
            }
            return null;
        }).when(action).queue(any(Consumer.class), any(Consumer.class));
    }

    public static void mockQueueVoidConsumer(RestAction action) {
        doAnswer(invocationOnMock -> {
            Object consumerObj = invocationOnMock.getArguments()[0];
            if(consumerObj instanceof Consumer) {
                Consumer<Void> consumer = (Consumer) consumerObj;
                consumer.accept(null);
            }
            return null;
        }).when(action).queue(any(Consumer.class));
    }
}
