
import React, { useEffect, useState } from "react";
import "../styles/Folder.css";
import api from "../services/axiosConfig";
import Sidebar from "../pages/Sidebar";
import ShareModal from "../pages/ShareModal";

const Folders = () => {

    
    const [folders, setFolders] = useState([]);

  
    const [currentFolder, setCurrentFolder] = useState(null);

    
    const [folderPath, setFolderPath] = useState([]);

   
    const [loading, setLoading] = useState(false);

    const [files, setFiles] = useState([]);

    const [showFolderModal, setShowFolderModal] = useState(false);

    const [newFolderName, setNewFolderName] = useState("");
    
    const [creatingFolder, setCreatingFolder] = useState(false);

    const [showDeleteFolderModal, setShowFolderDeleteModal] = useState(false);

    const [folderToDelete, setFolderToDelete] = useState(null);

    const [isDeleting, setIsDeleting] = useState(false);

    const [showDeleteFileModal, setShowDeleteFileModal] = useState(false);

    const [fileToDelete, setFileToDelete] = useState(null);

    const [deletingFile, setDeletingFile] = useState(false);

    const [openMenuId, setOpenMenuId] = useState(null);

    const [openMenuType, setOpenMenuType] = useState(null);

    const [showRenameModal, setShowRenameModal] = useState(false);

    const [renameItem, setRenameItem] = useState(null);

    const [renameType, setRenameType] = useState(null);

    const [renameName, setRenameName] = useState("");

    const [renaming, setRenaming] = useState(false);

    const [showShareModal, setShowShareModal] = useState(false);

    const [shareItem, setShareItem] = useState(null);

    const [shareType, setShareType] = useState(null);

    const [showSharedWithMe, setShowSharedWithMe] = useState(false);

    const [sharedItems, setSharedItems] = useState([]);

    const [sharedLoading, setSharedLoading] = useState(false);

    const fetchSharedWithMe = async () => {

        try {
            setSharedLoading(true);

            const response = await api.get(
                "/api/shares/shared-with-me"
            );

            console.log("Shared with me:", response.data);

            setSharedItems(response.data);

        } catch (error) {

            console.error(
                "Error loading shared items:",
                error
            );

            alert("Failed to load shared items.");

        } finally {
            setSharedLoading(false);
        }
    };

    const handleSharedWithMe = () => {
    setShowSharedWithMe(true);
    setCurrentFolder(null);
    setFolderPath([]);
    fetchSharedWithMe();
    };

    const handleMyFiles = () => {
    setShowSharedWithMe(false);
    setCurrentFolder(null);
    setFolderPath([]);
    };

    const openShareModal = (item, type) => {
    setShareItem(item);
    setShareType(type);
    setShowShareModal(true);
    closeMenu();
    };

    const openRenameModal = (item, type) => {
    setRenameItem(item);
    setRenameType(type);

    if (type === "folder") {
        setRenameName(item.folderName);
    } else {
        setRenameName(item.fileName);
    }

    setShowRenameModal(true);
    closeMenu();
    };

    const closeRenameModal = () => {
    if (renaming) return;

    setShowRenameModal(false);
    setRenameItem(null);
    setRenameType(null);
    setRenameName("");
    };

    const toggleMenu = (id, type) => {
    if (openMenuId === id && openMenuType === type) {
        setOpenMenuId(null);
        setOpenMenuType(null);
    } else {
        setOpenMenuId(id);
        setOpenMenuType(type);
    }
    };

    const closeMenu = () => {
    setOpenMenuId(null);
    setOpenMenuType(null);
    };

    useEffect(() => {

    const handleClickOutside = () => {
        closeMenu();
    };

    document.addEventListener("click", handleClickOutside);

    return () => {
        document.removeEventListener("click", handleClickOutside);
    };

    }, []);
    const handleRename = async () => {

    const trimmedName = renameName.trim();

    if (!trimmedName) {
        alert("Name cannot be empty");
        return;
    }

    try {

        setRenaming(true);

        if (renameType === "folder") {

            await api.put(
                `/api/folders/folder/${renameItem.id}/rename`,
                null,
                {
                    params: {
                        newName: trimmedName
                    }
                }
            );

            setFolders((prev) =>
                prev.map((folder) =>
                    folder.id === renameItem.id
                        ? {
                              ...folder,
                              folderName: trimmedName
                          }
                        : folder
                )
            );

        } else {

            await api.put(
                `/api/files/file/${renameItem.id}/rename`,
                null,
                {
                    params: {
                        newName: trimmedName
                    }
                }
            );

            setFiles((prev) =>
                prev.map((file) =>
                    file.id === renameItem.id
                        ? {
                              ...file,
                              fileName: trimmedName
                          }
                        : file
                )
            );
        }

        closeRenameModal();

    } catch (error) {

        console.error("Rename failed:", error);

        alert(
            error.response?.data ||
            "Failed to rename"
        );

    } finally {
        setRenaming(false);
    }
};

    const openDeleteFileModal = (file) => {
        console.log("openDeleteFileModal");
        setFileToDelete(file);
        setShowDeleteFileModal(true);
    };

    const closeDeleteFileModal = () => {
    if (deletingFile) return;

    setShowDeleteFileModal(false);
    setFileToDelete(null);
    };

    const handleDeleteFile = async () => {
    if (!fileToDelete) return;

    try {
        setDeletingFile(true);

        await api.delete(
            `/api/files/file/${fileToDelete.id}`
        );

        
        setFiles((prevFiles) =>
            prevFiles.filter(
                (file) => file.id !== fileToDelete.id
            )
        );

        setShowDeleteFileModal(false);
        setFileToDelete(null);

    } catch (error) {

        console.error("File delete failed:", error);

        alert(
            error.response?.data ||
            "Failed to delete file"
        );

    } finally {
        setDeletingFile(false);
    }
    };

    const openDeleteFolderModal = (folder) => {

    setFolderToDelete(folder);

    setShowFolderDeleteModal(true);
    };

    const closeDeleteModal = () => {

    if (isDeleting) {
        return;
    }

    setShowFolderDeleteModal(false);

    setFolderToDelete(null);
    };
    


    const fetchFiles = async () => {

    try {
        setLoading(true);

        let response;

        if (currentFolder === null) {

           
            response = await api.get("/api/files");

        } else {

           
            response = await api.get(
                `/api/files?folderId=${currentFolder.id}`
            );
        }

        console.log("Files received:", response.data);

        setFiles(response.data);

    } catch (error) {

        console.error("Error loading files:", error);

    }finally {

            setLoading(false);
        }
};

const handleDeleteFolder = async () => {

    if (!folderToDelete) {
        return;
    }

    try {

        setIsDeleting(true);

        await api.delete(
            `/api/folders/${folderToDelete.id}`
        );

        // Remove deleted folder from UI
        setFolders((prevFolders) =>
            prevFolders.filter(
                (folder) =>
                    folder.id !== folderToDelete.id
            )
        );

        setShowFolderDeleteModal(false);

        setFolderToDelete(null);

        alert("Folder deleted successfully");

    } catch (error) {

        console.error(
            "Error deleting folder:",
            error
        );

        alert(
            error.response?.data ||
            "Failed to delete folder"
        );

    } finally {

        setIsDeleting(false);
    }
};


    const fetchFolders = async () => {

        try {

            setLoading(true);

            let response;

            if (currentFolder === null) {

                
                response = await api.get("/api/folders/root");

            } else {

               
                response = await api.get(
                    `/api/folders/${currentFolder.id}/children`
                );
            }

            setFolders(response.data);

        } catch (error) {

            console.error("Error loading folders:", error);

        } finally {

            setLoading(false);
        }
    };



    useEffect(() => {

        fetchFolders();
        fetchFiles();

    }, [currentFolder]);


    
    const openFolder = (folder) => {


         console.log("Opening folder:", folder);
       
        if (currentFolder !== null) {

            setFolderPath((previousPath) => [
                ...previousPath,
                currentFolder
            ]);

        }

       setCurrentFolder(folder);
    };


  

    const goToRoot = () => {

        setCurrentFolder(null);
        setFolderPath([]);
    };


   

    const goBack = () => {

        if (folderPath.length === 0) {

          
            goToRoot();
            return;
        }

        const previousPath = [...folderPath];

        
        previousPath.pop();

        setFolderPath(previousPath);

        
        if (previousPath.length === 0) {

            setCurrentFolder(null);

        } else {

            setCurrentFolder(
                previousPath[previousPath.length - 1]
            );
        }
    };

    const handleNewFolder = () => {
        setNewFolderName("");
        setShowFolderModal(true);
    };


const createFolder = async () => {

    const trimmedName = newFolderName.trim();

    if (trimmedName === "") {
        alert("Folder name cannot be empty.");
        return;
    }

    try {

        setCreatingFolder(true);

       const folderData = {
        folderName: trimmedName,
        parentFolder: currentFolder
        ? {
            id: currentFolder.id
        }
        : null
        };
        console.log(folderData);

        await api.post(
            "/api/folders",
            folderData
        );

        setShowFolderModal(false);
        setNewFolderName("");

        fetchFolders();

    } catch (error) {

        console.error(
            "Error creating folder:",
            error
        );

        alert("Failed to create folder.");

    } finally {

        setCreatingFolder(false);
    }
};

 
const handleNewFile = async (file) => {

    if (!file) {
        return;
    }

    try {

        console.log("Selected file:", file.name);

        const formData = new FormData();

      
        formData.append("file", file);


        
        if (currentFolder !== null) {

            formData.append(
                "folderId",
                currentFolder.id
            );
        }


       
        const response = await api.post(
            "/api/files/upload",
            formData
        );


        console.log(
            "File uploaded:",
            response.data
        );


        alert("File uploaded successfully!");


       
        fetchFolders();


    } catch (error) {

        console.error(
            "File upload failed:",
            error
        );

        alert("File upload failed.");
    }
};
const handleDownload = async (fileId) => {
    try {
        const response = await api.get(
            `/api/files/download/${fileId}`,
            {
                responseType: "blob"
            }
        );

        const blob = new Blob(
            [response.data],
            {
                type: response.headers["content-type"]
            }
        );

        const url = window.URL.createObjectURL(blob);

        const link = document.createElement("a");
        link.href = url;

        // Get filename from backend response
        const contentDisposition =
            response.headers["content-disposition"];

        let fileName = "downloaded-file";

        if (contentDisposition) {

            const match = contentDisposition.match(
                /filename="?([^"]+)"?/
            );

            if (match && match[1]) {
                fileName = match[1];
            }
        }

        console.log("Downloaded filename:", fileName);

        link.download = fileName;

        document.body.appendChild(link);
        link.click();

        link.remove();

        window.URL.revokeObjectURL(url);

    } catch (error) {
        console.error("Download failed:", error);
        alert("File download failed");
    }
};



    return (

        <div className="storage-layout">

           

           <Sidebar
                onNewFolder={handleNewFolder}
                onNewFile={handleNewFile}
                
                onSharedWithMe={handleSharedWithMe}
            />


            <main className="folder-content">

                <div className="folder-header">

                    <div>

                <h1>
                    {showSharedWithMe
                        ? "Shared With Me"
                        : currentFolder
                            ? currentFolder.folderName
                            : "My Files"
                    }
                </h1>



                        <div className="breadcrumb">

                            <button
                                onClick={goToRoot}
                                className="breadcrumb-button"
                            >
                                🏠 My Files
                            </button>


                            {folderPath.map((folder) => (

                                <React.Fragment
                                    key={folder.id}
                                >

                                    <span className="breadcrumb-arrow">
                                        /
                                    </span>

                                    <span className="breadcrumb-folder">
                                        {folder.folderName}
                                    </span>

                                </React.Fragment>

                            ))}


                            {currentFolder && (

                                <>
                                    <span className="breadcrumb-arrow">
                                        /
                                    </span>

                                    <span className="breadcrumb-current">
                                        {currentFolder.folderName}
                                    </span>
                                </>

                            )}

                        </div>

                    </div>


                    

                    {currentFolder && (

                        <button
                            className="back-button"
                            onClick={goBack}
                        >
                            ← Back
                        </button>

                    )}

                </div>


              

                {loading && (

                    <div className="loading">
                        Loading folders...
                    </div>

                )}

                {showFolderModal && (

    <div
        className="modal-overlay"
        onClick={() => setShowFolderModal(false)}
    >

        <div
            className="folder-modal"
            onClick={(e) => e.stopPropagation()}
        >

            <div className="modal-header">

                <div>
                    <h2>New Folder</h2>

                    <p>
                        Create a new folder in{" "}
                        <strong>
                            {currentFolder
                                ? currentFolder.folderName
                                : "My Files"}
                        </strong>
                    </p>
                </div>

                <button
                    className="modal-close"
                    onClick={() => setShowFolderModal(false)}
                >
                    ×
                </button>

            </div>


            <div className="modal-body">

                <label htmlFor="folderName">
                    Folder name
                </label>

                <div className="folder-input-wrapper">

                    <span className="folder-input-icon">
                        📁
                    </span>

                    <input
                        id="folderName"
                        type="text"
                        placeholder="Enter folder name"
                        value={newFolderName}
                        onChange={(e) =>
                            setNewFolderName(e.target.value)
                        }
                        onKeyDown={(e) => {

                            if (e.key === "Enter") {
                                createFolder();
                            }

                            if (e.key === "Escape") {
                                setShowFolderModal(false);
                            }
                        }}
                        autoFocus
                    />

                </div>

            </div>


            <div className="modal-footer">

                <button
                    className="cancel-button"
                    onClick={() =>
                        setShowFolderModal(false)
                    }
                    disabled={creatingFolder}
                >
                    Cancel
                </button>


                <button
                    className="create-folder-button"
                    onClick={createFolder}
                    disabled={creatingFolder}
                >

                    {creatingFolder
                        ? "Creating..."
                        : "Create Folder"}

                </button>

            </div>

        </div>

    </div>

    )}


               

                {!loading && folders.length === 0 && files.length==0 &&(

                    <div className="empty-folder">

                        <div className="empty-icon">
                            📁
                        </div>

                        <h2>
                            This folder is empty
                        </h2>

                        <p>
                            Create a new folder using
                            the sidebar.
                        </p>

                    </div>

                )}


             

                {!loading && folders.length > 0 && (

                    <div className="folder-grid">

                       {folders.map((folder) => (
                            <div className="storage-item folder-item" key={folder.id}>

                                <div
                                    className="storage-item-main"
                                    onClick={() => setCurrentFolder(folder)}
                                >
                                    <div className="storage-icon">
                                        📁
                                    </div>

                                    <div className="storage-info">
                                        <div className="storage-name">
                                            {folder.folderName}
                                        </div>

                                        <div className="storage-type">
                                            Folder
                                        </div>
                                    </div>
                                </div>

        {/* Three dots */}
                            <button
                                className="storage-menu-button"
                                onClick={(e) => {
                                    e.stopPropagation();
                                    toggleMenu(folder.id, "folder");
                                }}
                            >
                                ⋮
                            </button>

                    {/* Folder Menu */}
                    {openMenuId === folder.id &&
                        openMenuType === "folder" && (

                            <div
                                className="premium-menu"
                                onClick={(e) => e.stopPropagation()}
                            >

                                <button
                                    onClick={() => {
                                        closeMenu();

                                        openRenameModal(folder, "folder");
                                        console.log("Rename folder", folder.id);
                                    }}
                                >
                                    <span>✏️</span>
                                    Rename
                                </button>

                                
                                <button
                                    onClick={() => openShareModal(folder, "folder")}
                                     >
                                <span>👥</span>
                                Share
                                </button>

                                <div className="menu-divider"></div>

                                <button
                                    className="delete-menu-item"
                                    onClick={() => {
                                        closeMenu();

                                        openDeleteFolderModal(folder);
                                        console.log("Delete folder", folder.id);
                                    }}
                                >
                                    <span>🗑️</span>
                                    Delete
                                </button>

                            </div>
                        )}
                        </div>
                    ))}

                    </div>

                )}

                {!loading && files.length > 0 && (

            <div className="files-section">

            <div className="file-grid">

            {files.map((file) => (
    <div className="storage-item file-item" key={file.id}>

        <div className="storage-item-main">

            <div className="storage-icon">
                📄
            </div>

            <div className="storage-info">

                <div className="storage-name">
                    {file.fileName}
                </div>

                <div className="storage-type">
                    {file.fileType} • {file.fileSize} bytes
                </div>

            </div>

        </div>

        {/* Three dots */}
        <button
            className="storage-menu-button"
            onClick={(e) => {
                e.stopPropagation();
                toggleMenu(file.id, "file");
            }}
        >
            ⋮
        </button>

        {/* File Menu */}
        {openMenuId === file.id &&
            openMenuType === "file" && (

                <div
                    className="premium-menu"
                    onClick={(e) => e.stopPropagation()}
                >

                    <button
                        onClick={() => {
                            closeMenu();

                            openRenameModal(file, "file");
                            console.log("Rename file", file.id);
                        }}
                    >
                        <span>✏️</span>
                        Rename
                    </button>

                    <button
                        onClick={() => {
                            closeMenu();
                            handleDownload(file.id);
                        }}
                    >
                        <span>⬇️</span>
                        Download
                    </button>

                    <button
                        onClick={() => openShareModal(file, "file")}
                >
                <span>👥</span>
                Share
                </button>

                    <div className="menu-divider"></div>

                    <button
                        className="delete-menu-item"
                        onClick={() => {
                            closeMenu();
                            openDeleteFileModal(file);
                        }}
                    >
                        <span>🗑️</span>
                        Delete
                    </button>

                </div>
            )}
    </div>
))}

                </div>

                

         </div>

        )}

            </main>

            

            {showDeleteFolderModal && folderToDelete && (

        <div className="modal-overlay">

        <div className="delete-modal">

            <div className="delete-modal-icon">
                🗑️
            </div>

            <h2>Delete Folder?</h2>

            <p>
                Are you sure you want to delete
            </p>

            <p className="folder-name">
                "{folderToDelete.folderName}"
            </p>

            <p className="warning-text">
                This will permanently delete this folder,
                all files inside it, and all subfolders.
            </p>


            <div className="modal-actions">

                <button
                    className="cancel-btn"
                    onClick={closeDeleteModal}
                    disabled={isDeleting}
                >
                    Cancel
                </button>


                <button
                    className="confirm-delete-btn"
                    onClick={handleDeleteFolder}
                    disabled={isDeleting}
                >

                    {isDeleting
                        ? "Deleting..."
                        : "Delete Folder"
                    }

                </button>

            </div>

        </div>

    </div>
)}

{showDeleteFileModal && (
    <div className="delete-modal-overlay">

        <div className="delete-modal">

            <button
                className="delete-modal-close"
                onClick={closeDeleteModal}
                disabled={deletingFile}
            >
                ×
            </button>

            <div className="delete-icon-wrapper">
                🗑️
            </div>

            <h2>Delete File?</h2>

            <p className="delete-warning">
                Are you sure you want to delete this file?
            </p>

            {fileToDelete && (
                <div className="delete-file-preview">

                    <div className="preview-file-icon">
                        📄
                    </div>

                    <div className="preview-file-details">
                        <span className="preview-file-name">
                            {fileToDelete.fileName}
                        </span>

                        <span className="preview-file-type">
                            {fileToDelete.fileType || "File"}
                        </span>
                    </div>

                </div>
            )}

            <p className="delete-note">
                This action cannot be undone.
            </p>

            <div className="delete-modal-actions">

                <button
                    className="cancel-delete-btn"
                    onClick={closeDeleteModal}
                    disabled={deletingFile}
                >
                    Cancel
                </button>

                <button
                    className="confirm-delete-btn"
                    onClick={handleDeleteFile}
                    disabled={deletingFile}
                >
                    {deletingFile ? (
                        <>
                            <span className="delete-spinner"></span>
                            Deleting...
                        </>
                    ) : (
                        <>
                            🗑️ Delete
                        </>
                    )}
                </button>

            </div>

        </div>

    </div>
)}

    {showSharedWithMe ? (
            <div className="file-grid">

                {sharedLoading ? (
                    <p>Loading shared items...</p>
                ) : sharedItems.length === 0 ? (
                    <div className="empty-state">
                        <div className="empty-icon">🤝</div>
                        <h3>No shared items</h3>
                        <p>Files and folders shared with you will appear here.</p>
                    </div>
                ) : (
                    sharedItems.map((item) => (
                        <div className="shared-item-card">
                                <div className="shared-item-icon">
                                    {item.itemType === "FOLDER" ? "📁" : "📄"}
                                </div>

                                <div className="shared-item-name">
                                    {item.itemName}
                                </div>

                                <div className="shared-item-info">
                                    Shared with you
                                </div>

                                <div className="shared-item-permission">
                                    {item.permission}
                                </div>

                                <button className="shared-item-menu">
                                    ⋮
                                </button>
                            </div>
                    ))
                )}

            </div>
        ) : (
            <h1>File Folder Display</h1>
        )}

{showRenameModal && (
    <div
        className="rename-modal-overlay"
        onClick={closeRenameModal}
    >
        <div
            className="rename-modal"
            onClick={(e) => e.stopPropagation()}
        >

            <div className="rename-modal-header">

                <div>
                    <h3>
                        Rename {renameType === "folder" ? "Folder" : "File"}
                    </h3>

                    <p>
                        Enter a new name for your{" "}
                        {renameType === "folder" ? "folder" : "file"}.
                    </p>
                </div>

                <button
                    className="rename-close-btn"
                    onClick={closeRenameModal}
                >
                    ×
                </button>

            </div>


            <div className="rename-modal-body">

                <label>
                    Name
                </label>

                <input
                    type="text"
                    value={renameName}
                    onChange={(e) => setRenameName(e.target.value)}
                    autoFocus
                    onKeyDown={(e) => {
                        if (e.key === "Enter") {
                            handleRename();
                        }
                    }}
                />

            </div>


            <div className="rename-modal-actions">

                <button
                    className="rename-cancel-btn"
                    onClick={closeRenameModal}
                    disabled={renaming}
                >
                    Cancel
                </button>

                <button
                    className="rename-save-btn"
                    onClick={handleRename}
                    disabled={renaming}
                >
                    {renaming ? "Renaming..." : "Rename"}
                </button>

            </div>

        </div>
    </div>
)}

<ShareModal
    isOpen={showShareModal}
    item={shareItem}
    itemType={shareType}
    onClose={() => setShowShareModal(false)}
/>

        </div>
    );
};

export default Folders;

