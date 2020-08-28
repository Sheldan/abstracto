package dev.sheldan.abstracto.test.command;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.Parameters;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ResultState;
import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.test.MockUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.junit.Assert;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandTestUtilities {

    private CommandTestUtilities() {

    }

    public static void executeNoParametersTest(Command com) {
        CommandContext context = CommandTestUtilities.getNoParameters();
        com.execute(context);
    }

    public static void executeAsyncNoParametersTest(Command com) {
        CommandContext context = CommandTestUtilities.getNoParameters();
        com.executeAsync(context);
    }

    public static void executeWrongParametersTest(Command com) {
        executeWrongParametersTest(com, new ArrayList<>());
    }

    public static void executeWrongParametersTest(Command com, Object value) {
        CommandContext context = CommandTestUtilities.getWithParameters(Arrays.asList(value));
        com.execute(context);
    }

    public static void executeAsyncWrongParametersTest(Command com) {
        executeAsyncWrongParametersTest(com, new ArrayList<>());
    }

    public static void executeAsyncWrongParametersTest(Command com, Object value) {
        CommandContext context = CommandTestUtilities.getWithParameters(Arrays.asList(value));
        com.executeAsync(context);
    }

    public static CommandContext getNoParameters() {
        AServer server = MockUtils.getServer();
        AUserInAServer author = MockUtils.getUserObject(3L, server);
        CommandContext context = CommandContext
                .builder()
                .build();
        Guild guild = Mockito.mock(Guild.class);
        context.setGuild(guild);
        Member member = Mockito.mock(Member.class);
        context.setAuthor(member);
        long channelId = 4L;
        TextChannel mockedTextChannel = Mockito.mock(TextChannel.class);
        UserInitiatedServerContext userInitiatedContext = UserInitiatedServerContext
                .builder()
                .server(server)
                .guild(guild)
                .aUserInAServer(author)
                .member(member)
                .user(author.getUserReference())
                .channel(MockUtils.getTextChannel(server, channelId))
                .messageChannel(mockedTextChannel)
                .build();
        context.setUserInitiatedContext(userInitiatedContext);
        context.setChannel(mockedTextChannel);
        context.setParameters(Parameters.builder().parameters(new ArrayList<>()).build());
        context.setMessage(Mockito.mock(Message.class));
        return context;
    }

    public static CommandContext getWithParameters(List<Object> parameters) {
        CommandContext context = getNoParameters();
        context.getParameters().getParameters().addAll(parameters);
        return context;
    }

    public static CommandContext enhanceWithParameters(CommandContext context, List<Object> parameters) {
        context.getParameters().getParameters().addAll(parameters);
        return context;
    }

    public static void checkSuccessfulCompletion(CommandResult result){
        Assert.assertEquals(ResultState.SUCCESSFUL, result.getResult());
    }


}
