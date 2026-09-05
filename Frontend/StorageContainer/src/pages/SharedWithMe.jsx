
import React, { useEffect, useState } from "react";
import api from "../services/axiosConfig";
import "../styles/SharedWithMe.css";

const SharedWithMe = () => {

    const [sharedItems, setSharedItems] = useState([]);
    const [loading, setLoading] = useState(true);

    // Three dot menu
    const [menuItem, setMenuItem] = useState(null);

    // Rename
    const [renameItem, setRenameItem] = useState(null);
    const [newName, setNewName] = useState("");
    const [renaming, setRenaming] = useState(false);

    // Delete shared item
    const [deleteItem, setDeleteItem] = useState(null);
    const [deleting, setDeleting] = useState(false);


    // ==========================================
    // FETCH SHARED ITEMS
    // ==========================================

    const fetchSharedItems = async () => {

        try {

            setLoading(true);

            const response = await api.get(
                "/api/shares/shared-with-me"
            );

            console.log(
                "Shared items:",
                response.data
            );

            setSharedItems(response.data);

        } catch (error) {

            console.error(
                "Error fetching shared items:",
                error
            );

        } finally {

            setLoading(false);

        }
    };


    useEffect(() => {

        fetchSharedItems();

    }, []);


    // ==========================================
    // ICONS
    // ==========================================

    const getItemIcon = (itemType) => {

        return itemType === "FOLDER"
            ? "📁"
            : "📄";
    };


    const getPermissionIcon = (permission) => {

        return permission === "EDITOR"
            ? "✏️"
            : "👁️";
    };


    const getPermissionText = (permission) => {

        return permission === "EDITOR"
            ? "Can edit"
            : "View only";
    };


    // ==========================================
    // THREE DOT MENU
    // ==========================================

    const toggleMenu = (item) => {

        if (
            menuItem &&
            menuItem.shareId === item.shareId
        ) {

            setMenuItem(null);

        } else {

            setMenuItem(item);

        }
    };


    // ==========================================
    // OPEN
    // ==========================================

    const handleOpen = async (item) => {

        setMenuItem(null);

        try {

            if (item.itemType === "FOLDER") {

                console.log(
                    "Open shared folder:",
                    item.itemId
                );

                /*
                 * Folder navigation can be connected
                 * to your existing folder navigation.
                 */

            } else {

                const response = await api.get(
                    `/api/files/download/${item.itemId}`,
                    {
                        responseType: "blob"
                    }
                );

                const blob = new Blob(
                    [response.data],
                    {
                        type:
                            response.headers[
                                "content-type"
                            ]
                    }
                );

                const url =
                    window.URL.createObjectURL(blob);

                window.open(url, "_blank");

            }

        } catch (error) {

            console.error(
                "Error opening item:",
                error
            );

            alert(
                "Unable to open this item."
            );
        }
    };


    // ==========================================
    // RENAME MODAL
    // ==========================================

    const openRenameModal = (item) => {

        setMenuItem(null);

        if (item.permission === "VIEWER") {

            return;
        }

        setRenameItem(item);

        setNewName(
            item.itemName || ""
        );
    };


    const closeRenameModal = () => {

        if (renaming) {
            return;
        }

        setRenameItem(null);
        setNewName("");
    };


    // ==========================================
    // RENAME
    // ==========================================

    const handleRename = async () => {

        const trimmedName =
            newName.trim();

        if (!trimmedName) {

            alert(
                "Please enter a name."
            );

            return;
        }

        if (!renameItem) {
            return;
        }

        if (
            renameItem.permission ===
            "VIEWER"
        ) {

            return;
        }

        try {

            setRenaming(true);

            // Folder rename
            if (
                renameItem.itemType ===
                "FOLDER"
            ) {

                await api.put(
                    `/api/folders/folder/${renameItem.itemId}/rename`,
                    null,
                    {
                        params: {
                            newName:
                                trimmedName
                        }
                    }
                );
            }

            // File rename
            else if (
                renameItem.itemType ===
                "FILE"
            ) {

                await api.put(
                    `/api/files/file/${renameItem.itemId}/rename`,
                    null,
                    {
                        params: {
                            newName:
                                trimmedName
                        }
                    }
                );
            }

            setRenameItem(null);
            setNewName("");

            await fetchSharedItems();

        } catch (error) {

            console.error(
                "Rename error:",
                error
            );

            if (
                error.response?.status ===
                403
            ) {

                alert(
                    "You don't have permission to rename this item."
                );

            } else {

                alert(
                    error.response?.data ||
                    "Failed to rename item."
                );
            }

        } finally {

            setRenaming(false);
        }
    };


    // ==========================================
    // DELETE MODAL
    // ==========================================

    const openDeleteModal = (item) => {

        setMenuItem(null);

        if (
            item.permission ===
            "VIEWER"
        ) {

            return;
        }

        console.log(
            "Delete item:",
            item
        );

        setDeleteItem(item);
    };


    const closeDeleteModal = () => {

        if (deleting) {
            return;
        }

        setDeleteItem(null);
    };


    // ==========================================
    // DELETE SHARE
    // ==========================================

    const handleDelete = async () => {

        if (!deleteItem) {
            return;
        }

        console.log(
            "Deleting share:",
            deleteItem
        );

        if (!deleteItem.shareId) {

            console.error(
                "shareId is missing:",
                deleteItem
            );

            alert(
                "Unable to remove this shared item because the share ID is missing."
            );

            return;
        }

        try {

            setDeleting(true);

            // IMPORTANT:
            // This deletes ONLY the Share record.
            // The actual file/folder is NOT deleted.

            await api.delete(
                `/api/shares/${deleteItem.shareId}`
            );

            setDeleteItem(null);

            await fetchSharedItems();

        } catch (error) {

            console.error(
                "Delete error:",
                error
            );

            if (
                error.response?.status ===
                403
            ) {

                alert(
                    "You don't have permission to remove this shared item."
                );

            } else {

                alert(
                    error.response?.data ||
                    "Failed to remove shared item."
                );
            }

        } finally {

            setDeleting(false);
        }
    };


    // ==========================================
    // LOADING
    // ==========================================

    if (loading) {

        return (
            <div className="shared-page">

                <div className="shared-loading">
                    Loading shared items...
                </div>

            </div>
        );
    }


    // ==========================================
    // UI
    // ==========================================

    return (

        <div className="shared-page">

            {/* ==================================
                HEADER
            ================================== */}

            <div className="shared-page-header">

                <div>

                    <h1>
                        Shared with me
                    </h1>

                    <p>
                        Files and folders shared with you
                    </p>

                </div>

            </div>


            {/* ==================================
                EMPTY STATE
            ================================== */}

            {sharedItems.length === 0 ? (

                <div className="shared-empty">

                    <div className="shared-empty-icon">
                        📂
                    </div>

                    <h2>
                        No shared items
                    </h2>

                    <p>
                        Files and folders shared with
                        you will appear here.
                    </p>

                </div>

            ) : (

                <div className="shared-list">

                    {sharedItems.map((item) => (

                        <div
                            className="shared-item"
                            key={`${item.shareId}-${item.itemType}-${item.itemId}`}
                        >

                            {/* ==================================
                                ICON
                            ================================== */}

                            <div className="shared-item-icon">

                                {getItemIcon(
                                    item.itemType
                                )}

                            </div>


                            {/* ==================================
                                INFORMATION
                            ================================== */}

                            <div className="shared-item-info">

                                <h3>
                                    {item.itemName}
                                </h3>

                                <div className="shared-item-details">

                                    <span>
                                        Shared by{" "}
                                        {item.shareBy ||
                                            "User"}
                                    </span>

                                    <span className="shared-dot">
                                        •
                                    </span>

                                    <span>
                                        {getPermissionIcon(
                                            item.permission
                                        )}{" "}
                                        {getPermissionText(
                                            item.permission
                                        )}
                                    </span>

                                </div>

                            </div>


                            {/* ==================================
                                PERMISSION
                            ================================== */}

                            <div
                                className={`shared-permission ${
                                    item.permission ===
                                    "EDITOR"
                                        ? "editor"
                                        : "viewer"
                                }`}
                            >

                                <span>
                                    {getPermissionIcon(
                                        item.permission
                                    )}
                                </span>

                                <span>
                                    {item.permission}
                                </span>

                            </div>


                            {/* ==================================
                                OPEN BUTTON
                            ================================== */}

                            <button
                                className="shared-open-btn"
                                onClick={() =>
                                    handleOpen(item)
                                }
                            >
                                Open
                            </button>


                            {/* ==================================
                                THREE DOT MENU
                            ================================== */}

                            <div className="shared-item-menu-container">

                                <button
                                    className="shared-three-dot"
                                    onClick={() =>
                                        toggleMenu(item)
                                    }
                                >
                                    ⋮
                                </button>


                                {/* ==================================
                                    DROPDOWN
                                ================================== */}

                                {menuItem &&
                                    menuItem.shareId ===
                                        item.shareId && (

                                        <div className="shared-dropdown">

                                            {/* OPEN */}

                                            <button
                                                onClick={() =>
                                                    handleOpen(
                                                        item
                                                    )
                                                }
                                            >

                                                <span>
                                                    ↗
                                                </span>

                                                <span>
                                                    Open
                                                </span>

                                            </button>


                                            {/* RENAME */}

                                            {item.permission ===
                                                "EDITOR" && (

                                                <button
                                                    onClick={() =>
                                                        openRenameModal(
                                                            item
                                                        )
                                                    }
                                                >

                                                    <span>
                                                        ✏️
                                                    </span>

                                                    <span>
                                                        Rename
                                                    </span>

                                                </button>

                                            )}


                                            {/* REMOVE SHARE */}

                                            <button
                                                className="delete-menu-item"
                                                onClick={() =>
                                                    openDeleteModal(
                                                        item
                                                    )
                                                }
                                            >

                                                <span>
                                                    🗑️
                                                </span>

                                                <span>
                                                    Remove
                                                </span>

                                            </button>

                                        </div>

                                    )}

                            </div>

                        </div>

                    ))}

                </div>

            )}


            {/* ==================================
                RENAME MODAL
            ================================== */}

            {renameItem && (

                <div
                    className="rename-modal-overlay"
                    onClick={closeRenameModal}
                >

                    <div
                        className="rename-modal"
                        onClick={(e) =>
                            e.stopPropagation()
                        }
                    >

                        <h3>
                            Rename
                        </h3>

                        <p>
                            Enter a new name for this{" "}
                            {renameItem.itemType ===
                            "FOLDER"
                                ? "folder"
                                : "file"}.
                        </p>

                        <input
                            type="text"
                            value={newName}
                            onChange={(e) =>
                                setNewName(
                                    e.target.value
                                )
                            }
                            autoFocus
                            disabled={renaming}
                            onKeyDown={(e) => {

                                if (
                                    e.key ===
                                        "Enter" &&
                                    !renaming
                                ) {

                                    handleRename();
                                }

                                if (
                                    e.key ===
                                        "Escape" &&
                                    !renaming
                                ) {

                                    closeRenameModal();
                                }

                            }}
                        />

                        <div className="rename-modal-buttons">

                            <button
                                className="rename-cancel-btn"
                                onClick={
                                    closeRenameModal
                                }
                                disabled={renaming}
                            >
                                Cancel
                            </button>

                            <button
                                className="rename-confirm-btn"
                                onClick={
                                    handleRename
                                }
                                disabled={
                                    renaming ||
                                    !newName.trim()
                                }
                            >

                                {renaming
                                    ? "Renaming..."
                                    : "Rename"}

                            </button>

                        </div>

                    </div>

                </div>

            )}


            {/* ==================================
                DELETE / REMOVE MODAL
            ================================== */}

            {deleteItem && (

                <div
                    className="rename-modal-overlay"
                    onClick={closeDeleteModal}
                >

                    <div
                        className="rename-modal delete-modal"
                        onClick={(e) =>
                            e.stopPropagation()
                        }
                    >

                        <h3>
                            Remove{" "}
                            {deleteItem.itemType ===
                            "FOLDER"
                                ? "Folder"
                                : "File"}
                        </h3>

                        <p>
                            Are you sure you want to
                            remove{" "}
                            <strong>
                                {deleteItem.itemName}
                            </strong>{" "}
                            from Shared with me?
                        </p>

                        <p className="delete-warning">
                            The original file/folder will
                            not be deleted.
                        </p>

                        <div className="rename-modal-buttons">

                            <button
                                className="rename-cancel-btn"
                                onClick={
                                    closeDeleteModal
                                }
                                disabled={deleting}
                            >
                                Cancel
                            </button>

                            <button
                                className="delete-confirm-btn"
                                onClick={
                                    handleDelete
                                }
                                disabled={deleting}
                            >

                                {deleting
                                    ? "Removing..."
                                    : "Remove"}

                            </button>

                        </div>

                    </div>

                </div>

            )}

        </div>
    );
};

export default SharedWithMe;
