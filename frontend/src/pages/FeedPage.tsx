import { useEffect, useMemo, useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  addReadLater,
  ApiError,
  getArticles,
  getAllBlogs,
  getFeedKeywordCounts,
  getSubscriptions,
  markArticleRead,
  reportArticle,
  removeReadLater,
  subscribeBlog,
} from "../api";
import { ArticleResponse, BlogResponse, FeedKeywordCountResponse, SessionState } from "../types";
import { ArticleCard } from "../components/ArticleCard";
import { EmptyState } from "../components/EmptyState";
import { Toast } from "../components/Toast";

type FeedPageProps = {
  session: SessionState;
  loginUrl: string;
};

const DEFAULT_FROM_DATE = "2000-01-01";
const DEFAULT_TOAST_DURATION = 2800;
const KEYWORD_COLLAPSE_MAX = 10;
const KEYWORD_COLLAPSE_STEP = 10;
const FEED_RECOMMENDED_BLOG_LIMIT = 6;
const REPORT_REASON_OPTIONS = [
  { value: "요약 결과가 이상해요.", label: "요약 결과가 이상해요." },
  { value: "부적절한 내용이에요.", label: "부적절한 내용이에요." },
] as const;

type FeedToast = {
  id: number;
  message: string;
  tone: "success" | "error" | "info";
  actionLabel?: string;
  action?: "moveReadLater";
  durationMs?: number;
};

export function FeedPage({ session, loginUrl }: FeedPageProps) {
  const navigate = useNavigate();
  const [mode, setMode] = useState<"my" | "recommend">(
    session.status === "authenticated" ? "my" : "recommend"
  );
  const [selectedKeywords, setSelectedKeywords] = useState<string[]>([]);
  const [isCompactKeywordLayout, setIsCompactKeywordLayout] = useState<boolean>(
    typeof window !== "undefined" ? window.innerWidth < 1100 : false
  );
  const [compactKeywordVisibleCount, setCompactKeywordVisibleCount] =
    useState<number>(KEYWORD_COLLAPSE_MAX);
  const [items, setItems] = useState<ArticleResponse[]>([]);
  const [nextCursor, setNextCursor] = useState<number | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [loadingMore, setLoadingMore] = useState<boolean>(false);
  const [toast, setToast] = useState<FeedToast | null>(null);
  const [readLaterPendingArticleId, setReadLaterPendingArticleId] = useState<number | null>(null);
  const [reportingArticleId, setReportingArticleId] = useState<number | null>(null);
  const [reportModalArticle, setReportModalArticle] = useState<ArticleResponse | null>(null);
  const [reportReason, setReportReason] = useState<string>(REPORT_REASON_OPTIONS[0].value);
  const [reportDetail, setReportDetail] = useState("");
  const [allBlogs, setAllBlogs] = useState<BlogResponse[]>([]);
  const [loadingRecommendedBlogs, setLoadingRecommendedBlogs] = useState<boolean>(false);
  const [subscribedBlogIds, setSubscribedBlogIds] = useState<Set<number>>(new Set());
  const [subscribingBlogId, setSubscribingBlogId] = useState<number | null>(null);
  const [keywordCounts, setKeywordCounts] = useState<FeedKeywordCountResponse[]>([]);
  const toastTimerRef = useRef<number | null>(null);
  const toastSeqRef = useRef<number>(0);

  const isAuthenticated = session.status === "authenticated" && !!session.token;

  useEffect(() => {
    return () => {
      if (toastTimerRef.current !== null) {
        window.clearTimeout(toastTimerRef.current);
      }
    };
  }, []);

  function showToast(nextToast: Omit<FeedToast, "id">, autoHide = true) {
    if (toastTimerRef.current !== null) {
      window.clearTimeout(toastTimerRef.current);
      toastTimerRef.current = null;
    }
    const next = { id: toastSeqRef.current + 1, ...nextToast };
    toastSeqRef.current = next.id;
    setToast(next);
    if (!autoHide) return;
    const durationMs = nextToast.durationMs ?? DEFAULT_TOAST_DURATION;
    toastTimerRef.current = window.setTimeout(() => {
      setToast(null);
      toastTimerRef.current = null;
    }, durationMs);
  }

  useEffect(() => {
    if (!isAuthenticated && mode === "my") {
      setMode("recommend");
    }
  }, [isAuthenticated, mode]);

  useEffect(() => {
    function handleResize() {
      setIsCompactKeywordLayout(window.innerWidth < 1100);
    }
    handleResize();
    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  useEffect(() => {
    if (isCompactKeywordLayout) {
      setCompactKeywordVisibleCount(KEYWORD_COLLAPSE_MAX);
    }
  }, [isCompactKeywordLayout]);

  useEffect(() => {
    async function loadSubscriptions() {
      if (!isAuthenticated || !session.token) {
        setSubscribedBlogIds(new Set());
        return;
      }
      try {
        const subscriptions = await getSubscriptions(session.token);
        setSubscribedBlogIds(new Set(subscriptions.map((subscription) => subscription.blog.id)));
      } catch (_) {
        // 구독 정보 조회 실패 시 피드 동작을 막지 않음
      }
    }
    loadSubscriptions();
  }, [isAuthenticated, session.token]);

  useEffect(() => {
    if (!isAuthenticated || mode !== "recommend" || allBlogs.length > 0) return;
    let isCancelled = false;
    setLoadingRecommendedBlogs(true);
    async function loadAllBlogs() {
      try {
        const blogs = await getAllBlogs();
        if (!isCancelled) {
          setAllBlogs(blogs);
        }
      } catch (_) {
        if (!isCancelled) {
          setAllBlogs([]);
        }
      } finally {
        if (!isCancelled) {
          setLoadingRecommendedBlogs(false);
        }
      }
    }
    void loadAllBlogs();
    return () => {
      isCancelled = true;
    };
  }, [allBlogs.length, isAuthenticated, mode]);

  useEffect(() => {
    if (!isAuthenticated || mode !== "recommend") {
      setLoadingRecommendedBlogs(false);
    }
  }, [isAuthenticated, mode]);

  useEffect(() => {
    async function loadKeywordCounts() {
      if (mode === "my" && (!isAuthenticated || !session.token)) {
        setKeywordCounts([]);
        return;
      }
      try {
        const counts = await getFeedKeywordCounts({
          token: session.token,
          from: DEFAULT_FROM_DATE,
          myOnly: mode === "my",
        });
        setKeywordCounts(counts);
      } catch (_) {
        setKeywordCounts([]);
      }
    }
    void loadKeywordCounts();
  }, [isAuthenticated, mode, session.token]);

  const canLoadFeed = mode === "recommend" || isAuthenticated;

  async function fetchPage(append: boolean) {
    if (!canLoadFeed) return;

    if (append) {
      setLoadingMore(true);
    } else {
      setLoading(true);
    }

    try {
      const response = await getArticles({
        token: session.token,
        from: DEFAULT_FROM_DATE,
        next: append ? nextCursor : null,
        size: 20,
        myOnly: mode === "my",
        keywords: selectedKeywords.length > 0 ? selectedKeywords : undefined,
      });

      setItems((prev) => (append ? [...prev, ...response.items] : response.items));
      setNextCursor(response.next);
    } catch (error) {
      const message =
        error instanceof ApiError
          ? error.message
          : "피드를 불러오는 중 오류가 발생했습니다.";
      showToast({ message, tone: "error" });
    } finally {
      setLoading(false);
      setLoadingMore(false);
    }
  }

  useEffect(() => {
    fetchPage(false);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [mode, session.token, selectedKeywords]);

  async function handleToggleReadLater(article: ArticleResponse, nextValue: boolean) {
    if (!isAuthenticated || !session.token) {
      const shouldMove = window.confirm(
        "나중에 읽기는 로그인 후 사용할 수 있어요.\n로그인 화면으로 이동할까요?"
      );
      if (shouldMove) {
        window.location.href = loginUrl;
      }
      return;
    }
    setReadLaterPendingArticleId(article.id);

    try {
      if (nextValue) {
        await addReadLater(session.token, article.id);
        showToast(
          {
            message: "나중에 읽기 목록에 저장했어요.",
            tone: "success",
            actionLabel: "이동하기",
            action: "moveReadLater",
            durationMs: 4600,
          }
        );
      } else {
        await removeReadLater(session.token, article.id);
        showToast({ message: "나중에 읽기에서 해제했어요.", tone: "info" });
      }
      setItems((prev) =>
        prev.map((it) =>
          it.id === article.id ? { ...it, isReadLater: nextValue } : it
        )
      );
    } catch (error) {
      const message =
        error instanceof ApiError ? error.message : "나중에 읽기 처리에 실패했습니다.";
      showToast({ message, tone: "error" });
    } finally {
      setReadLaterPendingArticleId(null);
    }
  }

  async function handleSubscribeBlog(blogId: number) {
    if (!isAuthenticated || !session.token) return;
    setSubscribingBlogId(blogId);
    try {
      await subscribeBlog(session.token, blogId);
      setSubscribedBlogIds((prev) => {
        const nextSet = new Set(prev);
        nextSet.add(blogId);
        return nextSet;
      });
      showToast({ message: "블로그를 구독했습니다.", tone: "success" });
    } catch (error) {
      const message =
        error instanceof ApiError ? error.message : "블로그 구독에 실패했습니다.";
      showToast({ message, tone: "error" });
    } finally {
      setSubscribingBlogId(null);
    }
  }

  function handleKeywordClick(keyword: string) {
    setSelectedKeywords((prev) =>
      prev.includes(keyword)
        ? prev.filter((item) => item !== keyword)
        : [...prev, keyword]
    );
  }

  function openReportModal(article: ArticleResponse) {
    if (!isAuthenticated || !session.token) {
      const shouldMove = window.confirm(
        "글 신고는 로그인 후 이용할 수 있어요.\n로그인 화면으로 이동할까요?"
      );
      if (shouldMove) {
        window.location.href = loginUrl;
      }
      return;
    }

    setReportModalArticle(article);
    setReportReason(REPORT_REASON_OPTIONS[0].value);
    setReportDetail("");
  }

  function closeReportModal() {
    if (reportingArticleId !== null) return;
    setReportModalArticle(null);
    setReportReason(REPORT_REASON_OPTIONS[0].value);
    setReportDetail("");
  }

  async function submitReport() {
    if (!isAuthenticated || !session.token || !reportModalArticle) return;
    const articleId = reportModalArticle.id;

    setReportingArticleId(articleId);
    try {
      await reportArticle(
        session.token,
        articleId,
        reportReason,
        reportDetail.trim() ? reportDetail.trim().slice(0, 1000) : undefined
      );
      setReportModalArticle(null);
      setReportReason(REPORT_REASON_OPTIONS[0].value);
      setReportDetail("");
      showToast({ message: "신고가 접수되었습니다.", tone: "success" });
    } catch (error) {
      const message =
        error instanceof ApiError ? error.message : "신고 접수에 실패했습니다.";
      showToast({ message, tone: "error" });
    } finally {
      setReportingArticleId(null);
    }
  }

  const title = useMemo(() => {
    if (mode === "my") return "내 피드";
    return "추천 피드";
  }, [mode]);

  const selectedKeywordSet = useMemo(() => {
    return new Set(selectedKeywords);
  }, [selectedKeywords]);

  const keywordOptions = useMemo(() => {
    const counts = new Map<string, number>();
    keywordCounts.forEach((item) => {
      counts.set(item.keyword, item.count);
    });
    selectedKeywords.forEach((keyword) => {
      if (!counts.has(keyword)) {
        counts.set(keyword, 0);
      }
    });
    return Array.from(counts.entries())
      .sort((a, b) => {
        if (a[1] === b[1]) return a[0].localeCompare(b[0], "ko");
        return b[1] - a[1];
      })
      .map(([value, count]) => ({ value, count }));
  }, [keywordCounts, selectedKeywords]);

  const recommendedBlogs = useMemo(() => {
    if (mode !== "recommend" || !isAuthenticated) return [];
    return allBlogs
      .filter((blog) => !subscribedBlogIds.has(blog.id))
      .sort((a, b) => {
        if (a.isShowOnMain && !b.isShowOnMain) return -1;
        if (!a.isShowOnMain && b.isShowOnMain) return 1;
        return a.name.localeCompare(b.name, "ko");
      })
      .slice(0, FEED_RECOMMENDED_BLOG_LIMIT);
  }, [allBlogs, isAuthenticated, mode, subscribedBlogIds]);

  const isKeywordFiltering = selectedKeywords.length > 0;
  const visibleKeywordLimit = isCompactKeywordLayout
    ? Math.min(compactKeywordVisibleCount, keywordOptions.length)
    : keywordOptions.length;
  const visibleKeywordOptions = useMemo(() => {
    return keywordOptions.slice(0, visibleKeywordLimit);
  }, [keywordOptions, visibleKeywordLimit]);
  const hiddenKeywordCount = Math.max(0, keywordOptions.length - visibleKeywordOptions.length);
  const canExpandKeywordList = isCompactKeywordLayout && hiddenKeywordCount > 0;
  const nextExpandCount = Math.min(KEYWORD_COLLAPSE_STEP, hiddenKeywordCount);

  return (
    <section className="page-section">
      <div className="page-head">
        <div>
          <h1>{title}</h1>
        </div>
        <div className="segmented">
          <button
            type="button"
            className={mode === "my" ? "segmented__item segmented__item--active" : "segmented__item"}
            onClick={() => {
              if (!isAuthenticated) {
                const shouldMove = window.confirm(
                  "내 구독 피드는 로그인 후 이용할 수 있어요.\n로그인 화면으로 이동할까요?"
                );
                if (shouldMove) {
                  window.location.href = loginUrl;
                }
                return;
              }
              setMode("my");
            }}
            title={isAuthenticated ? "" : "로그인이 필요합니다"}
          >
            내 구독
          </button>
          <button
            type="button"
            className={
              mode === "recommend" ? "segmented__item segmented__item--active" : "segmented__item"
            }
            onClick={() => setMode("recommend")}
          >
            추천
          </button>
        </div>
      </div>

      <div className="feed-layout">
        <aside className="feed-keyword-panel" aria-label="키워드 필터">
          <div className="feed-keyword-panel__head">
            <h2>키워드</h2>
            {isKeywordFiltering ? (
              <button
                type="button"
                className="btn btn--ghost btn--tiny"
                onClick={() => setSelectedKeywords([])}
              >
                전체 해제
              </button>
            ) : null}
          </div>
          <p className="feed-keyword-panel__selected">
            {isKeywordFiltering
              ? `${selectedKeywords.length}개 선택됨`
              : "여러 키워드를 동시에 선택할 수 있어요."}
          </p>
          {keywordOptions.length > 0 ? (
            <ul className="feed-keyword-list">
              {visibleKeywordOptions.map(({ value, count }) => {
                const isActive = selectedKeywordSet.has(value);
                return (
                  <li key={value}>
                    <button
                      type="button"
                      className={
                        isActive
                          ? "feed-keyword-item feed-keyword-item--active"
                          : "feed-keyword-item"
                      }
                      aria-pressed={isActive}
                      onClick={() => handleKeywordClick(value)}
                    >
                      <span className="feed-keyword-item__name">#{value}</span>
                      <span className="feed-keyword-item__count">{count}</span>
                    </button>
                  </li>
                );
              })}
            </ul>
          ) : (
            <p className="feed-keyword-panel__empty">
              키워드가 붙은 글을 불러오면 여기서 선택할 수 있어요.
            </p>
          )}
          {canExpandKeywordList ? (
            <button
              type="button"
              className="btn btn--ghost btn--tiny feed-keyword-panel__toggle"
              onClick={() =>
                setCompactKeywordVisibleCount((prev) =>
                  Math.min(prev + KEYWORD_COLLAPSE_STEP, keywordOptions.length)
                )
              }
            >
              {nextExpandCount}개 더 보기
            </button>
          ) : null}
        </aside>

        <div className="feed-main">
          {!isAuthenticated && mode === "my" ? (
            <EmptyState
              title="로그인 후 내 피드를 볼 수 있어요"
              description="Google 로그인 후 구독한 블로그의 글만 모아서 확인할 수 있습니다."
              action={
                <a className="btn btn--primary" href={loginUrl}>
                  Google로 로그인
                </a>
              }
            />
          ) : null}

          {loading ? <p className="status-text">피드를 불러오는 중...</p> : null}

          {mode === "recommend" && isAuthenticated ? (
            <section className="panel feed-blog-recommendation" aria-label="블로그 추천">
              <div className="feed-blog-recommendation__head">
                <h2>추천 블로그</h2>
                <p>아직 구독하지 않은 블로그를 먼저 확인해 보세요.</p>
              </div>
              {loadingRecommendedBlogs ? (
                <p className="feed-blog-recommendation__empty">추천 블로그를 불러오는 중...</p>
              ) : recommendedBlogs.length === 0 ? (
                <p className="feed-blog-recommendation__empty">
                  지금은 추가로 추천할 블로그가 없어요. 설정에서 새 블로그를 찾아보세요.
                </p>
              ) : (
                <ul className="feed-blog-recommendation__list">
                  {recommendedBlogs.map((blog) => (
                    <li key={blog.id} className="feed-blog-recommendation__item">
                      <div className="feed-blog-recommendation__meta">
                        <strong>{blog.name}</strong>
                        <p>{blog.url}</p>
                      </div>
                      <button
                        type="button"
                        className="btn btn--primary btn--tiny"
                        disabled={subscribingBlogId === blog.id}
                        onClick={() => handleSubscribeBlog(blog.id)}
                      >
                        구독
                      </button>
                    </li>
                  ))}
                </ul>
              )}
            </section>
          ) : null}

          {!loading && items.length === 0 ? (
            <EmptyState
              title={isKeywordFiltering ? "선택한 키워드 글이 없어요" : "아직 표시할 글이 없어요"}
              description={
                isKeywordFiltering
                  ? "다른 키워드를 선택하거나 필터를 해제해 보세요."
                  : "설정 탭에서 블로그를 더 구독해 보세요."
              }
              action={
                isKeywordFiltering ? (
                  <button
                    type="button"
                    className="btn btn--ghost"
                    onClick={() => setSelectedKeywords([])}
                  >
                    필터 해제
                  </button>
                ) : (
                  <Link to="/settings" className="btn btn--primary">
                    구독 설정으로 이동
                  </Link>
                )
              }
            />
          ) : (
            <div className="articles-grid">
              {items.map((article) => (
                <ArticleCard
                  key={article.id}
                  article={article}
                  readLaterActive={article.isReadLater}
                  busy={readLaterPendingArticleId === article.id}
                  onOpen={(target) => {
                    window.open(target.url, "_blank", "noopener,noreferrer");
                    if (isAuthenticated && session.token) {
                      markArticleRead(session.token, target.id).catch(() => undefined);
                    }
                  }}
                  onToggleReadLater={(target, nextValue) =>
                    handleToggleReadLater(target as ArticleResponse, nextValue)
                  }
                  onKeywordClick={handleKeywordClick}
                  activeKeywords={selectedKeywordSet}
                  blogAction={
                    mode === "recommend" &&
                    isAuthenticated &&
                    !subscribedBlogIds.has(article.blog.id) ? (
                      <button
                        type="button"
                        className="btn btn--primary btn--tiny"
                        disabled={subscribingBlogId === article.blog.id}
                        onClick={() => handleSubscribeBlog(article.blog.id)}
                      >
                        구독
                      </button>
                    ) : null
                  }
                  bottomAction={
                    isAuthenticated ? (
                      <button
                        type="button"
                        className="btn btn--ghost btn--tiny btn--feed-report"
                        disabled={reportingArticleId === article.id}
                        onClick={() => openReportModal(article)}
                      >
                        신고하기
                      </button>
                    ) : null
                  }
                />
              ))}
            </div>
          )}

          {nextCursor !== null && !loading ? (
            <div className="more-wrap">
              <button
                type="button"
                className="btn btn--ghost"
                disabled={loadingMore}
                onClick={() => fetchPage(true)}
              >
                {loadingMore ? "불러오는 중..." : "더 보기"}
              </button>
            </div>
          ) : null}

          {nextCursor === null && !loading && items.length > 0 ? (
            <p className="feed-end-text">더 이상 불러올 글이 없습니다.</p>
          ) : null}
        </div>
      </div>

      {toast ? (
        <Toast
          key={toast.id}
          message={toast.message}
          tone={toast.tone}
          durationMs={toast.durationMs}
          actionLabel={toast.actionLabel}
          onAction={
            toast.action === "moveReadLater"
              ? () => {
                  setToast(null);
                  navigate("/read-later");
                }
              : undefined
          }
          onClose={() => setToast(null)}
        />
      ) : null}

      {reportModalArticle ? (
        <div
          className="modal-overlay"
          role="dialog"
          aria-modal="true"
          aria-labelledby="report-modal-title"
          onClick={() => closeReportModal()}
        >
          <div className="modal report-modal" onClick={(event) => event.stopPropagation()}>
            <h2 id="report-modal-title">신고하기</h2>
            <p className="report-modal__target">{reportModalArticle.title}</p>

            <label className="feed-report-form__label" htmlFor="report-modal-reason">
              신고 유형
            </label>
            <select
              id="report-modal-reason"
              className="feed-report-form__select"
              value={reportReason}
              onChange={(event) => setReportReason(event.target.value)}
              disabled={reportingArticleId === reportModalArticle.id}
            >
              {REPORT_REASON_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>

            <label className="feed-report-form__label" htmlFor="report-modal-detail">
              상세 설명 (선택)
            </label>
            <textarea
              id="report-modal-detail"
              className="feed-report-form__textarea"
              value={reportDetail}
              maxLength={1000}
              onChange={(event) => setReportDetail(event.target.value)}
              placeholder="어떤 점이 문제인지 입력해 주세요."
              disabled={reportingArticleId === reportModalArticle.id}
            />

            <div className="feed-report-form__actions">
              <span className="feed-report-form__counter">{reportDetail.length}/1000</span>
              <button
                type="button"
                className="btn btn--ghost btn--tiny"
                onClick={() => closeReportModal()}
                disabled={reportingArticleId === reportModalArticle.id}
              >
                취소
              </button>
              <button
                type="button"
                className="btn btn--danger btn--tiny"
                disabled={reportingArticleId === reportModalArticle.id}
                onClick={() => void submitReport()}
              >
                {reportingArticleId === reportModalArticle.id ? "신고 중..." : "신고 접수"}
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </section>
  );
}
