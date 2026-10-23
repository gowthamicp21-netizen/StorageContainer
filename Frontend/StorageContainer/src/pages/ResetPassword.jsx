import { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import "../styles/ForgotPassword.css";
import api, { apiErrorMessage } from "../services/axiosConfig";

function ResetPassword() {

    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    const [token] = useState(searchParams.get("token") || "");
    const [password, setPassword] = useState("");
    const [confirm, setConfirm] = useState("");
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {

        e.preventDefault();

        setError("");
        setMessage("");

        if (password !== confirm) {
            setError("Passwords do not match.");
            return;
        }

        if (password.length < 8) {
            setError("Password must be at least 8 characters.");
            return;
        }

        try {

            setLoading(true);

            const response = await api.post("/api/auth/reset-password", {
                token,
                newPassword: password
            });

            setMessage(response.data.message);

            setTimeout(() => navigate("/login"), 2500);

        } catch (err) {

            setError(apiErrorMessage(err, "Could not reset your password."));

        } finally {
            setLoading(false);
        }
    };

    if (!token) {
        return (
            <div className="forgot-page">
                <div className="forgot-box">
                    <h2>Reset link not valid</h2>
                    <p>
                        This link is missing its reset token. Request a new one
                        and open the link from your email.
                    </p>
                    <Link to="/forgotPassword">Request a new link</Link>
                </div>
            </div>
        );
    }

    return (
        <div className="forgot-page">

            <div className="forgot-box">

                <h2>Choose a new password</h2>

                <form onSubmit={handleSubmit}>

                    <label htmlFor="password">New Password</label>

                    <input
                        id="password"
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        autoComplete="new-password"
                        required
                    />

                    <label htmlFor="confirm">Confirm Password</label>

                    <input
                        id="confirm"
                        type="password"
                        value={confirm}
                        onChange={(e) => setConfirm(e.target.value)}
                        autoComplete="new-password"
                        required
                    />

                    <button type="submit" disabled={loading}>
                        {loading ? "Updating..." : "Update password"}
                    </button>

                </form>

                {message && (
                    <p className="forgot-message">{message}</p>
                )}

                {error && (
                    <p className="forgot-message" role="alert">{error}</p>
                )}

                <Link to="/login">Back to Login</Link>

            </div>

        </div>
    );
}

export default ResetPassword;
