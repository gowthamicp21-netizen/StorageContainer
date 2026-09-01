import React, { useState } from "react";
import api from "../services/axiosConfig";
import "../styles/FileUpload.css";

const FileUpload = ({ folderId, onUploadSuccess }) => {

    const [selectedFile, setSelectedFile] = useState(null);
    const [uploading, setUploading] = useState(false);
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");

    const handleFileChange = (event) => {

        const file = event.target.files[0];

        if (!file) {
            return;
        }

        setSelectedFile(file);
        setMessage("");
        setError("");
    };

  

    const handleUpload = async () => {

        if (!selectedFile) {
            setError("Please select a file.");
            return;
        }

        try {

            setUploading(true);
            setMessage("");
            setError("");

            const formData = new FormData();

          
            formData.append(
                "file",
                selectedFile
            );

            
            if (
                folderId !== null &&
                folderId !== undefined
            ) {
                formData.append(
                    "folderId",
                    folderId
                );
            }

            const response = await api.post(
                "/files/upload",
                formData
            );

            console.log(
                "Uploaded file:",
                response.data
            );

            setMessage(
                "File uploaded successfully!"
            );

            setSelectedFile(null);

           
            document.getElementById(
                "fileInput"
            ).value = "";

           
            if (onUploadSuccess) {
                onUploadSuccess(response.data);
            }

        } catch (error) {

            console.error(
                "Upload error:",
                error
            );

            setError(
                error.response?.data ||
                "File upload failed."
            );

        } finally {

            setUploading(false);
        }
    };

    return (
        <div className="file-upload">

            <div className="upload-title">
                <h3>Upload File</h3>

                <p>
                    Select a file to upload
                </p>
            </div>

            <div className="upload-box">

                <input
                    id="fileInput"
                    type="file"
                    onChange={handleFileChange}
                    disabled={uploading}
                />

                {selectedFile && (
                    <div className="selected-file">

                        <div className="file-icon">
                            📄
                        </div>

                        <div className="file-info">

                            <strong>
                                {selectedFile.name}
                            </strong>

                            <span>
                                {(
                                    selectedFile.size /
                                    1024
                                ).toFixed(2)} KB
                            </span>

                        </div>

                    </div>
                )}

                <button
                    className="upload-btn"
                    onClick={handleUpload}
                    disabled={
                        !selectedFile ||
                        uploading
                    }
                >
                    {uploading
                        ? "Uploading..."
                        : "Upload"}
                </button>

                {message && (
                    <p className="success-message">
                        {message}
                    </p>
                )}

                {error && (
                    <p className="error-message">
                        {error}
                    </p>
                )}

            </div>

        </div>
    );
};

export default FileUpload;
