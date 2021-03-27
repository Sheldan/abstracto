package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.exception.CommandAliasAlreadyExistsException;
import dev.sheldan.abstracto.core.command.exception.CommandAliasDoesNotExistsException;
import dev.sheldan.abstracto.core.command.exception.CommandAliasHidesCommandException;
import dev.sheldan.abstracto.core.command.model.database.ACommand;
import dev.sheldan.abstracto.core.command.model.database.ACommandInAServer;
import dev.sheldan.abstracto.core.command.model.database.ACommandInServerAlias;
import dev.sheldan.abstracto.core.command.model.database.CommandInServerAliasId;
import dev.sheldan.abstracto.core.command.service.management.CommandInServerAliasManagementServiceBean;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CommandInServerAliasServiceBeanTest {

    @InjectMocks
    private CommandInServerAliasServiceBean testUnit;

    @Mock
    private CommandInServerAliasManagementServiceBean commandInServerAliasManagementServiceBean;

    @Mock
    private ServerManagementService serverManagementService;

    @Mock
    private CommandInServerService commandInServerService;

    @Mock
    private CommandRegistry commandRegistry;

    @Mock
    private AServer server;

    @Mock
    private ACommandInServerAlias alias;

    @Mock
    private ACommandInAServer aCommandInAServer;

    @Mock
    private Command command;

    private static final Long SERVER_ID = 1L;
    private static final String COMMAND_NAME = "command";
    private static final String ALIAS_NAME = "alias";

    @Test
    public void testCreateAlias() {
        when(serverManagementService.loadServer(SERVER_ID)).thenReturn(server);
        when(commandInServerAliasManagementServiceBean.getCommandInServerAlias(server, ALIAS_NAME)).thenReturn(Optional.empty());
        when(commandRegistry.getCommandByNameOptional(ALIAS_NAME, false, SERVER_ID)).thenReturn(Optional.empty());
        when(commandInServerService.getCommandInAServer(server, COMMAND_NAME)).thenReturn(aCommandInAServer);
        when(commandInServerAliasManagementServiceBean.createAliasForCommand(aCommandInAServer, ALIAS_NAME)).thenReturn(alias);
        ACommandInServerAlias createdAlias = testUnit.createAliasForCommandInServer(SERVER_ID, COMMAND_NAME, ALIAS_NAME);
        Assert.assertEquals(alias, createdAlias);
    }

    @Test(expected = CommandAliasAlreadyExistsException.class)
    public void testCreateAliasAlreadyExists() {
        when(serverManagementService.loadServer(SERVER_ID)).thenReturn(server);
        when(commandInServerAliasManagementServiceBean.getCommandInServerAlias(server, ALIAS_NAME)).thenReturn(Optional.of(alias));
        ACommandInAServer commandInAServer = Mockito.mock(ACommandInAServer.class);
        when(alias.getCommandInAServer()).thenReturn(commandInAServer);
        ACommand command = Mockito.mock(ACommand.class);
        when(commandInAServer.getCommandReference()).thenReturn(command);
        when(command.getName()).thenReturn(COMMAND_NAME);
        testUnit.createAliasForCommandInServer(SERVER_ID, COMMAND_NAME, ALIAS_NAME);
    }

    @Test(expected = CommandAliasHidesCommandException.class)
    public void testCreateAliasForCommandName() {
        when(serverManagementService.loadServer(SERVER_ID)).thenReturn(server);
        when(commandInServerAliasManagementServiceBean.getCommandInServerAlias(server, ALIAS_NAME)).thenReturn(Optional.empty());
        when(commandRegistry.getCommandByNameOptional(ALIAS_NAME, false, SERVER_ID)).thenReturn(Optional.of(command));
        CommandConfiguration config = Mockito.mock(CommandConfiguration.class);
        when(command.getConfiguration()).thenReturn(config);
        when(config.getName()).thenReturn(COMMAND_NAME);
        testUnit.createAliasForCommandInServer(SERVER_ID, COMMAND_NAME, ALIAS_NAME);
    }

    @Test
    public void testGetCommandInServerAliasExisting() {
        when(serverManagementService.loadServer(SERVER_ID)).thenReturn(server);
        when(commandInServerAliasManagementServiceBean.getCommandInServerAlias(server, ALIAS_NAME)).thenReturn(Optional.of(alias));
        Optional<ACommandInServerAlias> optional = testUnit.getCommandInServerAlias(SERVER_ID, ALIAS_NAME);
        Assert.assertTrue(optional.isPresent());
        optional.ifPresent(returnedAlias ->
            Assert.assertEquals(alias, returnedAlias)
        );
    }

    @Test
    public void testGetCommandInServerAliasNotExisting() {
        when(serverManagementService.loadServer(SERVER_ID)).thenReturn(server);
        when(commandInServerAliasManagementServiceBean.getCommandInServerAlias(server, ALIAS_NAME)).thenReturn(Optional.empty());
        Optional<ACommandInServerAlias> optional = testUnit.getCommandInServerAlias(SERVER_ID, ALIAS_NAME);
        Assert.assertFalse(optional.isPresent());
    }

    @Test
    public void testDeleteCommandInServerAlias() {
        when(serverManagementService.loadServer(SERVER_ID)).thenReturn(server);
        when(commandInServerAliasManagementServiceBean.getCommandInServerAlias(server, ALIAS_NAME)).thenReturn(Optional.of(alias));
        testUnit.deleteCommandInServerAlias(SERVER_ID, ALIAS_NAME);
        verify(commandInServerAliasManagementServiceBean, times(1)).deleteCommandInServerAlias(alias);
    }

    @Test(expected = CommandAliasDoesNotExistsException.class)
    public void testDeleteCommandInServerAliasNotExisting() {
        when(serverManagementService.loadServer(SERVER_ID)).thenReturn(server);
        when(commandInServerAliasManagementServiceBean.getCommandInServerAlias(server, ALIAS_NAME)).thenReturn(Optional.empty());
        testUnit.deleteCommandInServerAlias(SERVER_ID, ALIAS_NAME);
    }

    @Test
    public void testGetAliasesForCommandNoResults() {
        when(serverManagementService.loadServer(SERVER_ID)).thenReturn(server);
        when(commandInServerAliasManagementServiceBean.getAliasesForCommandInServer(server, COMMAND_NAME)).thenReturn(new ArrayList<>());
        List<String> aliases = testUnit.getAliasesForCommand(SERVER_ID, COMMAND_NAME);
        Assert.assertEquals(0, aliases.size());
    }

    @Test
    public void testGetAliasesForCommandWithResults() {
        when(serverManagementService.loadServer(SERVER_ID)).thenReturn(server);
        when(commandInServerAliasManagementServiceBean.getAliasesForCommandInServer(server, COMMAND_NAME)).thenReturn(Arrays.asList(alias));
        CommandInServerAliasId id = Mockito.mock(CommandInServerAliasId.class);
        when(alias.getAliasId()).thenReturn(id);
        when(id.getName()).thenReturn(ALIAS_NAME);
        List<String> aliases = testUnit.getAliasesForCommand(SERVER_ID, COMMAND_NAME);
        Assert.assertEquals(1, aliases.size());
        Assert.assertEquals(ALIAS_NAME, aliases.get(0));
    }

}
