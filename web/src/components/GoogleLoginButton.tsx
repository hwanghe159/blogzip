import React from 'react';
import Button from "@mui/material/Button";

export function handleLogin() {
  const queryParams = {
    client_id: '538244264177-f2a956r71n7g6cp1tomqfmcsvdu6nhcp.apps.googleusercontent.com',
    redirect_uri: process.env.REACT_APP_GOOGLE_LOGIN_REDIRECT_URI || '',
    response_type: 'code',
    scope: 'openid profile email',
    access_type: 'offline',
  };

  // API 설명 : https://developers.google.com/identity/protocols/oauth2/web-server#creatingclient
  window.location.href =
      `https://accounts.google.com/o/oauth2/v2/auth?${new URLSearchParams(queryParams).toString()}`
}

function GoogleLoginButton() {

  return (
      <Button
          onClick={handleLogin}
          size="large"
          disableRipple={true}
      >
        로그인
      </Button>
  );
}

export default GoogleLoginButton;