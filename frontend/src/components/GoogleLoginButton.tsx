export function handleLogin() {
  const queryParams = {
    client_id:
      process.env.REACT_APP_GOOGLE_CLIENT_ID ||
      "538244264177-f2a956r71n7g6cp1tomqfmcsvdu6nhcp.apps.googleusercontent.com",
    redirect_uri: process.env.REACT_APP_GOOGLE_LOGIN_REDIRECT_URI || "",
    response_type: "code",
    scope: "openid profile email",
    access_type: "offline",
  };

  window.location.href = `https://accounts.google.com/o/oauth2/v2/auth?${new URLSearchParams(
    queryParams
  ).toString()}`;
}

export default function GoogleLoginButton() {
  return (
    <button type="button" className="btn btn--primary" onClick={handleLogin}>
      Google로 로그인
    </button>
  );
}
