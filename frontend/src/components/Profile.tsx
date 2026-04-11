import React, { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getLoginUser, removeLoginUser } from '../utils/LoginUserHelper';

function Profile() {
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    const onClickOutside = (event: MouseEvent) => {
      if (!containerRef.current) {
        return;
      }

      if (!containerRef.current.contains(event.target as Node)) {
        setOpen(false);
      }
    };

    if (open) {
      document.addEventListener('mousedown', onClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', onClickOutside);
    };
  }, [open]);

  const menus: Array<{ name: string; onClick: () => void }> = [
    {
      name: '나중에 읽기',
      onClick: () => navigate('/bookmark'),
    },
    {
      name: '구독 설정',
      onClick: () => navigate('/subscription'),
    },
    {
      name: '이메일 설정',
      onClick: () => navigate('/email'),
    },
    {
      name: '로그아웃',
      onClick: () => {
        removeLoginUser();
        window.location.href = '/';
      },
    },
  ];

  const image = getLoginUser()?.image;

  return (
    <div ref={containerRef} style={{ position: 'relative' }}>
      <button
        type="button"
        className="avatar-button"
        aria-label="프로필 메뉴"
        onClick={() => setOpen((prev) => !prev)}
      >
        {image ? <img src={image} alt="프로필" /> : <span className="avatar-fallback">ME</span>}
      </button>

      {open ? (
        <div className="dropdown-menu">
          {menus.map((menu) => (
            <button
              key={menu.name}
              type="button"
              className="dropdown-item"
              onClick={() => {
                setOpen(false);
                menu.onClick();
              }}
            >
              {menu.name}
            </button>
          ))}
        </div>
      ) : null}
    </div>
  );
}

export default Profile;
