import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";

const OAuth2Success = () => {

    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    useEffect(() => {

        const token = searchParams.get("token");

        if (token) {

            localStorage.setItem("token", token);

            console.log("Google login successful");
            console.log("JWT saved");

            navigate("/dashboard");

        } else {

            console.error("Token not found");

            navigate("/login");
        }

    }, [searchParams, navigate]);

    return (
        <div>
            <h2>Logging you in...</h2>
        </div>
    );
};

export default OAuth2Success;