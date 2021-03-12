package dev.sheldan.abstracto.repostdetection.service.management;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.repostdetection.exception.RepostNotFoundException;
import dev.sheldan.abstracto.repostdetection.model.database.PostedImage;
import dev.sheldan.abstracto.repostdetection.model.database.Repost;
import dev.sheldan.abstracto.repostdetection.model.database.embed.PostIdentifier;
import dev.sheldan.abstracto.repostdetection.model.database.embed.RepostIdentifier;
import dev.sheldan.abstracto.repostdetection.model.database.result.RepostLeaderboardResult;
import dev.sheldan.abstracto.repostdetection.repository.RepostRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RepostManagementServiceBeanTest {

    @InjectMocks
    private RepostManagementServiceBean testUnit;

    @Mock
    private RepostRepository repostRepository;

    @Mock
    private PostedImage postedImage;

    @Mock
    private AUserInAServer poster;

    @Mock
    private PostIdentifier postIdentifier;

    @Mock
    private Repost repost;

    @Mock
    private AServer server;

    @Captor
    private ArgumentCaptor<Repost> repostArgumentCaptor;

    @Captor
    private ArgumentCaptor<Pageable> pageableArgumentCaptor;

    private static final Long MESSAGE_ID = 1L;
    private static final Integer POSITION = 2;
    private static final Long USER_IN_SERVER_ID = 3L;
    private static final Integer COUNT = 4;
    private static final Long SERVER_ID = 5L;
    private static final Integer PAGE = 6;
    private static final Integer PAGE_SIZE = 7;

    @Test
    public void testCreateRepost() {
        setupPostIdentifier();
        testUnit.createRepost(postedImage, poster);
        verify(repostRepository, times(1)).save(repostArgumentCaptor.capture());

        Repost capturedRepost = repostArgumentCaptor.getValue();
        Assert.assertEquals(poster, capturedRepost.getPoster());
        Assert.assertEquals(postedImage, capturedRepost.getOriginalPost());
        Assert.assertEquals(MESSAGE_ID, capturedRepost.getRepostId().getMessageId());
        Assert.assertEquals(POSITION, capturedRepost.getRepostId().getPosition());
        Assert.assertEquals(USER_IN_SERVER_ID, capturedRepost.getRepostId().getUserInServerId());
    }

    @Test
    public void testFindRepostOptional() {
        setupPostIdentifier();
        when(repostRepository.findById(any(RepostIdentifier.class))).thenReturn(Optional.of(repost));
        Optional<Repost> repostOptional = testUnit.findRepostOptional(postedImage, poster);
        Assert.assertTrue(repostOptional.isPresent());
        repostOptional.ifPresent(foundRepost -> Assert.assertEquals(repost, foundRepost));
    }

    @Test
    public void testFindRepostOptionalNotPresent() {
        setupPostIdentifier();
        when(repostRepository.findById(any(RepostIdentifier.class))).thenReturn(Optional.empty());
        Optional<Repost> repostOptional = testUnit.findRepostOptional(postedImage, poster);
        Assert.assertFalse(repostOptional.isPresent());
    }

    @Test
    public void testSetRepostCount() {
        setupPostIdentifier();
        when(repostRepository.findById(any(RepostIdentifier.class))).thenReturn(Optional.of(repost));
        Repost updatedRepost = testUnit.setRepostCount(postedImage, poster, COUNT);
        verify(repost, times(1)).setCount(4);
        Assert.assertEquals(repost, updatedRepost);
    }

    @Test(expected = RepostNotFoundException.class)
    public void testSetRepostCountNotFound() {
        setupPostIdentifier();
        when(repostRepository.findById(any(RepostIdentifier.class))).thenReturn(Optional.empty());
        testUnit.setRepostCount(postedImage, poster, COUNT);
    }

    @Test(expected = RepostNotFoundException.class)
    public void testFindRepostNotFound() {
        setupPostIdentifier();
        when(repostRepository.findById(any(RepostIdentifier.class))).thenReturn(Optional.empty());
        testUnit.findRepost(postedImage, poster);
    }

    @Test
    public void testFindRepost() {
        setupPostIdentifier();
        when(repostRepository.findById(any(RepostIdentifier.class))).thenReturn(Optional.of(repost));
        Repost foundRepost = testUnit.findRepost(postedImage, poster);
        Assert.assertEquals(repost, foundRepost);
    }

    @Test
    public void testFindTopRepostingUsersOfServer() {
        RepostLeaderboardResult singleResult = Mockito.mock(RepostLeaderboardResult.class);
        when(repostRepository.findTopRepostingUsers(eq(SERVER_ID), pageableArgumentCaptor.capture())).thenReturn(Arrays.asList(singleResult));
        List<RepostLeaderboardResult> foundResult = testUnit.findTopRepostingUsersOfServer(SERVER_ID, PAGE, PAGE_SIZE);
        checkLeaderboardResult(singleResult, foundResult);
    }

    @Test
    public void testFindTopRepostingUsersOfServerAServer() {
        when(server.getId()).thenReturn(SERVER_ID);
        RepostLeaderboardResult singleResult = Mockito.mock(RepostLeaderboardResult.class);
        when(repostRepository.findTopRepostingUsers(eq(SERVER_ID), pageableArgumentCaptor.capture())).thenReturn(Arrays.asList(singleResult));
        List<RepostLeaderboardResult> foundResult = testUnit.findTopRepostingUsersOfServer(server, PAGE, PAGE_SIZE);
        checkLeaderboardResult(singleResult, foundResult);
    }

    @Test
    public void testGetRepostRankOfUser() {
        setupPoster();
        RepostLeaderboardResult singleResult = Mockito.mock(RepostLeaderboardResult.class);
        when(repostRepository.getRepostRankOfUserInServer(USER_IN_SERVER_ID, SERVER_ID)).thenReturn(singleResult);
        RepostLeaderboardResult foundRank = testUnit.getRepostRankOfUser(poster);
        Assert.assertEquals(singleResult, foundRank);
    }

    @Test
    public void testDeleteRepostsFromUser() {
        setupPoster();
        testUnit.deleteRepostsFromUser(poster);
        verify(repostRepository, times(1)).deleteByRepostId_UserInServerIdAndServerId(USER_IN_SERVER_ID, SERVER_ID);
    }

    @Test
    public void testDeleteRepostsFromServer() {
        when(server.getId()).thenReturn(SERVER_ID);
        testUnit.deleteRepostsFromServer(server);
        verify(repostRepository, times(1)).deleteByServerId(SERVER_ID);
    }

    private void setupPoster() {
        when(poster.getServerReference()).thenReturn(server);
        when(server.getId()).thenReturn(SERVER_ID);
        when(poster.getUserInServerId()).thenReturn(USER_IN_SERVER_ID);
    }

    private void checkLeaderboardResult(RepostLeaderboardResult singleResult, List<RepostLeaderboardResult> foundResult) {
        Pageable usedPaging = pageableArgumentCaptor.getValue();
        Assert.assertEquals(PAGE - 1, usedPaging.getPageNumber());
        Assert.assertEquals(PAGE_SIZE.intValue(), usedPaging.getPageSize());
        Assert.assertEquals(1, foundResult.size());
        Assert.assertEquals(singleResult, foundResult.get(0));
    }

    private void setupPostIdentifier() {
        when(postedImage.getPostId()).thenReturn(postIdentifier);
        when(postIdentifier.getMessageId()).thenReturn(MESSAGE_ID);
        when(postIdentifier.getPosition()).thenReturn(POSITION);
        when(poster.getUserInServerId()).thenReturn(USER_IN_SERVER_ID);
    }
}
