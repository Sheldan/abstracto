package dev.sheldan.abstracto.core.command.service;

import dev.sheldan.abstracto.core.command.model.database.ACommand;
import dev.sheldan.abstracto.core.command.model.database.ACommandInAServer;
import dev.sheldan.abstracto.core.command.service.management.CommandInServerManagementService;
import dev.sheldan.abstracto.core.command.service.management.CommandManagementService;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommandInServerServiceBeanTest {

    @InjectMocks
    private CommandInServerServiceBean testUnit;

    @Mock
    private CommandManagementService commandManagementService;

    @Mock
    private ServerManagementService serverManagementService;

    @Mock
    private CommandInServerManagementService commandInServerManagementService;

    @Mock
    private AServer server;

    @Mock
    private ACommand command;

    @Mock
    private ACommandInAServer commandInAServer;

    private static final Long SERVER_ID = 4L;
    private static final String COMMAND_NAME = "command";

    @Test
    public void testGetCommandInAServerWithId() {
        when(serverManagementService.loadServer(SERVER_ID)).thenReturn(server);
        when(commandManagementService.findCommandByName(COMMAND_NAME)).thenReturn(command);
        when(commandInServerManagementService.getCommandForServer(command, server)).thenReturn(commandInAServer);
        ACommandInAServer foundCommandInServer = testUnit.getCommandInAServer(SERVER_ID, COMMAND_NAME);
        Assert.assertEquals(commandInAServer, foundCommandInServer);
    }

    @Test
    public void testGetCommandInAServer() {
        when(commandManagementService.findCommandByName(COMMAND_NAME)).thenReturn(command);
        when(commandInServerManagementService.getCommandForServer(command, server)).thenReturn(commandInAServer);
        ACommandInAServer foundCommandInServer = testUnit.getCommandInAServer(server, COMMAND_NAME);
        Assert.assertEquals(commandInAServer, foundCommandInServer);
    }
}
