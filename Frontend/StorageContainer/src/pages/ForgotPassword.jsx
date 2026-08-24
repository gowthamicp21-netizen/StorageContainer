import React, { useState } from "react";
import axios from "axios";
import '../styles/ForgotPassword.css';
import api from "../services/axiosConfig";

function ForgotPassword() {

    const [email, setEmail] = useState("");
    const [message, setMessage] = useState("");
    const [password ,setPassword]=useState("");

    const handleSubmit = async (e) => {

        e.preventDefault();

        try {

            const response = await api.put(
                "/api/auth/forgot-password",
                {
                    user_email: email,
                    user_password:password
                }
            );

            setMessage(response.data);

        } catch (error) {

            console.error(error);

            setMessage(
                "Unable to process your request."
            );
        }
    };

    return (
        <div className="forgot-page">

            <div className="forgot-box">

                <h2>Forgot Password?</h2>

                <p>
                    Enter your registered email address
                    to reset your password.
                </p>

                <form onSubmit={handleSubmit}>

                    <label>Email Address</label>

                    <input
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        placeholder="Enter your email"
                        required
                    />


                     <div className="form-group">
                    <label htmlFor="password">Password</label>

                    <input
                         type="password"
                        id="password"
                        name="password"
                        value={password}
                        onChange={(e)=>setPassword(e.target.value)}
                        placeholder="Create a password"
                    />
          </div>

         

                    <button type="submit">
                        Password Reset
                    </button>

                </form>

                {message && (
                    <p className="forgot-message">
                        {message}
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