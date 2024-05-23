import * as React from 'react';
import Typography from "@mui/material/Typography";
import {ArticleResponse} from "./ArticlesPage";
import Article from "./Article";
import Box from "@mui/material/Box";

interface ArticlesWithDateProps {
  date: string,
  articles: ArticleResponse[]
}

function ArticlesWithDate({date, articles}: ArticlesWithDateProps) {

  return (
      <Box sx={{
        marginY: '50px',
      }}>
        <Typography component="h6" variant="h6">
          {date}
        </Typography>
        {articles.map(article =>
            <Article key={article.id} article={article}/>
        )}
      </Box>
  );
}

export default ArticlesWithDate;
