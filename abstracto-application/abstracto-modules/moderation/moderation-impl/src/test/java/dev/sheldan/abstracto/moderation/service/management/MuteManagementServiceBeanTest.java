package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.AServerAChannelMessage;
import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AChannel;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.moderation.model.database.Mute;
import dev.sheldan.abstracto.moderation.repository.MuteRepository;
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

    private static final Long SERVER_ID = 1L;
    private static final Long MUTE_ID = 2L;

    @Test
    public void testCreateMute() {
        AServer server = Mockito.mock(AServer.class);
        long messageId = 9L;
        AChannel channel = Mockito.mock(AChannel.class);
        AUserInAServer mutingUser = Mockito.mock(AUserInAServer.class);
        AUserInAServer mutedUser = Mockito.mock(AUserInAServer.class);
        AUser user = Mockito.mock(AUser.class);
        when(mutedUser.getUserReference()).thenReturn(user);
        when(mutedUser.getServerReference()).thenReturn(server);
        AUser secondUser = Mockito.mock(AUser.class);
        when(mutingUser.getUserReference()).thenReturn(secondUser);
        String reason = "reason";
        String triggerKey = "key";
        Instant unMuteDate = Instant.now();
        AServerAChannelMessage muteMessage = Mockito.mock(AServerAChannelMessage.class);
        when(muteMessage.getMessageId()).thenReturn(messageId);
        when(muteMessage.getServer()).thenReturn(server);
        when(muteMessage.getChannel()).thenReturn(channel);

        testUnit.createMute(mutedUser, mutingUser, reason, unMuteDate, muteMessage, triggerKey, 8L);
        verify(muteRepository, times(1)).save(muteArgumentCaptor.capture());
        Mute createdMute = muteArgumentCaptor.getValue();
        Assert.assertEquals(reason, createdMute.getReason());
        Assert.assertEquals(mutingUser, createdMute.getMutingUser());
        Assert.assertEquals(mutedUser, createdMute.getMutedUser());
        Assert.assertEquals(server, createdMute.getServer());
        Assert.assertFalse(createdMute.getMuteEnded());
        Assert.assertEquals(messageId, createdMute.getMessageId().longValue());
        Assert.assertEquals(channel, createdMute.getMutingChannel());
        Assert.assertEquals(unMuteDate, createdMute.getMuteTargetDate());
    }

    @Test
    public void testFindMute() {
        Mute mute = Mockito.mock(Mute.class);
        ServerSpecificId muteId = Mockito.mock(ServerSpecificId.class);
        when(mute.getMuteId()).thenReturn(muteId);
        when(muteId.getId()).thenReturn(MUTE_ID);
        when(muteRepository.findByMuteId_IdAndMuteId_ServerId(MUTE_ID, SERVER_ID)).thenReturn(Optional.of(mute));
        Optional<Mute> foundMuteOptional = testUnit.findMuteOptional(MUTE_ID, SERVER_ID);
        Assert.assertTrue(foundMuteOptional.isPresent());
        foundMuteOptional.ifPresent(foundMute -> Assert.assertEquals(MUTE_ID, foundMute.getMuteId().getId()));
    }

    @Test
    public void testFindNonExistingMute() {
        when(muteRepository.findByMuteId_IdAndMuteId_ServerId(MUTE_ID, SERVER_ID)).thenReturn(Optional.empty());
        Optional<Mute> foundMuteOptional = testUnit.findMuteOptional(MUTE_ID, SERVER_ID);
        Assert.assertFalse(foundMuteOptional.isPresent());
    }

    @Test
    public void testSaveMute() {
        Mute mute = Mockito.mock(Mute.class);
        testUnit.saveMute(mute);
        verify(muteRepository, times(1)).save(mute);
    }

    @Test
    public void testGetMuteOfUser() {
        AUserInAServer userInAServer = Mockito.mock(AUserInAServer.class);
        Mute mute = Mockito.mock(Mute.class);
        when(muteRepository.findTopByMutedUserAndMuteEndedFalse(userInAServer)).thenReturn(mute);
        Mute aMuteOf = testUnit.getAMuteOf(userInAServer);
        Assert.assertEquals(mute, aMuteOf);
    }

    @Test
    public void testGetMuteOfMember() {
        AUserInAServer userInAServer = Mockito.mock(AUserInAServer.class);
        Mute mute = Mockito.mock(Mute.class);
        Member member = Mockito.mock(Member.class);
        when(userInServerManagementService.loadOrCreateUser(member)).thenReturn(userInAServer);
        when(muteRepository.findTopByMutedUserAndMuteEndedFalse(userInAServer)).thenReturn(mute);
        Mute aMuteOf = testUnit.getAMuteOf(member);
        Assert.assertEquals(mute, aMuteOf);
    }

    @Test
    public void testGetAllMutesOf() {
        AUserInAServer userInAServer = Mockito.mock(AUserInAServer.class);
        Mute mute = Mockito.mock(Mute.class);
        Mute mute2 = Mockito.mock(Mute.class);
        when(muteRepository.findAllByMutedUserAndMuteEndedFalseOrderByMuteId_IdDesc(userInAServer)).thenReturn(Arrays.asList(mute, mute2));
        List<Mute> allMutesOf = testUnit.getAllActiveMutesOf(userInAServer);
        Assert.assertEquals(2, allMutesOf.size());
        Assert.assertEquals(mute, allMutesOf.get(0));
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
        AUserInAServer userInAServer = Mockito.mock(AUserInAServer.class);
        when(muteRepository.existsByMutedUserAndMuteEndedFalse(userInAServer)).thenReturn(value);
        boolean result = testUnit.hasActiveMute(userInAServer);
        Assert.assertEquals(value, result);
    }
}
