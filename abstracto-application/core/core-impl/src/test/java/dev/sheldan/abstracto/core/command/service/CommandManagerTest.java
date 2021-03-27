package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.ModuleDefinition;
import dev.sheldan.abstracto.core.command.config.ModuleInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.exception.CommandNotFoundException;
import dev.sheldan.abstracto.core.command.exception.InsufficientParametersException;
import dev.sheldan.abstracto.core.command.execution.UnParsedCommandParameter;
import dev.sheldan.abstracto.core.command.model.database.ACommand;
import dev.sheldan.abstracto.core.command.model.database.ACommandInAServer;
import dev.sheldan.abstracto.core.command.model.database.ACommandInServerAlias;
import dev.sheldan.abstracto.core.metric.service.MetricService;
import dev.sheldan.abstracto.core.models.property.SystemConfigProperty;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.management.DefaultConfigManagementService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static dev.sheldan.abstracto.core.command.service.CommandManager.PREFIX;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommandManagerTest {

    public static final String DEFAULT_PREFIX = "!";
    @InjectMocks
    private CommandManager testUnit;

    @Mock
    private ConfigService configService;

    @Mock
    private DefaultConfigManagementService defaultConfigManagementService;

    @Mock
    private MetricService metricService;

    @Mock
    private CommandInServerAliasService commandInServerAliasService;

    @Mock
    private ServerManagementService serverManagementService;

    @Spy
    private List<Command> commands = new ArrayList<>();

    @Mock
    private Command firstCommand;

    @Mock
    private CommandConfiguration commandConfiguration;

    @Mock
    private UnParsedCommandParameter parsedCommandParameter;

    @Mock
    private Parameter parameter;

    @Mock
    private ACommandInServerAlias alias;

    @Mock
    private ACommandInAServer commandInAServer;

    @Mock
    private ACommand aCommand;

    private static final String COMMAND_NAME = "name";
    private static final String COMMAND_NAME_2 = COMMAND_NAME + "suffix";
    private static final String ALIAS_NAME = "name2";
    private static final Long SERVER_ID = 1L;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = CommandNotFoundException.class)
    public void testFindByParametersNoCommands(){
        when(commandInServerAliasService.getCommandInServerAlias(SERVER_ID, COMMAND_NAME)).thenReturn(Optional.empty());
        testUnit.findCommandByParameters(COMMAND_NAME, parsedCommandParameter, SERVER_ID);
    }

    @Test
    public void testFindByParametersViaCommandNameNecessaryParameter() {
        commands.add(firstCommand);
        when(commandConfiguration.getName()).thenReturn(COMMAND_NAME);
        when(firstCommand.getConfiguration()).thenReturn(commandConfiguration);
        when(commandConfiguration.getNecessaryParameterCount()).thenReturn(0);
        when(parsedCommandParameter.getParameters()).thenReturn(new ArrayList<>());
        Command foundCommand = testUnit.findCommandByParameters(COMMAND_NAME, parsedCommandParameter, SERVER_ID);
        Assert.assertEquals(firstCommand, foundCommand);
    }

    @Test
    public void testFindByParametersViaCommandNameNoParameters() {
        commands.add(firstCommand);
        when(commandConfiguration.getName()).thenReturn(COMMAND_NAME);
        when(firstCommand.getConfiguration()).thenReturn(commandConfiguration);
        when(commandConfiguration.getParameters()).thenReturn(null);
        Command foundCommand = testUnit.findCommandByParameters(COMMAND_NAME, parsedCommandParameter, SERVER_ID);
        Assert.assertEquals(firstCommand, foundCommand);
    }

    @Test
    public void testFindByParametersViaCommandNameNoParametersButParametersGiven() {
        commands.add(firstCommand);
        when(commandConfiguration.getName()).thenReturn(COMMAND_NAME);
        when(firstCommand.getConfiguration()).thenReturn(commandConfiguration);
        when(commandConfiguration.getParameters()).thenReturn(null);
        Command foundCommand = testUnit.findCommandByParameters(COMMAND_NAME, parsedCommandParameter, SERVER_ID);
        Assert.assertEquals(firstCommand, foundCommand);
    }

    @Test
    public void testFindByParametersViaCommandAliasAdequateParameter() {
        commands.add(firstCommand);
        when(commandConfiguration.getName()).thenReturn(COMMAND_NAME_2);
        when(commandConfiguration.getAliases()).thenReturn(Arrays.asList(COMMAND_NAME));
        when(firstCommand.getConfiguration()).thenReturn(commandConfiguration);
        when(commandConfiguration.getNecessaryParameterCount()).thenReturn(0);
        when(parsedCommandParameter.getParameters()).thenReturn(new ArrayList<>());
        Command foundCommand = testUnit.findCommandByParameters(COMMAND_NAME, parsedCommandParameter, SERVER_ID);
        Assert.assertEquals(firstCommand, foundCommand);
    }

    @Test(expected = InsufficientParametersException.class)
    public void testFindByParametersViaCommandNameInsufficientParameters() {
        commands.add(firstCommand);
        when(commandConfiguration.getParameters()).thenReturn(Arrays.asList(parameter));
        when(firstCommand.getConfiguration()).thenReturn(commandConfiguration);
        when(commandConfiguration.getName()).thenReturn(COMMAND_NAME);
        when(commandConfiguration.getNecessaryParameterCount()).thenReturn(1);
        when(parsedCommandParameter.getParameters()).thenReturn(new ArrayList<>());
        testUnit.findCommandByParameters(COMMAND_NAME, parsedCommandParameter, SERVER_ID);
    }

    @Test
    public void testFindByParametersViaServerAliasAdequateParameter() {
        commands.add(firstCommand);
        when(commandConfiguration.getName()).thenReturn(COMMAND_NAME);
        when(firstCommand.getConfiguration()).thenReturn(commandConfiguration);
        when(commandConfiguration.getNecessaryParameterCount()).thenReturn(0);
        when(parsedCommandParameter.getParameters()).thenReturn(new ArrayList<>());
        setupAliasTest();
        Command foundCommand = testUnit.findCommandByParameters(ALIAS_NAME, parsedCommandParameter, SERVER_ID);
        Assert.assertEquals(firstCommand, foundCommand);
    }

    @Test
    public void testFindCommandViaName() {
        commands.add(firstCommand);
        when(firstCommand.getConfiguration()).thenReturn(commandConfiguration);
        when(commandConfiguration.getName()).thenReturn(COMMAND_NAME);
        Command foundName = testUnit.findCommandViaName(COMMAND_NAME);
        Assert.assertEquals(firstCommand, foundName);
    }

    @Test(expected = CommandNotFoundException.class)
    public void testFindCommandViaNameNotFound() {
        commands.add(firstCommand);
        when(firstCommand.getConfiguration()).thenReturn(commandConfiguration);
        when(commandConfiguration.getName()).thenReturn(COMMAND_NAME_2);
        testUnit.findCommandViaName(COMMAND_NAME);
    }

    @Test
    public void testIsCommand() {
        Message message = setupIsCommandScenario();
        when(message.getContentRaw()).thenReturn("!com");
        Assert.assertTrue(testUnit.isCommand(message));
    }

    @Test
    public void testIsCommandNoneFound() {
        Message message = setupIsCommandScenario();
        when(message.getContentRaw()).thenReturn("com");
        Assert.assertFalse(testUnit.isCommand(message));
    }

    @Test
    public void testGetCommand() {
        setupPrefix();
        String foundCommandName = testUnit.getCommandName("!com", SERVER_ID);
        Assert.assertEquals("com", foundCommandName);
    }

    @Test
    public void testGetCommandByNameWithoutAlias() {
        commands.add(firstCommand);
        when(commandConfiguration.getName()).thenReturn(COMMAND_NAME);
        when(firstCommand.getConfiguration()).thenReturn(commandConfiguration);
        Command foundCommand = testUnit.getCommandByName(COMMAND_NAME, false, SERVER_ID);
        Assert.assertEquals(firstCommand, foundCommand);
    }

    @Test
    public void testGetCommandByNameOptionalWithoutAlias() {
        commands.add(firstCommand);
        when(commandConfiguration.getName()).thenReturn(COMMAND_NAME);
        when(firstCommand.getConfiguration()).thenReturn(commandConfiguration);
        Optional<Command> foundCommandOptional = testUnit.getCommandByNameOptional(COMMAND_NAME, false, SERVER_ID);
        Assert.assertTrue(foundCommandOptional.isPresent());
        foundCommandOptional.ifPresent(command -> {
            Assert.assertEquals(firstCommand, foundCommandOptional.get());
        });
    }

    @Test
    public void testGetCommandByNameNotFoundWithoutAlias() {
        commands.add(firstCommand);
        when(commandConfiguration.getName()).thenReturn(COMMAND_NAME_2);
        when(firstCommand.getConfiguration()).thenReturn(commandConfiguration);
        Command foundCommand = testUnit.getCommandByName(COMMAND_NAME, false, SERVER_ID);
        Assert.assertNull(foundCommand);
    }

    @Test
    public void testGetCommandByNameViaAlias() {
        commands.add(firstCommand);
        when(commandConfiguration.getName()).thenReturn(COMMAND_NAME);
        when(firstCommand.getConfiguration()).thenReturn(commandConfiguration);
        setupAliasTest();
        Command foundCommand = testUnit.getCommandByName(ALIAS_NAME, true, SERVER_ID);
        Assert.assertEquals(firstCommand, foundCommand);
    }

    @Test
    public void testGetCommandByNameViaAliasNotFound() {
        commands.add(firstCommand);
        when(commandConfiguration.getName()).thenReturn(COMMAND_NAME);
        when(firstCommand.getConfiguration()).thenReturn(commandConfiguration);
        Command foundCommand = testUnit.getCommandByName(ALIAS_NAME, true, SERVER_ID);
        Assert.assertNull(foundCommand);
    }

    @Test
    public void testCommandExistsWithoutAlias() {
        commands.add(firstCommand);
        when(commandConfiguration.getName()).thenReturn(COMMAND_NAME);
        when(firstCommand.getConfiguration()).thenReturn(commandConfiguration);
        Assert.assertTrue(testUnit.commandExists(COMMAND_NAME, false, SERVER_ID));
    }

    @Test
    public void testCommandExistsWithoutAliasNotFound() {
        commands.add(firstCommand);
        when(commandConfiguration.getName()).thenReturn(COMMAND_NAME_2);
        when(firstCommand.getConfiguration()).thenReturn(commandConfiguration);
        Assert.assertFalse(testUnit.commandExists(COMMAND_NAME, false, SERVER_ID));
    }

    @Test
    public void testCommandExistsWithAlias() {
        commands.add(firstCommand);
        when(commandConfiguration.getName()).thenReturn(COMMAND_NAME);
        when(firstCommand.getConfiguration()).thenReturn(commandConfiguration);
        setupAliasTest();
        Assert.assertTrue(testUnit.commandExists(COMMAND_NAME, true, SERVER_ID));
    }

    @Test
    public void testCommandExistsWithAliasNotFound() {
        commands.add(firstCommand);
        when(commandConfiguration.getName()).thenReturn(COMMAND_NAME_2);
        when(firstCommand.getConfiguration()).thenReturn(commandConfiguration);
        setupAliasTest();
        Assert.assertFalse(testUnit.commandExists(COMMAND_NAME, true, SERVER_ID));
    }

    @Test
    public void testGetAllCommandsFromModuleOneCommandFound() {
        ModuleDefinition moduleDefinition = Mockito.mock(ModuleDefinition.class);
        ModuleInfo info = Mockito.mock(ModuleInfo.class);
        String moduleName = "module";
        when(info.getName()).thenReturn(moduleName);
        when(moduleDefinition.getInfo()).thenReturn(info);
        when(commandConfiguration.getModule()).thenReturn(moduleName);
        when(firstCommand.getConfiguration()).thenReturn(commandConfiguration);
        commands.add(firstCommand);
        List<Command> foundCommands = testUnit.getAllCommandsFromModule(moduleDefinition);
        Assert.assertEquals(1, foundCommands.size());
        Assert.assertEquals(firstCommand, foundCommands.get(0));
    }

    @Test
    public void testGetAllCommandsFromModuleNoCommandFound() {
        ModuleDefinition moduleDefinition = Mockito.mock(ModuleDefinition.class);
        ModuleInfo info = Mockito.mock(ModuleInfo.class);
        String moduleName = "module";
        when(info.getName()).thenReturn(moduleName);
        when(moduleDefinition.getInfo()).thenReturn(info);
        when(commandConfiguration.getModule()).thenReturn(moduleName + "2");
        when(firstCommand.getConfiguration()).thenReturn(commandConfiguration);
        commands.add(firstCommand);
        List<Command> foundCommands = testUnit.getAllCommandsFromModule(moduleDefinition);
        Assert.assertEquals(0, foundCommands.size());
    }


    private void setupAliasTest() {
        when(commandInServerAliasService.getCommandInServerAlias(SERVER_ID, ALIAS_NAME)).thenReturn(Optional.of(alias));
        when(alias.getCommandInAServer()).thenReturn(commandInAServer);
        when(commandInAServer.getCommandReference()).thenReturn(aCommand);
        when(aCommand.getName()).thenReturn(COMMAND_NAME);
    }

    private Message setupIsCommandScenario() {
        Message message = Mockito.mock(Message.class);
        setupPrefix();
        Guild guild = Mockito.mock(Guild.class);
        when(message.getGuild()).thenReturn(guild);
        when(guild.getIdLong()).thenReturn(SERVER_ID);
        return message;
    }

    private void setupPrefix() {
        SystemConfigProperty defaultConfig = Mockito.mock(SystemConfigProperty.class);
        when(defaultConfig.getStringValue()).thenReturn(DEFAULT_PREFIX);
        when(defaultConfigManagementService.getDefaultConfig(PREFIX)).thenReturn(defaultConfig);
        when(configService.getStringValue(PREFIX, SERVER_ID, DEFAULT_PREFIX)).thenReturn(DEFAULT_PREFIX);
    }
}
