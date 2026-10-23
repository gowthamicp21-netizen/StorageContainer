import axios from "axios";

export const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

const api = axios.create({
    baseURL: API_BASE_URL
});

api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("token");

        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            clearSession();

            if (!window.location.pathname.startsWith("/login")) {
                window.location.assign("/login");
            }
        }

        return Promise.reject(error);
    }
);

export function clearSession() {
    localStorage.removeItem("token");
    localStorage.removeItem("userEmail");
    localStorage.removeItem("user");
    localStorage.removeItem("role");
}

export function saveSession(token, email) {
    localStorage.setItem("token", token);

    if (email) {
        localStorage.setItem("userEmail", email);
    }
}

/**
 * The backend returns failures as `{ message }`.
 */
export function apiErrorMessage(error, fallback = "Request failed") {
    return error.response?.data?.message
        || (typeof error.response?.data === "string" ? error.response.data : null)
        || fallback;
}

export default api;
