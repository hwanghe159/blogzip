import React, { useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { Api } from '../utils/Api';

function RedirectPage() {
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    const searchParams = new URLSearchParams(location.search);
    const url = searchParams.get('url');
    const userId = searchParams.get('userId');

    if (!url || !userId) {
      alert('잘못된 요청입니다.');
      navigate('/');
      return;
    }

    Api.post(`/api/v1/user/${userId}/read`, { articleUrl: url }, {})
      .onSuccess(() => {})
      .on4XX(() => {})
      .on5XX(() => {});

    window.location.href = url;
  }, [location.search, navigate]);

  return (
    <section className="center-screen">
      <div className="stack" style={{ justifyItems: 'center' }}>
        <span className="dot-loader" />
        <p className="page-subtitle">잠시만요. 원문으로 이동하고 있어요...</p>
      </div>
    </section>
  );
}

export default RedirectPage;
