import React, {useState, useEffect} from 'react';
import {
  Card,
  CardContent,
  CardMedia,
  IconButton,
  Box,
  Typography,
  Tooltip,
  Divider, FormControl, InputLabel, Select, MenuItem
} from "@mui/material";
import {Api} from "../utils/Api";
import BookmarkBorderIcon from '@mui/icons-material/BookmarkBorder';
import BookmarkIcon from '@mui/icons-material/Bookmark';
import {getLoginUser} from "../utils/LoginUserHelper";
import {handleLogin} from "../components/GoogleLoginButton";
import {Link} from "react-router-dom";
import Button from "@mui/material/Button";

interface ReadLater {
  id: number
  article: Article
  isReadLater: boolean
}

interface Article {
  id: number
  blogId: number
  title: string
  url: string
  summary: string
  createdDate: string
}

interface Blog {
  id: number
  name: string
  url: string
  image: string
  rssStatus: string
  rss: string
  createdBy: number
  createdAt: string
}

interface Filter {
  text: string
  selected: boolean
}

interface Sorter {
  text: string
  selected: boolean
}

export default function ReadLaterPage() {
  const [readLaters, setReadLaters] = useState<ReadLater[]>([]);
  const [blogs, setBlogs] = useState<Map<number, Blog>>(new Map());
  const [filters, setFilters] = useState<Filter[]>([
    {text: '전체', selected: true},
  ]);
  const [sorters, setSorters] = useState<Sorter[]>([
    {text: '날짜순', selected: false},
    {text: '추가순', selected: false}
  ]);

  useEffect(() => {
    fetchArticles();
    fetchBlogs();
  }, []);

  const fetchArticles = () => {
    Api.get(`/api/v1/read-later`, {
      headers: {
        Authorization: `Bearer ${getLoginUser()?.accessToken}`,
      },
    }).onSuccess(response => {
      setReadLaters(response.data.map((item: ReadLater) => ({...item, isReadLater: true})));
    });
  };

  const fetchBlogs = () => {
    Api.get(`/api/v1/blog`)
    .onSuccess(response => {
      const blogs: Blog[] = response.data
      setBlogs(new Map(blogs.map(blog => [blog.id, blog] as const)))
    });
  }

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

  function changeFilter() {
  }

  function changeSorter() {
  }

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

          {/*필터, 정렬 추가*/}
          <Box display={"none"}>
            <FormControl sx={{width: '100px'}}>
              <InputLabel>블로그</InputLabel>
              <Select
                  label="블로그"
                  onChange={changeFilter}
              >
                {filters.map(filter => (
                    <MenuItem value={filter.text}>{filter.text}</MenuItem>
                ))}
              </Select>
            </FormControl>
            <FormControl sx={{width: '100px'}}>
              <InputLabel>정렬</InputLabel>
              <Select
                  label="정렬"
                  onChange={changeSorter}
              >
                {sorters.map(sorter => (
                    <MenuItem value={sorter.text}>{sorter.text}</MenuItem>
                ))}
              </Select>
            </FormControl>
          </Box>
        </Box>
        {readLaters.length > 0 ? (
            <Box>
              {readLaters.map(readLater => (
                  <Box>
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
                          window.open(readLater.article.url, '_blank');
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
                            image={blogs.get(readLater.article.blogId)?.image ?? "/default_blog_image.png"}
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
                          {blogs.get(readLater.article.blogId)?.name}
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
                          <Box
                              sx={{
                                marginTop: 1,
                                textAlign: 'left'
                              }}
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
              ))}
            </Box>
        ) : (
            <Box
                display="flex"
                flexDirection="column"
                justifyContent="center"
                alignItems="center"
                minHeight="50vh"
            >
              <Typography variant="subtitle1" gutterBottom>나중에 읽을 글이 없습니다. 홈에서 추가해 보세요.</Typography>
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
      </Box>
  );
}