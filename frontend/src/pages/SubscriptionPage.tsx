import React, { FormEvent, useEffect, useState } from 'react';
import BlogAddDialog from '../components/BlogAddDialog';
import { handleLogin } from '../components/GoogleLoginButton';
import { Api } from '../utils/Api';
import { getLoginUser } from '../utils/LoginUserHelper';
import { BlogResponse, SubscriptionResponse } from '../types';

interface BlogItem {
  id: number;
  name: string;
  url: string;
  isSubscribed: boolean;
}

export default function SubscriptionPage() {
  const [query, setQuery] = useState('');
  const [searchedBlogs, setSearchedBlogs] = useState<BlogItem[]>([]);
  const [subscribedBlogs, setSubscribedBlogs] = useState<BlogItem[]>([]);
  const [showAddDialog, setShowAddDialog] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);
  const [isSearching, setIsSearching] = useState(false);

  useEffect(() => {
    getMySubscriptions();
  }, []);

  const getMySubscriptions = () => {
    const loginUser = getLoginUser();
    if (!loginUser) {
      alert('로그인이 필요한 서비스입니다.');
      handleLogin();
      return;
    }

    Api.get('/api/v1/subscription', {
      headers: {
        Authorization: `Bearer ${loginUser.accessToken}`,
      },
    })
      .onSuccess((response) => {
        const subscriptions = response.data as SubscriptionResponse[];
        setSubscribedBlogs(
          subscriptions.map((subscription) => ({
            id: subscription.blog.id,
            name: subscription.blog.name,
            url: subscription.blog.url,
            isSubscribed: true,
          }))
        );
      })
      .on4XX((errorResponse) => {
        if (errorResponse.code === 'LOGIN_FAILED') {
          alert('로그인이 필요한 서비스입니다.');
          handleLogin();
        }
      })
      .on5XX(() => {});
  };

  const handleSearch = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const trimmed = query.trim();
    if (trimmed.length < 2) {
      alert('2글자 이상 입력해주세요.');
      return;
    }

    const subscribedIds = new Set(subscribedBlogs.map((blog) => blog.id));
    setIsSearching(true);

    Api.get('/api/v1/blog/search', {
      params: {
        query: trimmed,
      },
    })
      .onSuccess((response) => {
        const blogs = response.data as BlogResponse[];
        setSearchedBlogs(
          blogs.map((blog) => ({
            id: blog.id,
            name: blog.name,
            url: blog.url,
            isSubscribed: subscribedIds.has(blog.id),
          }))
        );
        setHasSearched(true);
        setIsSearching(false);
      })
      .on4XX(() => {
        setIsSearching(false);
        setHasSearched(true);
      })
      .on5XX(() => {
        setIsSearching(false);
        setHasSearched(true);
      });
  };

  const handleSubscribe = (blog: BlogItem) => {
    const loginUser = getLoginUser();
    if (!loginUser) {
      alert('로그인이 필요한 서비스입니다.');
      handleLogin();
      return;
    }

    if (blog.isSubscribed) {
      Api.delete('/api/v1/subscription', {
        headers: {
          Authorization: `Bearer ${loginUser.accessToken}`,
        },
        data: {
          blogId: blog.id,
        },
      })
        .onSuccess(() => {
          setSearchedBlogs((prevBlogs) =>
            prevBlogs.map((prevBlog) =>
              prevBlog.id === blog.id ? { ...prevBlog, isSubscribed: false } : prevBlog
            )
          );
          setSubscribedBlogs((prevBlogs) => prevBlogs.filter((prevBlog) => prevBlog.id !== blog.id));
        })
        .on4XX(() => {})
        .on5XX(() => {});
      return;
    }

    Api.post(
      '/api/v1/subscription',
      {
        blogId: blog.id,
      },
      {
        headers: {
          Authorization: `Bearer ${loginUser.accessToken}`,
        },
      }
    )
      .onSuccess(() => {
        setSearchedBlogs((prevBlogs) =>
          prevBlogs.map((prevBlog) =>
            prevBlog.id === blog.id ? { ...prevBlog, isSubscribed: true } : prevBlog
          )
        );

        setSubscribedBlogs((prevBlogs) => {
          if (prevBlogs.some((prevBlog) => prevBlog.id === blog.id)) {
            return prevBlogs.map((prevBlog) =>
              prevBlog.id === blog.id ? { ...prevBlog, isSubscribed: true } : prevBlog
            );
          }

          return [
            {
              id: blog.id,
              name: blog.name,
              url: blog.url,
              isSubscribed: true,
            },
            ...prevBlogs,
          ];
        });
      })
      .on4XX(() => {})
      .on5XX(() => {});
  };

  return (
    <div className="stack">
      <section className="surface panel">
        <h1 className="page-title">구독 관리</h1>
        <p className="page-subtitle">
          관심 블로그를 검색해서 바로 구독하고, 매일 아침 받을 요약 피드를 내 취향에 맞게 관리하세요.
        </p>
      </section>

      <section className="grid-2">
        <div className="surface-strong panel stack">
          <div className="section-heading">
            <h3>블로그 검색</h3>
            <p>이름 또는 URL 일부로 검색</p>
          </div>

          <form className="search-form" onSubmit={handleSearch}>
            <input
              className="input"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder="예: velog, tistory"
            />
            <button type="submit" className="btn btn-primary" disabled={isSearching}>
              검색
            </button>
          </form>

          {searchedBlogs.length > 0 ? (
            <div className="blog-list">
              {searchedBlogs.map((blog) => (
                <article key={blog.id} className="blog-item">
                  <div className="stack" style={{ gap: 4 }}>
                    <strong>{blog.name}</strong>
                    <span>{blog.url}</span>
                  </div>
                  <button
                    type="button"
                    className={`btn ${blog.isSubscribed ? 'btn-outline' : 'btn-primary'}`}
                    onClick={() => handleSubscribe(blog)}
                  >
                    {blog.isSubscribed ? '구독중' : '구독하기'}
                  </button>
                </article>
              ))}
            </div>
          ) : hasSearched ? (
            <div className="list-empty">검색 결과가 없습니다. 직접 URL로 추가해보세요.</div>
          ) : null}

          {hasSearched ? (
            <button type="button" className="btn btn-ghost" onClick={() => setShowAddDialog(true)}>
              원하는 블로그가 없나요? URL로 직접 추가
            </button>
          ) : null}
        </div>

        <div className="surface-strong panel stack">
          <div className="section-heading">
            <h3>내가 구독중인 블로그</h3>
            <p>{subscribedBlogs.length}개</p>
          </div>

          {subscribedBlogs.length > 0 ? (
            <div className="blog-list">
              {subscribedBlogs.map((blog) => (
                <article key={blog.id} className="blog-item">
                  <div className="stack" style={{ gap: 4 }}>
                    <strong>{blog.name}</strong>
                    <span>{blog.url}</span>
                  </div>
                  <button type="button" className="btn btn-outline" onClick={() => handleSubscribe(blog)}>
                    구독 해지
                  </button>
                </article>
              ))}
            </div>
          ) : (
            <div className="list-empty">아직 구독 중인 블로그가 없습니다.</div>
          )}
        </div>
      </section>

      {showAddDialog ? (
        <div className="modal-backdrop" role="dialog" aria-modal="true">
          <div className="modal-panel">
            <BlogAddDialog
              onClose={() => {
                setShowAddDialog(false);
                getMySubscriptions();
              }}
            />
          </div>
        </div>
      ) : null}
    </div>
  );
}
