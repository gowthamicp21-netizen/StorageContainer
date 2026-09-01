import React, { useEffect, useState } from "react";
import { Navigate, Outlet } from "react-router-dom";
import api from "../services/axiosConfig";

const ProtectedRoute = () => {

    const [loading, setLoading] = useState(true);
    const [authenticated, setAuthenticated] = useState(false);

    useEffect(() => {

        const token = localStorage.getItem("token");

        if (!token) {
            setLoading(false);
            return;
        }

        api.get("/api/auth/me")
            .then(() => {
                setAuthenticated(true);
            })
            .catch(() => {
                localStorage.removeItem("token");
                localStorage.removeItem("userEmail");
                localStorage.removeItem("role");

                setAuthenticated(false);
            })
            .finally(() => {
                setLoading(false);
            });

    }, []);

    if (loading) {
        return (
            <div style={{
                minHeight: "100vh",
                display: "flex",
                justifyContent: "center",
                alignItems: "center"
            }}>
                Checking authentication...
            </div>
        );
    }

    if (!authenticated) {
        return <Navigate to="/login" replace />;
    }

    return <Outlet />;
};

export default ProtectedRoute;