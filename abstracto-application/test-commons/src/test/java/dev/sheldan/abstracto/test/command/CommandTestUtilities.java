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
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;
import net.dv8tion.jda.internal.entities.TextChannelImpl;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandTestUtilities {

    private CommandTestUtilities() {

    }

    public static void executeNoParametersTest(Command com, JDAImpl jda) {
        CommandContext context = CommandTestUtilities.getNoParameters(jda);
        com.execute(context);
    }

    public static void executeWrongParametersTest(Command com, JDAImpl jda) {
        executeWrongParametersTest(com, jda, "");
    }

    public static void executeWrongParametersTest(Command com, JDAImpl jda, Object value) {
        CommandContext context = CommandTestUtilities.getWithParameters(jda, Arrays.asList(value));
        com.execute(context);
    }

    public static CommandContext getNoParameters(JDAImpl jda) {
        AServer server = MockUtils.getServer();
        AUserInAServer author = MockUtils.getUserObject(3L, server);
        CommandContext context = CommandContext
                .builder()
                .build();
        GuildImpl guild = MockUtils.getGuild(server, jda);
        context.setGuild(guild);
        MemberImpl member = MockUtils.getMockedMember(server, author, jda);
        context.setAuthor(member);
        long channelId = 4L;
        TextChannelImpl mockedTextChannel = MockUtils.getMockedTextChannel(channelId, guild);
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
        context.setJda(jda);
        context.setChannel(mockedTextChannel);
        context.setParameters(Parameters.builder().parameters(new ArrayList<>()).build());
        context.setMessage(MockUtils.buildMockedMessage(3L, "text", member));
        return context;
    }

    public static CommandContext getWithParameters(JDAImpl jda, List<Object> parameters) {
        CommandContext context = getNoParameters(jda);
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
