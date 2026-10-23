import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import "../styles/PublicShare.css";
import api, { API_BASE_URL } from "../services/axiosConfig";

const viewUrl = (token, fileId) =>
    `${API_BASE_URL}/api/public/share/${token}/file/${fileId}/view`;

const downloadUrl = (token, fileId) =>
    `${API_BASE_URL}/api/public/share/${token}/file/${fileId}/download`;

const formatSize = (bytes) => {

    if (bytes === null || bytes === undefined) {
        return "";
    }

    const units = ["B", "KB", "MB", "GB"];

    let value = bytes;
    let unit = 0;

    while (value >= 1024 && unit < units.length - 1) {
        value /= 1024;
        unit += 1;
    }

    return `${unit === 0 ? value : value.toFixed(1)} ${units[unit]}`;
};

const Preview = ({ token, file }) => {

    const url = viewUrl(token, file.id);
    const type = file.mimeType || "";

    if (type.startsWith("image/")) {
        return <img className="share-image" src={url} alt={file.name} />;
    }

    if (type === "application/pdf") {
        return <iframe className="share-frame" src={url} title={file.name} />;
    }

    if (type.startsWith("video/")) {
        return <video className="share-video" src={url} controls />;
    }

    if (type.startsWith("audio/")) {
        return <audio className="share-audio" src={url} controls />;
    }

    if (type.startsWith("text/")) {
        return <iframe className="share-frame" src={url} title={file.name} />;
    }

    return (
        <div className="share-no-preview">
            <span>📄</span>
            <h3>No preview for this type</h3>
            <p>{file.name}</p>
            <a className="share-primary-btn" href={downloadUrl(token, file.id)}>
                Download
            </a>
        </div>
    );
};

const PublicShare = () => {

    const { token } = useParams();

    const [data, setData] = useState(null);
    const [rootName, setRootName] = useState("");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [breadcrumbs, setBreadcrumbs] = useState([]);
    const [selected, setSelected] = useState(null);

    useEffect(() => {

        let ignore = false;

        const load = async () => {

            try {

                const response = await api.get(`/api/public/share/${token}`);

                if (!ignore) {
                    setData(response.data);
                    setRootName(response.data.itemName);
                }

            } catch (err) {

                if (ignore) {
                    return;
                }

                setError(
                    err.response?.status === 404
                        ? "This link no longer exists. The owner may have turned off link sharing."
                        : "This link could not be opened."
                );
            } finally {
                if (!ignore) {
                    setLoading(false);
                }
            }
        };

        load();

        return () => {
            ignore = true;
        };

    }, [token]);

    const openFolder = async (folder) => {

        try {

            const response = await api.get(
                `/api/public/share/${token}/folder/${folder.id}`
            );

            setData(response.data);
            setBreadcrumbs((current) => [...current, folder]);

        } catch {
            setError("This folder is no longer part of the shared link.");
        }
    };

    const openCrumb = async (index) => {

        try {

            if (index < 0) {

                const response = await api.get(`/api/public/share/${token}`);

                setData(response.data);
                setBreadcrumbs([]);
                setSelected(null);
                return;
            }

            const target = breadcrumbs[index];

            const response = await api.get(
                `/api/public/share/${token}/folder/${target.id}`
            );

            setData(response.data);
            setBreadcrumbs((current) => current.slice(0, index + 1));
            setSelected(null);

        } catch {
            setError("This folder is no longer part of the shared link.");
        }
    };

    if (loading) {
        return (
            <div className="public-share-page">
                <div className="share-status">Opening link...</div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="public-share-page">
                <div className="share-status">
                    <span>🔒</span>
                    <h2>{error}</h2>
                    <Link to="/login">Go to StorageContainer</Link>
                </div>
            </div>
        );
    }

    const isFile = data.itemType === "FILE";

    // A file link already carries the file itself, so it renders straight into the
    // preview instead of showing a one-item list.
    const singleFile = isFile ? data.files[0] : null;

    const openFile = data.itemType === "FOLDER" ? selected : singleFile;

    return (
        <div className="public-share-page">

            <header className="public-share-header">

                <div className="public-share-title">
                    <span className="public-share-icon">
                        {openFile ? "📄" : "📁"}
                    </span>

                    <div>
                        <h1>{openFile ? openFile.name : data.itemName}</h1>

                        <p>
                            Shared by {data.ownerEmail}
                            {" · "}
                            <strong>Anyone with the link</strong>
                        </p>
                    </div>
                </div>

                <Link className="public-share-signin" to="/login">
                    Sign in
                </Link>

            </header>

            {openFile ? (
                <main className="public-share-body">

                    {!isFile && (
                        <button
                            className="share-back-btn"
                            onClick={() => setSelected(null)}
                        >
                            ← {data.itemName}
                        </button>
                    )}

                    <Preview token={token} file={openFile} />

                    <div className="public-share-file-actions">
                        <a
                            className="share-primary-btn"
                            href={downloadUrl(token, openFile.id)}
                        >
                            Download · {formatSize(openFile.sizeInBytes)}
                        </a>
                    </div>

                </main>
            ) : (
                <main className="public-share-body">

                    {breadcrumbs.length > 0 && (
                        <nav className="share-breadcrumbs">
                            <button onClick={() => openCrumb(-1)}>
                                {rootName}
                            </button>

                            {breadcrumbs.map((crumb, index) => (
                                <span key={crumb.id}>
                                    <i>/</i>
                                    <button onClick={() => openCrumb(index)}>
                                        {crumb.name}
                                    </button>
                                </span>
                            ))}
                        </nav>
                    )}

                    {data.folders.length === 0 && data.files.length === 0 ? (
                        <p className="share-empty">
                            This folder is empty.
                        </p>
                    ) : (
                        <div className="share-grid">

                            {data.folders.map((folder) => (
                                <button
                                    key={`folder-${folder.id}`}
                                    className="share-tile"
                                    onClick={() => openFolder(folder)}
                                >
                                    <span>📁</span>
                                    <strong>{folder.name}</strong>
                                    <small>Folder</small>
                                </button>
                            ))}

                            {data.files.map((file) => (
                                <button
                                    key={`file-${file.id}`}
                                    className="share-tile"
                                    onClick={() => setSelected(file)}
                                >
                                    <span>📄</span>
                                    <strong>{file.name}</strong>
                                    <small>{formatSize(file.sizeInBytes)}</small>
                                </button>
                            ))}

                        </div>
                    )}

                </main>
            )}

        </div>
    );
};

export default PublicShare;
