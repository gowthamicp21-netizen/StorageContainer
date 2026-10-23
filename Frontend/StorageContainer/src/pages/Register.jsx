import React, { useState } from "react";
import '../styles/Register.css';
import api, { apiErrorMessage } from "../services/axiosConfig";

const Register = () => {
    const [user, setUser] = useState({
        userEmail: "",
        userPassword: ""
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
            await api.post("/api/auth/register", {
                userEmail: user.userEmail,
                userPassword: user.userPassword
            });

            alert("Registration successful!");

            setUser({
                userEmail: "",
                userPassword: ""
            });

        } catch (error) {
            alert(apiErrorMessage(error, "Registration failed"));
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