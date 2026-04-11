import React, { useState } from 'react';
import { Api } from '../utils/Api';
import { getLoginUser } from '../utils/LoginUserHelper';
import { handleLogin } from './GoogleLoginButton';
import { ArticleResponse } from '../types';

interface ArticleProps {
  article: ArticleResponse;
  onReadLaterChange?: (articleId: number, isReadLater: boolean) => void;
}

function Article({ article: initialArticle, onReadLaterChange }: ArticleProps) {
  const [article, setArticle] = useState<ArticleResponse>(initialArticle);

  const openOriginalArticle = () => {
    const accessToken = getLoginUser()?.accessToken;

    if (accessToken) {
      Api.post(
        `/api/v1/article/${article.id}/read`,
        {},
        {
          headers: {
            Authorization: `Bearer ${accessToken}`,
          },
        }
      );
    }

    window.open(article.url, '_blank', 'noopener,noreferrer');
  };

  const toggleReadLater = (event: React.MouseEvent<HTMLButtonElement>) => {
    event.stopPropagation();

    const accessToken = getLoginUser()?.accessToken;
    if (!accessToken) {
      alert('로그인이 필요한 서비스입니다.');
      handleLogin();
      return;
    }

    if (article.isReadLater) {
      Api.delete('/api/v1/read-later', {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
        data: {
          articleId: article.id,
        },
      })
        .onSuccess(() => {
          setArticle((prevArticle) => ({ ...prevArticle, isReadLater: false }));
          onReadLaterChange?.(article.id, false);
        })
        .on4XX(() => {})
        .on5XX(() => {});
      return;
    }

    Api.post(
      '/api/v1/read-later',
      {
        articleId: article.id,
      },
      {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      }
    )
      .onSuccess(() => {
        setArticle((prevArticle) => ({ ...prevArticle, isReadLater: true }));
        onReadLaterChange?.(article.id, true);
      })
      .on4XX(() => {})
      .on5XX(() => {});
  };

  return (
    <article className="article-card" onClick={openOriginalArticle}>
      <div className="article-card__cover">
        <img src={article.blog.image ?? '/default_blog_image.png'} alt={article.title} />
        <span className="article-card__blog">{article.blog.name}</span>
      </div>

      <div className="article-card__body">
        <h3 className="article-card__title">{article.title}</h3>
        <p className="article-card__summary">{article.summary}</p>

        {article.keywords.length > 0 ? (
          <div className="tag-list">
            {article.keywords.map((keyword) => (
              <span key={`${article.id}-${keyword}`} className="tag">
                #{keyword}
              </span>
            ))}
          </div>
        ) : null}

        <div className="article-card__meta">
          <span className="article-card__date">{article.createdDate}</span>
          <div className="article-actions" onClick={(event) => event.stopPropagation()}>
            <button type="button" className="btn btn-ghost" onClick={openOriginalArticle}>
              원문 보기
            </button>
            <button
              type="button"
              className={`icon-action ${article.isReadLater ? 'active' : ''}`}
              onClick={toggleReadLater}
              aria-label={article.isReadLater ? '나중에 읽기 제거' : '나중에 읽기 추가'}
              title={article.isReadLater ? '나중에 읽기 제거' : '나중에 읽기 추가'}
            >
              {article.isReadLater ? '★' : '☆'}
            </button>
          </div>
        </div>
      </div>
    </article>
  );
}

export default Article;
