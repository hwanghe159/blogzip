import React from 'react';
import './App.css';
import {PaletteMode} from '@mui/material';
import CssBaseline from '@mui/material/CssBaseline';
import Box from '@mui/material/Box';
import {ThemeProvider, createTheme} from '@mui/material/styles';
import AppAppBar from './components/AppAppBar';
import MainPage from './components/MainPage';
import Footer from './components/Footer';
import {BrowserRouter, Route, Routes} from "react-router-dom";
import EmailVerifyPage from "./components/EmailVerifyPage";
import NotFoundPage from "./components/NotFoundPage";
import NavBar from "./components/NavBar";
import ArticlesPage from "./components/ArticlesPage";

function App() {

  return (
      <BrowserRouter>
        <NavBar/>
        <Routes>
          <Route path="/" element={<ArticlesPage/>}/>
          <Route path="*" element={<NotFoundPage/>}/>
        </Routes>
      </BrowserRouter>
  );
}

export default App;
