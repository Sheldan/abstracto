package dev.sheldan.abstracto.core.command.service.management;

import dev.sheldan.abstracto.core.command.exception.CommandNotFoundException;
import dev.sheldan.abstracto.core.command.model.database.ACommand;
import dev.sheldan.abstracto.core.command.model.database.ACommandInAServer;
import dev.sheldan.abstracto.core.command.repository.CommandInServerRepository;
import dev.sheldan.abstracto.core.models.database.AServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommandInServerManagementServiceBeanTest {
    @InjectMocks
    private CommandInServerManagementServiceBean unitToTest;

    @Mock
    private CommandInServerRepository repository;

    private static final Long COMMAND_ID = 1L;
    private static final Long SERVER_ID = 2L;

    @Test
    public void getCommandsFromServer() {
        List<Long> commandIds = Arrays.asList(COMMAND_ID);
        List<ACommandInAServer> commandsInServer = commandsInServer();
        when(repository.findAllById(commandIds)).thenReturn(commandsInServer);

        assertThat(unitToTest.getCommandsForServer(commandIds)).isEqualTo(commandsInServer);
    }

    @Test
    public void getCommandForServerViaId() {
        ACommandInAServer aCommandInServer = aCommandInAServer();
        when(repository.getReferenceById(COMMAND_ID)).thenReturn(aCommandInServer);

        assertThat(unitToTest.getCommandForServer(COMMAND_ID)).isEqualTo(aCommandInServer);
    }

    @Test
    public void getCommandForServerViaServerId() {
        ACommand aCommand = aCommand();
        ACommandInAServer aCommandInServer = aCommandInAServer();
        when(repository.findByServerReference_IdAndCommandReference(SERVER_ID, aCommand)).thenReturn(aCommandInServer);

        assertThat(unitToTest.getCommandForServer(aCommand, SERVER_ID)).isEqualTo(aCommandInServer);
    }

    @Test
    public void getCommandForServerViaServerObj() {
        ACommand aCommand = aCommand();
        AServer aServer = aServer();
        ACommandInAServer aCommandInServer = aCommandInAServer();
        when(repository.findByServerReferenceAndCommandReference(aServer, aCommand)).thenReturn(Optional.of(aCommandInServer));

        assertThat(unitToTest.getCommandForServer(aCommand, aServer)).isEqualTo(aCommandInServer);
    }

    @Test
    public void getCommandNotFoundForServerViaServerObj() {
        ACommand aCommand = aCommand();
        AServer aServer = aServer();
        ACommandInAServer aCommandInServer = aCommandInAServer();
        when(repository.findByServerReferenceAndCommandReference(aServer, aCommand)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> {
            assertThat(unitToTest.getCommandForServer(aCommand, aServer)).isEqualTo(aCommandInServer);
        }).isInstanceOf(CommandNotFoundException.class);
    }

    @Test
    public void setMemberCoolDownForCommandInServer() {
        ACommand aCommand = aCommand();
        AServer aServer = aServer();
        Duration duration = Duration.ofMinutes(1);
        ACommandInAServer aCommandInServer = aCommandInAServer();
        when(repository.findByServerReferenceAndCommandReference(aServer, aCommand)).thenReturn(Optional.of(aCommandInServer));

        unitToTest.setCooldownForCommandInServer(aCommand, aServer, duration);

        assertThat(aCommandInServer.getMemberCooldown()).isEqualTo(duration);
    }

    @Test
    public void doesCommandExistInServer() {
        ACommand aCommand = aCommand();
        AServer aServer = aServer();
        ACommandInAServer aCommandInServer = aCommandInAServer();
        when(repository.findByServerReferenceAndCommandReference(aServer, aCommand)).thenReturn(Optional.of(aCommandInServer));

        assertThat(unitToTest.doesCommandExistInServer(aCommand, aServer)).isTrue();
    }

    @Test
    public void createCommandInServer() {
        ACommand aCommand = aCommand();
        AServer aServer = aServer();
        ArgumentCaptor<ACommandInAServer> commandInAServerArgumentCaptor = ArgumentCaptor.forClass(ACommandInAServer.class);

        unitToTest.createCommandInServer(aCommand, aServer);

        verify(repository).save(commandInAServerArgumentCaptor.capture());
        ACommandInAServer aCommandInServer = commandInAServerArgumentCaptor.getValue();
        assertThat(aCommandInServer.getServerReference()).isEqualTo(aServer);
        assertThat(aCommandInServer.getCommandReference()).isEqualTo(aCommand);
        assertThat(aCommandInServer.getCoolDown()).isEqualTo(0L);
        assertThat(aCommandInServer.getRestricted()).isFalse();
    }

    public List<ACommandInAServer> commandsInServer() {
        return Collections.singletonList(ACommandInAServer
                .builder()
                .build());
    }

    public ACommandInAServer aCommandInAServer() {
        return ACommandInAServer
                .builder()
                .build();
    }

    private ACommand aCommand() {
        return ACommand
                .builder()
                .build();
    }

    private AServer aServer() {
        return AServer
                .builder()
                .build();
    }
}
