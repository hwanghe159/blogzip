import React, {Fragment, useEffect, useState} from 'react'
import {Button, Box, TextField, Card, CardContent, Typography, Dialog} from '@mui/material'
import {Add, Search} from '@mui/icons-material'
import {getLoginUser} from "../utils/LoginUserHelper";
import {handleLogin} from "../components/GoogleLoginButton";
import {Api} from "../utils/Api";
import {useNavigate} from "react-router-dom";
import BlogAddDialog from "../components/BlogAddDialog";

interface Blog {
  id: number
  name: string
  url: string
  isSubscribed: boolean
}

export default function SubscriptionPage() {
  const [query, setQuery] = useState('')
  const [searchedBlogs, setSearchedBlogs] = useState<Blog[]>([])
  const [subscribedBlogs, setSubscribedBlogs] = useState<Blog[]>([])
  const [showAddButton, setShowAddButton] = useState<boolean>(false)
  const [open, setOpen] = useState(false);

  const navigate = useNavigate()

  useEffect(() => {
    getMySubscriptions()
  }, [navigate])

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
      setSubscribedBlogs(
          response.data.map((subscription: { blog: any }) => ({
            id: subscription.blog.id,
            name: subscription.blog.name,
            url: subscription.blog.url,
            isSubscribed: true
          }))
      )
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

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()

    const minLength = 2
    if (query.trim().length < minLength) {
      alert(`${minLength}글자 이상 입력해주세요.`)
      return
    }

    const subscribedBlogIds = new Set(subscribedBlogs.map(blog => blog.id))
    Api.get(`/api/v1/blog/search?query=${query}`)
    .onSuccess((response) => {
      setSearchedBlogs(
          response.data.map((searchedBlog: { id: number; name: string; url: string; }) => ({
            id: searchedBlog.id,
            name: searchedBlog.name,
            url: searchedBlog.url,
            isSubscribed: subscribedBlogIds.has(searchedBlog.id)
          }))
      )
    })
    .on4XX((response) => {
    })
    .on5XX((response) => {
    })
    setShowAddButton(true)
  }

  const handleSubscribe = (blog: Blog) => {
    if (blog.isSubscribed) {
      // 구독 해지
      Api.delete(`/api/v1/subscription`, {
        headers: {
          Authorization: `Bearer ${getLoginUser()?.accessToken}`,
        },
        data: {
          blogId: blog.id
        }
      })
      .onSuccess((response) => {
        setSearchedBlogs(
            searchedBlogs.map(searchedBlog =>
                searchedBlog.id === blog.id
                    ? {...searchedBlog, isSubscribed: false}
                    : searchedBlog
            )
        )
        setSubscribedBlogs(
            subscribedBlogs.map((subscribedBlog) =>
                subscribedBlog.id === blog.id
                    ? {...subscribedBlog, isSubscribed: false}
                    : subscribedBlog
            )
        )
      })
      .on4XX((response) => {
      })
      .on5XX((response) => {
      })
    } else {
      // 구독 추가
      Api.post(`/api/v1/subscription`, {
            blogId: blog.id
          },
          {
            headers: {
              Authorization: `Bearer ${getLoginUser()?.accessToken}`,
            }
          })
      .onSuccess((response) => {
        const newBlog: Blog = {
          id: blog.id,
          name: blog.name,
          url: blog.url,
          isSubscribed: true,
        }
        setSubscribedBlogs((prevBlogs) => {
          const blogExists = prevBlogs.some((blog) => blog.id === newBlog.id);
          if (blogExists) {
            return prevBlogs.map((blog) =>
                blog.id === newBlog.id ? {...blog, isSubscribed: true} : blog
            );
          } else {
            return [...prevBlogs, {...newBlog, isSubscribed: true}];
          }
        })
        setSearchedBlogs(searchedBlogs => searchedBlogs.map(searchedBlog =>
            searchedBlog.id === blog.id
                ? {...searchedBlog, isSubscribed: true}
                : searchedBlog
        ))
      })
      .on4XX((response) => {
      })
      .on5XX((response) => {
      })
    }
  }

  function addBlog() {

  }

  return (
      <Box sx={{width: '90%', maxWidth: 800, mx: 'auto', p: 3}}>
        <Box sx={{py: 4}}>
          <Typography variant="h4" component="h1" gutterBottom>
            구독하고 싶은 블로그를 검색해보세요.
          </Typography>
          <Card>
            <CardContent>
              <Box component="form" onSubmit={handleSearch} sx={{display: 'flex', gap: 2}}>
                <TextField
                    fullWidth
                    size={"small"}
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    placeholder="블로그 주소 또는 이름"
                />
                <Button
                    type="submit" variant="contained" startIcon={<Search/>}
                ></Button>
              </Box>

              {searchedBlogs.length > 0 ? (
                  <Box component="ul" sx={{mt: 2, listStyle: 'none', p: 0}}>
                    {searchedBlogs.map(blog => (
                        <Box
                            key={blog.id}
                            component="li"
                            sx={{
                              display: 'flex',
                              justifyContent: 'space-between',
                              alignItems: 'center',
                              p: 1,
                              bgcolor: 'background.paper',
                              borderRadius: 2,
                              mb: 1
                            }}
                        >
                          <Box sx={{display: 'flex', flexDirection: 'column'}}>
                            <Typography
                                variant="body1"
                                color="textPrimary"
                                sx={{
                                  wordBreak: 'break-word', // 줄바꿈 허용
                                }}
                            >
                              {blog.name}
                            </Typography>
                            <Typography
                                variant="caption"
                                color="textSecondary"
                                sx={{
                                  wordBreak: 'break-word', // 줄바꿈 허용
                                }}
                            >
                              {blog.url}
                            </Typography>
                          </Box>
                          <Button
                              size={"small"}
                              variant={blog.isSubscribed ? 'outlined' : 'contained'}
                              onClick={() => handleSubscribe(blog)}
                          >
                            {blog.isSubscribed ? '구독중' : '구독하기'}
                          </Button>
                        </Box>
                    ))}
                  </Box>
              ) : (
                  <></>
              )}
              {showAddButton ? (<Box sx={{
                mt: 4,
                pt: 4,
                borderTop: 1,
                borderColor: 'divider',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'flex-end',
                gap: 2
              }}>
                <Typography variant="body2" color="textSecondary">
                  원하는 블로그가 없으신가요?
                </Typography>
                <Fragment>
                  <Button
                      size={"small"}
                      onClick={() => setOpen(true)}
                      variant="outlined"
                      startIcon={<Add/>}
                  >
                    직접 추가하기
                  </Button>
                  <Dialog
                      open={open}
                      onClose={() => setOpen(false)}
                      fullWidth={true}
                  >
                    <BlogAddDialog onClose={() => setOpen(false)}/>
                  </Dialog>
                </Fragment>
              </Box>) : (<div></div>)}
            </CardContent>
          </Card>
        </Box>
        <Box sx={{py: 4}}>
          <Typography variant="h4" component="h1" gutterBottom>
            내가 구독중인 블로그
          </Typography>
          <Card sx={{mt: 4}}>
            <CardContent>
              {subscribedBlogs.length > 0 ? (
                  <Box component="ul" sx={{listStyle: 'none', p: 0}}>
                    {subscribedBlogs.map(blog => (
                        <Box
                            key={blog.id}
                            component="li"
                            sx={{
                              display: 'flex',
                              justifyContent: 'space-between',
                              alignItems: 'center',
                              p: 1,
                              bgcolor: 'background.paper',
                              borderRadius: 2,
                              mb: 1
                            }}
                        >
                          <Box sx={{display: 'flex', flexDirection: 'column'}}>
                            <Typography
                                variant="body1"
                                color="textPrimary"
                                sx={{
                                  wordBreak: 'break-word', // 줄바꿈 허용
                                }}
                            >
                              {blog.name}
                            </Typography>
                            <Typography
                                variant="caption"
                                color="textSecondary"
                                sx={{
                                  wordBreak: 'break-word', // 줄바꿈 허용
                                }}
                            >
                              {blog.url}
                            </Typography>
                          </Box>
                          <Button
                              size={"small"}
                              variant={blog.isSubscribed ? 'outlined' : 'contained'}
                              onClick={() => handleSubscribe(blog)}
                          >
                            {blog.isSubscribed ? '구독중' : '구독하기'}
                          </Button>
                        </Box>
                    ))}
                  </Box>
              ) : (
                  <Typography align="center" color="textSecondary">
                    아직 구독 중인 블로그가 없습니다.
                  </Typography>
              )}
            </CardContent>
          </Card>
        </Box>
      </Box>
  )
}
