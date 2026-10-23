package com.cloudBasedStorageService.StorageContainer.service;

import com.cloudBasedStorageService.StorageContainer.model.*;
import com.cloudBasedStorageService.StorageContainer.model.dto.ShareRequest;
import com.cloudBasedStorageService.StorageContainer.repo.FileRepository;
import com.cloudBasedStorageService.StorageContainer.repo.FolderRepository;
import com.cloudBasedStorageService.StorageContainer.repo.ShareRepository;
import com.cloudBasedStorageService.StorageContainer.repo.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ShareServiceTest {

    private static final String OWNER_EMAIL = "owner@example.com";
    private static final String RECIPIENT_EMAIL = "friend@example.com";

    private ShareRepository shareRepository;
    private UserRepository userRepository;
    private FileRepository fileRepository;
    private FolderRepository folderRepository;
    private UserService userService;
    private ShareService service;

    private User owner;
    private User stranger;

    @BeforeEach
    void setUp() {

        shareRepository = mock(ShareRepository.class);
        userRepository = mock(UserRepository.class);
        fileRepository = mock(FileRepository.class);
        folderRepository = mock(FolderRepository.class);
        userService = mock(UserService.class);

        service = new ShareService(
                shareRepository, userService, userRepository,
                fileRepository, folderRepository);

        owner = user(1, OWNER_EMAIL);
        stranger = user(2, RECIPIENT_EMAIL);

        when(userService.getUserByUsername(OWNER_EMAIL)).thenReturn(owner);
        when(shareRepository.save(any(Share.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        signIn(OWNER_EMAIL);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ownerCanShareAFileAndBecomesTheRecordedSharer() {

        givenFile(10, owner);
        givenRecipientExists();

        Share saved = service.createShare(
                new ShareRequest(RECIPIENT_EMAIL, 10, ItemType.FILE, Role.VIEWER));

        assertThat(saved.getSharedBy().getUserId()).isEqualTo(1);
        assertThat(saved.getEmail()).isEqualTo(RECIPIENT_EMAIL);
    }

    @Test
    void cannotShareSomeoneElsesFile() {

        givenFile(10, stranger);
        givenRecipientExists();

        assertThatThrownBy(() -> service.createShare(
                new ShareRequest(RECIPIENT_EMAIL, 10, ItemType.FILE, Role.VIEWER)))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(shareRepository, never()).save(any());
    }

    @Test
    void cannotShareWithYourself() {

        givenFile(10, owner);

        assertThatThrownBy(() -> service.createShare(
                new ShareRequest(OWNER_EMAIL, 10, ItemType.FILE, Role.VIEWER)))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void cannotShareWithAnUnknownAddress() {

        givenFile(10, owner);

        when(userRepository.findByUserEmail(RECIPIENT_EMAIL))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createShare(
                new ShareRequest(RECIPIENT_EMAIL, 10, ItemType.FILE, Role.VIEWER)))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void cannotGrantARoleInsteadOfASharePermission() {

        givenFile(10, owner);
        givenRecipientExists();

        assertThatThrownBy(() -> service.createShare(
                new ShareRequest(RECIPIENT_EMAIL, 10, ItemType.FILE, Role.ADMIN)))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void cannotShareTheSameItemTwiceWithOnePerson() {

        givenFile(10, owner);
        givenRecipientExists();

        when(shareRepository.findByEmailAndItemIdAndItemType(
                RECIPIENT_EMAIL, 10, ItemType.FILE))
                .thenReturn(Optional.of(new Share()));

        assertThatThrownBy(() -> service.createShare(
                new ShareRequest(RECIPIENT_EMAIL, 10, ItemType.FILE, Role.VIEWER)))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void recipientEmailIsStoredLowercased() {

        givenFile(10, owner);

        when(userRepository.findByUserEmail(RECIPIENT_EMAIL)).thenReturn(Optional.of(stranger));

        service.createShare(new ShareRequest(
                "Friend@Example.COM", 10, ItemType.FILE, Role.VIEWER));

        ArgumentCaptor<Share> captor = ArgumentCaptor.forClass(Share.class);

        verify(shareRepository).save(captor.capture());

        assertThat(captor.getValue().getEmail()).isEqualTo(RECIPIENT_EMAIL);
    }

    @Test
    void sharerCanRevokeTheirOwnShare() {

        givenFile(10, owner);

        when(shareRepository.findById(99)).thenReturn(Optional.of(
                share(99, owner, RECIPIENT_EMAIL, 10, ItemType.FILE)));

        service.deleteShare(99);

        verify(shareRepository).delete(any(Share.class));
    }

    @Test
    void itemOwnerCanRevokeAShareTheyDidNotCreate() {

        givenFile(10, owner);

        User anotherSharer = user(5, "someone-else@example.com");

        when(shareRepository.findById(99)).thenReturn(Optional.of(
                share(99, anotherSharer, RECIPIENT_EMAIL, 10, ItemType.FILE)));

        service.deleteShare(99);

        verify(shareRepository).delete(any(Share.class));
    }

    @Test
    void unrelatedUserCannotRevokeSomeoneElsesShare() {

        User unrelatedFileOwner = user(3, "third-party@example.com");

        givenFile(10, unrelatedFileOwner);

        when(shareRepository.findById(99)).thenReturn(Optional.of(
                share(99, unrelatedFileOwner, RECIPIENT_EMAIL, 10, ItemType.FILE)));

        assertThatThrownBy(() -> service.deleteShare(99))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(shareRepository, never()).delete(any());
    }

    private void signIn(String email) {

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null));
    }

    private void givenFile(int id, User creator) {

        File file = new File();

        file.setId(id);
        file.setFileName("report.pdf");
        file.setCreatedBy(creator);

        when(fileRepository.findById(id)).thenReturn(Optional.of(file));
    }

    private void givenRecipientExists() {

        when(userRepository.findByUserEmail(RECIPIENT_EMAIL))
                .thenReturn(Optional.of(stranger));
    }

    private static User user(int id, String email) {

        User user = new User();

        user.setUserId(id);
        user.setUserEmail(email);
        user.setUserRole(Role.PUBLIC_USER);

        return user;
    }

    private static Share share(int id, User sharedBy, String email, int itemId, ItemType type) {

        Share share = new Share();

        share.setId(id);
        share.setSharedBy(sharedBy);
        share.setEmail(email);
        share.setItemId(itemId);
        share.setItemType(type);
        share.setPermission(Role.VIEWER);

        return share;
    }
}
