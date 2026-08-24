import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import '../styles/Login.css';
import { jwtDecode } from "jwt-decode";


function Login() {

  const navigate = useNavigate();

    const [formData, setFormData] = useState({
        user_email: "",
        user_password: ""
    });

    const [message, setMessage] = useState("");

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };

    const handleSubmit = async (e) => {

        e.preventDefault();

        try {

            const response = await axios.post(
                "http://localhost:8080/api/auth/login",
                formData
            );

            const token = response.data;
            localStorage.setItem("token", response.data)
            setMessage("Login successful!");

            

        } catch (error) {

            console.error("Login error:", error);

            setMessage("Invalid email or password");
        }
    };

    return (

        <div className="login-page">

            <div className="login-box">

                {/* LEFT SIDE */}

                <div className="login-left">

                    <div className="StorageContainer-icon">
                        📦
                    </div>

                    <h1>StorageContainer</h1>

                    <p>
                       A cloud-based file storage and sharing web application
                    </p>

                </div>


                {/* RIGHT SIDE */}

                <div className="login-right">

                    <h2>Welcome Back!</h2>

                    <p className="login-subtitle">
                        Login to continue 
                    </p>


                    <form onSubmit={handleSubmit}>

                        <div className="login-form-group">

                            <label>Email Address</label>

                            <input
                                type="email"
                                name="user_email"
                                value={formData.user_email}
                                onChange={handleChange}
                                placeholder="Enter your email"
                                required
                            />

                        </div>


                        <div className="login-form-group">

                            <label>Password</label>

                            <input
                                type="password"
                                name="user_password"
                                value={formData.user_password}
                                onChange={handleChange}
                                placeholder="Enter your password"
                                required
                            />

                        </div>


                        <button
                            type="submit"
                            className="login-button"
                        >
                            Login
                        </button>

                    </form>


                    {message && (
                        <p className="login-message">
                            {message}
                        </p>
                    )}

                    <div className="forgot-password">
                        <a href="/forgot-password">
                              Forgot Password?
                        </a>
                    </div>
                    <p className="register-text">
                        Don't have an account?{" "}
                        <a href="/register">
                            Create Account
                        </a>
                    </p>

                </div>

            </div>

        </div>
    );
}

export default Login;