import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { toast } from "react-hot-toast";
import { authApi } from "../api/bankApi";
import { useAuth } from "../context/AuthContext";
import Loader from "../components/Loader";

export default function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [form, setForm] = useState({ email: "", password: "" });
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleChange = (event) => {
    setForm((prev) => ({ ...prev, [event.target.name]: event.target.value }));
  };

  const onSubmit = async (event) => {
    event.preventDefault();

    if (!form.email || !form.password) {
      toast.error("All fields are required");
      return;
    }

    setIsSubmitting(true);
    try {
      const { data } = await authApi.login(form);
      if (!data.success || !data.token) {
        toast.error(data.message || "Login failed");
        return;
      }
      login(data);
      toast.success("Logged in successfully");
      navigate("/dashboard");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="auth-screen">
      <div className="auth-card glass">
        <h2>Login to INBANK</h2>
        <p>Smart Banking System</p>
        <form onSubmit={onSubmit} className="form-grid">
          <input name="email" type="email" placeholder="Email" value={form.email} onChange={handleChange} required />
          <input name="password" type="password" placeholder="Password" value={form.password} onChange={handleChange} required />
          <button type="submit" disabled={isSubmitting}>
            {isSubmitting ? "Signing in..." : "Login"}
          </button>
        </form>
        {isSubmitting ? <Loader text="Verifying credentials..." /> : null}
        <small>
          New user? <Link to="/signup">Create account</Link>
        </small>
      </div>
    </div>
  );
}
