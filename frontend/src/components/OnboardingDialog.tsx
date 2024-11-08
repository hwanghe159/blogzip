import React, {useState, useEffect} from 'react';
import {
  Button,
  TextField,
  Card,
  CardContent,
  CardActions,
  Typography,
  LinearProgress,
  Box,
  Alert,
  AlertTitle, Dialog, FormGroup, FormControlLabel, Checkbox, IconButton, Slide
} from '@mui/material';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import {Api} from "../utils/Api";
import {getLoginUser} from "../utils/LoginUserHelper";
import {handleLogin} from "./GoogleLoginButton";

interface OnboardingDialogProps {
  onClose: () => void; // 다이얼로그를 닫는 함수 prop
}

interface Keyword {
  id: number,
  name: string,
  isChecked: boolean,
}

enum Step {
  SELECT_KEYWORDS,
  RECOMMEND_BLOGS,
  FINAL,
}

// todo
export default function OnboardingDialog({onClose}: OnboardingDialogProps) {

  const [step, setStep] = useState<Step>(Step.SELECT_KEYWORDS)
  const [checked, setChecked] = useState<boolean>(true);
  const [slideDirection, setSlideDirection] = useState<string>('right');
  const [keywords, setKeywords] = useState<Keyword[]>([
    {id: 1, name: "백엔드", isChecked: false},
    {id: 2, name: "프론트엔드", isChecked: false},
    {id: 3, name: "AI", isChecked: false},
    {id: 4, name: "DevOps", isChecked: false},
    {id: 5, name: "디자인", isChecked: false},
  ])

  function handleCheck(id: number) {
    setKeywords((prevKeyword) =>
        prevKeyword.map((keyword) =>
            keyword.id === id ? {...keyword, isChecked: !keyword.isChecked} : keyword
        )
    );
  }

  function handleNext() {
    if (step === Step.SELECT_KEYWORDS) {
      setStep(Step.RECOMMEND_BLOGS)
    } else if (step === Step.RECOMMEND_BLOGS) {
      setStep(Step.FINAL)
    } else {
      onClose()
    }
  }

  return (
      <Box sx={{width: '90%', maxWidth: 500, mx: 'auto', my: 3}}>
        {step === Step.SELECT_KEYWORDS && (
            <>
              <Typography variant="h5" component="h1" gutterBottom>
                관심있는 키워드들을 모두 골라주세요
              </Typography>
              <FormGroup sx={{mt: 0, display: 'grid', gridTemplateColumns: '1fr 1fr'}}>
                {keywords.map((keyword) => (
                    <FormControlLabel
                        key={keyword.id}
                        control={
                          <Checkbox
                              id={keyword.name}
                              checked={keyword.isChecked}
                              onChange={() => handleCheck(keyword.id)}
                          />
                        }
                        label={keyword.name}
                    />
                ))}
              </FormGroup>
              <Button fullWidth={true} onClick={handleNext}>다음</Button>
            </>
        )}
        {step === Step.RECOMMEND_BLOGS && (
            <>
              <Typography variant="h5" component="h1" gutterBottom>
                블로그 추천
              </Typography>
              <Button fullWidth={true} onClick={handleNext}>다음</Button>
            </>
        )}
        {step === Step.FINAL && (
            <>
              <Typography variant="h5" component="h1" gutterBottom>
                최종
              </Typography>
              <Button fullWidth={true} onClick={handleNext}>다음</Button>
            </>
        )}
      </Box>
  );
}
