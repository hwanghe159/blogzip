import React from 'react';
import './App.css';
import {BrowserRouter, Route, Routes} from "react-router-dom";
import NotFoundPage from "./components/NotFoundPage";
import NavBar from "./components/NavBar";
import ArticlesPage from "./components/ArticlesPage";
import {LoginPage} from "./components/LoginPage";
import MyPage from "./components/MyPage";
import {GoogleLoginPage} from "./components/GoogleLoginPage";

function App() {

  return (
      <BrowserRouter>
        <NavBar/>
        <Routes>
          <Route path="/" element={<ArticlesPage/>}/>
          <Route path="/login" element={<LoginPage/>}/>
          <Route path="/login/google" element={<GoogleLoginPage/>}/>
          <Route path="/my" element={<MyPage/>}/>
          <Route path="*" element={<NotFoundPage/>}/>
        </Routes>
      </BrowserRouter>
  );
}

export default App;
