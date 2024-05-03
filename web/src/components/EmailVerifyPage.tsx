import React, {useEffect, useState} from 'react';
import {useLocation, useNavigate} from 'react-router-dom';
import {Api} from "../utils/Api";
import Box from "@mui/material/Box";
import Typography from "@mui/material/Typography";
import GoogleLoginButton from "./GoogleLoginButton";

function EmailVerifyPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const searchParams = new URLSearchParams(location.search);
  const address = searchParams.get('address');
  const code = searchParams.get('code');
  const [message, setMessage] = useState("이메일 인증 중...")
  const [showDetails, setShowDetails] = useState(false)

  useEffect(() => {
    if (address == null || code == null) {
      setMessage("인증에 실패했습니다.");
      return
    }

    Api.get(`/api/v1/email/${address}/verify/${code}`)
    .onSuccess((response) => {
      setMessage("인증 완료! 내일부터 이메일을 보내드릴게요.");
      setShowDetails(true)
    })
    .on4XX((response) => {
      if (response.code === "ALREADY_VERIFIED") {
        setMessage("이미 인증이 완료된 이메일입니다.");
      } else {
        setMessage("인증에 실패했습니다.");
      }
    })
    .on5XX((response) => {
      setMessage("인증에 실패했습니다.");
    })
  }, [address, code, navigate]);

  return (
      <Box
          display="flex"
          flexDirection="column"
          justifyContent="center"
          alignItems="center"
          minHeight="100vh"
          sx={{
            px: 2,
            textAlign: 'center',
          }}
      >
        <Typography variant="h3">{message}</Typography>
        {showDetails && (
            <Box>
              <Typography variant="h5">
                좋아하실 만한 블로그들을 구독 목록에 포함시켜 드렸어요.
              </Typography>
              <GoogleLoginButton buttonProps={{
                color: "primary",
                variant: "contained",
                component: "a",
                sx: {
                  px: 3
                },
              }}
              text="구독 목록 편집하기"/>
            </Box>
        )}
      </Box>
  );
}

export default EmailVerifyPage;