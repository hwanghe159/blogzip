import {Fragment, useEffect, useState} from "react";
import {Api} from "../utils/Api";
import {useNavigate} from "react-router-dom";
import {getLoginUser} from "../utils/LoginUserHelper";
import {handleLogin} from "../components/GoogleLoginButton";
import {BlogList} from "../components/BlogList";
import {BlogResponse} from "./MainPage";
import Typography from "@mui/material/Typography";
import styled from "styled-components";
import Box from "@mui/material/Box";
import {
  CircularProgress, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, Grid,
  List,
  ListItem,
  ListItemText,
  TextField, useMediaQuery, useTheme
} from "@mui/material";
import SearchIcon from '@mui/icons-material/Search';
import * as React from "react";
import Button from "@mui/material/Button";

interface Subscription {
  id: number;
  blog: BlogResponse;
}

function SubscriptionPage() {
  const [subscriptions, setSubscriptions] = useState<Subscription[]>([])
  const navigate = useNavigate();

  useEffect(() => {
    getMySubscriptions()
  }, [navigate])

  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const [open, setOpen] = useState(false);

  function handleClickOpen() {
    // todo 제거
    alert("준비중이에요!")
    return
    setOpen(true);
  };

  const handleClose = () => {
    setOpen(false);
  };

  const [query, setQuery] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [blogs, setBlogs] = useState<BlogResponse[]>([]);

  function getMySubscriptions() {
    const loginUser = getLoginUser()
    if (loginUser == null || !loginUser.accessToken) {
      alert("로그인이 필요한 서비스입니다.")
      handleLogin()
      return
    }

    Api.get(`/api/v1/subscription`, {
      headers: {
        Authorization: `Bearer ${loginUser.accessToken}`,
      }
    })
    .onSuccess((response) => {
      setSubscriptions(response.data)
    })
    .on4XX((response) => {
      if (response.code === 'LOGIN_FAILED') {
        alert("로그인이 필요한 서비스입니다.")
        handleLogin()
      }
    })
    .on5XX((response) => {
    })
  }

  function handleSearch() {
    const minLength = 2
    if (query.trim().length < minLength) {
      setErrorMessage(`${minLength}글자 이상 입력해주세요.`)
      return
    }
    setErrorMessage('')
    setLoading(true);

    Api.get(`/api/v1/blog/search?query=${query}`)
    .onSuccess((response) => {
      setBlogs(response.data)
      setLoading(false);
    })
    .on4XX((response) => {
      setLoading(false);
    })
    .on5XX((response) => {
      setLoading(false);
    })
  }

  const handleInputChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setQuery(event.target.value);
  };

  const handleKeyPress = (event: React.KeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'Enter') {
      handleSearch();
    }
  };

  function addBlog(event: React.KeyboardEvent<HTMLInputElement>) {
    if (event.key !== 'Enter') {
      return;
    }

    console.log(event)

    // Api.post(`/api/v1/blog`, {
    //       url: urlll
    //     },
    //     {
    //       headers: {
    //         Authorization: `Bearer ${getLoginUser()?.accessToken}`,
    //       }
    //     })
    // .onSuccess((response) => {
    //   handleClose();
    // })
    // .on4XX((response) => {
    //   handleClose();
    // })
    // .on5XX((response) => {
    //   handleClose();
    // })

  }

  function addSubscription(blogId: number) {
    Api.post(`/api/v1/subscription`, {
          blogId: blogId
        },
        {
          headers: {
            Authorization: `Bearer ${getLoginUser()?.accessToken}`,
          }
        })
    .onSuccess((response) => {
      getMySubscriptions();
    })
    .on4XX((response) => {
    })
    .on5XX((response) => {
    })
  }

  function cancelSubscription(blogId: number) {
    Api.delete(`/api/v1/subscription`, {
      headers: {
        Authorization: `Bearer ${getLoginUser()?.accessToken}`,
      },
      data: {
        blogId: blogId
      }
    })
    .onSuccess((response) => {
      setSubscriptions(subscriptions.filter(s => s.blog.id !== blogId));
    })
    .on4XX((response) => {
    })
    .on5XX((response) => {
    })
  }

  return (
      <Box
          sx={{
            width: '100%',
            maxWidth: isMobile ? 'none' : '1500px',
            marginTop: '50px',
            marginLeft: 'auto',
            marginRight: 'auto',
            display: 'flex',
            flexDirection: {md: 'row', xs: 'column'},
          }}>
        <Box
            sx={{
              m: 1
            }}>
          <Typography variant="h4">내가 구독한 블로그</Typography>
          <BlogList blogs={subscriptions.map(s => s.blog)}/>
        </Box>

        <Box sx={{
          width: {md: 100, xs: 0},
          height: {md: 0, xs: 100},
        }}/>

        <Box
            sx={{
              m: 1,
              width: {md: 600, xs: 'auto'}
            }}
        >
          <Typography variant="h4">구독 추가하기</Typography>
          <Box sx={{width: '100%', margin: 'auto', textAlign: 'center', mt: 4}}>
            <Box sx={{display: 'flex', alignItems: 'center', mb: 2}}>
              <TextField
                  error={errorMessage !== ''}
                  helperText={errorMessage}
                  fullWidth
                  label="블로그 이름 또는 URL"
                  value={query}
                  onChange={handleInputChange}
                  onKeyPress={handleKeyPress}
              />
              <Button
                  variant="contained"
                  color="primary"
                  onClick={handleSearch}
                  sx={{ml: 2, height: '56px'}}
              >
                {loading ? <CircularProgress size={24} color="inherit"/> : <SearchIcon/>}
              </Button>
            </Box>
            <List>
              {blogs.map((blog) => (
                  <ListItem key={blog.id} divider>
                    <Grid container alignItems="center">
                      <Grid item xs={10}>
                        <ListItemText primary={blog.name} secondary={blog.url}/>
                      </Grid>
                      <Grid>
                        {subscriptions.map(s => s.blog.id).includes(blog.id) ?
                            <Button variant="outlined" size={'small'} sx={{height: 40}}
                                    onClick={() => cancelSubscription(blog.id)}>구독중</Button> :
                            <Button variant="contained" size={'small'} sx={{height: 40}}
                                    onClick={() => addSubscription(blog.id)}>구독하기</Button>
                        }
                      </Grid>
                    </Grid>
                  </ListItem>
              ))}
            </List>
          </Box>
          <Fragment>
            <Button variant="outlined" onClick={handleClickOpen}>
              직접 추가하기
            </Button>
            <Dialog
                open={open}
                onClose={handleClose}
                fullWidth={true}
                PaperProps={{
                  component: 'form'
                }}
            >
              <DialogTitle>URL로 직접 추가</DialogTitle>
              <DialogContent>
                <TextField
                    autoFocus
                    required
                    margin="dense"
                    name="url"
                    fullWidth
                    variant="standard"
                    placeholder="https://blogzip.co.kr/posts"
                    onKeyPress={addBlog}
                />
              </DialogContent>
              <DialogActions>
                <Button onClick={handleClose}>취소</Button>
                <Button type="submit">추가</Button>
              </DialogActions>
            </Dialog>
          </Fragment>
        </Box>
      </Box>
  )
}

export default SubscriptionPage;