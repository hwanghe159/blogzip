import React, {useState, useEffect} from 'react';
import {
  Card,
  CardContent,
  CardMedia,
  IconButton,
  Box,
  Typography,
  Tooltip,
  Divider, Chip, CircularProgress
} from "@mui/material";
import {Api} from "../utils/Api";
import BookmarkBorderIcon from '@mui/icons-material/BookmarkBorder';
import BookmarkIcon from '@mui/icons-material/Bookmark';
import {getLoginUser} from "../utils/LoginUserHelper";
import {handleLogin} from "../components/GoogleLoginButton";
import {Link} from "react-router-dom";
import Button from "@mui/material/Button";
import InfiniteScroll from "react-infinite-scroll-component";

interface ReadLater {
  id: number
  article: Article
  isReadLater: boolean
}

interface Article {
  id: number
  blog: Blog
  title: string
  url: string
  summary: string
  keywords: string[]
  createdDate: string
}

interface Blog {
  id: number
  name: string
  url: string
  image: string | null
}

export default function ReadLaterPage() {
  const [readLaters, setReadLaters] = useState<ReadLater[]>([]);
  const [fetchCompleted, setFetchCompleted] = useState<boolean>(false);
  const [hasMore, setHasMore] = useState<boolean>(true);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = () => {
    Api.get(`/api/v1/read-later`, {
      headers: {
        Authorization: `Bearer ${getLoginUser()?.accessToken}`,
      },
      params: {
        next: readLaters.length == 0 ? null : readLaters[readLaters.length - 1].id,
        size: 20,
      }
    }).onSuccess(response => {
      setReadLaters(prevReadLaters => {
            const newItems: ReadLater[] = response.data.items
            .filter((newItem: ReadLater) => !prevReadLaters.some(prevItem => prevItem.id === newItem.id))
            .map((newItem: ReadLater) => {
              newItem.isReadLater = true
              return newItem
            })
            return [...prevReadLaters, ...newItems]
          }
      )
      if (response.data.next == null) {
        setHasMore(false)
      }
      setFetchCompleted(true)
    });
  };

  const addReadLater = (readLater: ReadLater) => {
    Api.post(`/api/v1/read-later`, {
      articleId: readLater.article.id
    }, {
      headers: {
        Authorization: `Bearer ${getLoginUser()?.accessToken}`,
      }
    })
    .onSuccess(response => {
      setReadLaters(
          readLaters.map((item) =>
              item.id === readLater.id
                  ? {...item, isReadLater: true}
                  : item
          )
      )
    }).on4XX((response) => {
      if (response.code === 'LOGIN_FAILED') {
        alert("로그인이 필요한 서비스입니다.")
        handleLogin()
      }
    });

  };

  const deleteReadLater = async (readLater: ReadLater) => {
    Api.delete(`/api/v1/read-later`, {
      headers: {
        Authorization: `Bearer ${getLoginUser()?.accessToken}`,
      },
      data: {
        articleId: readLater.article.id
      }
    })
    .onSuccess(response => {
      setReadLaters(
          readLaters.map((item) =>
              item.id === readLater.id
                  ? {...item, isReadLater: false}
                  : item
          )
      )
    })
    .on4XX(response => {
      if (response.code === 'LOGIN_FAILED') {
        alert("로그인이 필요한 서비스입니다.")
        handleLogin()
      }
    })
    .on5XX(() => {

    })
  };

  return (
      <Box sx={{
        width: {xs: 'calc(100% - 20px)', md: '100%'},
        maxWidth: 900,
        marginTop: 5,
        marginLeft: 'auto',
        marginRight: 'auto'
      }}>
        <Box
            sx={{
              display: 'flex',
              justifyContent: 'space-between' // 아이템 사이 균일한 간격
            }}
        >
          <Typography variant="h4" component="h4">나중에 읽기</Typography>
        </Box>
        {fetchCompleted && readLaters.length == 0 && (
            <Box
                display="flex"
                flexDirection="column"
                justifyContent="center"
                alignItems="center"
                minHeight="50vh"
            >
              <Typography variant="subtitle1" gutterBottom>나중에 읽을 글이 없습니다. 홈에서 추가해
                보세요.</Typography>
              <Button
                  component={Link}
                  to="/"
                  variant="contained"
                  color="primary"
              >
                홈으로 돌아가기
              </Button>
            </Box>
        )}
        <InfiniteScroll
            dataLength={readLaters.length}
            next={fetchData}
            hasMore={hasMore}
            loader=
                {!fetchCompleted &&
                    <Box
                        mt={10}
                        display={'flex'}
                        justifyContent={'center'}
                    >
                      <CircularProgress/>
                    </Box>
                }
        >
          {readLaters.length > 0 && (
              <Box>
                {readLaters.map(readLater =>
                    <Box key={readLater.id}>
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
                          onClick={() => {
                            Api.post(`/api/v1/article/${readLater.article.id}/read`, {},
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
                            window.open(readLater.article.url);
                          }}
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
                              image={readLater.article.blog.image ?? "/default_blog_image.png"}
                              alt={readLater.article.title}
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
                            {readLater.article.blog.name}
                          </Typography>
                        </Box>
                        <Box sx={{display: 'flex', flexDirection: 'column', flex: 1}}>
                          <CardContent sx={{
                            p: 2,
                          }}>
                            <Typography component="div" variant="h5">
                              {readLater.article.title}
                            </Typography>
                            <Typography variant="body2" color="text.secondary"
                                        sx={{fontSize: '15px'}}>
                              {readLater.article.summary}
                            </Typography>
                            <Typography variant="body2" color="text.secondary" sx={{mt: 1}}>
                              {readLater.article.createdDate}
                            </Typography>
                            <Box pb={1}>
                              {readLater.article.keywords.map(keyword =>
                                  <Chip key={keyword} label={`# ${keyword}`} size={"small"}
                                        sx={{mr: 1, mt: 1}}/>
                              )}
                            </Box>
                            <Box
                                py={1}
                                textAlign={'left'}
                                onClick={(e) => e.stopPropagation()}
                            >
                              {readLater.isReadLater ?
                                  <Tooltip title="나중에 읽기 제거" arrow={true}>
                                    <IconButton onClick={() => deleteReadLater(readLater)}
                                                sx={{p: 0}}>
                                      <BookmarkIcon/>
                                    </IconButton>
                                  </Tooltip> :
                                  <Tooltip title="나중에 읽기" arrow={true}>
                                    <IconButton onClick={() => addReadLater(readLater)} sx={{p: 0}}>
                                      <BookmarkBorderIcon/>
                                    </IconButton>
                                  </Tooltip>
                              }
                            </Box>
                          </CardContent>
                        </Box>
                      </Card>
                      <Divider/>
                    </Box>
                )
                }</Box>
          )
          }
        </InfiniteScroll>
      </Box>
  );
}