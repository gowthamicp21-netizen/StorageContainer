import React, { useEffect, useState } from "react";
import "../styles/ShareModal.css";

const ShareModal = ({
    isOpen,
    item,
    itemType,
    onClose
}) => {

    const [email, setEmail] = useState("");
    const [permission, setPermission] = useState("VIEWER");

    useEffect(() => {
        if (isOpen) {
            setEmail("");
            setPermission("VIEWER");
        }
    }, [isOpen]);

    if (!isOpen || !item) {
        return null;
    }

    const itemName =
        itemType === "folder"
            ? item.folderName
            : item.fileName;

    const handleShare = async () => {

        if (!email.trim()) {
            alert("Please enter an email address");
            return;
        }

        console.log("Share item:", {
            itemId: item.id,
            itemType,
            email: email.trim(),
            permission
        });

       
    };

    return (
        <div
            className="share-modal-overlay"
            onClick={onClose}
        >

            <div
                className="share-modal"
                onClick={(e) => e.stopPropagation()}
            >

                {/* HEADER */}
                <div className="share-modal-header">

                    <div className="share-title-section">

                        <div className="share-icon">
                            👥
                        </div>

                        <div>
                            <h2>Share</h2>

                            <p>
                                Share this {itemType} with another user
                            </p>
                        </div>

                    </div>

                    <button
                        className="share-close-btn"
                        onClick={onClose}
                    >
                        ×
                    </button>

                </div>


                {/* ITEM */}
                <div className="share-item-preview">

                    <div className="share-item-icon">

                        {itemType === "folder"
                            ? "📁"
                            : "📄"}

                    </div>

                    <div className="share-item-info">

                        <span className="share-item-label">
                            {itemType === "folder"
                                ? "Folder"
                                : "File"}
                        </span>

                        <strong>
                            {itemName}
                        </strong>

                    </div>

                </div>


                {/* FORM */}
                <div className="share-form">

                    <label>
                        Email address
                    </label>

                    <div className="share-input-wrapper">

                        <span>
                            👤
                        </span>

                        <input
                            type="email"
                            placeholder="Enter user's email"
                            value={email}
                            onChange={(e) =>
                                setEmail(e.target.value)
                            }
                        />

                    </div>


                    <label>
                        Permission
                    </label>

                    <div className="permission-options">

                        <button
                            type="button"
                            className={
                                permission === "VIEWER"
                                    ? "permission-option active"
                                    : "permission-option"
                            }
                            onClick={() =>
                                setPermission("VIEWER")
                            }
                        >

                            <span className="permission-icon">
                                👁️
                            </span>

                            <div>
                                <strong>Viewer</strong>

                                <small>
                                    Can view and download
                                </small>
                            </div>

                        </button>


                        <button
                            type="button"
                            className={
                                permission === "EDITOR"
                                    ? "permission-option active"
                                    : "permission-option"
                            }
                            onClick={() =>
                                setPermission("EDITOR")
                            }
                        >

                            <span className="permission-icon">
                                ✏️
                            </span>

                            <div>
                                <strong>Editor</strong>

                                <small>
                                    Can modify the content
                                </small>
                            </div>

                        </button>

                    </div>

                </div>


                {/* FOOTER */}
                <div className="share-modal-actions">

                    <button
                        className="share-cancel-btn"
                        onClick={onClose}
                    >
                        Cancel
                    </button>

                    <button
                        className="share-submit-btn"
                        onClick={handleShare}
                    >
                        👥 Share
                    </button>

                </div>

            </div>

        </div>
    );
};

export default ShareModal;