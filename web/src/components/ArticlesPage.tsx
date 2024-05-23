import * as React from 'react';
import Typography from "@mui/material/Typography";
import {useEffect, useState} from "react";
import {Api} from "../utils/Api";
import styled from "styled-components";
import InfiniteScroll from "react-infinite-scroll-component";
import ArticlesWithDate from "./ArticlesWithDate";

export interface ArticleResponse {
  id: number;
  blog: BlogResponse;
  title: string;
  url: string;
  summary: string;
  createdDate: string;
}

export interface BlogResponse {
  id: number;
  name: string;
  url: string;
  image: string | null;
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

  const [articles, setArticles] = useState<ArticleResponse[]>([]);
  const [hasMore, setHasMore] = useState<boolean>(true);
  const past = new Date(new Date().setDate(new Date().getDate() - 7));

  useEffect(() => {
    fetchData()
  }, []);

  function fetchData() {
    Api.get(`/api/v1/article`, {
      params:
          {
            from: past.toLocaleDateString('en-CA'),
            next: articles.length == 0 ? null : articles[articles.length - 1].id,
          }
    })
    .onSuccess((response) => {
      setArticles(prevArticles => {
            const newItems = response.data.items
            .filter((newItem: ArticleResponse) => !prevArticles.some(prevItem => prevItem.id === newItem.id))
            return [...prevArticles, ...newItems]
          }
      )
      if (response.data.next == null) {
        setHasMore(false)
      }
    })
    .on4XX((response) => {
    })
    .on5XX((response) => {
    });
  }

  function groupArticlesByDate(articles: ArticleResponse[]): ArticleResponse[][] {
    const groupedArticles: { [key: string]: ArticleResponse[] } = {};
    articles.forEach((article) => {
      const date = article.createdDate;
      if (!groupedArticles[date.toString()]) {
        groupedArticles[date.toString()] = [];
      }
      groupedArticles[date.toString()].push(article);
    });
    return Object.values(groupedArticles);
  }

  return (
      <Container>
        <Typography variant="h4" component="h4">최근 일주일간 올라온 새 글</Typography>
        <Typography variant="h6" component="h6">ChatGPT 요약</Typography>
        <InfiniteScroll
            dataLength={articles.length}
            next={fetchData}
            hasMore={hasMore}
            loader={<h4>Loading...</h4>}
            endMessage={
              <p style={{
                textAlign: 'center',
                marginTop: '50px',
                marginBottom: '50px'
              }}>
                <b>더 올라온 글이 없어요</b>
              </p>
            }
        >
          {groupArticlesByDate(articles)
          .map(articles =>
              <ArticlesWithDate key={articles[0].createdDate}
                                date={articles[0].createdDate}
                                articles={articles}
              />
          )}
        </InfiniteScroll>
      </Container>
  );
}

export default ArticlesPage;
