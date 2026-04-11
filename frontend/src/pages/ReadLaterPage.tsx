import React, { useCallback, useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import InfiniteScroll from 'react-infinite-scroll-component';
import Article from '../components/Article';
import { Api } from '../utils/Api';
import { getLoginUser } from '../utils/LoginUserHelper';
import { handleLogin } from '../components/GoogleLoginButton';
import { PaginationResponse, ReadLaterResponse } from '../types';

export default function ReadLaterPage() {
  const [readLaters, setReadLaters] = useState<ReadLaterResponse[]>([]);
  const [hasMore, setHasMore] = useState(true);
  const [isLoading, setIsLoading] = useState(true);
  const [isFetching, setIsFetching] = useState(false);

  const nextCursorRef = useRef<number | null>(null);
  const hasMoreRef = useRef(true);
  const fetchingRef = useRef(false);

  const fetchReadLaters = useCallback(() => {
    const loginUser = getLoginUser();
    if (!loginUser) {
      alert('로그인이 필요한 서비스입니다.');
      handleLogin();
      return;
    }

    if (fetchingRef.current || !hasMoreRef.current) {
      return;
    }

    fetchingRef.current = true;
    setIsFetching(true);

    Api.get('/api/v1/read-later', {
      headers: {
        Authorization: `Bearer ${loginUser.accessToken}`,
      },
      params: {
        next: nextCursorRef.current,
        size: 20,
      },
    })
      .onSuccess((response) => {
        const data = response.data as PaginationResponse<ReadLaterResponse>;

        setReadLaters((prevItems) => {
          const deduplicated = data.items.filter(
            (newItem) => !prevItems.some((existingItem) => existingItem.id === newItem.id)
          );

          return [...prevItems, ...deduplicated].map((item) => ({
            ...item,
            article: {
              ...item.article,
              isReadLater: true,
            },
          }));
        });

        nextCursorRef.current = data.next;
        const nextExists = data.next !== null;
        hasMoreRef.current = nextExists;
        setHasMore(nextExists);

        setIsLoading(false);
        setIsFetching(false);
        fetchingRef.current = false;
      })
      .on4XX(() => {
        setIsLoading(false);
        setIsFetching(false);
        fetchingRef.current = false;
      })
      .on5XX(() => {
        setIsLoading(false);
        setIsFetching(false);
        fetchingRef.current = false;
      });
  }, []);

  useEffect(() => {
    fetchReadLaters();
  }, [fetchReadLaters]);

  return (
    <div className="stack">
      <section className="surface panel">
        <h1 className="page-title">나중에 읽기</h1>
        <p className="page-subtitle">저장해 둔 글을 한곳에서 모아보고, 읽은 후에는 바로 정리할 수 있어요.</p>
      </section>

      {!isLoading && readLaters.length === 0 ? (
        <section className="list-empty stack">
          <p>아직 저장된 글이 없습니다.</p>
          <Link to="/" className="btn btn-primary" style={{ width: 'fit-content', margin: '0 auto' }}>
            홈에서 글 보러가기
          </Link>
        </section>
      ) : null}

      <InfiniteScroll
        dataLength={readLaters.length}
        next={fetchReadLaters}
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
      >
        <div className="article-list">
          {readLaters.map((readLater) => (
            <Article
              key={readLater.id}
              article={readLater.article}
              onReadLaterChange={(articleId, isReadLater) => {
                if (!isReadLater) {
                  setReadLaters((prevItems) =>
                    prevItems.filter((currentItem) => currentItem.article.id !== articleId)
                  );
                }
              }}
            />
          ))}
        </div>
      </InfiniteScroll>
    </div>
  );
}
