import { buildGoogleAuthUrl } from "../api";

export function handleLogin() {
  window.location.href = buildGoogleAuthUrl();
}

export default function GoogleLoginButton() {
  return (
    <button type="button" className="btn btn--primary" onClick={handleLogin}>
      Google로 로그인
    </button>
  );
}
