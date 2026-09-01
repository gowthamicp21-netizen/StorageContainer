
import React, { useEffect, useState } from "react";
import "../styles/Folder.css";
import api from "../services/axiosConfig";
import Sidebar from "../pages/Sidebar";

const Folders = () => {

    
    const [folders, setFolders] = useState([]);

  
    const [currentFolder, setCurrentFolder] = useState(null);

    
    const [folderPath, setFolderPath] = useState([]);

   
    const [loading, setLoading] = useState(false);

    const [files, setFiles] = useState([]);

    const [showFolderModal, setShowFolderModal] = useState(false);

    const [newFolderName, setNewFolderName] = useState("");
    
    const [creatingFolder, setCreatingFolder] = useState(false);


    const fetchFiles = async () => {

    try {

        let response;

        if (currentFolder === null) {

           
            response = await api.get("/api/files");

        } else {

           
            response = await api.get(
                `/api/files?folderId=${currentFolder.id}`
            );
        }

        console.log("Files received:", response.data);

        setFiles(response.data);

    } catch (error) {

        console.error("Error loading files:", error);

    }
};


    const fetchFolders = async () => {

        try {

            setLoading(true);

            let response;

            if (currentFolder === null) {

                
                response = await api.get("/api/folders/root");

            } else {

               
                response = await api.get(
                    `/api/folders/${currentFolder.id}/children`
                );
            }

            setFolders(response.data);

        } catch (error) {

            console.error("Error loading folders:", error);

        } finally {

            setLoading(false);
        }
    };



    useEffect(() => {

        fetchFolders();
        fetchFiles();

    }, [currentFolder]);


    
    const openFolder = (folder) => {


         console.log("Opening folder:", folder);
       
        if (currentFolder !== null) {

            setFolderPath((previousPath) => [
                ...previousPath,
                currentFolder
            ]);

        }

       setCurrentFolder(folder);
    };


  

    const goToRoot = () => {

        setCurrentFolder(null);
        setFolderPath([]);
    };


   

    const goBack = () => {

        if (folderPath.length === 0) {

          
            goToRoot();
            return;
        }

        const previousPath = [...folderPath];

        
        previousPath.pop();

        setFolderPath(previousPath);

        
        if (previousPath.length === 0) {

            setCurrentFolder(null);

        } else {

            setCurrentFolder(
                previousPath[previousPath.length - 1]
            );
        }
    };

    const handleNewFolder = () => {
        setNewFolderName("");
        setShowFolderModal(true);
    };


const createFolder = async () => {

    const trimmedName = newFolderName.trim();

    if (trimmedName === "") {
        alert("Folder name cannot be empty.");
        return;
    }

    try {

        setCreatingFolder(true);

        const folderData = {
            folderName: trimmedName,
            parentFolderId:
                currentFolder === null
                    ? null
                    : currentFolder.id
        };

        await api.post(
            "/api/folders",
            folderData
        );

        setShowFolderModal(false);
        setNewFolderName("");

        fetchFolders();

    } catch (error) {

        console.error(
            "Error creating folder:",
            error
        );

        alert("Failed to create folder.");

    } finally {

        setCreatingFolder(false);
    }
};

 
const handleNewFile = async (file) => {

    if (!file) {
        return;
    }

    try {

        console.log("Selected file:", file.name);

        const formData = new FormData();

      
        formData.append("file", file);


        
        if (currentFolder !== null) {

            formData.append(
                "folderId",
                currentFolder.id
            );
        }


       
        const response = await api.post(
            "/api/files/upload",
            formData
        );


        console.log(
            "File uploaded:",
            response.data
        );


        alert("File uploaded successfully!");


       
        fetchFolders();


    } catch (error) {

        console.error(
            "File upload failed:",
            error
        );

        alert("File upload failed.");
    }
};
const handleDownload = async (fileId) => {
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

        const link = document.createElement("a");
        link.href = url;

        // Get filename from backend response
        const contentDisposition =
            response.headers["content-disposition"];

        let fileName = "downloaded-file";

        if (contentDisposition) {

            const match = contentDisposition.match(
                /filename="?([^"]+)"?/
            );

            if (match && match[1]) {
                fileName = match[1];
            }
        }

        console.log("Downloaded filename:", fileName);

        link.download = fileName;

        document.body.appendChild(link);
        link.click();

        link.remove();

        window.URL.revokeObjectURL(url);

    } catch (error) {
        console.error("Download failed:", error);
        alert("File download failed");
    }
};



    return (

        <div className="storage-layout">

           

            <Sidebar
                onNewFolder={handleNewFolder}
                onNewFile={handleNewFile}
            />



            <main className="folder-content">

                <div className="folder-header">

                    <div>

                        <h1>
                            {currentFolder
                                ? currentFolder.folderName
                                : "My Files"}
                        </h1>



                        <div className="breadcrumb">

                            <button
                                onClick={goToRoot}
                                className="breadcrumb-button"
                            >
                                🏠 My Files
                            </button>


                            {folderPath.map((folder) => (

                                <React.Fragment
                                    key={folder.id}
                                >

                                    <span className="breadcrumb-arrow">
                                        /
                                    </span>

                                    <span className="breadcrumb-folder">
                                        {folder.folderName}
                                    </span>

                                </React.Fragment>

                            ))}


                            {currentFolder && (

                                <>
                                    <span className="breadcrumb-arrow">
                                        /
                                    </span>

                                    <span className="breadcrumb-current">
                                        {currentFolder.folderName}
                                    </span>
                                </>

                            )}

                        </div>

                    </div>


                    

                    {currentFolder && (

                        <button
                            className="back-button"
                            onClick={goBack}
                        >
                            ← Back
                        </button>

                    )}

                </div>


              

                {loading && (

                    <div className="loading">
                        Loading folders...
                    </div>

                )}

                {showFolderModal && (

    <div
        className="modal-overlay"
        onClick={() => setShowFolderModal(false)}
    >

        <div
            className="folder-modal"
            onClick={(e) => e.stopPropagation()}
        >

            <div className="modal-header">

                <div>
                    <h2>New Folder</h2>

                    <p>
                        Create a new folder in{" "}
                        <strong>
                            {currentFolder
                                ? currentFolder.folderName
                                : "My Files"}
                        </strong>
                    </p>
                </div>

                <button
                    className="modal-close"
                    onClick={() => setShowFolderModal(false)}
                >
                    ×
                </button>

            </div>


            <div className="modal-body">

                <label htmlFor="folderName">
                    Folder name
                </label>

                <div className="folder-input-wrapper">

                    <span className="folder-input-icon">
                        📁
                    </span>

                    <input
                        id="folderName"
                        type="text"
                        placeholder="Enter folder name"
                        value={newFolderName}
                        onChange={(e) =>
                            setNewFolderName(e.target.value)
                        }
                        onKeyDown={(e) => {

                            if (e.key === "Enter") {
                                createFolder();
                            }

                            if (e.key === "Escape") {
                                setShowFolderModal(false);
                            }
                        }}
                        autoFocus
                    />

                </div>

            </div>


            <div className="modal-footer">

                <button
                    className="cancel-button"
                    onClick={() =>
                        setShowFolderModal(false)
                    }
                    disabled={creatingFolder}
                >
                    Cancel
                </button>


                <button
                    className="create-folder-button"
                    onClick={createFolder}
                    disabled={creatingFolder}
                >

                    {creatingFolder
                        ? "Creating..."
                        : "Create Folder"}

                </button>

            </div>

        </div>

    </div>

    )}


               

                {!loading && folders.length === 0 && (

                    <div className="empty-folder">

                        <div className="empty-icon">
                            📁
                        </div>

                        <h2>
                            This folder is empty
                        </h2>

                        <p>
                            Create a new folder using
                            the sidebar.
                        </p>

                    </div>

                )}


             

                {!loading && folders.length > 0 && (

                    <div className="folder-grid">

                        {folders.map((folder) => (

                            <div
                                className="folder-card"
                                key={folder.id}
                                onClick={() =>
                                    openFolder(folder)
                                }
                            >

                                <div className="folder-icon">
                                    📁
                                </div>

                                <div className="folder-name">
                                    {folder.folderName}
                                </div>

                            </div>

                        ))}

                    </div>

                )}

                {!loading && files.length > 0 && (

            <div className="files-section">

        <div className="file-grid">

            {files.map((file) => (

                <div
                    className="file-card"
                    key={file.id}
                >

                    <div className="file-icon">
                        📄
                    </div>

                    <div className="file-name">
                        {file.fileName}
                    </div>
                    <button onClick={() => handleDownload(file.id)}>
                        Download
                    </button>

                </div>

                     ))}

                </div>

         </div>

        )}

            </main>

        </div>
    );
};

export default Folders;

