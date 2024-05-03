import React from 'react';
import Button from "@mui/material/Button";

function GoogleLoginButton({text, buttonProps}: any) {

  function handleLogin() {
    const queryParams = {
      client_id: '538244264177-f2a956r71n7g6cp1tomqfmcsvdu6nhcp.apps.googleusercontent.com',
      redirect_uri: 'http://localhost:3000/login/google',
      response_type: 'code',
      scope: 'email profile',
      // access_type: 'offline',
      // state: 'state_parameter_passthrough_value',
      // prompt: 'consent',
    };
    window.location.href =
        `https://accounts.google.com/o/oauth2/v2/auth?${new URLSearchParams(queryParams).toString()}`
  }

  return (
      <Button
          {...buttonProps}
          onClick={handleLogin}
      >
        {text}
      </Button>
  );
}

export default GoogleLoginButton;