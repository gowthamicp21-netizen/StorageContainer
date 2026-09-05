import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import "../styles/SharedFileViewer.css";
import api from "../services/axiosConfig";

const SharedFileViewer = () => {

    const { fileId } = useParams();
    const navigate = useNavigate();

    const [fileUrl, setFileUrl] = useState(null);
    const [fileName, setFileName] = useState("");
    const [fileType, setFileType] = useState("");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {

        fetchFile();

        return () => {
            if (fileUrl) {
                window.URL.revokeObjectURL(fileUrl);
            }
        };

    }, [fileId]);

    const fetchFile = async () => {

        try {

            setLoading(true);
            setError("");

            const response = await api.get(
               `/api/files/view/${fileId}`,
                {
                    responseType: "blob"
                }
            );

            const contentType =
                response.headers["content-type"] ||
                "application/octet-stream";

            const blob = new Blob(
                [response.data],
                {
                    type: contentType
                }
            );

            const url = window.URL.createObjectURL(blob);

            setFileUrl(url);
            setFileType(contentType);

            const disposition =
                response.headers["content-disposition"];

            let name = `File ${fileId}`;

            if (disposition) {

                const match =
                    disposition.match(
                        /filename="?([^"]+)"?/
                    );

                if (match) {
                    name = match[1];
                }
            }

            setFileName(name);

        } catch (error) {

            console.error(
                "Error loading shared file:",
                error
            );

            if (error.response?.status === 403) {
                setError(
                    "You don't have permission to view this file."
                );
            } else if (error.response?.status === 404) {
                setError("File not found.");
            } else {
                setError("Unable to open this file.");
            }

        } finally {

            setLoading(false);

        }
    };

    const handleBack = () => {
        navigate("/shared-with-me");
    };

    if (loading) {

        return (
            <div className="shared-file-viewer-page">

                <div className="shared-file-loading">
                    <div className="shared-file-spinner">
                        ⏳
                    </div>

                    <h2>
                        Opening file...
                    </h2>
                </div>

            </div>
        );
    }

    if (error) {

        return (
            <div className="shared-file-viewer-page">

                <div className="shared-file-error">

                    <div className="shared-file-error-icon">
                        ⚠️
                    </div>

                    <h2>
                        {error}
                    </h2>

                    <button
                        onClick={handleBack}
                        className="shared-file-back-button"
                    >
                        ← Back to Shared With Me
                    </button>

                </div>

            </div>
        );
    }

    return (
        <div className="shared-file-viewer-page">

            <header className="shared-file-viewer-header">

                <button
                    className="shared-file-back"
                    onClick={handleBack}
                >
                    ←
                </button>

                <div className="shared-file-info">

                    <div className="shared-file-icon">
                        📄
                    </div>

                    <div>
                        <h1>
                            {fileName}
                        </h1>

                        <p>
                            View only
                        </p>
                    </div>

                </div>

            </header>

            <main className="shared-file-viewer-content">

                {fileType === "application/pdf" ? (

                    <iframe
                        src={fileUrl}
                        title={fileName}
                        className="shared-file-pdf"
                    />

                ) : fileType.startsWith("image/") ? (

                    <div className="shared-file-image-container">

                        <img
                            src={fileUrl}
                            alt={fileName}
                            className="shared-file-image"
                        />

                    </div>

                ) : fileType.startsWith("text/") ? (

                    <iframe
                        src={fileUrl}
                        title={fileName}
                        className="shared-file-text"
                    />

                ) : (

                    <div className="shared-file-unsupported">

                        <div className="shared-file-unsupported-icon">
                            📄
                        </div>

                        <h2>
                            Preview not available
                        </h2>

                        <p>
                            This file type cannot be previewed
                            in the browser.
                        </p>

                        <p>
                            {fileName}
                        </p>

                    </div>
                )}

            </main>

        </div>
    );
};

export default SharedFileViewer;