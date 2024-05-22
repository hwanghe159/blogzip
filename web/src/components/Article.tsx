import * as React from 'react';
import {Card, CardContent, CardMedia} from "@mui/material";
import Box from "@mui/material/Box";
import Typography from "@mui/material/Typography";
import {ArticleResponse} from "./ArticlesPage"

interface ArticleProps {
  article: ArticleResponse
}

function Article({article}: ArticleProps) {
  function toOriginalUrl() {
    window.open(article.url, '_blank');
  }

  return (
      <Card sx={{display: 'flex', margin: 2, maxWidth: 800}} onClick={toOriginalUrl}>
        <CardMedia
            component="img"
            sx={{width: 200}}
            image={"https://techblog.woowahan.com/wp-content/uploads/2021/06/screenshot.jpg"}
            alt={article.title}
        />
        <Box sx={{display: 'flex', flexDirection: 'column', flex: 1}}>
          <CardContent>
            <Typography component="div" variant="h5">
              {article.title}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {article.summary}
            </Typography>
          </CardContent>
        </Box>
      </Card>
  );
}

export default Article;
