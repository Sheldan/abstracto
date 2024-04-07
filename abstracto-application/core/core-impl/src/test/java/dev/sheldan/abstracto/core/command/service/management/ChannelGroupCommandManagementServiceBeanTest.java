package dev.sheldan.abstracto.core.command.service.management;

import dev.sheldan.abstracto.core.command.exception.CommandNotFoundInGroupException;
import dev.sheldan.abstracto.core.command.model.database.ACommand;
import dev.sheldan.abstracto.core.command.repository.ChannelGroupCommandRepository;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.AChannelGroupCommand;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChannelGroupCommandManagementServiceBeanTest {

    @InjectMocks
    private ChannelGroupCommandManagementServiceBean unitToTest;

    @Mock
    private ChannelGroupCommandRepository repository;

    @Test
    public void allGroupsForCommand() {
        List<AChannelGroupCommand> channelGroupCommands = channelGroupCommands();
        ACommand aCommand = aCommand();
        when(repository.findByCommand(aCommand)).thenReturn(channelGroupCommands);

        assertThat(unitToTest.getAllGroupCommandsForCommand(aCommand)).isEqualTo(channelGroupCommands);
    }

    @Test
    public void allGroupsForCommandAndGroups() {
        List<AChannelGroup> channelGroups = getChannelGroups();
        ACommand aCommand = aCommand();
        List<AChannelGroupCommand> channelGroupCommands = channelGroupCommands();
        when(repository.findByCommandAndGroupIn(aCommand, channelGroups)).thenReturn(channelGroupCommands);

        assertThat(unitToTest.getAllGroupCommandsForCommandInGroups(aCommand, channelGroups)).isEqualTo(channelGroupCommands);
    }

    @Test
    public void allGroupsForCommandsAndGroupsOfType() {
        ACommand aCommand = aCommand();
        List<AChannelGroupCommand> channelGroupCommands = channelGroupCommands();
        String channelGroupType = "test";
        when(repository.findByCommandAndGroup_ChannelGroupType_GroupTypeKey(aCommand, channelGroupType)).thenReturn(channelGroupCommands);

        assertThat(unitToTest.getAllGroupCommandsForCommandWithType(aCommand, channelGroupType)).isEqualTo(channelGroupCommands);
    }

    @Test
    public void getChannelGroupCommand() {
        ACommand aCommand = aCommand();
        AChannelGroup aChannelGroup = aChannelGroup();
        AChannelGroupCommand aChannelGroupCommand = aChannelGroupCommand();
        when(repository.findByCommandAndGroup(aCommand, aChannelGroup)).thenReturn(Optional.of(aChannelGroupCommand));

        assertThat(unitToTest.getChannelGroupCommand(aCommand, aChannelGroup)).isEqualTo(aChannelGroupCommand);
    }

    @Test
    public void getChannelGroupCommandNotFound() {
        ACommand command = aCommand();
        AChannelGroup aChannelGroup = aChannelGroup();
        when(repository.findByCommandAndGroup(command, aChannelGroup)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> {
            unitToTest.getChannelGroupCommand(command, aChannelGroup);
        }).isInstanceOf(CommandNotFoundInGroupException.class);
    }

    @Test
    public void createCommandInGroup() {
        ACommand aCommand = aCommand();
        AChannelGroup aChannelGroup = aChannelGroup();
        ArgumentCaptor<AChannelGroupCommand> channelGroupCommandArgumentCaptor = ArgumentCaptor.forClass(AChannelGroupCommand.class);

        unitToTest.createCommandInGroup(aCommand, aChannelGroup);

        verify(repository).save(channelGroupCommandArgumentCaptor.capture());
        AChannelGroupCommand aChannelGroupCommand = channelGroupCommandArgumentCaptor.getValue();
        assertThat(aChannelGroupCommand.getCommand()).isEqualTo(aCommand);
        assertThat(aChannelGroupCommand.getEnabled()).isTrue();
        assertThat(aChannelGroupCommand.getGroup()).isEqualTo(aChannelGroup);
    }

    @Test
    public void removeCommandFromGroup() {
        ACommand aCommand = aCommand();
        AChannelGroup aChannelGroup = aChannelGroup();
        AChannelGroupCommand aChannelGroupCommand = aChannelGroupCommand();
        when(repository.findByCommandAndGroup(aCommand, aChannelGroup)).thenReturn(Optional.of(aChannelGroupCommand));

        unitToTest.removeCommandFromGroup(aCommand, aChannelGroup);

        verify(repository).delete(aChannelGroupCommand);
    }

    @Test
    public void removeCommandFromGroupNotFound() {
        ACommand aCommand = aCommand();
        AChannelGroup aChannelGroup = aChannelGroup();
        when(repository.findByCommandAndGroup(aCommand, aChannelGroup)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> {
            unitToTest.removeCommandFromGroup(aCommand, aChannelGroup);
        }).isInstanceOf(CommandNotFoundInGroupException.class);
    }

    @Test
    public void addCommandToGroup() {
        ACommand aCommand = aCommand();
        AChannelGroup aChannelGroup = aChannelGroup();
        when(repository.findByCommandAndGroup(aCommand, aChannelGroup)).thenReturn(Optional.empty());

        unitToTest.addCommandToGroup(aCommand, aChannelGroup);

        ArgumentCaptor<AChannelGroupCommand> channelGroupCommandArgumentCaptor = ArgumentCaptor.forClass(AChannelGroupCommand.class);
        verify(repository).save(channelGroupCommandArgumentCaptor.capture());
        AChannelGroupCommand aChannelGroupCommand = channelGroupCommandArgumentCaptor.getValue();
        assertThat(aChannelGroupCommand.getCommand()).isEqualTo(aCommand);
        assertThat(aChannelGroupCommand.getEnabled()).isTrue();
        assertThat(aChannelGroupCommand.getGroup()).isEqualTo(aChannelGroup);
    }

    @Test
    public void setCommandInGroupEnabledNotExistingToFalse() {
        ACommand aCommand = aCommand();
        AChannelGroup aChannelGroup = aChannelGroup();
        when(repository.findByCommandAndGroup(aCommand, aChannelGroup)).thenReturn(Optional.empty());
        AChannelGroupCommand aChannelGroupCommand = aChannelGroupCommand();
        when(repository.save(any())).thenReturn(aChannelGroupCommand);

        unitToTest.setCommandInGroupTo(aCommand, aChannelGroup, false);

        ArgumentCaptor<AChannelGroupCommand> channelGroupCommandArgumentCaptor = ArgumentCaptor.forClass(AChannelGroupCommand.class);
        verify(repository).save(channelGroupCommandArgumentCaptor.capture());
        AChannelGroupCommand savedChannelGroupCommand = channelGroupCommandArgumentCaptor.getValue();
        assertThat(savedChannelGroupCommand.getCommand()).isEqualTo(aCommand);
        assertThat(savedChannelGroupCommand.getEnabled()).isTrue();
        assertThat(savedChannelGroupCommand.getGroup()).isEqualTo(aChannelGroup);
        assertThat(aChannelGroupCommand.getEnabled()).isFalse();
    }


    private ACommand aCommand() {
        return ACommand
                .builder()
                .build();
    }


    private List<AChannelGroupCommand> channelGroupCommands() {
        return Collections.singletonList(AChannelGroupCommand
                .builder()
                .build());
    }

    private AChannelGroupCommand aChannelGroupCommand() {
        return AChannelGroupCommand
                .builder()
                .build();
    }

    private List<AChannelGroup> getChannelGroups() {
        return Collections.singletonList(AChannelGroup
                .builder()
                .build());
    }

    private AChannelGroup aChannelGroup() {
        return AChannelGroup
                .builder()
                .build();
    }
}
