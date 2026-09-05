import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import "../styles/SharedFolder.css";
import api from "../services/axiosConfig";

const SharedFolder = () => {

    const { folderId } = useParams();
    const navigate = useNavigate();

    const [folder, setFolder] = useState(null);
    const [folders, setFolders] = useState([]);
    const [files, setFiles] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        fetchSharedFolder();
    }, [folderId]);

    const fetchSharedFolder = async () => {

        try {

            setLoading(true);
            setError("");

            const response = await api.get(
                `/api/shares/folder/${folderId}`
            );

            console.log("Shared folder:", response.data);

            setFolder(response.data.folder);
            setFolders(response.data.folders || []);
            setFiles(response.data.files || []);

        } catch (error) {

            console.error(
                "Error loading shared folder:",
                error
            );

            if (error.response?.status === 403) {
                setError(
                    "You don't have permission to access this folder."
                );
            } else if (error.response?.status === 404) {
                setError("Shared folder not found.");
            } else {
                setError("Unable to load shared folder.");
            }

        } finally {

            setLoading(false);

        }
    };

    const handleOpenFolder = (folderId) => {

        navigate(`/shared-folder/${folderId}`);

    };

    const handleOpenFile = async (fileId) => {

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

            window.open(url, "_blank");

            setTimeout(() => {
                window.URL.revokeObjectURL(url);
            }, 1000);

        } catch (error) {

            console.error(
                "Error opening file:",
                error
            );

            alert("Unable to open file.");

        }
    };

    if (loading) {

        return (
            <div className="shared-folder-page">

                <div className="shared-folder-loading">
                    Loading folder...
                </div>

            </div>
        );
    }

    if (error) {

        return (
            <div className="shared-folder-page">

                <div className="shared-folder-error">

                    <div className="shared-folder-error-icon">
                        ⚠️
                    </div>

                    <h2>{error}</h2>

                    <button
                        onClick={() =>
                            navigate("/shared-with-me")
                        }
                    >
                        ← Back to Shared With Me
                    </button>

                </div>

            </div>
        );
    }

    return (
        <div className="shared-folder-page">

           
            <div className="shared-folder-header">

                <button
                    className="shared-folder-back"
                    onClick={() =>
                        navigate("/shared-with-me")
                    }
                >
                    ←
                </button>

                <div>

                    <h1>
                        📁 {folder?.folderName}
                    </h1>

                    <p>
                        Shared folder
                    </p>

                </div>

            </div>


            {/* Folder contents */}

            <div className="shared-folder-content">

                {folders.length === 0 &&
                 files.length === 0 ? (

                    <div className="shared-folder-empty">

                        <div>
                            📂
                        </div>

                        <h2>
                            This folder is empty
                        </h2>

                        <p>
                            There are no files or folders here.
                        </p>

                    </div>

                ) : (

                    <div className="shared-folder-list">

                        {/* Folders */}

                        {folders.map((childFolder) => (

                            <div
                                className="shared-content-item"
                                key={`folder-${childFolder.id}`}
                            >

                                <div className="shared-content-icon">
                                    📁
                                </div>

                                <div className="shared-content-info">

                                    <h3>
                                        {childFolder.folderName}
                                    </h3>

                                    <span>
                                        Folder
                                    </span>

                                </div>

                                <button
                                    className="shared-content-open"
                                    onClick={() =>
                                        handleOpenFolder(
                                            childFolder.id
                                        )
                                    }
                                >
                                    Open
                                </button>

                            </div>

                        ))}


                        
                        {files.map((file) => (

                            <div
                                className="shared-content-item"
                                key={`file-${file.id}`}
                            >

                                <div className="shared-content-icon">
                                    📄
                                </div>

                                <div className="shared-content-info">

                                    <h3>
                                        {file.fileName}
                                    </h3>

                                    <span>
                                        File
                                    </span>

                                </div>

                                <button
                                    className="shared-content-open"
                                    onClick={() =>
                                        handleOpenFile(
                                            file.id
                                        )
                                    }
                                >
                                    Open
                                </button>

                            </div>

                        ))}

                    </div>

                )}

            </div>

        </div>
    );
};

export default SharedFolder;