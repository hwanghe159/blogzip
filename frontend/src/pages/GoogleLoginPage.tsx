import React, { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Api } from '../utils/Api';
import { setLoginUser } from '../utils/LoginUserHelper';
import { LoginResponse } from '../types';

export function GoogleLoginPage() {
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    const searchParams = new URLSearchParams(location.search);
    const code = searchParams.get('code');

    if (!code) {
      alert('구글 로그인에 실패했습니다.');
      navigate('/');
      return;
    }

    Api.get('/api/v1/login/google', {
      params: {
        code,
      },
    })
      .onSuccess((response) => {
        const loginResponse = response.data as LoginResponse;
        setLoginUser({
          id: loginResponse.id,
          accessToken: loginResponse.accessToken,
          email: loginResponse.email,
          image: loginResponse.image,
        });
        window.location.href = '/';
      })
      .on4XX(() => {
        alert('구글 로그인에 실패했습니다.');
        navigate('/');
      })
      .on5XX(() => {
        alert('구글 로그인 중 오류가 발생했습니다.');
        navigate('/');
      });
  }, [location.search, navigate]);

  return (
    <section className="center-screen">
      <div className="stack" style={{ justifyItems: 'center' }}>
        <span className="dot-loader" />
        <p className="page-subtitle">로그인 처리 중입니다...</p>
      </div>
    </section>
  );
}
