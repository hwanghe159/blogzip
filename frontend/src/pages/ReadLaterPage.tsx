import { useEffect, useRef, useState } from "react";
import { addReadLater, ApiError, getReadLater, removeReadLater } from "../api";
import { ReadLaterDetailResponse, SessionState } from "../types";
import { ArticleCard } from "../components/ArticleCard";
import { EmptyState } from "../components/EmptyState";
import { Toast } from "../components/Toast";

type ReadLaterPageProps = {
  session: SessionState;
  loginUrl: string;
};

type ReadLaterToast = {
  id: number;
  message: string;
  tone: "error" | "info" | "success";
};

export function ReadLaterPage({ session, loginUrl }: ReadLaterPageProps) {
  const [items, setItems] = useState<ReadLaterDetailResponse[]>([]);
  const [nextCursor, setNextCursor] = useState<number | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [loadingMore, setLoadingMore] = useState<boolean>(false);
  const [toast, setToast] = useState<ReadLaterToast | null>(null);
  const [removingId, setRemovingId] = useState<number | null>(null);
  const [locallyRemovedArticleIds, setLocallyRemovedArticleIds] = useState<Set<number>>(new Set());
  const toastTimerRef = useRef<number | null>(null);
  const toastSeqRef = useRef<number>(0);

  const token = session.token;
  const isAuthenticated = session.status === "authenticated" && !!token;

  useEffect(() => {
    return () => {
      if (toastTimerRef.current !== null) {
        window.clearTimeout(toastTimerRef.current);
      }
    };
  }, []);

  function showToast(message: string, tone: "error" | "info" | "success" = "info") {
    if (toastTimerRef.current !== null) {
      window.clearTimeout(toastTimerRef.current);
    }
    const next = { id: toastSeqRef.current + 1, message, tone };
    toastSeqRef.current = next.id;
    setToast(next);
    toastTimerRef.current = window.setTimeout(() => {
      setToast(null);
      toastTimerRef.current = null;
    }, 2200);
  }

  async function fetchPage(append: boolean) {
    if (!token) return;

    if (append) setLoadingMore(true);
    else setLoading(true);

    try {
      const response = await getReadLater({
        token,
        next: append ? nextCursor : null,
        size: 20,
      });
      setItems((prev) => (append ? [...prev, ...response.items] : response.items));
      setNextCursor(response.next);
    } catch (error) {
      const message =
        error instanceof ApiError
          ? error.message
          : "나중에 읽기 목록을 불러오지 못했습니다.";
      showToast(message, "error");
    } finally {
      setLoading(false);
      setLoadingMore(false);
    }
  }

  useEffect(() => {
    fetchPage(false);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);

  async function handleToggleReadLater(articleId: number, nextValue: boolean) {
    if (!token) return;
    setRemovingId(articleId);
    try {
      if (nextValue) {
        await addReadLater(token, articleId);
        setLocallyRemovedArticleIds((prev) => {
          const next = new Set(prev);
          next.delete(articleId);
          return next;
        });
        showToast("나중에 읽기에 다시 저장했어요.", "success");
      } else {
        await removeReadLater(token, articleId);
        setLocallyRemovedArticleIds((prev) => {
          const next = new Set(prev);
          next.add(articleId);
          return next;
        });
        showToast("나중에 읽기에서 해제했어요.", "info");
      }
    } catch (error) {
      const message =
        error instanceof ApiError ? error.message : "나중에 읽기 처리에 실패했습니다.";
      showToast(message, "error");
    } finally {
      setRemovingId(null);
    }
  }

  if (!isAuthenticated) {
    return (
      <section className="page-section">
        <EmptyState
          title="로그인 후 이용 가능해요"
          description="읽고 싶은 글을 저장하려면 먼저 로그인해 주세요."
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
          <p className="eyebrow">READ LATER</p>
          <h1>나중에 읽기</h1>
        </div>
      </div>

      {loading ? <p className="status-text">목록을 불러오는 중...</p> : null}

      {!loading && items.length === 0 ? (
        <EmptyState
          title="보관한 글이 없습니다"
          description="피드 카드의 북마크 아이콘을 누르면 이곳에 모아볼 수 있습니다."
        />
      ) : (
        <div className="articles-grid">
          {items.map((item) => (
            <ArticleCard
              key={item.id}
              article={item.article}
              readLaterActive={!locallyRemovedArticleIds.has(item.article.id)}
              busy={removingId === item.article.id}
              onOpen={(article) => {
                window.open(article.url, "_blank", "noopener,noreferrer");
              }}
              onToggleReadLater={(article, nextValue) =>
                handleToggleReadLater(article.id, nextValue)
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

      {toast ? (
        <Toast
          key={toast.id}
          message={toast.message}
          tone={toast.tone}
          onClose={() => setToast(null)}
        />
      ) : null}
    </section>
  );
}
