import React, { useEffect, useState } from 'react';
import { NavLink, useLocation, useNavigate } from 'react-router-dom';
import GoogleLoginButton, { handleLogin } from './GoogleLoginButton';
import Profile from './Profile';
import {
  getLoginUser,
  LoginUser,
  subscribeLoginUserChange,
} from '../utils/LoginUserHelper';

const navItems = [
  { to: '/', label: '홈' },
  { to: '/subscription', label: '구독 관리' },
  { to: '/bookmark', label: '나중에 읽기' },
  { to: '/email', label: '이메일 설정' },
];

function NavBar() {
  const navigate = useNavigate();
  const location = useLocation();
  const [loginUser, setLoginUser] = useState<LoginUser | null>(getLoginUser());

  useEffect(() => {
    const unsubscribe = subscribeLoginUserChange(() => {
      setLoginUser(getLoginUser());
    });

    return unsubscribe;
  }, []);

  return (
    <header className="top-nav">
      <div className="top-nav__inner">
        <div className="brand" onClick={() => navigate('/')}>
          <p className="brand__title">Blogzip</p>
          <p className="brand__subtitle">Daily Curated Digest</p>
        </div>

        <nav className="nav-links" aria-label="주요 메뉴">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `nav-link ${isActive || location.pathname.startsWith(item.to + '/') ? 'active' : ''}`
              }
              end={item.to === '/'}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="nav-actions">
          {!loginUser ? (
            <>
              <GoogleLoginButton />
              <button type="button" className="btn btn-primary" onClick={handleLogin}>
                구독 시작하기
              </button>
            </>
          ) : (
            <Profile />
          )}
        </div>
      </div>
    </header>
  );
}

export default NavBar;
