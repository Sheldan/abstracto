package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.model.database.Warning;
import dev.sheldan.abstracto.moderation.repository.WarnRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WarnManagementServiceBeanTest {

    @InjectMocks
    private WarnManagementServiceBean testUnit;

    @Mock
    private WarnRepository warnRepository;

    @Mock
    private AUserInAServer warnedUser;

    @Mock
    private AServer server;

    private static final Long SERVER_ID = 1L;
    private static final Long WARN_ID = 2L;

    @Test
    public void testCreateWarning() {
        AUserInAServer warningUser = Mockito.mock(AUserInAServer.class);
        AUser user = Mockito.mock(AUser.class);
        when(warningUser.getServerReference()).thenReturn(server);
        when(warningUser.getUserReference()).thenReturn(user);
        when(warnedUser.getUserReference()).thenReturn(user);
        String reason = "REASON";
        ArgumentCaptor<Warning> warningArgumentCaptor = ArgumentCaptor.forClass(Warning.class);
        Warning savedWarning = Mockito.mock(Warning.class);
        when(warnRepository.save(warningArgumentCaptor.capture())).thenReturn(savedWarning);
        Warning warning = testUnit.createWarning(warnedUser, warningUser, reason, 8L);
        Assert.assertEquals(savedWarning, warning);
        Warning capturedWarning = warningArgumentCaptor.getValue();
        Assert.assertEquals(warningUser, capturedWarning.getWarningUser());
        Assert.assertEquals(warnedUser, capturedWarning.getWarnedUser());
        Assert.assertEquals(reason, capturedWarning.getReason());
        Assert.assertFalse(capturedWarning.getDecayed());
    }

    @Test
    public void testRetrieveWarningsOlderThan() {
        Instant date = Instant.now();
        List<Warning> existingWarnings = Arrays.asList(Mockito.mock(Warning.class), Mockito.mock(Warning.class));
        when(warnRepository.findAllByWarnedUser_ServerReferenceAndDecayedFalseAndWarnDateLessThan(server, date)).thenReturn(existingWarnings);
        List<Warning> activeWarningsInServerOlderThan = testUnit.getActiveWarningsInServerOlderThan(server, date);
        checkFoundWarns(existingWarnings, activeWarningsInServerOlderThan);
    }

    @Test
    public void testWarnCountOfUser() {
        Long count = 5L;
        when(warnRepository.countByWarnedUser(warnedUser)).thenReturn(count);
        Long activeWarnsForUserCount = testUnit.getTotalWarnsForUser(warnedUser);
        Assert.assertEquals(count, activeWarnsForUserCount);
    }

    @Test
    public void testGetAllWarningsOfUser() {
        List<Warning> existingWarnings = Arrays.asList(Mockito.mock(Warning.class), Mockito.mock(Warning.class));
        when(warnRepository.findByWarnedUser(warnedUser)).thenReturn(existingWarnings);
        List<Warning> foundWarnings = testUnit.getAllWarnsForUser(warnedUser);
        checkFoundWarns(existingWarnings, foundWarnings);
    }

    @Test
    public void testGetAllWarningsOfServer() {
        List<Warning> existingWarnings = Arrays.asList(Mockito.mock(Warning.class), Mockito.mock(Warning.class));
        when(warnRepository.findAllByWarnedUser_ServerReference(server)).thenReturn(existingWarnings);
        List<Warning> foundWarnings = testUnit.getAllWarningsOfServer(server);
        checkFoundWarns(existingWarnings, foundWarnings);
    }

    @Test
    public void testActiveWarnCountOfUser() {
        Long count = 5L;
        when(warnRepository.countByWarnedUserAndDecayedFalse(warnedUser)).thenReturn(count);
        Long activeWarnsForUserCount = testUnit.getActiveWarnsForUser(warnedUser);
        Assert.assertEquals(count, activeWarnsForUserCount);
    }

    @Test
    public void testFindByIdExisting() {
        Warning existingWarning = Mockito.mock(Warning.class);
        when(warnRepository.findByWarnId_IdAndWarnId_ServerId(WARN_ID, SERVER_ID)).thenReturn(Optional.ofNullable(existingWarning));
        Optional<Warning> warningOptional = testUnit.findByIdOptional(WARN_ID, SERVER_ID);
        Assert.assertTrue(warningOptional.isPresent());
        warningOptional.ifPresent(foundWarning -> Assert.assertEquals(existingWarning, foundWarning));
    }

    @Test
    public void testFindByIdNotExisting() {
        Long warnId = 6L;
        when(warnRepository.findByWarnId_IdAndWarnId_ServerId(warnId, SERVER_ID)).thenReturn(Optional.ofNullable(null));
        Optional<Warning> warningOptional = testUnit.findByIdOptional(warnId, SERVER_ID);
        Assert.assertFalse(warningOptional.isPresent());
    }

    @Test
    public void testDeleteWarning() {
        Warning warning = Mockito.mock(Warning.class);
        ServerSpecificId warnId = Mockito.mock(ServerSpecificId.class);
        when(warnId.getServerId()).thenReturn(SERVER_ID);
        when(warnId.getId()).thenReturn(WARN_ID);
        when(warning.getWarnId()).thenReturn(warnId);
        testUnit.deleteWarning(warning);
        verify(warnRepository, times(1)).delete(warning);
    }


    private void checkFoundWarns(List<Warning> existingWarnings, List<Warning> activeWarningsInServerOlderThan) {
        Assert.assertEquals(existingWarnings.size(), activeWarningsInServerOlderThan.size());
        for (int i = 0; i < existingWarnings.size(); i++) {
            Warning existingWarning = existingWarnings.get(i);
            Warning foundWarning = activeWarningsInServerOlderThan.get(i);
            Assert.assertEquals(existingWarning, foundWarning);
        }
    }


}
