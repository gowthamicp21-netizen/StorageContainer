import axios from 'axios';

const API_URL = 'http://localhost:8080/api/auth';

export const registerUser = async (userData) => {
  console.log("Calling register API...");
  console.log("User data:", userData);

  try {
    const response = await axios.post(
      `${API_URL}/register`,
      userData
    );

    console.log("STATUS:", response.status);
    console.log("HEADERS:", response.headers);
    console.log("DATA:", response.data);

    return response.data;

  } catch (error) {
    console.error("STATUS:", error.response?.status);
    console.error("DATA:", error.response?.data);
    console.error("ERROR:", error.message);

    throw error;
  }
};