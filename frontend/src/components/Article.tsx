import * as React from 'react';
import {Card, CardContent, CardMedia, Divider, IconButton} from "@mui/material";
import Box from "@mui/material/Box";
import Typography from "@mui/material/Typography";
import BookmarkBorderIcon from '@mui/icons-material/BookmarkBorder';
import BookmarkIcon from '@mui/icons-material/Bookmark';
import Tooltip from '@mui/material/Tooltip';
import {ArticleResponse} from "../pages/MainPage";
import {Api} from "../utils/Api";
import {getLoginUser} from "../utils/LoginUserHelper";
import {handleLogin} from "./GoogleLoginButton";

interface ArticleProps {
  article: ArticleResponse
}

function Article({article: initialArticle}: ArticleProps) {
  const [article, setArticle] = React.useState(initialArticle);


  function toOriginalUrl() {
    Api.post(`/api/v1/article/${article.id}/read`, {},
        {
          headers: {
            Authorization: `Bearer ${getLoginUser()?.accessToken}`,
          }
        })
    .onSuccess(response => {
    })
    .on4XX((response) => {
    })
    .on5XX((response) => {
    })
    window.open(article.url, '_blank');
  }

  const addReadLater = async (e: React.FormEvent) => {
    e.preventDefault();

    Api.post(`/api/v1/read-later`, {
          articleId: article.id
        },
        {
          headers: {
            Authorization: `Bearer ${getLoginUser()?.accessToken}`,
          }
        })
    .onSuccess((response) => {
      setArticle(prevArticle => ({...prevArticle, isReadLater: true}));
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

  const deleteReadLater = async (e: React.FormEvent) => {
    e.preventDefault();

    Api.delete(`/api/v1/read-later`, {
      headers: {
        Authorization: `Bearer ${getLoginUser()?.accessToken}`,
      },
      data: {
        articleId: article.id
      }
    })
    .onSuccess((response) => {
      setArticle(prevArticle => ({...prevArticle, isReadLater: false}));
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

  const blogImageUrl = article.blog.image ?? "/default_blog_image.png";

  return (
      <div>
        <Card
            sx={{
              boxShadow: 0,
              display: 'flex',
              flexDirection: {xs: 'column', md: 'row'},
              margin: 2,
              maxWidth: 800,
              cursor: 'pointer',
              position: 'relative'
            }}
            onClick={toOriginalUrl}
        >
          <Box sx={{
            position: 'relative',
            width: {xs: '100%', md: 200},
            height: {xs: '150px', md: 'auto'}
          }}>
            <CardMedia
                component="img"
                sx={{
                  width: '100%',
                  height: '100%',
                  objectFit: 'cover',
                }}
                image={blogImageUrl}
                alt={article.title}
            />
            <Typography
                component="div"
                sx={{
                  position: 'absolute',
                  bottom: 0,
                  left: 0,
                  width: '100%',
                  color: 'white',
                  backgroundColor: 'rgba(0, 0, 0, 0.6)',
                  paddingTop: '8px',
                  paddingBottom: '8px',
                  textAlign: 'center',
                }}
            >
              {article.blog.name}
            </Typography>
          </Box>
          <Box sx={{display: 'flex', flexDirection: 'column', flex: 1}}>
            <CardContent sx={{
              p: 2,
            }}>
              <Typography component="div" variant="h5">
                {article.title}
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{fontSize: '15px'}}>
                {article.summary}
              </Typography>
              <Box
                  sx={{
                    marginTop: 1,
                    textAlign: 'left'
                  }}
                  onClick={(e) => e.stopPropagation()}
              >
                {article.isReadLater ?
                    <Tooltip title="나중에 읽기 제거" arrow={true}>
                      <IconButton onClick={deleteReadLater} sx={{p: 0}}>
                        <BookmarkIcon/>
                      </IconButton>
                    </Tooltip> :
                    <Tooltip title="나중에 읽기" arrow={true}>
                      <IconButton onClick={addReadLater} sx={{p: 0}}>
                        <BookmarkBorderIcon/>
                      </IconButton>
                    </Tooltip>
                }
              </Box>
            </CardContent>
          </Box>
        </Card>
        <Divider/>
      </div>
  );
}

export default Article;
