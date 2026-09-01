import React, { useState } from "react";
import axios from "axios";
import '../styles/Register.css';
import api from "../services/axiosConfig";

const Register = () => {
    const [user, setUser] = useState({
        userEmail: "",
        userPassword: "",
        userRole: "PUBLIC_USER"
    });

    const handleChange = (e) => {
        setUser({
            ...user,
            [e.target.name]: e.target.value
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            const response = await api.post(
                "/api/auth/register",
                user
            );

            alert("Registration successful!");

            console.log(response.data);

            setUser({
                userEmail: "",
                userPassword: "",
                userRole: "PUBLIC_USER"
            });

        } catch (error) {
            console.error(error);
            alert(
                error.response?.data?.message ||
                "Registration failed"
            );
        }
    };

    return (
        <div className="register-container">

            <div className="register-card">

                <h2>Create Account</h2>

                <form onSubmit={handleSubmit}>

                    <div className="form-group">
                        <label>Email</label>

                        <input
                            type="email"
                            name="userEmail"
                            value={user.userEmail}
                            onChange={handleChange}
                            placeholder="Enter your email"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label>Password</label>

                        <input
                            type="password"
                            name="userPassword"
                            value={user.userPassword}
                            onChange={handleChange}
                            placeholder="Enter your password"
                            required
                        />
                    </div>

                  

                    <button type="submit">
                        Register
                    </button>

                </form>

            </div>

        </div>
    );
};

export default Register;