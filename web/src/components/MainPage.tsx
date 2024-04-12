import * as React from 'react';
import {alpha} from '@mui/material';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Container from '@mui/material/Container';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import Typography from '@mui/material/Typography';
import {useState} from "react";
import {Api} from '../utils/Api';

export default function MainPage() {

  const [email, setEmail] = useState('');
  const [isSending, setIsSending] = useState(false);
  const [isEmpty, setIsEmpty] = useState(false);

  const sendVerificationEmail = async (event: React.FormEvent) => {
    event.preventDefault();

    if (isSending) {
      return
    }
    if (email.trim() === '') {
      setIsEmpty(true);
      return
    }

    setIsSending(true);
    setIsEmpty(false);

    Api.post('/api/v1/user', {email: email.trim()})
    .onSuccess((response) => {
      alert(`${email}로 인증 메일을 보냈습니다. 메일함을 확인해주세요.`)
    })
    .on4XX((response) => {
      alert(response.message)
    })
    .on5XX((response) => {
      alert(`요청에 실패했습니다. 잠시 후 다시 시도해주세요.`)
    });

    setIsSending(false);
  };

  return (
      <Box
          id="mainPage"
          sx={(theme) => ({
            width: '100%',
            backgroundImage:
                theme.palette.mode === 'light'
                    ? 'linear-gradient(180deg, #CEE5FD, #FFF)'
                    : `linear-gradient(#02294F, ${alpha('#090E10', 0.0)})`,
          })}
      >
        <Container
            sx={{
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              pt: {xs: 14, sm: 20},
              pb: {xs: 8, sm: 12},
            }}
        >
          <Stack spacing={2} useFlexGap sx={{width: {xs: '100%', sm: '70%'}}}>
            <Typography
                variant="h1"
                sx={{
                  display: 'flex',
                  flexDirection: {xs: 'column', md: 'row'},
                  alignSelf: 'center',
                  textAlign: 'center',
                  fontSize: 'clamp(3.5rem, 10vw, 4rem)',
                }}
            >
              blogzip
            </Typography>
            <Typography
                textAlign="center"
                color="text.secondary"
                sx={{alignSelf: 'center', width: {sm: '100%', md: '80%'}}}
            >
              원하는 블로그만 구독하세요. 매일 아침 AI가 요약해서 메일로 전달드릴게요.
            </Typography>
            <form onSubmit={sendVerificationEmail}>
              <Stack
                  direction={{xs: 'row', sm: 'row'}}
                  justifyContent="center"
                  spacing={1}
                  useFlexGap
                  sx={{pt: 2, width: {xs: '100%', sm: 'auto'}}}
              >
                <TextField
                    id="outlined-basic"
                    hiddenLabel
                    size="small"
                    variant="outlined"
                    aria-label="Enter your email address"
                    placeholder="이메일 주소"
                    error={isEmpty}
                    helperText={isEmpty ? "이메일 주소를 입력해주세요." : ""}
                    inputProps={{
                      autoComplete: 'off',
                    }}
                    onChange={(e) => {
                      setEmail(e.target.value)
                      if (e.target.value) setIsEmpty(false);
                    }}
                    onKeyPress={(e) => {
                      if (e.key === 'Enter') {
                        sendVerificationEmail(e);
                      }
                    }}
                />
                <Box sx={{height: '100%'}}>
                  <Button variant="contained" color="primary" type="submit" disabled={isSending}>
                    {isSending ? "진행중.." : "구독하기"}
                  </Button>
                </Box>
              </Stack>
            </form>
          </Stack>
        </Container>
      </Box>
  );
}
