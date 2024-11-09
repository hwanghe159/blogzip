import * as React from 'react';
import Typography from "@mui/material/Typography";
import {useEffect, useState} from "react";
import {Api} from "../utils/Api";
import styled from "styled-components";
import InfiniteScroll from "react-infinite-scroll-component";
import ArticlesWithDate from "../components/ArticlesWithDate";
import {CircularProgress, Dialog} from "@mui/material";
import Box from "@mui/material/Box";
import {getLoginUser, isLogined, removeLoginUser} from "../utils/LoginUserHelper";
import Tooltip from "@mui/material/Tooltip";
import InfoIcon from '@mui/icons-material/Info';
import OnboardingDialog from "../components/OnboardingDialog";

export interface ArticleResponse {
  id: number;
  blog: BlogResponse;
  title: string;
  url: string;
  summary: string;
  isReadLater: boolean;
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

function MainPage() {

  const [openTooltip, setOpenTooltip] = useState<boolean>(false);
  const [openOnboarding, setOpenOnboarding] = useState<boolean>(false);
  const [articles, setArticles] = useState<ArticleResponse[]>([]);
  const [hasMore, setHasMore] = useState<boolean>(true);
  const past = new Date(new Date().setDate(new Date().getDate() - 7));

  useEffect(() => {
    fetchData()
  }, []);

  function fetchData() {
    let url: string;
    let headers = {};
    if (isLogined()) {
      url = '/api/v1/my/article';
      headers = {
        Authorization: `Bearer ${getLoginUser()?.accessToken}`,
      }
    } else {
      url = '/api/v1/article';
    }
    Api.get(url, {
      headers: headers,
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
      removeLoginUser()
      window.location.href = '/';
    })
    .on5XX((response) => {
    });

    if (isLogined()) {
      Api.get(`/api/v1/subscription`, {
        headers: {
          Authorization: `Bearer ${getLoginUser()?.accessToken}`,
        }
      })
      .onSuccess((response) => {
        if (response.data.length === 0) {
          setOpenOnboarding(true)
        }
      })
      .on4XX((response) => {
      })
      .on5XX((response) => {
      })
    }
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
      <Box sx={{
        width: {xs: 'calc(100% - 20px)', md: '100%'},
        maxWidth: 900,
        marginTop: 5,
        marginLeft: 'auto',
        marginRight: 'auto'
      }}>
        <Box display={"flex"} alignItems="center">
          <Typography variant="h5" component="h5">최근 일주일간 올라온 새 글</Typography>
          <Tooltip open={openTooltip} onClick={() => setOpenTooltip(!openTooltip)}
                   title="ChatGPT4.0을 사용하여 요약하였습니다." sx={{ml: 1}}>
            <InfoIcon color={"action"}/>
          </Tooltip>
        </Box>
        <InfiniteScroll
            dataLength={articles.length}
            next={fetchData}
            hasMore={hasMore}
            loader=
                {
                  <Box sx={{display: 'flex', justifyContent: 'center'}}>
                    <CircularProgress/>
                  </Box>
                }
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
        <Dialog
            open={openOnboarding}
            onClose={() => setOpenOnboarding(false)}
            fullWidth={true}
        >
          <OnboardingDialog onClose={() => setOpenOnboarding(false)}/>
        </Dialog>
      </Box>
  );
}

export default MainPage;
