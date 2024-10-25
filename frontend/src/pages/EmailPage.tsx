import React, {useEffect, useState} from "react";
import {Api} from "../utils/Api";
import {useNavigate} from "react-router-dom";
import {getLoginUser} from "../utils/LoginUserHelper";
import {
  Card,
  CardActions,
  CardContent,
  Checkbox,
  FormControlLabel,
  FormGroup,
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
    {kor: '월요일', eng: 'MONDAY', isChecked: false},
    {kor: '화요일', eng: 'TUESDAY', isChecked: false},
    {kor: '수요일', eng: 'WEDNESDAY', isChecked: false},
    {kor: '목요일', eng: 'THURSDAY', isChecked: false},
    {kor: '금요일', eng: 'FRIDAY', isChecked: false},
    {kor: '토요일', eng: 'SATURDAY', isChecked: false},
    {kor: '일요일', eng: 'SUNDAY', isChecked: false},
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
      }, [navigate]
  )


  function handleSubmit() {
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
          alert(`수정되었습니다.\n${receiveDays.map(d => d.kor).join(', ')} 아침에만 메일을 보내드릴게요!`);
        }
      });
    } else {
      alert("로그인이 필요한 서비스입니다.")
      handleLogin()
      return
    }
  }

  function handleDayChange(eng: string) {
    setDaysOfWeek((prevDaysOfWeek) =>
        prevDaysOfWeek.map((day) =>
            day.eng === eng ? {...day, isChecked: !day.isChecked} : day
        )
    );
  }

  return (
      <Box sx={{maxWidth: 300, mx: 'auto', pt: 10}}>
        <Typography variant="h4" component="h1" gutterBottom>
          이메일 설정
        </Typography>
        <Card sx={{p: 2}}>
          <CardContent>
            <form onSubmit={handleSubmit}>
              <Box sx={{display: 'flex', flexDirection: 'column', gap: 2}}>
                <Box>
                  <TextField
                      id="email"
                      type="email"
                      value={user?.email || ''}
                      fullWidth
                      disabled
                      InputProps={{
                        readOnly: true,
                      }}
                  />
                </Box>

                <Box sx={{mt: 3}}>
                  <Typography variant="subtitle1">이메일을 받고 싶은 요일을 선택해주세요.</Typography>
                  <FormGroup sx={{mt: 0, display: 'grid', gridTemplateColumns: '1fr 1fr'}}>
                    {daysOfWeek.map((day) => (
                        <FormControlLabel
                            key={day.kor}
                            control={
                              <Checkbox
                                  id={day.kor}
                                  checked={day.isChecked}
                                  onChange={() => handleDayChange(day.eng)}
                              />
                            }
                            label={day.kor}
                        />
                    ))}
                  </FormGroup>
                </Box>

              </Box>
            </form>
          </CardContent>

          <CardActions>
            <Button type="submit" variant="contained" fullWidth onClick={handleSubmit}>
              수정
            </Button>
          </CardActions>
        </Card>
      </Box>
  )
}

export default EmailPage;