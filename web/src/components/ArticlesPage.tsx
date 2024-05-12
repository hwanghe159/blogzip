import * as React from 'react';
import AppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import Button from '@mui/material/Button';
import Typography from "@mui/material/Typography";
import {useEffect, useRef, useState} from "react";
import {Api} from "../utils/Api";
import styled from "styled-components";

interface Article {
  id: number;
  blog: Blog;
  title: string;
  url: string;
  summary: string;
  createdDate: string;
}

interface Blog {
  id: number;
  name: string;
  url: string;
  rssStatus: string;
  rss: string;
  createdBy: number;
  createdAt: string;
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

function ArticlesPage() {

  const [articles, setArticles] = useState<Article[]>([]);
  const [next, setNext] = useState<string | null>(null);
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    loadMoreArticles(true)
  }, []);

  // todo 무한스크롤 작동 안함
  const handleScroll = () => {
    if (!containerRef.current) return;

    const {scrollTop, scrollHeight, clientHeight} = containerRef.current;
    if (scrollTop + clientHeight >= scrollHeight) {
      loadMoreArticles(false);
    }
  };

  const loadMoreArticles = (initialRequest: boolean) => {
    const today = new Date()
    const yesterday = new Date(today)
    yesterday.setDate(today.getDate() - 1)

    if (!initialRequest && !next) {
      return
    }

    Api.get(`/api/v1/article`, {
      params:
          {
            from: yesterday.toLocaleDateString('en-CA'),
            next: next,
            size: 5, // todo 제거
          }
    })
    .onSuccess((response) => {
      setArticles(prevArticles => {
            const newItems = response.data.items
            .filter((newItem: Article) => !prevArticles.some(prevItem => prevItem.id === newItem.id))
            return [...prevArticles, ...newItems]
          }
      )
      setNext(response.data.next)
    })
    .on4XX((response) => {
    })
    .on5XX((response) => {
    });
  };

  return (
      <Container ref={containerRef} onScroll={handleScroll}>
        <Typography variant="h4" component="h1">어제 올라온 새 글</Typography>
        <ul>
          {articles.map(article => (
              <li key={article.id}>
                <h2>{article.title}</h2>
                <p>{article.summary}</p>
                {article.blog.name}
                <a href={article.url} target="_blank">원본 보기</a>
              </li>
          ))}
        </ul>
      </Container>
  );
}

export default ArticlesPage;
