package dev.sheldan.abstracto.core.test.command;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.condition.ConditionResult;
import dev.sheldan.abstracto.core.command.config.Parameters;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ResultState;
import dev.sheldan.abstracto.core.models.context.UserInitiatedServerContext;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.junit.Assert;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CommandTestUtilities {

    private CommandTestUtilities() {

    }

    public static void checkSuccessfulCondition(ConditionResult result) {
        Assert.assertTrue(result.isResult());
    }

    public static void checkUnmetCondition(ConditionResult result) {
        Assert.assertFalse(result.isResult());
        Assert.assertNotNull(result.getConditionDetail());
    }

    public static void executeNoParametersTest(Command com) {
        CommandContext context = CommandTestUtilities.getNoParameters();
        com.execute(context);
    }


    public static CommandContext getNoParameters() {
        CommandContext context = CommandContext
                .builder()
                .build();
        Guild guild = Mockito.mock(Guild.class);
        context.setGuild(guild);
        Member member = Mockito.mock(Member.class);
        context.setAuthor(member);
        TextChannel mockedTextChannel = Mockito.mock(TextChannel.class);
        UserInitiatedServerContext userInitiatedContext = UserInitiatedServerContext
                .builder()
                .guild(guild)
                .member(member)
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
        ResultState resultState = result.getResult();
        checkForSuccessResultState(resultState);
    }

    private static void checkForSuccessResultState(ResultState resultState) {
        boolean canBeConsideredSuccessful = ResultState.SUCCESSFUL.equals(resultState)
                || ResultState.IGNORED.equals(resultState)
                || ResultState.SELF_DESTRUCT.equals(resultState);
        Assert.assertTrue(canBeConsideredSuccessful);
    }

    public static void checkSuccessfulCompletionAsync(CompletableFuture<CommandResult> result){
        ResultState resultState = result.join().getResult();
        checkForSuccessResultState(resultState);
    }

    public static List<CompletableFuture<Message>> messageFutureList() {
        return Arrays.asList(CompletableFuture.completedFuture(null));
    }


}
