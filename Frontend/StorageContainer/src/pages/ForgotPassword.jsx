import { useState } from "react";
import "../styles/ForgotPassword.css";
import api, { apiErrorMessage } from "../services/axiosConfig";

function ForgotPassword() {

    const [email, setEmail] = useState("");
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {

        e.preventDefault();

        setMessage("");
        setError("");

        try {

            setLoading(true);

            const response = await api.post(
                "/api/auth/forgot-password",
                { userEmail: email }
            );

            setMessage(response.data.message);

        } catch (err) {

            setError(apiErrorMessage(err, "Unable to process your request."));

        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="forgot-page">

            <div className="forgot-box">

                <h2>Forgot Password?</h2>

                <p>
                    Enter your registered email address and we will send you a
                    link to choose a new password.
                </p>

                <form onSubmit={handleSubmit}>

                    <label htmlFor="email">Email Address</label>

                    <input
                        id="email"
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        placeholder="Enter your email"
                        autoComplete="email"
                        required
                    />

                    <button type="submit" disabled={loading}>
                        {loading ? "Sending..." : "Send reset link"}
                    </button>

                </form>

                {message && (
                    <p className="forgot-message">
                        {message}
                    </p>
                )}

                {error && (
                    <p className="forgot-message" role="alert">
                        {error}
                    </p>
                )}

                <a href="/login">
                    Back to Login
                </a>

            </div>

        </div>
    );
}

export default ForgotPassword;
