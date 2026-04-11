import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import InfiniteScroll from 'react-infinite-scroll-component';
import ArticlesWithDate from '../components/ArticlesWithDate';
import OnboardingDialog from '../components/OnboardingDialog';
import { Api } from '../utils/Api';
import { getLoginUser, isLogined, removeLoginUser } from '../utils/LoginUserHelper';
import { ArticleResponse, PaginationResponse, SubscriptionResponse } from '../types';

function MainPage() {
  const [articles, setArticles] = useState<ArticleResponse[]>([]);
  const [hasMore, setHasMore] = useState(true);
  const [isFetching, setIsFetching] = useState(false);
  const [isInitialLoading, setIsInitialLoading] = useState(true);
  const [openOnboarding, setOpenOnboarding] = useState(false);

  const nextCursorRef = useRef<number | null>(null);
  const hasMoreRef = useRef(true);
  const fetchingRef = useRef(false);

  const fromDate = useMemo(() => {
    const past = new Date();
    past.setDate(past.getDate() - 7);
    return past.toISOString().slice(0, 10);
  }, []);

  const fetchArticles = useCallback(() => {
    if (fetchingRef.current || !hasMoreRef.current) {
      return;
    }

    const loginUser = getLoginUser();
    const endpoint = loginUser ? '/api/v1/my/article' : '/api/v1/article';

    fetchingRef.current = true;
    setIsFetching(true);

    Api.get(endpoint, {
      headers: loginUser
        ? {
            Authorization: `Bearer ${loginUser.accessToken}`,
          }
        : {},
      params: {
        from: fromDate,
        next: nextCursorRef.current,
        size: 20,
      },
    })
      .onSuccess((response) => {
        const data = response.data as PaginationResponse<ArticleResponse>;

        setArticles((prevArticles) => {
          const deduplicated = data.items.filter(
            (newArticle) => !prevArticles.some((existingArticle) => existingArticle.id === newArticle.id)
          );
          return [...prevArticles, ...deduplicated];
        });

        nextCursorRef.current = data.next;
        const nextExists = data.next !== null;
        hasMoreRef.current = nextExists;
        setHasMore(nextExists);

        setIsInitialLoading(false);
        setIsFetching(false);
        fetchingRef.current = false;
      })
      .on4XX(() => {
        setIsInitialLoading(false);
        setIsFetching(false);
        fetchingRef.current = false;

        if (loginUser) {
          removeLoginUser();
          window.location.href = '/';
        }
      })
      .on5XX(() => {
        setIsInitialLoading(false);
        setIsFetching(false);
        fetchingRef.current = false;
      });
  }, [fromDate]);

  const checkOnboarding = useCallback(() => {
    const loginUser = getLoginUser();
    if (!loginUser) {
      return;
    }

    Api.get('/api/v1/subscription', {
      headers: {
        Authorization: `Bearer ${loginUser.accessToken}`,
      },
    })
      .onSuccess((response) => {
        const subscriptions = response.data as SubscriptionResponse[];
        if (subscriptions.length === 0) {
          setOpenOnboarding(true);
        }
      })
      .on4XX(() => {})
      .on5XX(() => {});
  }, []);

  useEffect(() => {
    fetchArticles();
    if (isLogined()) {
      checkOnboarding();
    }
  }, [checkOnboarding, fetchArticles]);

  const groupedArticles = useMemo(() => {
    const grouped = new Map<string, ArticleResponse[]>();

    articles.forEach((article) => {
      const currentGroup = grouped.get(article.createdDate) ?? [];
      currentGroup.push(article);
      grouped.set(article.createdDate, currentGroup);
    });

    return Array.from(grouped.entries()).map(([date, groupedItems]) => ({
      date,
      items: groupedItems,
    }));
  }, [articles]);

  return (
    <div className="stack">
      <section className="surface hero">
        <div>
          <h1 className="page-title">최근 일주일간 올라온 새 글</h1>
          <p className="page-subtitle">
            읽기 편한 요약으로 빠르게 핵심만 확인하세요. 관심 글은 별표로 저장해두고 나중에 다시 볼 수 있어요.
          </p>
        </div>

        <div className="hero__stats">
          <div className="stat-card">
            <h4>수집 기간</h4>
            <p>최근 7일</p>
          </div>
          <div className="stat-card">
            <h4>현재 표시 글</h4>
            <p>{articles.length}개</p>
          </div>
          <div className="stat-card">
            <h4>요약 모델</h4>
            <p>GPT 기반</p>
          </div>
          <div className="stat-card">
            <h4>상태</h4>
            <p>{isLogined() ? '개인화 피드' : '공개 피드'}</p>
          </div>
        </div>
      </section>

      {isInitialLoading && articles.length === 0 ? (
        <div className="loader">
          <span className="dot-loader" />
        </div>
      ) : null}

      {!isInitialLoading && articles.length === 0 ? (
        <section className="list-empty">표시할 새 글이 없습니다. 잠시 후 다시 확인해 주세요.</section>
      ) : null}

      <InfiniteScroll
        dataLength={articles.length}
        next={fetchArticles}
        hasMore={hasMore}
        loader={
          isFetching ? (
            <div className="loader">
              <span className="dot-loader" />
            </div>
          ) : (
            <></>
          )
        }
        endMessage={
          articles.length > 0 ? (
            <p className="list-empty">이번 주 새 글을 모두 확인했어요.</p>
          ) : (
            <></>
          )
        }
      >
        {groupedArticles.map((group) => (
          <ArticlesWithDate key={group.date} date={group.date} articles={group.items} />
        ))}
      </InfiniteScroll>

      {openOnboarding ? (
        <div className="modal-backdrop" role="dialog" aria-modal="true">
          <div className="modal-panel">
            <OnboardingDialog onClose={() => setOpenOnboarding(false)} />
          </div>
        </div>
      ) : null}
    </div>
  );
}

export default MainPage;
