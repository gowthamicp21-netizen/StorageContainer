package com.cloudBasedStorageService.StorageContainer.service;

import com.cloudBasedStorageService.StorageContainer.model.File;
import com.cloudBasedStorageService.StorageContainer.model.Folder;
import com.cloudBasedStorageService.StorageContainer.model.ItemType;
import com.cloudBasedStorageService.StorageContainer.model.ShareLink;
import com.cloudBasedStorageService.StorageContainer.model.User;
import com.cloudBasedStorageService.StorageContainer.model.dto.PublicShareView;
import com.cloudBasedStorageService.StorageContainer.model.dto.ShareLinkResponse;
import com.cloudBasedStorageService.StorageContainer.repo.FileRepository;
import com.cloudBasedStorageService.StorageContainer.repo.FolderRepository;
import com.cloudBasedStorageService.StorageContainer.repo.ShareLinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The interesting behaviour here is all negative: a link is an unauthenticated door,
 * so the tests are mostly about what the door must refuse to open.
 */
class ShareLinkServiceTest {

    private static final String TOKEN = "Abc123_-xyZ456_-abcdef12";

    private ShareLinkRepository shareLinkRepository;
    private FileRepository fileRepository;
    private FolderRepository folderRepository;
    private FileStorageService fileStorageService;
    private ShareLinkService service;

    private User owner;
    private User stranger;

    @BeforeEach
    void setUp() {

        shareLinkRepository = mock(ShareLinkRepository.class);
        fileRepository = mock(FileRepository.class);
        folderRepository = mock(FolderRepository.class);
        fileStorageService = mock(FileStorageService.class);

        service = new ShareLinkService(
                shareLinkRepository, fileRepository, folderRepository, fileStorageService);

        owner = user(1, "owner@example.com");
        stranger = user(2, "stranger@example.com");
    }

    // ---------- creating ----------

    @Test
    void createsATokenTooRandomToGuess() {

        givenFile(10, owner, null);

        when(shareLinkRepository.save(any(ShareLink.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ShareLinkResponse created = service.create(10, ItemType.FILE, owner);

        ArgumentCaptor<ShareLink> captor = ArgumentCaptor.forClass(ShareLink.class);

        verify(shareLinkRepository).save(captor.capture());

        // 16 random bytes as base64url is 22 characters, and must not be a UUID
        // or anything a caller could derive from the item id.
        assertThat(captor.getValue().getToken()).hasSize(22).matches("[A-Za-z0-9_-]{22}");
        assertThat(created.token()).isEqualTo(captor.getValue().getToken());
        assertThat(captor.getValue().getCreatedBy()).isEqualTo(owner);
    }

    @Test
    void askingForALinkTwiceKeepsTheFirstToken() {

        givenFile(10, owner, null);

        when(shareLinkRepository.findByItemIdAndItemType(10, ItemType.FILE))
                .thenReturn(Optional.of(fileLink(TOKEN, 10, owner)));

        ShareLinkResponse result = service.create(10, ItemType.FILE, owner);

        assertThat(result.token()).isEqualTo(TOKEN);
        verify(shareLinkRepository, never()).save(any());
    }

    @Test
    void cannotLinkSomeoneElsesFile() {

        givenFile(10, stranger, null);

        assertThatThrownBy(() -> service.create(10, ItemType.FILE, owner))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(shareLinkRepository, never()).save(any());
    }

    @Test
    void onlyTheOwnerCanRevokeALink() {

        when(shareLinkRepository.findById(5))
                .thenReturn(Optional.of(fileLink(TOKEN, 10, stranger)));

        assertThatThrownBy(() -> service.revoke(5, owner))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(shareLinkRepository, never()).delete(any());
    }

    @Test
    void deletingAnItemTakesItsLinkWithIt() {

        ShareLink link = fileLink(TOKEN, 10, owner);

        when(shareLinkRepository.findByItemIdAndItemType(10, ItemType.FILE))
                .thenReturn(Optional.of(link));

        service.revokeForItem(ItemType.FILE, 10);

        verify(shareLinkRepository).delete(link);
    }

    // ---------- anonymous reads ----------

    @Test
    void anUnknownTokenLooksLikeAMissingPageNotAPermissionProblem() {

        when(shareLinkRepository.findByToken("nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.view("nope"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void aFileLinkServesOnlyThatFile() {

        givenFile(10, owner, null);
        givenFile(11, owner, null);

        when(shareLinkRepository.findByToken(TOKEN))
                .thenReturn(Optional.of(fileLink(TOKEN, 10, owner)));

        assertThat(service.resolveFile(TOKEN, 10).fileName()).isEqualTo("report.pdf");

        // The whole database is addressable by integer id, so a second id on the same
        // request must be refused rather than streamed.
        assertThatThrownBy(() -> service.resolveFile(TOKEN, 11))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void aFolderLinkReachesFilesNestedInsideIt() {

        Folder root = givenFolder(1, null, owner);
        Folder nested = givenFolder(2, root, owner);

        givenFile(20, owner, nested);

        when(shareLinkRepository.findByToken(TOKEN))
                .thenReturn(Optional.of(folderLink(TOKEN, 1, owner)));

        assertThat(service.resolveFile(TOKEN, 20).fileName()).isEqualTo("report.pdf");
    }

    @Test
    void aFolderLinkRefusesAFileHeldByAnotherAccount() {

        Folder linked = givenFolder(1, null, owner);
        Folder foreign = givenFolder(2, null, stranger);

        givenFile(20, owner, linked);
        givenFile(30, stranger, foreign);

        when(shareLinkRepository.findByToken(TOKEN))
                .thenReturn(Optional.of(folderLink(TOKEN, 1, owner)));

        assertThatThrownBy(() -> service.resolveFile(TOKEN, 30))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void aFileLinkCannotBeUsedToBrowseFolders() {

        when(shareLinkRepository.findByToken(TOKEN))
                .thenReturn(Optional.of(fileLink(TOKEN, 10, owner)));

        assertThatThrownBy(() -> service.viewSubfolder(TOKEN, 1))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void browsingReachesSubfoldersButNotSiblings() {

        Folder root = givenFolder(1, null, owner);
        Folder child = givenFolder(2, root, owner);
        Folder sibling = givenFolder(3, null, owner);

        when(shareLinkRepository.findByToken(TOKEN))
                .thenReturn(Optional.of(folderLink(TOKEN, 1, owner)));

        assertThat(service.viewSubfolder(TOKEN, 2).itemId()).isEqualTo(2);

        assertThatThrownBy(() -> service.viewSubfolder(TOKEN, 3))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("status")
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void thePublicListingNeverLeaksAStoragePath() {

        Folder root = givenFolder(1, null, owner);

        File inside = givenFile(10, owner, root);

        when(fileRepository.findByFolder(root)).thenReturn(List.of(inside));
        when(folderRepository.findByParentFolder(root)).thenReturn(List.of());
        when(shareLinkRepository.findByToken(TOKEN))
                .thenReturn(Optional.of(folderLink(TOKEN, 1, owner)));

        PublicShareView view = service.view(TOKEN);

        assertThat(view.itemName()).isEqualTo("Photos");
        assertThat(view.files()).singleElement()
                .extracting(PublicShareView.Item::name)
                .isEqualTo("report.pdf");

        // A record with only id/name/mime/size fields cannot carry filePath at all;
        // this asserts the shape the browser is handed.
        assertThat(view.files().getFirst().toString())
                .doesNotContain("filePath")
                .doesNotContain("1/abc");
    }

    // ---------- fixtures ----------

    private File givenFile(int id, User creator, Folder folder) {

        File file = new File();

        file.setId(id);
        file.setFileName("report.pdf");
        file.setFileType("application/pdf");
        file.setFileSize(1200L);
        file.setFilePath(creator.getUserId() + "/abc");
        file.setFolder(folder);
        file.setCreatedBy(creator);

        when(fileRepository.findById(id)).thenReturn(Optional.of(file));
        when(fileStorageService.resolve(file.getFilePath()))
                .thenReturn(java.nio.file.Path.of("build"));

        return file;
    }

    private Folder givenFolder(int id, Folder parent, User creator) {

        Folder folder = new Folder();

        folder.setId(id);
        folder.setFolderName(id == 1 ? "Photos" : "Child " + id);
        folder.setParentFolder(parent);
        folder.setCreatedBy(creator);

        when(folderRepository.findById(id)).thenReturn(Optional.of(folder));

        return folder;
    }

    private static ShareLink fileLink(String token, int itemId, User createdBy) {

        return link(token, itemId, ItemType.FILE, createdBy);
    }

    private static ShareLink folderLink(String token, int itemId, User createdBy) {

        return link(token, itemId, ItemType.FOLDER, createdBy);
    }

    private static ShareLink link(String token, int itemId, ItemType type, User createdBy) {

        ShareLink link = new ShareLink();

        link.setId(5);
        link.setToken(token);
        link.setItemId(itemId);
        link.setItemType(type);
        link.setCreatedBy(createdBy);

        return link;
    }

    private static User user(int id, String email) {

        User user = new User();

        user.setUserId(id);
        user.setUserEmail(email);

        return user;
    }
}
