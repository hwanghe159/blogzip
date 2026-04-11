import { useEffect, useMemo, useRef, useState } from "react";
import {
  ApiError,
  createBlog,
  getAllBlogs,
  getMyProfile,
  getSubscriptions,
  searchBlogs,
  subscribeBlog,
  unsubscribeBlog,
  updateReceiveDays,
} from "../api";
import {
  BlogResponse,
  DayOfWeek,
  SessionState,
  SubscriptionResponse,
  UserResponse,
} from "../types";
import { EmptyState } from "../components/EmptyState";
import { Toast } from "../components/Toast";

const dayOrder: DayOfWeek[] = [
  "MONDAY",
  "TUESDAY",
  "WEDNESDAY",
  "THURSDAY",
  "FRIDAY",
  "SATURDAY",
  "SUNDAY",
];

const dayLabels: Record<DayOfWeek, string> = {
  MONDAY: "월",
  TUESDAY: "화",
  WEDNESDAY: "수",
  THURSDAY: "목",
  FRIDAY: "금",
  SATURDAY: "토",
  SUNDAY: "일",
};

type SettingsPageProps = {
  session: SessionState;
  loginUrl: string;
  onUserUpdated: (nextUser: UserResponse) => void;
};

type SettingsToast = {
  id: number;
  message: string;
  tone: "success" | "error" | "info";
  durationMs?: number;
};

const SUBSCRIPTION_COLLAPSE_COUNT = 8;

export function SettingsPage({ session, loginUrl, onUserUpdated }: SettingsPageProps) {
  const [loading, setLoading] = useState<boolean>(false);
  const [toast, setToast] = useState<SettingsToast | null>(null);
  const [dayNotice, setDayNotice] = useState<string | null>(null);
  const [dayNoticeTone, setDayNoticeTone] = useState<"success" | "error">("success");
  const [subscriptions, setSubscriptions] = useState<SubscriptionResponse[]>([]);
  const [allBlogs, setAllBlogs] = useState<BlogResponse[]>([]);
  const [locallyUnsubscribedBlogIds, setLocallyUnsubscribedBlogIds] = useState<Set<number>>(new Set());
  const [receiveDays, setReceiveDays] = useState<DayOfWeek[]>(session.user?.receiveDays ?? []);
  const [searchQuery, setSearchQuery] = useState<string>("");
  const [hasSearched, setHasSearched] = useState<boolean>(false);
  const [searchResults, setSearchResults] = useState<SubscriptionResponse["blog"][]>([]);
  const [searching, setSearching] = useState<boolean>(false);
  const [newBlogUrl, setNewBlogUrl] = useState<string>("");
  const [creatingBlog, setCreatingBlog] = useState<boolean>(false);
  const [savingDays, setSavingDays] = useState<boolean>(false);
  const [submittingBlogId, setSubmittingBlogId] = useState<number | null>(null);
  const [showAllSubscriptions, setShowAllSubscriptions] = useState<boolean>(false);
  const toastTimerRef = useRef<number | null>(null);
  const toastSeqRef = useRef<number>(0);
  const dayNoticeTimerRef = useRef<number | null>(null);

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
    setReceiveDays(session.user?.receiveDays ?? []);
  }, [session.user]);

  useEffect(() => {
    return () => {
      if (toastTimerRef.current !== null) {
        window.clearTimeout(toastTimerRef.current);
      }
      if (dayNoticeTimerRef.current !== null) {
        window.clearTimeout(dayNoticeTimerRef.current);
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
        const [profile, mySubscriptions] = await Promise.all([
          getMyProfile(token),
          getSubscriptions(token),
        ]);
        onUserUpdated(profile);
        setReceiveDays(profile.receiveDays);
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
  }, [token, onUserUpdated]);

  function toggleReceiveDay(day: DayOfWeek) {
    setReceiveDays((prev) =>
      prev.includes(day) ? prev.filter((item) => item !== day) : [...prev, day]
    );
  }

  async function handleSaveReceiveDays() {
    if (!token) return;
    setSavingDays(true);
    setDayNotice(null);
    try {
      const sorted = [...receiveDays].sort(
        (a, b) => dayOrder.indexOf(a) - dayOrder.indexOf(b)
      );
      const updated = await updateReceiveDays(token, sorted);
      onUserUpdated(updated);
      setReceiveDays(updated.receiveDays);
      setDayNotice("저장됨");
      setDayNoticeTone("success");
      if (dayNoticeTimerRef.current !== null) {
        window.clearTimeout(dayNoticeTimerRef.current);
      }
      dayNoticeTimerRef.current = window.setTimeout(() => {
        setDayNotice(null);
        dayNoticeTimerRef.current = null;
      }, 1800);
    } catch (error) {
      const message =
        error instanceof ApiError
          ? error.message
          : "이메일 수신 요일 저장에 실패했습니다.";
      setDayNotice(message);
      setDayNoticeTone("error");
      if (dayNoticeTimerRef.current !== null) {
        window.clearTimeout(dayNoticeTimerRef.current);
      }
      dayNoticeTimerRef.current = window.setTimeout(() => {
        setDayNotice(null);
        dayNoticeTimerRef.current = null;
      }, 2600);
    } finally {
      setSavingDays(false);
    }
  }

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

  if (!isAuthenticated) {
    return (
      <section className="page-section">
        <EmptyState
          title="로그인 후 설정할 수 있어요"
          description="구독 설정과 이메일 수신 요일은 로그인한 사용자에게 저장됩니다."
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
          <h1>구독/이메일 설정</h1>
        </div>
      </div>

      {loading ? <p className="status-text">설정을 불러오는 중...</p> : null}

      <div className="settings-grid">
        <section className="panel">
          <h2>이메일 수신 요일</h2>
          <p className="panel__description">
            선택한 요일 오전 9시에 새로운 글을 요약해서 보내드려요.
          </p>
          <p className="panel__description panel__description--email">
            수신 이메일: <strong>{session.user?.email}</strong>
          </p>
          <div className="day-picker">
            {dayOrder.map((day) => {
              const active = receiveDays.includes(day);
              return (
                <button
                  key={day}
                  type="button"
                  className={active ? "day-chip day-chip--active" : "day-chip"}
                  onClick={() => toggleReceiveDay(day)}
                >
                  {dayLabels[day]}
                </button>
              );
            })}
          </div>
          <div className="day-save-row">
            <button
              type="button"
              className="btn btn--primary btn--compact"
              disabled={savingDays}
              onClick={handleSaveReceiveDays}
            >
              {savingDays ? "저장 중..." : "저장"}
            </button>
            {dayNotice ? (
              <span
                className={
                  dayNoticeTone === "success"
                    ? "inline-hint inline-hint--success"
                    : "inline-hint inline-hint--error"
                }
              >
                {dayNotice}
              </span>
            ) : null}
          </div>
        </section>

        <section className="panel">
          <h2>구독중인 블로그</h2>
          <p className="panel__description">
            현재 {activeSubscriptionCount}개의 블로그를 구독 중입니다.
          </p>
          {orderedSubscriptions.length === 0 ? (
            <EmptyState
              title="구독 블로그가 없습니다"
              description="URL을 추가하거나 검색 결과에서 구독을 눌러 시작해 보세요."
            />
          ) : (
            <ul className="list">
              {visibleSubscriptions.map((subscription) => (
                <li key={subscription.id} className="list__item">
                  <div>
                    <strong>{subscription.blog.name}</strong>
                    <p>{subscription.blog.url}</p>
                  </div>
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
                  <div>
                    <strong>{blog.name}</strong>
                    <p>{blog.url}</p>
                  </div>
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
        <p className="panel__description">
          아직 구독하지 않은 블로그를 모아봤어요.
        </p>
        {recommendedBlogs.length === 0 ? (
          <EmptyState
            title="추천할 블로그가 없어요"
            description="이미 모든 블로그를 구독 중이거나 아직 표시 가능한 블로그가 없습니다."
          />
        ) : (
          <ul className="list">
            {recommendedBlogs.map((blog) => (
              <li key={blog.id} className="list__item">
                <div>
                  <strong>{blog.name}</strong>
                  <p>{blog.url}</p>
                </div>
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
