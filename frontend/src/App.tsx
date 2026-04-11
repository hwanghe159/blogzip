import React from 'react';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import NotFoundPage from './pages/NotFoundPage';
import NavBar from './components/NavBar';
import MainPage from './pages/MainPage';
import SubscriptionPage from './pages/SubscriptionPage';
import { GoogleLoginPage } from './pages/GoogleLoginPage';
import EmailPage from './pages/EmailPage';
import BookmarkPage from './pages/ReadLaterPage';
import RedirectPage from './pages/RedirectPage';

function App() {
  return (
    <BrowserRouter>
      <div className="app-shell">
        <NavBar />
        <main className="page-wrap">
          <div className="app-container fade-in">
            <Routes>
              <Route path="/" element={<MainPage />} />
              <Route path="/email" element={<EmailPage />} />
              <Route path="/subscription" element={<SubscriptionPage />} />
              <Route path="/bookmark" element={<BookmarkPage />} />
              <Route path="/redirect" element={<RedirectPage />} />
              <Route path="/login/google" element={<GoogleLoginPage />} />
              <Route path="*" element={<NotFoundPage />} />
            </Routes>
          </div>
        </main>
      </div>
    </BrowserRouter>
  );
}

export default App;
