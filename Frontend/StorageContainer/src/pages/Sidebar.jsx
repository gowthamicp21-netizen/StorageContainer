
import React, { useRef } from "react";
import "../styles/Sidebar.css";

const Sidebar = ({ onNewFolder, onNewFile }) => {

    const fileInputRef = useRef(null);

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


            <button className="sidebar-menu">
                🏠 My Files
            </button>

            <button className="sidebar-menu">
                ⭐ Starred
            </button>

            <button className="sidebar-menu">
                🕘 Recent
            </button>

        </aside>
    );
};

export default Sidebar;
