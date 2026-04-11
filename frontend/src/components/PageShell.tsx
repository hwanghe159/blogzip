import { ReactNode, useEffect, useState } from "react";
import { Link, NavLink } from "react-router-dom";
import { SessionState } from "../types";
import GoogleLoginButton from "./GoogleLoginButton";

type PageShellProps = {
  session: SessionState;
  onLogout: () => void;
  children: ReactNode;
};

const navItems = [
  { to: "/", label: "피드" },
  { to: "/read-later", label: "나중에 읽기" },
  { to: "/settings", label: "구독 설정" },
];

export function PageShell({ session, onLogout, children }: PageShellProps) {
  const [compactTopbar, setCompactTopbar] = useState(false);

  useEffect(() => {
    const onScroll = () => {
      setCompactTopbar(window.scrollY > 20);
    };
    onScroll();
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => {
      window.removeEventListener("scroll", onScroll);
    };
  }, []);

  return (
    <div className="app-shell">
      <header className={compactTopbar ? "topbar topbar--compact" : "topbar"}>
        <div className="topbar__brand">
          <Link to="/" className="topbar__home-link">
            <span className="topbar__logo">blogzip</span>
            <p className="topbar__tagline">구독한 블로그를 한곳에서 읽고, 메일로 요약본을 받아보세요.</p>
          </Link>
        </div>
        <nav className="topbar__nav desktop-only">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) => (isActive ? "nav-pill nav-pill--active" : "nav-pill")}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="topbar__auth">
          {session.status === "authenticated" && session.user ? (
            <div className="user-chip">
              <Link to="/account" className="user-chip__profile-link" aria-label="계정으로 이동">
                {session.profileImage ? (
                  <img src={session.profileImage} alt="프로필" className="user-chip__avatar" />
                ) : (
                  <div className="user-chip__avatar user-chip__avatar--fallback">
                    {session.user.email.slice(0, 1).toUpperCase()}
                  </div>
                )}
                <div className="user-chip__meta">
                  <span className="user-chip__label">로그인됨</span>
                  <strong>{session.user.email}</strong>
                </div>
              </Link>
              <button type="button" onClick={onLogout} className="btn btn--ghost btn--small">
                로그아웃
              </button>
            </div>
          ) : (
            <GoogleLoginButton />
          )}
        </div>
      </header>

      <main className="content">{children}</main>

      <nav className="mobile-nav">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) => (isActive ? "mobile-nav__item mobile-nav__item--active" : "mobile-nav__item")}
          >
            {item.label}
          </NavLink>
        ))}
      </nav>
    </div>
  );
}
