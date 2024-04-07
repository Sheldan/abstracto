package dev.sheldan.abstracto.core.command.service.management;

import dev.sheldan.abstracto.core.command.model.database.CommandDisabledChannelGroup;
import dev.sheldan.abstracto.core.command.repository.CommandDisabledChannelGroupRepository;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import dev.sheldan.abstracto.core.models.database.AServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommandDisabledChannelGroupManagementServiceBeanTest {

    @InjectMocks
    private CommandDisabledChannelGroupManagementServiceBean unitToTest;

    @Mock
    private CommandDisabledChannelGroupRepository repository;

    private static final Long CHANNEL_GROUP_ID = 1L;

    @Test
    public void findViaChannelGroup() {
        AChannelGroup aChannelGroup = aChannelGroup();
        CommandDisabledChannelGroup aCommandDisabledChannelGroup = aCommandDisabledChannelGroup();
        when(repository.getReferenceById(CHANNEL_GROUP_ID)).thenReturn(aCommandDisabledChannelGroup);

        CommandDisabledChannelGroup viaChannelGroup = unitToTest.findViaChannelGroup(aChannelGroup);

        assertThat(viaChannelGroup).isEqualTo(aCommandDisabledChannelGroup);
    }

    @Test
    public void testDeleteCommandDisabledChannelGroup() {
        unitToTest.deleteCommandDisabledChannelGroup(aChannelGroup());

        verify(repository).deleteById(CHANNEL_GROUP_ID);
    }

    @Test
    public void testCreateCommandDisabledChannelGroup() {

        AChannelGroup aChannelGroup = aChannelGroup();

        unitToTest.createCommandDisabledChannelGroup(aChannelGroup);

        ArgumentCaptor<CommandDisabledChannelGroup> commandDisabledChannelGroupArgumentCaptor = ArgumentCaptor.forClass(CommandDisabledChannelGroup.class);
        verify(repository).save(commandDisabledChannelGroupArgumentCaptor.capture());
        CommandDisabledChannelGroup commandDisabledChannelGroup = commandDisabledChannelGroupArgumentCaptor.getValue();
        assertThat(commandDisabledChannelGroup.getId()).isEqualTo(CHANNEL_GROUP_ID);
        assertThat(commandDisabledChannelGroup.getChannelGroup()).isEqualTo(aChannelGroup);
    }

    private AChannelGroup aChannelGroup() {
        AServer server = AServer
                .builder()
                .build();

        return AChannelGroup
                .builder()
                .id(CHANNEL_GROUP_ID)
                .server(server)
                .build();
    }

    private CommandDisabledChannelGroup aCommandDisabledChannelGroup() {
        return CommandDisabledChannelGroup
                .builder()
                .build();
    }
}
