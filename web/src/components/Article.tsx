import * as React from 'react';
import {Card, CardContent, CardMedia, Divider} from "@mui/material";
import Box from "@mui/material/Box";
import Typography from "@mui/material/Typography";
import {ArticleResponse} from "../pages/MainPage";

interface ArticleProps {
  article: ArticleResponse
}

function Article({article}: ArticleProps) {
  function toOriginalUrl() {
    window.open(article.url, '_blank');
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
            <CardContent>
              <Typography component="div" variant="h5">
                {article.title}
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{fontSize: '15px'}}>
                {article.summary}
              </Typography>
            </CardContent>
          </Box>
        </Card>
        <Divider/>
      </div>
  );
}

export default Article;
