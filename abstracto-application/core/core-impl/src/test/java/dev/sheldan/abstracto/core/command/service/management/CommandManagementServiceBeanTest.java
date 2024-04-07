package dev.sheldan.abstracto.core.command.service.management;

import dev.sheldan.abstracto.core.command.exception.CommandNotFoundException;
import dev.sheldan.abstracto.core.command.model.database.ACommand;
import dev.sheldan.abstracto.core.command.model.database.AModule;
import dev.sheldan.abstracto.core.command.repository.CommandRepository;
import dev.sheldan.abstracto.core.models.database.AFeature;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommandManagementServiceBeanTest {

    @InjectMocks
    private CommandManagementServiceBean unitToTest;

    @Mock
    private CommandRepository repository;

    private static final String COMMAND_NAME = "NAME";
    private static final String COMMAND_NAME_LOWER = COMMAND_NAME.toLowerCase();

    @Test
    public void getAllCommands() {
        List<ACommand> commands = commands();
        when(repository.findAll()).thenReturn(commands);

        assertThat(unitToTest.getAllCommands()).isEqualTo(commands);
    }

    @Test
    public void doesCommandExist() {
        when(repository.existsByNameIgnoreCase(COMMAND_NAME_LOWER)).thenReturn(true);
        assertThat(unitToTest.doesCommandExist(COMMAND_NAME)).isTrue();
    }

    @Test
    public void findCommandByName() {
        ACommand aCommand = aCommand();
        when(repository.findByNameIgnoreCase(COMMAND_NAME_LOWER)).thenReturn(Optional.of(aCommand));
        ACommand commandByName = unitToTest.findCommandByName(COMMAND_NAME);
        assertThat(commandByName).isEqualTo(aCommand);
    }

    @Test
    public void findCommandByNameNotExists() {
        when(repository.findByNameIgnoreCase(COMMAND_NAME_LOWER)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> {
            unitToTest.findCommandByName(COMMAND_NAME);
        }).isInstanceOf(CommandNotFoundException.class);
    }

    @Test
    public void findCommandByNameOptional() {
        ACommand aCommand = aCommand();
        Optional<ACommand> optionalACommand = Optional.of(aCommand);
        when(repository.findByNameIgnoreCase(COMMAND_NAME_LOWER)).thenReturn(optionalACommand);
        Optional<ACommand> commandByName = unitToTest.findCommandByNameOptional(COMMAND_NAME);
        assertThat(commandByName).isEqualTo(optionalACommand);
    }

    @Test
    public void findCommandByNameOptionalEmpty() {
        when(repository.findByNameIgnoreCase(COMMAND_NAME_LOWER)).thenReturn(Optional.empty());
        Optional<ACommand> commandByName = unitToTest.findCommandByNameOptional(COMMAND_NAME);
        assertThat(commandByName).isEmpty();
    }

    @Test
    public void createCommandWithObj() {
        AModule aModule = aModule();
        AFeature aFeature = aFeature();
        ArgumentCaptor<ACommand> aCommandArgumentCaptor = ArgumentCaptor.forClass(ACommand.class);
        unitToTest.createCommand(COMMAND_NAME, aModule, aFeature);
        verify(repository).save(aCommandArgumentCaptor.capture());
        ACommand aCommand = aCommandArgumentCaptor.getValue();
        assertThat(aCommand.getModule()).isEqualTo(aModule);
        assertThat(aCommand.getFeature()).isEqualTo(aFeature);
        assertThat(aCommand.getName()).isEqualTo(COMMAND_NAME_LOWER);
    }

    private AModule aModule() {
        return AModule
                .builder()
                .build();
    }

    private AFeature aFeature() {
        return AFeature
                .builder()
                .build();
    }

    private List<ACommand> commands() {
        return Collections.singletonList(aCommand());
    }

    private ACommand aCommand() {
        return ACommand
                .builder()
                .build();
    }
}
