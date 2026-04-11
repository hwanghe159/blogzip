import React from 'react';
import { Link } from 'react-router-dom';

function NotFoundPage() {
  return (
    <section className="center-screen">
      <div className="surface-strong panel stack" style={{ maxWidth: 560, width: '100%', textAlign: 'center' }}>
        <h1 className="page-title">404</h1>
        <p className="page-subtitle">요청하신 페이지를 찾을 수 없습니다.</p>
        <Link to="/" className="btn btn-primary" style={{ width: 'fit-content', margin: '0 auto' }}>
          홈으로 돌아가기
        </Link>
      </div>
    </section>
  );
}

export default NotFoundPage;
