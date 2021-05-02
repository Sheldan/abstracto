package dev.sheldan.abstracto.core.command;

import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.config.Parameters;
import dev.sheldan.abstracto.core.command.exception.CommandNotFoundException;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.command.handler.CommandParameterHandler;
import dev.sheldan.abstracto.core.command.handler.CommandParameterIterators;
import dev.sheldan.abstracto.core.command.service.CommandManager;
import dev.sheldan.abstracto.core.command.service.CommandService;
import dev.sheldan.abstracto.core.command.service.ExceptionService;
import dev.sheldan.abstracto.core.command.service.PostCommandExecution;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.service.EmoteService;
import dev.sheldan.abstracto.core.service.RoleService;
import dev.sheldan.abstracto.core.service.management.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.collections4.Bag;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.Lazy;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CommandReceivedHandlerTest {

    @InjectMocks
    private CommandReceivedHandler testUnit;

    @Mock
    private CommandManager commandManager;

    @Mock
    private List<PostCommandExecution> executions;

    @Mock
    private ServerManagementService serverManagementService;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Mock
    private ChannelManagementService channelManagementService;

    @Mock
    @Lazy
    private CommandReceivedHandler self;

    @Mock
    private RoleManagementService roleManagementService;

    @Mock
    private CommandService commandService;

    @Mock
    private EmoteService emoteService;

    @Mock
    private ExceptionService exceptionService;

    @Mock
    private EmoteManagementService emoteManagementService;

    @Mock
    private RoleService roleService;

    @Spy
    private List<CommandParameterHandler> parameterHandlers = new ArrayList<>();

    @Mock
    private MetricService metricService;

    @Mock
    private MessageReceivedEvent event;

    @Mock
    private Message message;

    @Mock
    private Guild guild;

    @Mock
    private Command command;

    @Mock
    private MessageChannel channel;

    @Captor
    private ArgumentCaptor<Parameters> parametersArgumentCaptor;

    @Mock
    private CommandParameterHandler parameterHandler;

    @Mock
    private CommandParameterHandler secondParameterHandler;

    @Mock
    private CommandConfiguration commandConfiguration;

    @Mock
    private Parameter parameter;

    @Mock
    private Parameter secondParameter;

    @Mock
    private Bag<Emote> emotes;

    @Mock
    private List<Member> members;

    @Mock
    private Bag<Role> roles;

    private static final String COMMAND_NAME = "command";
    private static final String MESSAGE_CONTENT_COMMAND_ONLY = "message";
    public static final String PARAMETER_TEXT_1 = "param1";
    public static final String PARAMETER_TEXT_2 = "param2";
    private static final String MESSAGE_CONTENT_ONE_PARAMETER = MESSAGE_CONTENT_COMMAND_ONLY + " " + PARAMETER_TEXT_1;
    private static final String MESSAGE_CONTENT_TWO_PARAMETER = MESSAGE_CONTENT_COMMAND_ONLY + " " + PARAMETER_TEXT_1 + " " + PARAMETER_TEXT_2;
    private static final Long SERVER_ID = 1L;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testNotFromGuild() {
        when(event.isFromGuild()).thenReturn(false);
        testUnit.onMessageReceived(event);
        verify(commandManager, times(0)).isCommand(any());
    }

    @Test
    public void testNotACommandMessage() {
        when(event.isFromGuild()).thenReturn(true);
        when(event.getMessage()).thenReturn(message);
        when(commandManager.isCommand(message)).thenReturn(false);
        testUnit.onMessageReceived(event);
        verify(metricService, times(0)).incrementCounter(any());
    }

    @Test
    public void testCommandNotFoundException() {
        when(event.isFromGuild()).thenReturn(true);
        when(event.getMessage()).thenReturn(message);
        when(commandManager.isCommand(message)).thenReturn(true);
        when(event.getGuild()).thenReturn(guild);
        when(event.getChannel()).thenReturn(channel);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(message.getContentRaw()).thenReturn(MESSAGE_CONTENT_COMMAND_ONLY);
        when(commandManager.getCommandName(anyString(), eq(SERVER_ID))).thenReturn(COMMAND_NAME);
        when(commandManager.findCommandByParameters(eq(COMMAND_NAME), any(), eq(SERVER_ID))).thenThrow(new CommandNotFoundException());
        testUnit.onMessageReceived(event);
        verify(self, times(1)).executePostCommandListener(any(), any(), any());
    }

    @Test
    public void testParseDurationParameter() {
        setupCommandAndMessage(MESSAGE_CONTENT_ONE_PARAMETER);
        when(commandConfiguration.getParameters()).thenReturn(Arrays.asList(parameter));
        when(parameter.getType()).thenReturn(Duration.class);
        Duration parsedDuration = Duration.ofMinutes(1);
        when(parameterHandler.handles(eq(Duration.class), any())).thenReturn(true);
        when(parameterHandler.handle(any(UnparsedCommandParameterPiece.class), any(CommandParameterIterators.class), eq(parameter), eq(message), eq(command))).thenReturn(parsedDuration);
        testUnit.onMessageReceived(event);
        verify(self, times(1)).executeCommand(eq(event), eq(command), parametersArgumentCaptor.capture());
        Parameters usedParameters = parametersArgumentCaptor.getValue();
        List<Object> parameters = usedParameters.getParameters();
        Assert.assertEquals(1, parameters.size());
        Assert.assertEquals(parsedDuration, parameters.get(0));
    }

    @Test
    public void testParseStringParameter() {
        setupCommandAndMessage(MESSAGE_CONTENT_ONE_PARAMETER);
        when(commandConfiguration.getParameters()).thenReturn(Arrays.asList(parameter));
        when(parameter.getType()).thenReturn(String.class);
        when(parameterHandler.handles(eq(String.class), any())).thenReturn(true);
        when(parameterHandler.handle(any(UnparsedCommandParameterPiece.class), any(CommandParameterIterators.class), eq(parameter), eq(message), eq(command))).thenReturn(PARAMETER_TEXT_1);
        testUnit.onMessageReceived(event);
        verify(self, times(1)).executeCommand(eq(event), eq(command), parametersArgumentCaptor.capture());
        Parameters usedParameters = parametersArgumentCaptor.getValue();
        List<Object> parameters = usedParameters.getParameters();
        Assert.assertEquals(1, parameters.size());
        Assert.assertEquals(PARAMETER_TEXT_1, parameters.get(0));
    }

    @Test
    public void testParseStringRemainderParameter() {
        setupCommandAndMessage(MESSAGE_CONTENT_TWO_PARAMETER);
        when(parameter.isRemainder()).thenReturn(true);
        when(parameter.getType()).thenReturn(String.class);
        when(commandConfiguration.getParameters()).thenReturn(Arrays.asList(parameter));
        when(parameterHandler.handles(eq(String.class), any())).thenReturn(true);
        when(parameterHandler.handle(argThat(parameterPieceMatcher(PARAMETER_TEXT_1)), any(CommandParameterIterators.class), eq(parameter), eq(message), eq(command))).thenReturn(PARAMETER_TEXT_1);
        when(parameterHandler.handle(argThat(parameterPieceMatcher(PARAMETER_TEXT_2)), any(CommandParameterIterators.class), eq(parameter), eq(message), eq(command))).thenReturn(PARAMETER_TEXT_2);
        testUnit.onMessageReceived(event);
        verify(self, times(1)).executeCommand(eq(event), eq(command), parametersArgumentCaptor.capture());
        Parameters usedParameters = parametersArgumentCaptor.getValue();
        List<Object> parameters = usedParameters.getParameters();
        Assert.assertEquals(1, parameters.size());
        Assert.assertEquals(PARAMETER_TEXT_1  + " " + PARAMETER_TEXT_2, parameters.get(0));
    }

    @Test
    public void testParseDurationAndStringRemainderParameter() {
        setupCommandAndMessage(MESSAGE_CONTENT_TWO_PARAMETER);
        when(parameter.isRemainder()).thenReturn(false);
        when(parameter.getType()).thenReturn(Duration.class);
        when(secondParameter.getType()).thenReturn(String.class);
        when(secondParameter.isRemainder()).thenReturn(true);
        when(commandConfiguration.getParameters()).thenReturn(Arrays.asList(parameter, secondParameter));
        when(parameterHandler.handles(eq(Duration.class), any())).thenReturn(true);
        when(secondParameterHandler.handles(eq(String.class), any())).thenReturn(true);
        Duration parsedDuration = Duration.ofMinutes(1);
        when(parameterHandler.handle(argThat(parameterPieceMatcher(PARAMETER_TEXT_1)), any(CommandParameterIterators.class), eq(parameter), eq(message), eq(command))).thenReturn(parsedDuration);
        when(secondParameterHandler.handle(argThat(parameterPieceMatcher(PARAMETER_TEXT_2)), any(CommandParameterIterators.class), eq(secondParameter), eq(message), eq(command))).thenReturn(PARAMETER_TEXT_2);
        testUnit.onMessageReceived(event);
        verify(self, times(1)).executeCommand(eq(event), eq(command), parametersArgumentCaptor.capture());
        Parameters usedParameters = parametersArgumentCaptor.getValue();
        List<Object> parameters = usedParameters.getParameters();
        Assert.assertEquals(2, parameters.size());
        Assert.assertEquals(parsedDuration, parameters.get(0));
        Assert.assertEquals(PARAMETER_TEXT_2, parameters.get(1));
    }

    @Test
    public void testParseWithAsyncParameterHandler() {
        setupCommandAndMessage(MESSAGE_CONTENT_TWO_PARAMETER);
        when(parameter.getType()).thenReturn(Duration.class);
        when(parameter.isRemainder()).thenReturn(false);
        when(commandConfiguration.getParameters()).thenReturn(Arrays.asList(parameter, secondParameter));
        when(parameterHandler.handles(eq(Duration.class), any())).thenReturn(true);
        when(parameterHandler.async()).thenReturn(true);
        Duration parsedDuration = Duration.ofMinutes(1);
        when(parameterHandler.handleAsync(argThat(parameterPieceMatcher(PARAMETER_TEXT_1)), any(CommandParameterIterators.class),
                eq(parameter), eq(message), eq(command))).thenReturn(CompletableFuture.completedFuture(parsedDuration));
        testUnit.onMessageReceived(event);
        verify(self, times(1)).executeCommand(eq(event), eq(command), parametersArgumentCaptor.capture());
        Parameters usedParameters = parametersArgumentCaptor.getValue();
        List<Object> parameters = usedParameters.getParameters();
        Assert.assertEquals(1, parameters.size());
        Assert.assertEquals(parsedDuration, parameters.get(0));
    }

    @Test
    public void testParseDurationAndStringRemainderParameterAsyncMixed() {
        setupCommandAndMessage(MESSAGE_CONTENT_TWO_PARAMETER);
        when(parameter.isRemainder()).thenReturn(false);
        when(parameter.getType()).thenReturn(Duration.class);
        when(secondParameter.getType()).thenReturn(String.class);
        when(secondParameter.isRemainder()).thenReturn(true);
        when(commandConfiguration.getParameters()).thenReturn(Arrays.asList(parameter, secondParameter));
        when(parameterHandler.handles(eq(Duration.class), any())).thenReturn(true);
        when(secondParameterHandler.handles(eq(String.class), any())).thenReturn(true);
        when(secondParameterHandler.async()).thenReturn(true);
        Duration parsedDuration = Duration.ofMinutes(1);
        when(parameterHandler.handle(argThat(parameterPieceMatcher(PARAMETER_TEXT_1)), any(CommandParameterIterators.class), eq(parameter), eq(message), eq(command))).thenReturn(parsedDuration);
        when(secondParameterHandler.handleAsync(argThat(parameterPieceMatcher(PARAMETER_TEXT_2)),
                any(CommandParameterIterators.class), eq(secondParameter), eq(message), eq(command))).thenReturn(CompletableFuture.completedFuture(PARAMETER_TEXT_2));
        testUnit.onMessageReceived(event);
        verify(self, times(1)).executeCommand(eq(event), eq(command), parametersArgumentCaptor.capture());
        Parameters usedParameters = parametersArgumentCaptor.getValue();
        List<Object> parameters = usedParameters.getParameters();
        Assert.assertEquals(2, parameters.size());
        Assert.assertEquals(parsedDuration, parameters.get(0));
        Assert.assertEquals(PARAMETER_TEXT_2, parameters.get(1));
    }

    private void setupCommandAndMessage(String messageContentTwoParameter) {
        parameterHandlers.add(parameterHandler);
        parameterHandlers.add(secondParameterHandler);
        when(event.isFromGuild()).thenReturn(true);
        when(event.getMessage()).thenReturn(message);
        when(commandManager.isCommand(message)).thenReturn(true);
        when(event.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        when(message.getContentRaw()).thenReturn(messageContentTwoParameter);
        when(commandManager.getCommandName(anyString(), eq(SERVER_ID))).thenReturn(COMMAND_NAME);
        when(commandManager.findCommandByParameters(eq(COMMAND_NAME), any(), eq(SERVER_ID))).thenReturn(command);
        when(command.getConfiguration()).thenReturn(commandConfiguration);
        when(message.getEmotesBag()).thenReturn(emotes);
        when(message.getMentionedMembers()).thenReturn(members);
        when(message.getMentionedRolesBag()).thenReturn(roles);
    }

    private ArgumentMatcher<UnparsedCommandParameterPiece> parameterPieceMatcher(Object value) {
        return argument -> argument != null && value.equals(argument.getValue());
    }

}
