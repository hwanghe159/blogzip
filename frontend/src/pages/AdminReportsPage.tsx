import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import {
  ApiError,
  getAdminReceivedReports,
  updateAdminArticleReportStatus,
} from "../api";
import { EmptyState } from "../components/EmptyState";
import { Toast } from "../components/Toast";
import {
  AdminArticleReportResponse,
  AdminArticleReportStatus,
  SessionState,
} from "../types";

type AdminReportsPageProps = {
  session: SessionState;
  loginUrl: string;
};

type AdminToast = {
  id: number;
  message: string;
  tone: "success" | "error" | "info";
  durationMs?: number;
};

type LoadOptions = {
  reset: boolean;
  silent?: boolean;
};

const REPORT_STATUS_OPTIONS: Array<{ value: AdminArticleReportStatus; label: string }> = [
  { value: "RECEIVED", label: "접수" },
  { value: "REVIEWED", label: "검토" },
  { value: "RESOLVED", label: "처리 완료" },
  { value: "DISMISSED", label: "기각" },
];

function formatDateTime(value: string): string {
  try {
    return new Date(value).toLocaleString("ko-KR", {
      hour12: false,
    });
  } catch (_) {
    return value;
  }
}

function normalizeError(error: unknown, fallback: string): string {
  if (error instanceof ApiError) {
    return error.message;
  }
  return fallback;
}

export function AdminReportsPage({ session, loginUrl }: AdminReportsPageProps) {
  const token = session.token;
  const isAuthenticated = session.status === "authenticated" && !!token;
  const isAdmin = !!session.user?.isAdmin;

  const [toast, setToast] = useState<AdminToast | null>(null);
  const toastSeqRef = useRef(0);
  const toastTimerRef = useRef<number | null>(null);

  const [receivedReports, setReceivedReports] = useState<AdminArticleReportResponse[]>([]);
  const [receivedReportNext, setReceivedReportNext] = useState<number | null>(null);
  const [receivedLoading, setReceivedLoading] = useState(false);
  const [receivedLoadingMore, setReceivedLoadingMore] = useState(false);
  const [receivedInitialized, setReceivedInitialized] = useState(false);
  const [updatingReportId, setUpdatingReportId] = useState<number | null>(null);
  const [pendingReportStatus, setPendingReportStatus] = useState<Record<number, AdminArticleReportStatus>>(
    {}
  );

  function showToast(
    message: string,
    tone: AdminToast["tone"] = "info",
    durationMs = 3200
  ) {
    if (toastTimerRef.current !== null) {
      window.clearTimeout(toastTimerRef.current);
    }
    toastSeqRef.current += 1;
    setToast({
      id: toastSeqRef.current,
      message,
      tone,
      durationMs,
    });
    toastTimerRef.current = window.setTimeout(() => {
      setToast(null);
      toastTimerRef.current = null;
    }, durationMs);
  }

  useEffect(() => {
    return () => {
      if (toastTimerRef.current !== null) {
        window.clearTimeout(toastTimerRef.current);
      }
    };
  }, []);

  async function loadReceivedReports({ reset, silent = false }: LoadOptions) {
    if (!token) return;
    if (reset) {
      setReceivedLoading(true);
    } else {
      setReceivedLoadingMore(true);
    }
    try {
      const response = await getAdminReceivedReports(token, {
        next: reset ? null : receivedReportNext,
        size: 20,
      });
      const sortedItems = [...response.items].sort((a, b) => {
        const left = new Date(b.createdAt).getTime();
        const right = new Date(a.createdAt).getTime();
        return left - right;
      });
      setReceivedReports((prev) => {
        if (reset) {
          return sortedItems;
        }
        const merged = [...prev];
        const exists = new Set(prev.map((item) => item.id));
        sortedItems.forEach((item) => {
          if (!exists.has(item.id)) {
            merged.push(item);
          }
        });
        return merged.sort((a, b) => {
          const left = new Date(b.createdAt).getTime();
          const right = new Date(a.createdAt).getTime();
          return left - right;
        });
      });
      setPendingReportStatus((prev) => {
        const nextState: Record<number, AdminArticleReportStatus> = reset ? {} : { ...prev };
        sortedItems.forEach((item) => {
          nextState[item.id] = item.status;
        });
        return nextState;
      });
      setReceivedReportNext(response.next ?? null);
      if (!silent) {
        showToast("접수된 신고 목록을 불러왔습니다.", "success");
      }
    } catch (error) {
      showToast(normalizeError(error, "신고 목록 조회에 실패했습니다."), "error");
    } finally {
      if (reset) {
        setReceivedLoading(false);
        setReceivedInitialized(true);
      } else {
        setReceivedLoadingMore(false);
      }
    }
  }

  async function handleUpdateReportStatus(reportId: number) {
    if (!token) return;
    const status = pendingReportStatus[reportId];
    if (!status) {
      showToast("변경할 상태를 선택해 주세요.", "error");
      return;
    }

    setUpdatingReportId(reportId);
    try {
      const updatedReport = await updateAdminArticleReportStatus(token, reportId, status);
      setReceivedReports((prev) => {
        const next = prev.map((report) => (report.id === reportId ? updatedReport : report));
        return next.filter((report) => report.status === "RECEIVED");
      });
      setPendingReportStatus((prev) => {
        const next = { ...prev };
        if (updatedReport.status === "RECEIVED") {
          next[reportId] = updatedReport.status;
        } else {
          delete next[reportId];
        }
        return next;
      });
      if (updatedReport.status === "RECEIVED") {
        showToast("신고 상태를 업데이트했습니다.", "success");
      } else {
        showToast("신고를 처리해 접수 목록에서 제거했습니다.", "success");
      }
    } catch (error) {
      showToast(normalizeError(error, "신고 상태 업데이트에 실패했습니다."), "error");
    } finally {
      setUpdatingReportId(null);
    }
  }

  useEffect(() => {
    if (!isAuthenticated || !isAdmin || !token || receivedLoading || receivedInitialized) {
      return;
    }
    void loadReceivedReports({ reset: true, silent: true });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAuthenticated, isAdmin, token, receivedLoading, receivedInitialized]);

  if (!isAuthenticated) {
    return (
      <section className="page-section">
        <EmptyState
          title="관리자 화면은 로그인 후 사용할 수 있습니다"
          description="관리자 권한 계정으로 로그인해 주세요."
          action={
            <a className="btn btn--primary" href={loginUrl}>
              Google로 로그인
            </a>
          }
        />
      </section>
    );
  }

  if (!isAdmin) {
    return (
      <section className="page-section">
        <EmptyState
          title="접근 권한이 없습니다"
          description="이 화면은 관리자 계정만 접근할 수 있습니다."
        />
      </section>
    );
  }

  return (
    <section className="page-section">
      <div className="page-head">
        <div>
          <h1>운영 관리 콘솔</h1>
        </div>
      </div>

      <div className="admin-layout">
        <aside className="admin-sidebar panel">
          <h2>관리 메뉴</h2>
          <div className="admin-tab-list">
            <Link to="/admin" className="admin-tab-btn">
              게시글 관리
            </Link>
            <Link to="/admin/reports" className="admin-tab-btn admin-tab-btn--active">
              신고 관리
            </Link>
          </div>
        </aside>

        <div className="admin-main">
          <section className="panel">
            <div className="panel-actions panel-actions--between">
              <div>
                <h2>접수된 신고</h2>
                <p className="panel__description">
                  접수된 신고를 확인하고 상태를 변경할 수 있어요. 게시글 제목을 누르면 게시글 관리로
                  이동합니다.
                </p>
              </div>
              <div className="admin-page-actions">
                <Link to="/admin" className="btn btn--ghost btn--small">
                  게시글 관리로 이동
                </Link>
                <button
                  type="button"
                  className="btn btn--ghost btn--small"
                  onClick={() => void loadReceivedReports({ reset: true })}
                  disabled={receivedLoading}
                >
                  새로고침
                </button>
              </div>
            </div>

            {receivedLoading ? (
              <p className="status-text">신고 목록을 불러오는 중...</p>
            ) : receivedReports.length === 0 ? (
              <p className="status-text">현재 접수된 신고가 없습니다.</p>
            ) : (
              <div className="admin-report-list">
                {receivedReports.map((report) => (
                  <div key={report.id} className="admin-report-item">
                    <p className="admin-report-item__title">#{report.id} · 접수</p>
                    <Link
                      to={`/admin?articleId=${report.articleId}`}
                      className="admin-report-item__article-link"
                    >
                      {report.articleTitle}
                    </Link>
                    <p className="admin-report-item__meta">
                      {report.blogName} · 사용자 {report.userId} · {formatDateTime(report.createdAt)}
                    </p>
                    <p className="admin-report-item__reason">신고 유형: {report.reason}</p>
                    {report.detail ? <p className="admin-report-item__detail">상세: {report.detail}</p> : null}
                    <a href={report.articleUrl} target="_blank" rel="noreferrer" className="admin-link">
                      원문
                    </a>
                    <div className="admin-report-item__actions">
                      <select
                        value={pendingReportStatus[report.id] ?? report.status}
                        onChange={(event) =>
                          setPendingReportStatus((prev) => ({
                            ...prev,
                            [report.id]: event.target.value as AdminArticleReportStatus,
                          }))
                        }
                      >
                        {REPORT_STATUS_OPTIONS.map((statusOption) => (
                          <option key={statusOption.value} value={statusOption.value}>
                            {statusOption.label}
                          </option>
                        ))}
                      </select>
                      <button
                        type="button"
                        className="btn btn--primary btn--small"
                        onClick={() => void handleUpdateReportStatus(report.id)}
                        disabled={updatingReportId === report.id}
                      >
                        {updatingReportId === report.id ? "처리 중..." : "상태 저장"}
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}

            {receivedReportNext !== null ? (
              <div className="more-wrap">
                <button
                  type="button"
                  className="btn btn--ghost btn--small"
                  onClick={() => void loadReceivedReports({ reset: false })}
                  disabled={receivedLoadingMore}
                >
                  {receivedLoadingMore ? "불러오는 중..." : "신고 더 보기"}
                </button>
              </div>
            ) : null}
          </section>
        </div>
      </div>

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
