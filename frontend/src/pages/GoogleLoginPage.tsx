import { useEffect, useRef, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { ApiError, loginWithGoogleCode } from "../api";
import { LoginResponse } from "../types";

type GoogleLoginPageProps = {
  onLoginSuccess: (response: LoginResponse) => Promise<void>;
};

export function GoogleLoginPage({ onLoginSuccess }: GoogleLoginPageProps) {
  const location = useLocation();
  const navigate = useNavigate();
  const [message, setMessage] = useState("로그인 처리 중입니다...");
  const processedRef = useRef(false);

  useEffect(() => {
    if (processedRef.current) return;
    processedRef.current = true;

    const searchParams = new URLSearchParams(location.search);
    const code = searchParams.get("code");

    if (!code) {
      setMessage("구글 로그인에 실패했습니다.");
      window.setTimeout(() => navigate("/"), 700);
      return;
    }

    loginWithGoogleCode(code)
      .then(async (response) => {
        await onLoginSuccess(response);
        setMessage("로그인 성공! 홈으로 이동합니다.");
        window.location.href = "/";
      })
      .catch((error) => {
        const messageText =
          error instanceof ApiError
            ? error.message
            : "구글 로그인 중 오류가 발생했습니다.";
        setMessage(messageText);
        window.setTimeout(() => navigate("/"), 1000);
      });
  }, [location.search, navigate, onLoginSuccess]);

  return (
    <section className="auth-progress">
      <div className="auth-progress__spinner" />
      <div className="auth-progress__copy">
        <p className="eyebrow">LOGIN</p>
        <h1>로그인 처리 중입니다</h1>
        <p>{message}</p>
      </div>
    </section>
  );
}
