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

function App() {

  const [mode, setMode] = React.useState<PaletteMode>('light');
  const defaultTheme = createTheme({palette: {mode}});

  const toggleColorMode = () => {
    setMode((prev) => (prev === 'dark' ? 'light' : 'dark'));
  };

  return (
      <BrowserRouter>
        <ThemeProvider theme={defaultTheme}>
          <CssBaseline/>
          <Routes>
            <Route
                path="/"
                element={
                  <div>
                    <AppAppBar mode={mode} toggleColorMode={toggleColorMode}/>
                    <MainPage/>
                    <Box sx={{bgcolor: 'background.default'}}>
                      <Footer/>
                    </Box>
                  </div>
                }
            />
            <Route path="/email-verify" element={<EmailVerifyPage/>}/>
            <Route path="*" element={<NotFoundPage/>}/>
          </Routes>
        </ThemeProvider>
      </BrowserRouter>
  );
}

export default App;
