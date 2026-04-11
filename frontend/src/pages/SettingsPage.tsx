import { useEffect, useMemo, useRef, useState } from "react";
import {
  ApiError,
  createBlog,
  getAllBlogs,
  getSubscriptions,
  searchBlogs,
  subscribeBlog,
  unsubscribeBlog,
} from "../api";
import {
  BlogResponse,
  SessionState,
  SubscriptionResponse,
} from "../types";
import { EmptyState } from "../components/EmptyState";
import { Toast } from "../components/Toast";

type SettingsPageProps = {
  session: SessionState;
  loginUrl: string;
};

type SettingsToast = {
  id: number;
  message: string;
  tone: "success" | "error" | "info";
  durationMs?: number;
};

const SUBSCRIPTION_COLLAPSE_COUNT = 8;

function BlogListMeta({ blog }: { blog: SubscriptionResponse["blog"] | BlogResponse }) {
  const image = blog.image?.trim() || null;
  const [isImageBroken, setIsImageBroken] = useState(false);

  useEffect(() => {
    setIsImageBroken(false);
  }, [image]);

  const shouldShowImage = !!image && !isImageBroken;
  return (
    <div className="blog-list-meta">
      <div className="blog-list-meta__image-wrap">
        {shouldShowImage ? (
          <img
            src={image as string}
            alt={blog.name}
            className="blog-list-meta__image"
            onError={() => setIsImageBroken(true)}
          />
        ) : (
          <div className="blog-list-meta__image blog-list-meta__image--fallback">
            {blog.name.slice(0, 1).toUpperCase()}
          </div>
        )}
      </div>
      <div className="blog-list-meta__text">
        <strong>{blog.name}</strong>
        <p>{blog.url}</p>
      </div>
    </div>
  );
}

export function SettingsPage({ session, loginUrl }: SettingsPageProps) {
  const [loading, setLoading] = useState<boolean>(false);
  const [toast, setToast] = useState<SettingsToast | null>(null);
  const [subscriptions, setSubscriptions] = useState<SubscriptionResponse[]>([]);
  const [allBlogs, setAllBlogs] = useState<BlogResponse[]>([]);
  const [locallyUnsubscribedBlogIds, setLocallyUnsubscribedBlogIds] = useState<Set<number>>(new Set());
  const [searchQuery, setSearchQuery] = useState<string>("");
  const [hasSearched, setHasSearched] = useState<boolean>(false);
  const [searchResults, setSearchResults] = useState<SubscriptionResponse["blog"][]>([]);
  const [searching, setSearching] = useState<boolean>(false);
  const [newBlogUrl, setNewBlogUrl] = useState<string>("");
  const [creatingBlog, setCreatingBlog] = useState<boolean>(false);
  const [bulkSubscribing, setBulkSubscribing] = useState<boolean>(false);
  const [submittingBlogId, setSubmittingBlogId] = useState<number | null>(null);
  const [showAllSubscriptions, setShowAllSubscriptions] = useState<boolean>(false);
  const toastTimerRef = useRef<number | null>(null);
  const toastSeqRef = useRef<number>(0);

  const token = session.token;
  const isAuthenticated = session.status === "authenticated" && !!token;

  const activeSubscribedBlogIds = useMemo(
    () =>
      new Set(
        subscriptions
          .filter((subscription) => !locallyUnsubscribedBlogIds.has(subscription.blog.id))
          .map((subscription) => subscription.blog.id)
      ),
    [subscriptions, locallyUnsubscribedBlogIds]
  );
  const activeSubscriptionCount = useMemo(
    () =>
      subscriptions.filter((subscription) => !locallyUnsubscribedBlogIds.has(subscription.blog.id))
        .length,
    [subscriptions, locallyUnsubscribedBlogIds]
  );
  const orderedSubscriptions = useMemo(
    () =>
      [...subscriptions].sort((a, b) =>
        a.blog.name.localeCompare(b.blog.name, "ko")
      ),
    [subscriptions]
  );
  const visibleSubscriptions = useMemo(
    () =>
      showAllSubscriptions
        ? orderedSubscriptions
        : orderedSubscriptions.slice(0, SUBSCRIPTION_COLLAPSE_COUNT),
    [showAllSubscriptions, orderedSubscriptions]
  );
  const hasMoreSubscriptions =
    orderedSubscriptions.length > SUBSCRIPTION_COLLAPSE_COUNT;
  const recommendedBlogs = useMemo(
    () =>
      allBlogs
        .filter((blog) => !activeSubscribedBlogIds.has(blog.id))
        .sort((a, b) => {
          if (a.isShowOnMain && !b.isShowOnMain) return -1;
          if (!a.isShowOnMain && b.isShowOnMain) return 1;
          return a.name.localeCompare(b.name, "ko");
        })
        .slice(0, 12),
    [allBlogs, activeSubscribedBlogIds]
  );

  useEffect(() => {
    return () => {
      if (toastTimerRef.current !== null) {
        window.clearTimeout(toastTimerRef.current);
      }
    };
  }, []);

  function showToast(
    message: string,
    tone: "success" | "error" | "info" = "info",
    durationMs = 2600
  ) {
    if (toastTimerRef.current !== null) {
      window.clearTimeout(toastTimerRef.current);
    }
    const next = { id: toastSeqRef.current + 1, message, tone, durationMs };
    toastSeqRef.current = next.id;
    setToast(next);
    toastTimerRef.current = window.setTimeout(() => {
      setToast(null);
      toastTimerRef.current = null;
    }, durationMs);
  }

  useEffect(() => {
    async function bootstrap() {
      if (!token) return;
      setLoading(true);
      try {
        const mySubscriptions = await getSubscriptions(token);
        setLocallyUnsubscribedBlogIds(new Set());
        setSubscriptions(mySubscriptions);
        setShowAllSubscriptions(false);
        try {
          const blogs = await getAllBlogs();
          setAllBlogs(blogs);
        } catch (_) {
          setAllBlogs([]);
        }
      } catch (error) {
        const message =
          error instanceof ApiError
            ? error.message
            : "설정 정보를 불러오지 못했습니다.";
        showToast(message, "error");
      } finally {
        setLoading(false);
      }
    }

    bootstrap();
  }, [token]);

  async function handleSearchBlogs() {
    const trimmed = searchQuery.trim();
    if (!trimmed) {
      setHasSearched(false);
      setSearchResults([]);
      return;
    }

    setSearching(true);
    setHasSearched(true);
    try {
      const results = await searchBlogs(trimmed);
      setSearchResults(results);
    } catch (error) {
      const message =
        error instanceof ApiError ? error.message : "블로그 검색에 실패했습니다.";
      showToast(message, "error");
    } finally {
      setSearching(false);
    }
  }

  async function handleSubscribe(blogId: number) {
    if (!token) return;
    setSubmittingBlogId(blogId);
    try {
      const created = await subscribeBlog(token, blogId);
      setLocallyUnsubscribedBlogIds((prev) => {
        const next = new Set(prev);
        next.delete(blogId);
        return next;
      });
      setSubscriptions((prev) =>
        prev.some((subscription) => subscription.blog.id === blogId)
          ? prev
          : [...prev, created]
      );
      showToast("구독이 추가되었습니다.", "success");
    } catch (error) {
      const message =
        error instanceof ApiError ? error.message : "구독 추가에 실패했습니다.";
      showToast(message, "error");
    } finally {
      setSubmittingBlogId(null);
    }
  }

  async function handleUnsubscribe(blogId: number) {
    if (!token) return;
    setSubmittingBlogId(blogId);
    try {
      await unsubscribeBlog(token, blogId);
      setLocallyUnsubscribedBlogIds((prev) => {
        const next = new Set(prev);
        next.add(blogId);
        return next;
      });
      showToast("구독이 해제되었습니다.", "info");
    } catch (error) {
      const message =
        error instanceof ApiError ? error.message : "구독 해제에 실패했습니다.";
      showToast(message, "error");
    } finally {
      setSubmittingBlogId(null);
    }
  }

  async function handleCreateBlog() {
    if (!token) return;
    const url = newBlogUrl.trim();
    if (!url) {
      showToast("추가할 블로그 URL을 입력해 주세요.", "error");
      return;
    }

    setCreatingBlog(true);
    showToast("블로그 정보를 수집 중입니다. 최대 1분 정도 걸릴 수 있어요.", "info", 5200);
    try {
      const createdBlog = await createBlog(token, url);
      if (!activeSubscribedBlogIds.has(createdBlog.id)) {
        const createdSubscription = await subscribeBlog(token, createdBlog.id);
        setLocallyUnsubscribedBlogIds((prev) => {
          const next = new Set(prev);
          next.delete(createdBlog.id);
          return next;
        });
        setSubscriptions((prev) =>
          prev.some((subscription) => subscription.blog.id === createdBlog.id)
            ? prev
            : [...prev, createdSubscription]
        );
      }
      setNewBlogUrl("");
      setHasSearched(false);
      setSearchQuery("");
      setSearchResults([]);
      showToast("블로그 등록과 구독이 완료되었습니다.", "success");
    } catch (error) {
      const message =
        error instanceof ApiError
          ? error.message
          : "블로그 등록에 실패했습니다.";
      showToast(message, "error");
    } finally {
      setCreatingBlog(false);
    }
  }

  async function handleSubscribeAllRecommended() {
    if (!token) return;
    const targets = recommendedBlogs
      .filter((blog) => !activeSubscribedBlogIds.has(blog.id))
      .map((blog) => blog.id);
    if (targets.length === 0) {
      showToast("이미 모두 구독 중입니다.", "info");
      return;
    }

    setBulkSubscribing(true);
    try {
      const results = await Promise.allSettled(
        targets.map((blogId) => subscribeBlog(token, blogId))
      );
      const succeeded = results
        .filter(
          (result): result is PromiseFulfilledResult<SubscriptionResponse> =>
            result.status === "fulfilled"
        )
        .map((result) => result.value);
      const failedCount = results.length - succeeded.length;

      if (succeeded.length > 0) {
        setLocallyUnsubscribedBlogIds((prev) => {
          const next = new Set(prev);
          succeeded.forEach((subscription) => next.delete(subscription.blog.id));
          return next;
        });
        setSubscriptions((prev) => {
          const map = new Map(prev.map((subscription) => [subscription.blog.id, subscription]));
          succeeded.forEach((subscription) => {
            map.set(subscription.blog.id, subscription);
          });
          return Array.from(map.values());
        });
      }

      if (failedCount === 0) {
        showToast(`${succeeded.length}개 블로그를 모두 구독했어요.`, "success");
      } else {
        showToast(
          `${succeeded.length}개 구독 완료, ${failedCount}개는 실패했어요.`,
          "info",
          3200
        );
      }
    } catch (error) {
      const message =
        error instanceof ApiError ? error.message : "전체 구독 처리에 실패했습니다.";
      showToast(message, "error");
    } finally {
      setBulkSubscribing(false);
    }
  }

  if (!isAuthenticated) {
    return (
      <section className="page-section">
        <EmptyState
          title="로그인 후 설정할 수 있어요"
          description="구독 설정은 로그인한 사용자에게 저장됩니다."
          action={
            <a className="btn btn--primary" href={loginUrl}>
              Google로 로그인
            </a>
          }
        />
      </section>
    );
  }

  return (
    <section className="page-section">
      <div className="page-head">
        <div>
          <p className="eyebrow">SETTINGS</p>
          <h1>구독 설정</h1>
        </div>
      </div>

      {loading ? <p className="status-text">설정을 불러오는 중...</p> : null}

      <div className="settings-grid">
        <section className="panel">
          <h2>구독중인 블로그</h2>
          <p className="panel__description">
            현재 {activeSubscriptionCount}개의 블로그를 구독 중입니다.
          </p>
          {orderedSubscriptions.length === 0 ? (
            <EmptyState
              title="구독 블로그가 없습니다"
              description="아직 구독 중인 블로그가 없어요. 아래에서 블로그를 검색하거나 URL을 추가해 시작해 보세요."
            />
          ) : (
            <ul className="list">
              {visibleSubscriptions.map((subscription) => (
                <li key={subscription.id} className="list__item">
                  <BlogListMeta blog={subscription.blog} />
                  {activeSubscribedBlogIds.has(subscription.blog.id) ? (
                    <button
                      type="button"
                      className="btn btn--danger btn--small"
                      disabled={submittingBlogId === subscription.blog.id}
                      onClick={() => handleUnsubscribe(subscription.blog.id)}
                    >
                      구독 해제
                    </button>
                  ) : (
                    <button
                      type="button"
                      className="btn btn--primary btn--small"
                      disabled={submittingBlogId === subscription.blog.id}
                      onClick={() => handleSubscribe(subscription.blog.id)}
                    >
                      다시 구독
                    </button>
                  )}
                </li>
              ))}
            </ul>
          )}
          {hasMoreSubscriptions ? (
            <div className="list-expand-wrap">
              <button
                type="button"
                className="btn btn--ghost btn--small"
                onClick={() => setShowAllSubscriptions((prev) => !prev)}
              >
                {showAllSubscriptions
                  ? "접기"
                  : `펼치기 (${orderedSubscriptions.length - SUBSCRIPTION_COLLAPSE_COUNT}개 더)`}
              </button>
            </div>
          ) : null}
        </section>

        <section className="panel">
          <h2>구독할 블로그 검색</h2>
          <p className="panel__description">
            먼저 검색해 보고, 결과가 없을 때 URL을 추가해 등록할 수 있습니다.
          </p>
          <div className="form-row">
            <input
              value={searchQuery}
              onChange={(event) => setSearchQuery(event.target.value)}
              onKeyDown={(event) => {
                if (event.key === "Enter") {
                  handleSearchBlogs();
                }
              }}
              placeholder="이미 등록된 블로그 검색"
            />
            <button
              type="button"
              className="btn btn--ghost"
              disabled={searching}
              onClick={handleSearchBlogs}
            >
              {searching ? "검색 중..." : "검색"}
            </button>
          </div>

          {searchResults.length > 0 ? (
            <ul className="list">
              {searchResults.map((blog) => (
                <li key={blog.id} className="list__item">
                  <BlogListMeta blog={blog} />
                  {activeSubscribedBlogIds.has(blog.id) ? (
                    <button
                      type="button"
                      className="btn btn--danger btn--small"
                      disabled={submittingBlogId === blog.id}
                      onClick={() => handleUnsubscribe(blog.id)}
                    >
                      구독 해제
                    </button>
                  ) : (
                    <button
                      type="button"
                      className="btn btn--primary btn--small"
                      disabled={submittingBlogId === blog.id}
                      onClick={() => handleSubscribe(blog.id)}
                    >
                      구독
                    </button>
                  )}
                </li>
              ))}
            </ul>
          ) : null}

          {hasSearched && searchResults.length === 0 ? (
            <div className="add-blog-wrap">
              <p className="panel__description">
                검색 결과가 없어요. 블로그 URL을 직접 추가해보세요.
              </p>
              <p className="panel__description panel__description--pending">
                블로그 등록 시 메타데이터 수집 때문에 최대 1분 정도 걸릴 수 있습니다.
              </p>
              {creatingBlog ? (
                <p className="panel__description panel__description--progress">
                  등록을 진행 중입니다. 잠시만 기다려 주세요...
                </p>
              ) : null}
              <div className="form-row">
                <input
                  value={newBlogUrl}
                  onChange={(event) => setNewBlogUrl(event.target.value)}
                  placeholder="신규 블로그 URL 추가 (예: https://example.com)"
                />
                <button
                  type="button"
                  className="btn btn--primary"
                  disabled={creatingBlog}
                  onClick={handleCreateBlog}
                >
                  {creatingBlog ? "추가 중..." : "URL 추가"}
                </button>
              </div>
            </div>
          ) : null}
        </section>
      </div>

      <section className="panel">
        <h2>이런 블로그는 어때요?</h2>
        <div className="panel-head-inline">
          <p className="panel__description">
            아직 구독하지 않은 블로그를 모아봤어요.
          </p>
          {recommendedBlogs.length > 0 ? (
            <button
              type="button"
              className="btn btn--primary btn--small"
              disabled={bulkSubscribing}
              onClick={handleSubscribeAllRecommended}
            >
              {bulkSubscribing ? "전체 구독 중..." : "전체 구독"}
            </button>
          ) : null}
        </div>
        {recommendedBlogs.length === 0 ? (
          <EmptyState
            title="추천할 블로그가 없어요"
            description="이미 모든 블로그를 구독 중이거나 아직 표시 가능한 블로그가 없습니다."
          />
        ) : (
          <ul className="list">
            {recommendedBlogs.map((blog) => (
              <li key={blog.id} className="list__item">
                <BlogListMeta blog={blog} />
                <button
                  type="button"
                  className="btn btn--primary btn--small"
                  disabled={submittingBlogId === blog.id}
                  onClick={() => handleSubscribe(blog.id)}
                >
                  구독
                </button>
              </li>
            ))}
          </ul>
        )}
      </section>

      {toast ? (
        <Toast
          key={toast.id}
          message={toast.message}
          tone={toast.tone}
          durationMs={toast.durationMs}
          onClose={() => setToast(null)}
        />
      ) : null}
    </section>
  );
}
