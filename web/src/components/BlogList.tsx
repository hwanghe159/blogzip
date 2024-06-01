import React, {useEffect, useState} from "react";
import {BlogResponse} from "../pages/MainPage";
import {
  Card,
  CardActionArea,
  CardActions,
  CardContent,
  CardMedia,
  Divider,
  Grid
} from "@mui/material";
import Typography from "@mui/material/Typography";
import Button from "@mui/material/Button";
import Box from "@mui/material/Box";
import {Api} from "../utils/Api";
import {getLoginUser} from "../utils/LoginUserHelper";

interface Blog {
  id: number;
  name: string;
  url: string;
  image: string | null;
  rssStatus: string;
  rss: string;
  createdBy: number;
  createdAt: string;
  isSubscribed: boolean;
}

interface BlogsProps {
  blogs: BlogResponse[];
}

export function BlogList({blogs}: BlogsProps) {

  const [blogList, setBlogList] = useState<Blog[]>([]);

  useEffect(() => {
    setBlogList(blogs.map((blog) => ({
      ...blog,
      isSubscribed: true,
    })))
  }, [blogs]);

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
      const updatedBlogList = blogList.map(blog =>
          blog.id === blogId ? {...blog, isSubscribed: true} : blog
      );
      setBlogList(updatedBlogList);
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
      const updatedBlogList = blogList.map(blog =>
          blog.id === blogId ? {...blog, isSubscribed: false} : blog
      );
      setBlogList(updatedBlogList);
    })
    .on4XX((response) => {
    })
    .on5XX((response) => {
    })
  }

  return (
      <div>
        {blogList.map(blog =>
            <Card
                key={blog.id}
                sx={{
                  boxShadow: 0,
                  display: 'flex',
                  flexDirection: {md: 'row', xs: 'column'},
                  margin: 2,
                  maxWidth: 800,
                }}
            >
              <Box sx={{
                position: 'relative',
                width: {md: 200, xs: '100%'},
                height: {md: 80, xs: 'auto'},
              }}>
                <CardMedia
                    component="img"
                    sx={{
                      width: '100%',
                      height: {md: '100%', xs: 80},
                      objectFit: 'cover',
                    }}
                    image={blog.image ?? '/default_blog_image.png'}
                />
              </Box>
              <Box sx={{display: 'flex', flexDirection: 'column', flex: 1}}>
                <CardContent>
                  <Typography component="div" variant="h5">
                    {blog.name}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{fontSize: '15px'}}>
                    {blog.url}
                  </Typography>
                </CardContent>
              </Box>
              {blog.isSubscribed ?
                  <Button variant="outlined" size={'small'} sx={{height: 40}}
                          onClick={() => cancelSubscription(blog.id)}>구독중</Button> :
                  <Button variant="contained" size={'small'} sx={{height: 40}}
                          onClick={() => addSubscription(blog.id)}>구독하기</Button>
              }
              <Divider/>
            </Card>
        )}
      </div>
  );
}
