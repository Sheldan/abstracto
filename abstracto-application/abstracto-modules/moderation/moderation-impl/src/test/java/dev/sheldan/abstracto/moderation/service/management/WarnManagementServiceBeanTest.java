package dev.sheldan.abstracto.moderation.service.management;

import dev.sheldan.abstracto.core.models.ServerSpecificId;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.moderation.models.database.Warning;
import dev.sheldan.abstracto.moderation.repository.WarnRepository;
import dev.sheldan.abstracto.test.MockUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

    private AUserInAServer warnedUser;
    private AServer server;


    @Before
    public void setup() {
        this.server = MockUtils.getServer();
        this.warnedUser = MockUtils.getUserObject(5L, server);
    }

    @Test
    public void testCreateWarning() {
        AUserInAServer warningUser = MockUtils.getUserObject(7L, server);
        String reason = "REASON";
        Warning warning = testUnit.createWarning(warnedUser, warningUser, reason, 8L);
        Assert.assertEquals(warningUser, warning.getWarningUser());
        Assert.assertEquals(warnedUser, warning.getWarnedUser());
        Assert.assertEquals(reason, warning.getReason());
        Assert.assertFalse(warning.getDecayed());
        verify(warnRepository, times(1)).save(warning);
    }

    @Test
    public void testRetrieveWarningsOlderThan() {
        Instant date = Instant.now();
        List<Warning> existingWarnings = Arrays.asList(getWarning(), getWarning());
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
        List<Warning> existingWarnings = Arrays.asList(getWarning(), getWarning());
        when(warnRepository.findByWarnedUser(warnedUser)).thenReturn(existingWarnings);
        List<Warning> foundWarnings = testUnit.getAllWarnsForUser(warnedUser);
        checkFoundWarns(existingWarnings, foundWarnings);
    }

    @Test
    public void testGetAllWarningsOfServer() {
        List<Warning> existingWarnings = Arrays.asList(getWarning(), getWarning());
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
        Long warnId = 6L;
        Long serverId = 8L;
        Warning existingWarning = getWarning();
        when(warnRepository.findByWarnId_IdAndWarnId_ServerId(warnId, serverId)).thenReturn(Optional.ofNullable(existingWarning));
        Optional<Warning> warningOptional = testUnit.findById(warnId, serverId);
        Assert.assertTrue(warningOptional.isPresent());
        warningOptional.ifPresent(foundWarning -> Assert.assertEquals(existingWarning, foundWarning));
    }

    @Test
    public void testFindByIdNotExisting() {
        Long warnId = 6L;
        Long serverId = 8L;
        when(warnRepository.findByWarnId_IdAndWarnId_ServerId(warnId, serverId)).thenReturn(Optional.ofNullable(null));
        Optional<Warning> warningOptional = testUnit.findById(warnId, serverId);
        Assert.assertFalse(warningOptional.isPresent());
    }

    @Test
    public void testDeleteWarning() {
        Warning warning = getWarning();
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

    private Warning getWarning() {
        return Warning.builder().warnId(new ServerSpecificId(3L, 4L)).build();
    }
}
