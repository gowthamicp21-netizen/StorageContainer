import React, { useState } from "react";
import axios from "axios";
import { useNavigate, Link } from "react-router-dom";
import "../styles/Login.css";
import api from "../services/axiosConfig";

const Login = () => {

    const navigate = useNavigate();

    const [user, setUser] = useState({
        userEmail: "",
        userPassword: ""
    });

    const [showPassword, setShowPassword] = useState(false);

    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [loading, setLoading] = useState(false);

   
    const handleChange = (e) => {
        setUser({
            ...user,
            [e.target.name]: e.target.value
        });

        setError("");
    };

    
    const handleLogin = async (e) => {
        e.preventDefault();

        setError("");
        setSuccess("");

       
        if (!user.userEmail || !user.userPassword) {
            setError("Please enter email and password.");
            return;
        }

        setLoading(true);

        try {

            const response = await api.post(
                "http://localhost:8080/api/auth/login",
                user
            );

            const token = response.data;

            if (token) {

                
                localStorage.setItem("token", token);

               
                localStorage.setItem("userEmail", user.userEmail);

                setSuccess("Login successful!");

                
                setTimeout(() => {
                    navigate("/dashboard");
                }, 800);

            } else {
                setError("Token was not received from server.");
            }

        } catch (err) {

            console.error("Login error:", err);

            if (err.response) {

                if (err.response.status === 401) {
                    setError("Invalid email or password.");
                } else if (err.response.data?.message) {
                    setError(err.response.data.message);
                } else {
                    setError("Login failed. Please try again.");
                }

            } else if (err.request) {
                setError(
                    "Unable to connect to the server. Please make sure Spring Boot is running."
                );
            } else {
                setError("Something went wrong.");
            }

        } finally {
            setLoading(false);
        }
    };

  
    const handleGoogleLogin = () => {

        window.location.href =
            "http://localhost:8080/oauth2/authorization/google";
    };

    return (

        <div className="login-page">

            <div className="login-card">

               

                <div className="login-logo">
                    S
                    
                </div>


              

                <h1 className="login-title">
                    Welcome Back
                </h1>

                <p className="login-description">
                    Sign in to continue to your quiz account
                </p>


               
                {error && (
                    <div className="login-error">
                        {error}
                    </div>
                )}


               

                {success && (
                    <div className="login-success">
                        {success}
                    </div>
                )}


               

                <form
                    className="login-form"
                    onSubmit={handleLogin}
                >

                  
                    <div className="form-group">

                        <label htmlFor="userEmail">
                            Email Address
                        </label>

                        <input
                            type="email"
                            id="userEmail"
                            name="userEmail"
                            value={user.userEmail}
                            onChange={handleChange}
                            placeholder="Enter your email"
                            autoComplete="email"
                        />

                    </div>



                    <div className="form-group">

                        <label htmlFor="userPassword">
                            Password
                        </label>

                        <div className="password-input-wrapper">

                            <input
                                type={
                                    showPassword
                                        ? "text"
                                        : "password"
                                }
                                id="userPassword"
                                name="userPassword"
                                value={user.userPassword}
                                onChange={handleChange}
                                placeholder="Enter your password"
                                autoComplete="current-password"
                            />

                            <button
                                type="button"
                                className="password-toggle"
                                onClick={() =>
                                    setShowPassword(!showPassword)
                                }
                            >
                                {showPassword ? "Hide" : "Show"}
                            </button>

                        </div>

                    </div>


                 

                    <div className="password-row">

                        <Link
                            to="/forgotPassword"
                            className="forgot-link"
                        >
                            Forgot password?
                        </Link>

                    </div>


                   

                    <button
                        type="submit"
                        className="login-btn"
                        disabled={loading}
                    >

                        {loading
                            ? "Signing in..."
                            : "Sign In"
                        }

                    </button>

                </form>


                

                <div className="divider">
                    <span>OR</span>
                </div>


              
                <button
                    type="button"
                    className="google-btn"
                    onClick={handleGoogleLogin}
                >

                    <span className="google-icon">
                        G
                    </span>

                    Continue with Google

                </button>


                

                <div className="register-section">

                    <p className="register-text">

                        Don't have an account?

                        <Link
                            to="/register"
                            className="register-link"
                        >
                            Create account
                        </Link>

                    </p>

                </div>

            </div>

        </div>
    );
};

export default Login;