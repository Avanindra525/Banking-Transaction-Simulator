import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { toast } from "react-hot-toast";
import { authApi } from "../api/bankApi";

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const PASSWORD_PATTERN = /^(?=.*[A-Z])(?=.*\d).{8,}$/;

export default function SignupPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ fullName: "", email: "", password: "" });
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleChange = (event) => {
    setForm((prev) => ({ ...prev, [event.target.name]: event.target.value }));
  };

  const onSubmit = async (event) => {
    event.preventDefault();

    if (!form.fullName || !form.email || !form.password) {
      toast.error("All fields are required");
      return;
    }

    if (!EMAIL_PATTERN.test(form.email)) {
      toast.error("Enter a valid email address");
      return;
    }

    if (!PASSWORD_PATTERN.test(form.password)) {
      toast.error("Password must be 8+ chars with one uppercase letter and one number");
      return;
    }

    setIsSubmitting(true);
    try {
      const { data } = await authApi.signup(form);
      if (!data.success) {
        toast.error(data.message || "Signup failed");
        return;
      }
      toast.success("Account created successfully");
      navigate("/login");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="auth-screen">
      <div className="auth-card glass">
        <h2>Create your INBANK account</h2>
        <p>Fast. Secure. Smart.</p>
        <form onSubmit={onSubmit} className="form-grid">
          <input name="fullName" type="text" placeholder="Full Name" value={form.fullName} onChange={handleChange} required />
          <input name="email" type="email" placeholder="Email" value={form.email} onChange={handleChange} required />
          <input name="password" type="password" placeholder="Password" value={form.password} onChange={handleChange} required />
          <button type="submit" disabled={isSubmitting}>
            {isSubmitting ? "Creating account..." : "Signup"}
          </button>
        </form>
        <small>
          Already registered? <Link to="/login">Login</Link>
        </small>
      </div>
    </div>
  );
}
