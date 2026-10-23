import { useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { saveSession } from "../services/axiosConfig";

const OAuth2Success = () => {

    const navigate = useNavigate();

    // StrictMode runs effects twice in development, and the token only exists in
    // the address bar until the first pass strips it. Without this guard the second
    // pass reads an empty hash and bounces a successfully logged-in user to /login.
    const handled = useRef(false);

    useEffect(() => {
        if (handled.current) return;
        handled.current = true;

        // The backend hands the token over as a URL fragment, so it never reaches
        // a server or an access log.
        const token = new URLSearchParams(window.location.hash.slice(1)).get("token");

        if (!token) {
            navigate("/login", { replace: true });
            return;
        }

        saveSession(token);
        window.history.replaceState(null, "", "/dashboard");
        navigate("/dashboard", { replace: true });
    }, [navigate]);

    return (
        <div>
            <h2>Logging you in...</h2>
        </div>
    );
};

export default OAuth2Success;
