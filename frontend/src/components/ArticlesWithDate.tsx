import React from 'react';
import Article from './Article';
import { ArticleResponse } from '../types';

interface ArticlesWithDateProps {
  date: string;
  articles: ArticleResponse[];
}

function ArticlesWithDate({ date, articles }: ArticlesWithDateProps) {
  return (
    <section className="date-group">
      <span className="date-chip">{date}</span>
      <div className="article-list">
        {articles.map((article) => (
          <Article key={article.id} article={article} />
        ))}
      </div>
    </section>
  );
}

export default ArticlesWithDate;
