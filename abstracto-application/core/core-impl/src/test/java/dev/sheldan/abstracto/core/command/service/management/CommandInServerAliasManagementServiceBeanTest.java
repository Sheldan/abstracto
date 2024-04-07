package dev.sheldan.abstracto.core.command.service.management;

import dev.sheldan.abstracto.core.command.model.database.ACommandInAServer;
import dev.sheldan.abstracto.core.command.model.database.ACommandInServerAlias;
import dev.sheldan.abstracto.core.command.repository.CommandInServerAliasRepository;
import dev.sheldan.abstracto.core.models.database.AServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CommandInServerAliasManagementServiceBeanTest {

    @InjectMocks
    private CommandInServerAliasManagementServiceBean testUnit;

    @Mock
    private CommandInServerAliasRepository repository;

    @Mock
    private ACommandInServerAlias alias;

    @Mock
    private AServer server;

    private static final String ALIAS_NAME = "alias";
    private static final String COMMAND_NAME = "command";

    @Test
    public void testGetAliasesInServer() {
        when(repository.findByCommandInAServer_ServerReference(server)).thenReturn(Arrays.asList(alias));

        List<ACommandInServerAlias> foundAliases = testUnit.getAliasesInServer(server);

        assertThat(foundAliases).hasSize(1);
        assertThat(foundAliases.get(0)).isEqualTo(alias);
    }

    @Test
    public void testDoesCommandInServerAliasExist() {
        when(repository.existsByCommandInAServer_ServerReferenceAndAliasId_NameEqualsIgnoreCase(server, ALIAS_NAME)).thenReturn(true);

        assertThat(testUnit.doesCommandInServerAliasExist(server, ALIAS_NAME)).isTrue();
    }

    @Test
    public void testDoesCommandInServerAliasNotExist() {
        when(repository.existsByCommandInAServer_ServerReferenceAndAliasId_NameEqualsIgnoreCase(server, ALIAS_NAME)).thenReturn(false);

        assertThat(testUnit.doesCommandInServerAliasExist(server, ALIAS_NAME)).isFalse();
    }

    @Test
    public void testGetCommandInServerAlias() {
        when(repository.findByCommandInAServer_ServerReferenceAndAliasId_NameEqualsIgnoreCase(server, ALIAS_NAME)).thenReturn(Optional.of(alias));

        Optional<ACommandInServerAlias> commandOptional = testUnit.getCommandInServerAlias(server, ALIAS_NAME);

        assertThat(commandOptional.isPresent()).isTrue();
        commandOptional.ifPresent(existingAlias ->
            assertThat(existingAlias).isEqualTo(alias)
        );
    }

    @Test
    public void testGetCommandInServerAliasNotExist() {
        when(repository.findByCommandInAServer_ServerReferenceAndAliasId_NameEqualsIgnoreCase(server, ALIAS_NAME)).thenReturn(Optional.empty());

        Optional<ACommandInServerAlias> commandOptional = testUnit.getCommandInServerAlias(server, ALIAS_NAME);

        assertThat(commandOptional.isPresent()).isFalse();
    }

    @Test
    public void testCreateAliasForCommand() {
        ACommandInAServer commandInAServer = Mockito.mock(ACommandInAServer.class);
        ArgumentCaptor<ACommandInServerAlias> aliasArgumentCaptor = ArgumentCaptor.forClass(ACommandInServerAlias.class);
        ACommandInServerAlias savedAlias = Mockito.mock(ACommandInServerAlias.class);
        when(repository.save(aliasArgumentCaptor.capture())).thenReturn(savedAlias);

        ACommandInServerAlias createdAlias = testUnit.createAliasForCommand(commandInAServer, ALIAS_NAME);

        assertThat(createdAlias).isEqualTo(savedAlias);
        ACommandInServerAlias capturedAlias = aliasArgumentCaptor.getValue();
        assertThat(capturedAlias.getCommandInAServer()).isEqualTo(commandInAServer);
    }

    @Test
    public void testDeleteCommandInServerAlias() {
        testUnit.deleteCommandInServerAlias(alias);

        verify(repository, times(1)).delete(alias);
    }

    @Test
    public void testGetAliasesForCommandInServer() {
        when(repository.findByCommandInAServer_ServerReferenceAndCommandInAServer_CommandReference_NameEqualsIgnoreCase(server, COMMAND_NAME)).thenReturn(Arrays.asList(alias));

        List<ACommandInServerAlias> foundAliases = testUnit.getAliasesForCommandInServer(server, COMMAND_NAME);

        assertThat(foundAliases).hasSize(1);
        assertThat(foundAliases.get(0)).isEqualTo(alias);
    }
}
