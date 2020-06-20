package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.AServerAChannelMessage;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.moderation.models.database.Mute;
import dev.sheldan.abstracto.moderation.repository.MuteRepository;
import dev.sheldan.abstracto.test.MockUtils;
import net.dv8tion.jda.api.entities.Member;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MuteManagementServiceBeanTest {

    @InjectMocks
    private MuteManagementServiceBean testUnit;

    @Mock
    private MuteRepository muteRepository;

    @Mock
    private UserInServerManagementService userInServerManagementService;

    @Captor
    private ArgumentCaptor<Mute> muteArgumentCaptor;

    @Test
    public void testCreateMute() {
        AServer server = MockUtils.getServer();
        long messageId = 9L;
        AChannel channel = MockUtils.getTextChannel(server, 8L);
        AUserInAServer mutingUser = MockUtils.getUserObject(5L, server);
        AUserInAServer mutedUser = MockUtils.getUserObject(7L, server);
        String reason = "reason";
        Instant unMuteDate = Instant.now();
        AServerAChannelMessage muteMessage = AServerAChannelMessage.builder().server(server).channel(channel).messageId(messageId).build();

        testUnit.createMute(mutedUser, mutingUser, reason, unMuteDate, muteMessage);
        verify(muteRepository, times(1)).save(muteArgumentCaptor.capture());
        Mute createdMute = muteArgumentCaptor.getValue();
        Assert.assertEquals(reason, createdMute.getReason());
        Assert.assertEquals(mutingUser, createdMute.getMutingUser());
        Assert.assertEquals(mutedUser, createdMute.getMutedUser());
        Assert.assertEquals(server, createdMute.getMutingServer());
        Assert.assertFalse(createdMute.getMuteEnded());
        Assert.assertEquals(messageId, createdMute.getMessageId().longValue());
        Assert.assertEquals(channel, createdMute.getMutingChannel());
        Assert.assertEquals(unMuteDate, createdMute.getMuteTargetDate());
    }

    @Test
    public void testFindMute() {
        Long id = 5L;
        Mute mute = Mute.builder().id(id).build();
        when(muteRepository.findById(id)).thenReturn(Optional.of(mute));
        Optional<Mute> foundMuteOptional = testUnit.findMute(id);
        Assert.assertTrue(foundMuteOptional.isPresent());
        foundMuteOptional.ifPresent(foundMute -> Assert.assertEquals(id, foundMute.getId()));
    }

    @Test
    public void testFindNonExistingMute() {
        Long id = 5L;
        when(muteRepository.findById(id)).thenReturn(Optional.empty());
        Optional<Mute> foundMuteOptional = testUnit.findMute(id);
        Assert.assertFalse(foundMuteOptional.isPresent());
    }

    @Test
    public void testSaveMute() {
        Mute mute = Mute.builder().build();
        testUnit.saveMute(mute);
        verify(muteRepository, times(1)).save(mute);
    }

    @Test
    public void testGetMuteOfUser() {
        AServer server = MockUtils.getServer();
        AUserInAServer userInAServer = MockUtils.getUserObject(9L, server);
        Mute mute = Mute.builder().build();
        when(muteRepository.findTopByMutedUserAndMuteEndedFalse(userInAServer)).thenReturn(mute);
        Mute aMuteOf = testUnit.getAMuteOf(userInAServer);
        Assert.assertEquals(mute, aMuteOf);
    }

    @Test
    public void testGetMuteOfMember() {
        AServer server = MockUtils.getServer();
        AUserInAServer userInAServer = MockUtils.getUserObject(9L, server);
        Member member = Mockito.mock(Member.class);
        when(userInServerManagementService.loadUser(member)).thenReturn(userInAServer);
        Mute mute = Mute.builder().build();
        when(muteRepository.findTopByMutedUserAndMuteEndedFalse(userInAServer)).thenReturn(mute);
        Mute aMuteOf = testUnit.getAMuteOf(member);
        Assert.assertEquals(mute, aMuteOf);
    }

    @Test
    public void testGetAllMutesOf() {
        AServer server = MockUtils.getServer();
        AUserInAServer userInAServer = MockUtils.getUserObject(9L, server);
        Mute mute1 = Mute.builder().build();
        Mute mute2 = Mute.builder().build();
        when(muteRepository.findAllByMutedUserAndMuteEndedFalseOrderByIdDesc(userInAServer)).thenReturn(Arrays.asList(mute1, mute2));
        List<Mute> allMutesOf = testUnit.getAllMutesOf(userInAServer);
        Assert.assertEquals(2, allMutesOf.size());
        Assert.assertEquals(mute1, allMutesOf.get(0));
        Assert.assertEquals(mute2, allMutesOf.get(1));
    }

    @Test
    public void testHasActiveMute() {
        checkExist(true);
    }

    @Test
    public void testHasNoActiveMute() {
        checkExist(false);
    }

    private void checkExist(boolean value) {
        AServer server = MockUtils.getServer();
        AUserInAServer userInAServer = MockUtils.getUserObject(9L, server);
        when(muteRepository.existsByMutedUserAndMuteEndedFalse(userInAServer)).thenReturn(value);
        boolean result = testUnit.hasActiveMute(userInAServer);
        Assert.assertEquals(value, result);
    }
}
