import { KeyboardEvent, ReactNode, useEffect, useState } from "react";
import { ArticleResponse, ReadLaterArticleResponse } from "../types";

type BaseArticle = ArticleResponse | ReadLaterArticleResponse;

type ArticleCardProps = {
  article: BaseArticle;
  readLaterActive: boolean;
  onOpen: (article: BaseArticle) => void;
  onToggleReadLater?: (article: BaseArticle, nextValue: boolean) => void;
  onKeywordClick?: (keyword: string) => void;
  activeKeywords?: Set<string>;
  blogAction?: ReactNode;
  bottomAction?: ReactNode;
  busy?: boolean;
};

function extractDomain(url: string): string {
  try {
    return new URL(url).hostname.replace(/^www\./, "");
  } catch (_) {
    return url;
  }
}

export function ArticleCard({
  article,
  readLaterActive,
  onOpen,
  onToggleReadLater,
  onKeywordClick,
  activeKeywords,
  blogAction,
  bottomAction,
  busy = false,
}: ArticleCardProps) {
  const normalizedImage = article.blog.image?.trim() || null;
  const [isImageBroken, setIsImageBroken] = useState(false);

  useEffect(() => {
    setIsImageBroken(false);
  }, [normalizedImage, article.blog.id]);

  function handleCardKeyDown(event: KeyboardEvent<HTMLElement>) {
    if (event.key !== "Enter" && event.key !== " ") return;
    event.preventDefault();
    onOpen(article);
  }

  return (
    <article
      className="article-card article-card--clickable"
      role="button"
      tabIndex={0}
      aria-label={`${article.title} 원문으로 이동`}
      onClick={() => onOpen(article)}
      onKeyDown={handleCardKeyDown}
    >
      <header className="article-card__header">
        <div className="article-card__blog">
          <div className="article-card__image-wrap">
            {normalizedImage && !isImageBroken ? (
              <img
                src={normalizedImage}
                alt={article.blog.name}
                className="article-card__image"
                onError={() => setIsImageBroken(true)}
              />
            ) : (
              <div className="article-card__image article-card__image--fallback">
                {article.blog.name.slice(0, 1).toUpperCase()}
              </div>
            )}
          </div>
          <div className="article-card__blog-meta">
            <div className="article-card__blog-line">
              <p className="article-card__blog-name">{article.blog.name}</p>
              {blogAction ? (
                <span
                  className="article-card__blog-action"
                  onClick={(event) => event.stopPropagation()}
                >
                  {blogAction}
                </span>
              ) : null}
            </div>
            <p className="article-card__domain">{extractDomain(article.url)}</p>
          </div>
        </div>
        <div className="article-card__meta">
          <time className="article-card__date">{article.createdDate}</time>
          {onToggleReadLater ? (
            <button
              type="button"
              className={
                readLaterActive
                  ? "bookmark-toggle bookmark-toggle--active"
                  : "bookmark-toggle"
              }
              aria-label={
                readLaterActive
                  ? "나중에 읽기에서 제거"
                  : "나중에 읽기에 저장"
              }
              title={
                readLaterActive
                  ? "나중에 읽기에서 제거"
                  : "나중에 읽기에 저장"
              }
              disabled={busy}
              onClick={(event) => {
                event.stopPropagation();
                onToggleReadLater(article, !readLaterActive);
              }}
            >
              <svg
                viewBox="0 0 24 24"
                width="14"
                height="14"
                aria-hidden="true"
                focusable="false"
              >
                <path
                  d="M6 4.5a1.5 1.5 0 0 1 1.5-1.5h9A1.5 1.5 0 0 1 18 4.5v16.04a.5.5 0 0 1-.79.41L12 17.1l-5.21 3.85a.5.5 0 0 1-.79-.41z"
                  fill="currentColor"
                />
              </svg>
            </button>
          ) : null}
        </div>
      </header>

      <h3 className="article-card__title">{article.title}</h3>
      <p className="article-card__summary">{article.summary || "요약이 아직 준비되지 않았습니다."}</p>

      {article.keywords.length > 0 ? (
        <ul className="article-card__keywords">
          {article.keywords.map((keyword, index) => (
            <li key={`${keyword}-${index}`}>
              {onKeywordClick ? (
                <button
                  type="button"
                  className={
                    activeKeywords?.has(keyword)
                      ? "article-card__keyword-chip article-card__keyword-chip--active"
                      : "article-card__keyword-chip"
                  }
                  onClick={(event) => {
                    event.stopPropagation();
                    onKeywordClick(keyword);
                  }}
                >
                  #{keyword}
                </button>
              ) : (
                <span className="article-card__keyword-label">#{keyword}</span>
              )}
            </li>
          ))}
        </ul>
      ) : null}

      {bottomAction ? (
        <div
          className="article-card__bottom-action"
          onClick={(event) => event.stopPropagation()}
        >
          {bottomAction}
        </div>
      ) : null}

    </article>
  );
}
