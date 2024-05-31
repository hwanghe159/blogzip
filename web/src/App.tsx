import React from 'react';
import './App.css';
import {BrowserRouter, Route, Routes} from "react-router-dom";
import NotFoundPage from "./pages/NotFoundPage";
import NavBar from "./components/NavBar";
import MainPage from "./pages/MainPage";
import SubscriptionPage from "./pages/SubscriptionPage";
import {GoogleLoginPage} from "./pages/GoogleLoginPage";
import EmailPage from "./pages/EmailPage";

function App() {

  return (
      <BrowserRouter>
        <NavBar/>
        <Routes>
          <Route path="/" element={<MainPage/>}/>
          <Route path="/email" element={<EmailPage/>}/>
          <Route path="/subscription" element={<SubscriptionPage/>}/>

          <Route path="/login/google" element={<GoogleLoginPage/>}/>
          <Route path="*" element={<NotFoundPage/>}/>
        </Routes>
      </BrowserRouter>
  );
}

export default App;
