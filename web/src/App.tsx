import React, {useEffect, useState} from 'react';
import logo from './logo.svg';
import './App.css';
import axios from "axios";

function App() {

  const [message, setMessage] = useState([])

  useEffect(() => {
    Test();
  })

  function Test() {
    axios.get(`/api/v1/test`)
    .then((res) => {
      setMessage(res.data);
    })
    .catch((error) => {
      console.log("API call 실패")
    })
  }

  return (
    <div className="App">
      <header className="App-header">
        <img src={logo} className="App-logo" alt="logo" />
        <p>
          {message}
        </p>
      </header>
    </div>
  );
}

export default App;
