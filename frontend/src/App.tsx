import { useCallback, useEffect, useMemo, useState } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import {
  buildGoogleAuthUrl,
  clearAccessToken,
  getAccessToken,
  getMyProfile,
  getProfileImage,
  saveAccessToken,
  saveProfileImage,
} from "./api";
import { LoginResponse, SessionState, UserResponse } from "./types";
import { PageShell } from "./components/PageShell";
import { FeedPage } from "./pages/FeedPage";
import { ReadLaterPage } from "./pages/ReadLaterPage";
import { SettingsPage } from "./pages/SettingsPage";
import { GoogleLoginPage } from "./pages/GoogleLoginPage";

function toGuestSession(): SessionState {
  return {
    status: "guest",
    token: null,
    profileImage: null,
    user: null,
  };
}

export default function App() {
  const [session, setSession] = useState<SessionState>({
    status: "booting",
    token: null,
    profileImage: null,
    user: null,
  });

  const loginUrl = useMemo(() => buildGoogleAuthUrl(), []);

  const loadSession = useCallback(async (token: string, profileImage: string | null) => {
    try {
      const me = await getMyProfile(token);
      setSession({
        status: "authenticated",
        token,
        profileImage,
        user: me,
      });
    } catch (_) {
      clearAccessToken();
      setSession(toGuestSession());
    }
  }, []);

  useEffect(() => {
    const storedToken = getAccessToken();
    if (!storedToken) {
      setSession(toGuestSession());
      return;
    }
    const storedProfileImage = getProfileImage();
    loadSession(storedToken, storedProfileImage);
  }, [loadSession]);

  async function handleLoginSuccess(response: LoginResponse) {
    saveAccessToken(response.accessToken);
    saveProfileImage(response.image);
    await loadSession(response.accessToken, response.image);
  }

  function handleLogout() {
    clearAccessToken();
    setSession(toGuestSession());
  }

  const handleUserUpdated = useCallback((user: UserResponse) => {
    setSession((prev) => ({
      ...prev,
      user,
      status: prev.token ? "authenticated" : "guest",
    }));
  }, []);

  if (session.status === "booting") {
    return (
      <div className="boot-screen">
        <p>서비스를 준비하는 중입니다...</p>
      </div>
    );
  }

  return (
    <PageShell session={session} onLogout={handleLogout}>
      <Routes>
        <Route path="/" element={<FeedPage session={session} loginUrl={loginUrl} />} />
        <Route
          path="/read-later"
          element={<ReadLaterPage session={session} loginUrl={loginUrl} />}
        />
        <Route
          path="/settings"
          element={
            <SettingsPage
              session={session}
              loginUrl={loginUrl}
              onUserUpdated={handleUserUpdated}
            />
          }
        />
        <Route
          path="/login/google"
          element={<GoogleLoginPage onLoginSuccess={handleLoginSuccess} />}
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </PageShell>
  );
}
