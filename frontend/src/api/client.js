import axios from "axios";
import { toast } from "react-hot-toast";

const AUTH_KEY = "inbank-auth";

const apiClient = axios.create({
  baseURL: "/",
  headers: {
    "Content-Type": "application/json"
  },
  timeout: 15000
});

apiClient.interceptors.request.use((config) => {
  const raw = localStorage.getItem(AUTH_KEY);
  if (raw) {
    const auth = JSON.parse(raw);
    if (auth?.token) {
      config.headers.Authorization = `Bearer ${auth.token}`;
    }
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const message =
      error.response?.data?.message ||
      (error.code === "ECONNABORTED"
        ? "Server timeout. Please try again."
        : "Unable to reach server. Please check backend status.");
    toast.error(message);
    return Promise.reject(error);
  }
);

export default apiClient;
