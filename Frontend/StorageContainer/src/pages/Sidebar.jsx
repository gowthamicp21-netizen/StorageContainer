import React, { useRef } from "react";
import "../styles/Sidebar.css";
import { useNavigate } from "react-router-dom";

const Sidebar = ({ onNewFolder, onNewFile }) => {

    const fileInputRef = useRef(null);
    const navigate = useNavigate();

    const handleNewFileClick = () => {
        fileInputRef.current.click();
    };

    const handleFileChange = (event) => {

        const file = event.target.files[0];

        if (file) {
            onNewFile(file);
        }

        event.target.value = "";
    };

    const handleSharedWithMe = () => {
        navigate("/shared-with-me");
    };

    const handleLogout = () => {
        localStorage.removeItem("token");
        localStorage.removeItem("user");

        navigate("/login");
    };

    return (
        <aside className="sidebar">

            <h2 className="sidebar-title">
                StorageContainer
            </h2>

            <button
                className="sidebar-action"
                onClick={onNewFolder}
            >
                📁 New Folder
            </button>

            <button
                className="sidebar-action"
                onClick={handleNewFileClick}
            >
                📄 New File
            </button>

            <input
                type="file"
                ref={fileInputRef}
                onChange={handleFileChange}
                style={{ display: "none" }}
            />

            <div className="sidebar-divider"></div>

            <button
                className="sidebar-menu"
                onClick={handleSharedWithMe}
            >
                🤝 Shared With Me
            </button>

            <button className="sidebar-menu">
                ⭐ Starred
            </button>

            <button className="sidebar-menu">
                🕘 Recent
            </button>

            <div className="sidebar-bottom">

            <button
                className="sidebar-menu logout-button"
                onClick={handleLogout}
                                     >
                    🚪 Logout
        </button>

</div>

        </aside>
    );
};

export default Sidebar;
