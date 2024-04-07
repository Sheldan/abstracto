package dev.sheldan.abstracto.core.command.service.management;

import dev.sheldan.abstracto.core.command.model.database.CoolDownChannelGroup;
import dev.sheldan.abstracto.core.command.repository.CoolDownChannelGroupRepository;
import dev.sheldan.abstracto.core.models.database.AChannelGroup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CoolDownChannelGroupManagementServiceBeanTest {

    @InjectMocks
    private CoolDownChannelGroupManagementServiceBean unitToTest;

    @Mock
    private CoolDownChannelGroupRepository repository;

    private static final Long CHANNEL_GROUP_ID = 1L;

    @Test
    public void createCoolDownChannelGroup() {
        AChannelGroup aChannelGroup = aChannelGroup();

        ArgumentCaptor<CoolDownChannelGroup> coolDownChannelGroupArgumentCaptor = ArgumentCaptor.forClass(CoolDownChannelGroup.class);

        unitToTest.createCoolDownChannelGroup(aChannelGroup);

        verify(repository).save(coolDownChannelGroupArgumentCaptor.capture());

        CoolDownChannelGroup coolDownChannelGroupArgumentCaptorValue = coolDownChannelGroupArgumentCaptor.getValue();
        assertThat(coolDownChannelGroupArgumentCaptorValue.getChannelGroup()).isEqualTo(aChannelGroup);
        assertThat(coolDownChannelGroupArgumentCaptorValue.getMemberCoolDown()).isEqualTo(0L);
        assertThat(coolDownChannelGroupArgumentCaptorValue.getChannelCoolDown()).isEqualTo(0L);
        assertThat(coolDownChannelGroupArgumentCaptorValue.getId()).isEqualTo(CHANNEL_GROUP_ID);
    }

    @Test
    public void findByChannelGroupId() {
        CoolDownChannelGroup aCoolDownChannelGroup = aCooldownChannelGroup();
        when(repository.getReferenceById(CHANNEL_GROUP_ID)).thenReturn(aCoolDownChannelGroup);

        assertThat(unitToTest.findByChannelGroupId(CHANNEL_GROUP_ID)).isEqualTo(aCoolDownChannelGroup);
    }

    @Test
    public void deleteCoolDownChannelGroup() {
        AChannelGroup aChannelGroup = aChannelGroup();

        unitToTest.deleteCoolDownChannelGroup(aChannelGroup);

        verify(repository).deleteById(CHANNEL_GROUP_ID);
    }

    private CoolDownChannelGroup aCooldownChannelGroup() {
        return CoolDownChannelGroup
                .builder()
                .build();
    }

    private AChannelGroup aChannelGroup() {
        return AChannelGroup
                .builder()
                .id(CHANNEL_GROUP_ID)
                .build();
    }

}
