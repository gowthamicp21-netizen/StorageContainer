import { useEffect, useState } from "react";
import "../styles/ShareModal.css";
import api, { apiErrorMessage } from "../services/axiosConfig";

/**
 * The share address is built from the page's own origin rather than a URL the backend
 * returns, so a link copied while reaching the app over a LAN address or a deployed
 * host still resolves for the person you send it to.
 */
const shareUrl = (token) => `${window.location.origin}/s/${token}`;

const ShareModal = ({ isOpen, item, itemType, onClose }) => {

    if (!isOpen || !item) {
        return null;
    }

    // Keying on the item remounts the body, which is what resets its fields. Keeping
    // the state in here avoids an effect whose only job was to undo the previous item.
    return (
        <ShareModalBody
            key={`${itemType}-${item.id}`}
            item={item}
            itemType={itemType}
            onClose={onClose}
        />
    );
};

const ShareModalBody = ({ item, itemType, onClose }) => {

    const [email, setEmail] = useState("");
    const [permission, setPermission] = useState("VIEWER");
    const [loading, setLoading] = useState(false);

    const [link, setLink] = useState(null);
    const [linkBusy, setLinkBusy] = useState(false);
    const [copied, setCopied] = useState(false);
    const [linkError, setLinkError] = useState("");

    const itemName =
        itemType === "folder"
            ? item.folderName
            : item.fileName;

    useEffect(() => {

        let ignore = false;

        const lookup = async () => {

            try {

                const response = await api.get("/api/share-links", {
                    params: {
                        itemId: item.id,
                        itemType: itemType.toUpperCase()
                    }
                });

                if (!ignore) {
                    setLink(response.data);
                }

            } catch (error) {

                // 404 is the normal answer for an item that has never been linked.
                if (!ignore && error.response?.status !== 404) {
                    setLinkError(apiErrorMessage(error, "Could not load the link"));
                }
            }
        };

        lookup();

        return () => {
            ignore = true;
        };

    }, [item.id, itemType]);

    const handleShare = async () => {

        if (!email.trim()) {
            alert("Please enter an email address");
            return;
        }

        try {
            setLoading(true);

            await api.post("/api/shares", {
                email: email.trim(),
                itemId: item.id,
                itemType: itemType.toUpperCase(),
                permission: permission
            });

            alert(`Successfully shared "${itemName}" with ${email.trim()}`);
            onClose();

        } catch (error) {

            if (error.response) {
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

    const enableLink = async () => {

        try {
            setLinkBusy(true);
            setLinkError("");

            const response = await api.post("/api/share-links", {
                itemId: item.id,
                itemType: itemType.toUpperCase()
            });

            setLink(response.data);

        } catch (error) {
            setLinkError(apiErrorMessage(error, "Could not create the link"));
        } finally {
            setLinkBusy(false);
        }
    };

    const disableLink = async () => {

        try {
            setLinkBusy(true);
            setLinkError("");

            await api.delete(`/api/share-links/${link.id}`);

            setLink(null);

        } catch (error) {
            setLinkError(apiErrorMessage(error, "Could not turn off link sharing"));
        } finally {
            setLinkBusy(false);
        }
    };

    const copyLink = async () => {

        try {
            await navigator.clipboard.writeText(shareUrl(link.token));
            setCopied(true);
            setTimeout(() => setCopied(false), 2000);
        } catch {
            setLinkError("Your browser blocked the clipboard. Select the link and copy it manually.");
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
                        General access
                    </label>

                    <div className="general-access">

                        <div className="general-access-copy">
                            <strong>
                                Anyone with the link
                            </strong>

                            <small>
                                {itemType === "folder"
                                    ? "No sign-in needed. Opens this folder and anything inside it."
                                    : "No sign-in needed. Opens and downloads this file."}
                            </small>
                        </div>

                        {link ? (
                            <button
                                type="button"
                                className="link-off-btn"
                                onClick={disableLink}
                                disabled={linkBusy}
                            >
                                Turn off
                            </button>
                        ) : (
                            <button
                                type="button"
                                className="link-create-btn"
                                onClick={enableLink}
                                disabled={linkBusy}
                            >
                                {linkBusy ? "Working..." : "Create link"}
                            </button>
                        )}

                    </div>

                    {link && (
                        <div className="link-row">
                            <input
                                className="link-url"
                                readOnly
                                value={shareUrl(link.token)}
                                onFocus={(e) => e.target.select()}
                            />

                            <button
                                type="button"
                                className="link-copy-btn"
                                onClick={copyLink}
                            >
                                {copied ? "✓ Copied" : "📋 Copy"}
                            </button>
                        </div>
                    )}

                    {linkError && (
                        <p className="share-error">
                            {linkError}
                        </p>
                    )}

                    <div className="share-divider" />

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
