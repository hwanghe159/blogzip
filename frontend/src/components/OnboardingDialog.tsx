import React, {useState, useEffect, ChangeEvent} from 'react';
import {
  Button,
  Typography,
  Box,
  FormControlLabel,
  Checkbox,
  DialogContent, List, ListItem, ListItemText, DialogActions, Grid
} from '@mui/material';
import {Api} from "../utils/Api";
import {getLoginUser} from "../utils/LoginUserHelper";

interface OnboardingDialogProps {
  onClose: () => void; // 다이얼로그를 닫는 함수 prop
}

interface Blog {
  id: number
  name: string
  url: string
  image: string
  isShowOnMain: boolean
  isChecked: boolean
}

interface DayOfWeek {
  kor: string,
  eng: string,
  isChecked: boolean,
}

enum Step {
  SUBSCRIPTION_SETTING,
  EMAIL_SETTING,
}

export default function OnboardingDialog({onClose}: OnboardingDialogProps) {

  const [step, setStep] = useState<Step>(Step.SUBSCRIPTION_SETTING)

  const [blogs, setBlogs] = useState<Blog[]>([])
  const [daysOfWeek, setDaysOfWeek] = useState<DayOfWeek[]>([
    {kor: '월', eng: 'MONDAY', isChecked: false},
    {kor: '화', eng: 'TUESDAY', isChecked: false},
    {kor: '수', eng: 'WEDNESDAY', isChecked: false},
    {kor: '목', eng: 'THURSDAY', isChecked: false},
    {kor: '금', eng: 'FRIDAY', isChecked: false},
    {kor: '토', eng: 'SATURDAY', isChecked: false},
    {kor: '일', eng: 'SUNDAY', isChecked: false},
  ]);


  useEffect(() => {
        Api.get(`/api/v1/blog`)
        .onSuccess(response => {
          const blogs: Blog[] = response.data
          .filter((blog: { isShowOnMain: boolean }) => blog.isShowOnMain)
          .map((blog: Blog) => ({
            id: blog.id,
            name: blog.name,
            url: blog.url,
            image: blog.image,
            isShowOnMain: blog.isShowOnMain,
            isChecked: false,
          }))
          setBlogs(blogs)
        });
      }, []
  )

  function handleNext() {
    if (step === Step.SUBSCRIPTION_SETTING) {
      setStep(Step.EMAIL_SETTING)
    } else if (step === Step.EMAIL_SETTING) {
      window.location.reload()
    }
  }

  function handleSelectAll(event: ChangeEvent<HTMLInputElement>) {
    setBlogs(blogs.map(blog => ({...blog, isChecked: event.target.checked})))
  }

  function handleSelectBlog(blogId: number) {
    setBlogs(blogs.map(blog =>
        blog.id === blogId ? {...blog, isChecked: !blog.isChecked} : blog
    ))
  }

  function handleSubscribe() {
    const selectedBlogIds = blogs.filter(blog => blog.isChecked).map(blog => blog.id)
    selectedBlogIds.forEach(blogId => {
      Api.post(`/api/v1/subscription`, {
            blogId: blogId
          },
          {
            headers: {
              Authorization: `Bearer ${getLoginUser()?.accessToken}`,
            }
          })
    })
    handleNext()
  }

  function handleDayChange(eng: string) {
    setDaysOfWeek((prevDaysOfWeek) =>
        prevDaysOfWeek.map((day) =>
            day.eng === eng ? {...day, isChecked: !day.isChecked} : day
        )
    );
  }

  function handleEmailSetting(daysOfWeek: DayOfWeek[]) {
    Api.put(`/api/v1/me`, {
          receiveDays: daysOfWeek.map(d => d.eng)
        },
        {
          headers: {
            Authorization: `Bearer ${getLoginUser()?.accessToken}`,
          }
        })
    handleNext()
  }

  return (
      <Box sx={{width: '90%', maxWidth: 500, mx: 'auto', my: 3}}>
        {step === Step.SUBSCRIPTION_SETTING && (
            <>
              <Typography>아직 구독한 블로그가 없네요!</Typography>
              <Typography variant="h4" component="h4" gutterBottom>구독할 블로그를 추가해주세요.</Typography>
              <DialogContent sx={{p: 1}}>
                <Box sx={{mb: 2}}>
                  <FormControlLabel
                      control={
                        <Checkbox
                            checked={blogs.length > 0 && blogs.every(blog => blog.isChecked)}
                            onChange={handleSelectAll}
                            color="primary"
                        />
                      }
                      label="전체 선택"
                  />
                </Box>
                <List sx={{
                  width: '100%',
                  maxHeight: 400,
                  overflow: 'auto'
                }}>
                  {blogs.map((blog) => (
                      <ListItem
                          key={blog.id} alignItems="flex-start" divider
                          onClick={() => handleSelectBlog(blog.id)}
                          sx={{
                            gap: 2,
                            alignItems: "center",
                            px: 0
                          }}>
                        <Checkbox
                            checked={blog.isChecked}
                            onChange={() => handleSelectBlog(blog.id)}
                            color="primary"
                        />
                        <Box
                            sx={{
                              display: {xs: 'none', sm: 'flex'},
                              width: 120,
                              height: 80,
                              minWidth: 120,
                              borderRadius: 1,
                              overflow: 'hidden',
                              justifyContent: 'center',
                              alignItems: 'center',
                              backgroundColor: 'grey.200',
                            }}
                        >
                          <Box
                              component="img"
                              src={blog.image ?? "/default_blog_image.png"}
                              alt={`${blog.name} 대표 이미지`}
                              onError={(e) => {
                                e.currentTarget.src = "/default_blog_image.png";
                              }}
                              sx={{
                                width: '100%',
                                height: '100%',
                                objectFit: 'cover',
                              }}
                          />
                        </Box>
                        <ListItemText
                            primary={blog.name}
                            secondary={
                              <Typography
                                  component="div"
                                  variant="body2"
                                  color="text.secondary"
                                  sx={{wordWrap: "break-word"}}
                              >
                                {blog.url}
                              </Typography>
                            }
                        />
                      </ListItem>
                  ))}
                </List>
              </DialogContent>
              <DialogActions>
                <Button
                    onClick={handleSubscribe}
                    color="primary"
                    variant="contained"
                    disabled={!blogs.some(blog => blog.isChecked)}
                >
                  다음
                </Button>
              </DialogActions>
            </>
        )}
        {step === Step.EMAIL_SETTING && (
            <>
              <Typography variant="h4" component="h4" gutterBottom mt={3}>
                언제 이메일을 보내드릴까요?
              </Typography>
              <Typography>아침 9시에 보내드려요.</Typography>
              <DialogContent>
                <Grid container spacing={2} justifyContent="center" sx={{mt: 2}}>
                  {daysOfWeek.map((day) => (
                      <Grid item key={day.eng} xs={4} sm={3}>
                        <Button
                            variant={daysOfWeek.filter(day => day.isChecked).includes(day) ? "contained" : "outlined"}
                            color="primary"
                            onClick={() => handleDayChange(day.eng)}
                            fullWidth
                            sx={{
                              height: 64,
                              fontSize: '1.2rem',
                            }}
                        >
                          {day.kor}
                        </Button>
                      </Grid>
                  ))}
                </Grid>
              </DialogContent>
              <DialogActions sx={{
                display: "flex",
                flexDirection: "row",
                justifyContent: "space-between"
              }}>
                <Button onClick={() => handleEmailSetting([])}
                        color="primary">
                  안 받을래요
                </Button>
                <Button
                    onClick={() => handleEmailSetting(daysOfWeek.filter(d => d.isChecked))}
                    color="primary"
                    variant="contained"
                    disabled={daysOfWeek.filter(day => day.isChecked).length === 0}
                >
                  확인
                </Button>
              </DialogActions>
            </>
        )}
      </Box>
  );
}
