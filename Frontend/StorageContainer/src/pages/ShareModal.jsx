import React, { useEffect, useState } from "react";
import "../styles/ShareModal.css";
import api from "../services/axiosConfig";

const ShareModal = ({
    isOpen,
    item,
    itemType,
    onClose
}) => {

    const [email, setEmail] = useState("");
    const [permission, setPermission] = useState("VIEWER");
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (isOpen) {
            setEmail("");
            setPermission("VIEWER");
            setLoading(false);
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

        try {
            setLoading(true);

            const shareData = {
                email: email.trim(),
                itemId: item.id,
                itemType: itemType.toUpperCase(),
                permission: permission
            };

            console.log("Sending share data:", shareData);

            const response = await api.post(
                "/api/shares",
                shareData
            );

            console.log("Share successful:", response.data);

            alert(`Successfully shared "${itemName}" with ${email.trim()}`);

            onClose();

        } catch (error) {

            console.error("Share failed:", error);

            if (error.response) {
                console.error("Backend response:", error.response.data);

                alert(
                    error.response.data?.message ||
                    "Failed to share the item"
                );
            } else {
                alert("Unable to connect to the server");
            }

        } finally {
            setLoading(false);
        }
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
                            disabled={loading}
                        />

                    </div>


                    <label>
                        Permission
                    </label>

                    <div className="permission-options">

                        <button
                            type="button"
                            disabled={loading}
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
                            disabled={loading}
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


               
                <div className="share-modal-actions">

                    <button
                        className="share-cancel-btn"
                        onClick={onClose}
                        disabled={loading}
                    >
                        Cancel
                    </button>

                    <button
                        className="share-submit-btn"
                        onClick={handleShare}
                        disabled={loading}
                    >
                        {loading
                            ? "Sharing..."
                            : "👥 Share"}
                    </button>

                </div>

            </div>

        </div>
    );
};

export default ShareModal;

