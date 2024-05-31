import * as React from "react";
import {useEffect} from "react";
import {Api} from "../utils/Api";
import {useLocation, useNavigate} from "react-router-dom";
import Typography from "@mui/material/Typography";
import Box from "@mui/material/Box";
import {setLoginUser} from "../utils/LoginUserHelper";

export function GoogleLoginPage() {

  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const code = searchParams.get('code');
  const navigate = useNavigate();

  useEffect(() => {
    if (code == null) {
      alert("구글 로그인에 실패했습니다.")
      navigate("/")
      return
    }
    Api.get(`/api/v1/login/google?code=${code}`)
    .onSuccess((response) => {
      setLoginUser({
        id: response.data.id,
        accessToken: response.data.accessToken,
        email: response.data.email,
        image: response.data.image,
      })
      window.location.href = '/';
    })
  }, [code, navigate, location]);

  return (
      <Box
          display="flex"
          flexDirection="column"
          justifyContent="center"
          alignItems="center"
          minHeight="100vh"
      >
        <Typography component="h6" variant="h6">
          리다이렉트 중..
        </Typography>
      </Box>
  )
}