
import React, { useEffect, useState } from "react";
import "../styles/SharedWithMe.css";
import api from "../services/axiosConfig";
import { useNavigate } from "react-router-dom";

const SharedWithMe = () => {

    const [shares, setShares] = useState([]);
    const [loading, setLoading] = useState(true);
    const [openMenu, setOpenMenu] = useState(null);

    const navigate = useNavigate();

    useEffect(() => {
        fetchSharedItems();
    }, []);


    const handleMenuToggle = (index) => {
    setOpenMenu(openMenu === index ? null : index);
    };

   const handleOpen = async (share) => {
    try {

        if (share.itemType === "FILE") {

            navigate(`/shared-file/${share.itemId}`);

        } else if (share.itemType === "FOLDER") {

            navigate(`/shared-folder/${share.itemId}`);
        }

        setOpenMenu(null);

    } catch (error) {

        console.error(
            "Error opening shared item:",
            error
        );

        alert("Unable to open this item.");
    }
};

    const handleRename = (share) => {
        console.log("Rename shared item:", share);
        setOpenMenu(null);
    };

    const handleDelete = (share) => {
        console.log("Delete shared item:", share);
        setOpenMenu(null);
    };
    const fetchSharedItems = async () => {

        try {

            setLoading(true);

            const response = await api.get(
                "/api/shares/shared-with-me"
            );

            console.log("Shared items:", response.data);

            setShares(response.data);

        } catch (error) {

            console.error(
                "Error loading shared items:",
                error
            );

        } finally {

            setLoading(false);

        }
    };

    const getItemIcon = (itemType) => {

        if (itemType === "FOLDER") {
            return "📁";
        }

        return "📄";
    };

    const getPermissionIcon = (permission) => {

        if (permission === "EDITOR") {
            return "✏️";
        }
        return "👁️";
    };

    if (loading) {
        return (
            <div className="shared-page">

                <div className="shared-loading">
                    Loading shared items...
                </div>

            </div>
        );
    }

    return (
        <div className="shared-page">

          

            <div className="shared-page-header">

                <div>

                    <h1>Shared with me</h1>

                    <p>
                        Files and folders that others have shared with you
                    </p>

                </div>

            </div>

            {shares.length === 0 ? (

                <div className="shared-empty">

                    <div className="shared-empty-icon">
                        👥
                    </div>

                    <h2>Nothing shared with you</h2>

                    <p>
                        When someone shares a file or folder with you,
                        it will appear here.
                    </p>

                </div>

            ) : (

                <div className="shared-list">

                    {shares.map((share, index) => (

                        <div
                            className="shared-item"
                            key={`${share.itemType}-${share.itemId}-${index}`}
                        >
                            <div className="shared-item-icon">

                                {getItemIcon(
                                    share.itemType
                                )}

                            </div>                          
                            <div className="shared-item-info">

                                <h3>
                                    {share.itemName}
                                </h3>

                                <div className="shared-item-details">

                                    <span>
                                        Shared by:
                                        {" "}
                                        
                                        <strong>
                                            {share.shareBy}
                                        </strong>
                                    </span>
                                </div>

                            </div>
                            <div
                                className={`shared-permission ${
                                    share.permission === "EDITOR"
                                        ? "editor"
                                        : "viewer"
                                }`}
                            >

                                <span>
                                    {getPermissionIcon(
                                        share.permission
                                    )}
                                </span>

                                <span>
                                    {share.permission === "EDITOR"
                                        ? "Editor"
                                        : "Viewer"}
                                </span>

                            </div>


                            

                          
            <div className="shared-item-menu-container">

                <button
                    className="shared-three-dot"
                    onClick={() => handleMenuToggle(index)}
                >
                    ⋮
                </button>

                {openMenu === index && (
                    <div className="shared-item-menu">

                        <button
                            onClick={() => handleOpen(share)}
                        >
                            👁️ Open
                        </button>

                        {share.permission === "EDITOR" && (
                            <>
                                <button
                                    onClick={() => handleRename(share)}
                                >
                                    ✏️ Rename
                                </button>

                                <button
                                    onClick={() => handleDelete(share)}
                                >
                                    🗑️ Delete
                                </button>
                            </>
                        )}

                    </div>
                )}

</div>

                        </div>

                    ))}

                </div>

            )}

        </div>
    );
};

export default SharedWithMe;

