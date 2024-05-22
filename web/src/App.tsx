import React from 'react';
import './App.css';
import {BrowserRouter, Route, Routes} from "react-router-dom";
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
