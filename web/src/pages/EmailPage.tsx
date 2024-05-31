import React, {useEffect, useState} from "react";
import {Api} from "../utils/Api";
import {useNavigate} from "react-router-dom";
import {getLoginUser} from "../utils/LoginUserHelper";
import {
  Checkbox,
  Container,
  FormControlLabel, FormGroup,
  Grid,
  TextField
} from "@mui/material";
import Box from "@mui/material/Box";
import Typography from "@mui/material/Typography";
import Button from "@mui/material/Button";
import {handleLogin} from "../components/GoogleLoginButton";

export interface User {
  id: number;
  email: string,
  receiveDays: string[],
  createdAt: string,
  updatedAt: string,
}

function EmailPage() {

  const [user, setUser] = useState<User | null>(null);
  const [daysOfWeek, setDaysOfWeek] = useState([
    {kor: '월', eng: 'MONDAY', isChecked: false},
    {kor: '화', eng: 'TUESDAY', isChecked: false},
    {kor: '수', eng: 'WEDNESDAY', isChecked: false},
    {kor: '목', eng: 'THURSDAY', isChecked: false},
    {kor: '금', eng: 'FRIDAY', isChecked: false},
    {kor: '토', eng: 'SATURDAY', isChecked: false},
    {kor: '일', eng: 'SUNDAY', isChecked: false},
  ]);
  const navigate = useNavigate();

  useEffect(() => {
        const accessToken = getLoginUser()?.accessToken
        if (accessToken) {
          Api.get(`/api/v1/me`, {
            headers: {
              Authorization: `Bearer ${accessToken}`,
            }
          })
          .onSuccess((response) => {
            setUser(response.data)
            setDaysOfWeek(daysOfWeek.map((day) => ({
              ...day,
              isChecked: response.data.receiveDays.includes(day.eng),
            })));
          })
        } else {
          alert("로그인이 필요한 서비스입니다.")
          handleLogin()
          return
        }
      }
      ,
      [navigate]
  )

  function handleCheckboxChange(eng: string) {
    setDaysOfWeek((prevDaysOfWeek) =>
        prevDaysOfWeek.map((day) =>
            day.eng === eng ? {...day, isChecked: !day.isChecked} : day
        )
    );
  }

  function updateSettings() {
    const receiveDays = daysOfWeek
    .filter(d => d.isChecked)
    const accessToken = getLoginUser()?.accessToken
    if (accessToken) {
      Api.put(`/api/v1/me`, {
            receiveDays: receiveDays.map(d => d.eng)
          },
          {
            headers: {
              Authorization: `Bearer ${accessToken}`,
            }
          })
      .onSuccess(response => {
        if (receiveDays.length === 0) {
          alert(`수신거부 처리되었습니다.`);
        } else if (receiveDays.length === 7) {
          alert(`수정되었습니다.\n매일 아침 보내드릴게요!`);
        } else {
          alert(`수정되었습니다.\n${receiveDays.map(d => d.kor).join(', ')}요일 아침에만 메일을 보내드릴게요!`);
        }
      });
    } else {
      alert("로그인이 필요한 서비스입니다.")
      handleLogin()
      return
    }
  }


  return (
      <Container component="main">
        <Box
            sx={{
              marginTop: 8,
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
            }}
        >
          <Typography component="h3" variant="h3">
            이메일 설정
          </Typography>
          <Typography component="h5" variant="h5">
            이메일 주소
          </Typography>
          <Box component="form" noValidate sx={{mt: 3}}>
            <Grid container spacing={2}>
              <Grid item xs={12}>
                <TextField
                    disabled
                    fullWidth
                    id="email"
                    name="email"
                    autoComplete="email"
                    value={user?.email || ''}
                />
              </Grid>
            </Grid>
            <Grid container justifyContent="flex-end">
              <Grid item>
              </Grid>
            </Grid>
          </Box>
          <Typography component="h5" variant="h5">
            요일 선택
          </Typography>
          <FormGroup aria-label="position" row>
            {daysOfWeek.map((dayOfWeek) => (
                <FormControlLabel
                    key={dayOfWeek.kor}
                    control={<Checkbox checked={dayOfWeek.isChecked}
                                       onChange={() => handleCheckboxChange(dayOfWeek.eng)}/>}
                    label={dayOfWeek.kor}
                />
            ))}
          </FormGroup>
        </Box>
        <Button
            type="submit"
            fullWidth
            variant="contained"
            sx={{mt: 3, mb: 2}}
            onClick={updateSettings}
        >
          수정
        </Button>
      </Container>
  )
}

export default EmailPage;