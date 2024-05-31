import {useEffect, useState} from "react";
import {Api} from "../utils/Api";
import {useNavigate} from "react-router-dom";
import {getLoginUser} from "../utils/LoginUserHelper";
import {handleLogin} from "../components/GoogleLoginButton";
import {BlogList} from "../components/BlogList";
import {BlogResponse} from "./MainPage";
import Typography from "@mui/material/Typography";
import styled from "styled-components";
import Box from "@mui/material/Box";
import {CircularProgress, List, ListItem, ListItemText, TextField} from "@mui/material";
import SearchIcon from '@mui/icons-material/Search';
import * as React from "react";
import Button from "@mui/material/Button";

interface Subscription {
  id: number;
  blog: BlogResponse;
}

const Container = styled.nav`
  width: 100%;
  max-width: 900px;
  margin-top: 50px;
  margin-left: auto;
  margin-right: auto;

  @media screen and (max-width: 900px) {
    width: calc(100% - 20px);
    max-width: none;
  }
`;

function SubscriptionPage() {
  const [subscriptions, setSubscriptions] = useState<Subscription[]>([])
  const navigate = useNavigate();

  useEffect(() => {
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
  }, [navigate])

  const [query, setQuery] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [blogs, setBlogs] = useState<BlogResponse[]>([]);

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

  return (
      <Container>
        <Box sx={{width: '100%', maxWidth: 600, margin: 'auto', textAlign: 'center', mt: 4}}>
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
                  <ListItemText primary={blog.name} secondary={blog.url}/>
                </ListItem>
            ))}
          </List>
        </Box>
        <Typography variant="h4">내가 구독한 블로그</Typography>
        <BlogList blogs={subscriptions.map(s => s.blog)}/>
      </Container>
  )
}

export default SubscriptionPage;