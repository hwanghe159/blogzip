import { FormEvent, TouchEvent, useEffect, useMemo, useRef, useState } from "react";
import {
  ApiError,
  applyAdminArticleSummary,
  createAdminKeyword,
  getAdminArticleReports,
  getAdminArticleSummaries,
  getAdminKeywordOverview,
  getAdminRecentArticles,
  getAdminReceivedReports,
  mergeAdminKeywordsById,
  previewAdminArticleResummary,
  suggestAdminCssSelector,
  testAdminCssSelector,
  updateAdminArticleCreatedDate,
  updateAdminArticleReportStatus,
  updateAdminBlogCssSelector,
  updateAdminBlogCssSelectorByUrl,
  updateAdminKeywordById,
  updateAdminKeywordHead,
} from "../api";
import {
  AdminArticleReportResponse,
  AdminArticleReportStatus,
  AdminArticleSummaryResponse,
  AdminBlogCssSelectorUpdateResponse,
  AdminCssSelectorSuggestResponse,
  AdminCssSelectorTestResponse,
  AdminFollowerKeywordOverviewResponse,
  AdminHeadKeywordOverviewResponse,
  AdminKeywordOverviewResponse,
  AdminRecentArticleResponse,
  SessionState,
} from "../types";
import { EmptyState } from "../components/EmptyState";
import { Toast } from "../components/Toast";

type AdminPageProps = {
  session: SessionState;
  loginUrl: string;
};

type AdminTab = "articles" | "keywords" | "crawler";

type AdminToast = {
  id: number;
  message: string;
  tone: "success" | "error" | "info";
  durationMs?: number;
};

type DragKeywordItem = {
  id: number;
  type: "head" | "follower";
  label: string;
  headId: number | null;
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

function parseNumber(value: string): number | null {
  const num = Number(value);
  return Number.isFinite(num) && num > 0 ? num : null;
}

function formatDateTime(value: string): string {
  try {
    return new Date(value).toLocaleString("ko-KR", {
      hour12: false,
    });
  } catch (_) {
    return value;
  }
}

function reportStatusLabel(status: AdminArticleReportStatus): string {
  const found = REPORT_STATUS_OPTIONS.find((option) => option.value === status);
  return found?.label ?? status;
}

function normalizeError(error: unknown, fallback: string): string {
  if (error instanceof ApiError) {
    return error.message;
  }
  return fallback;
}

export function AdminPage({ session, loginUrl }: AdminPageProps) {
  const token = session.token;
  const isAuthenticated = session.status === "authenticated" && !!token;
  const isAdmin = !!session.user?.isAdmin;

  const [activeTab, setActiveTab] = useState<AdminTab>("articles");

  const [toast, setToast] = useState<AdminToast | null>(null);
  const toastSeqRef = useRef(0);
  const toastTimerRef = useRef<number | null>(null);

  const [recentArticles, setRecentArticles] = useState<AdminRecentArticleResponse[]>([]);
  const [recentNext, setRecentNext] = useState<number | null>(null);
  const [recentLoading, setRecentLoading] = useState(false);
  const [recentLoadingMore, setRecentLoadingMore] = useState(false);
  const [recentInitialized, setRecentInitialized] = useState(false);
  const [editingDates, setEditingDates] = useState<Record<number, string>>({});
  const [savingDateArticleId, setSavingDateArticleId] = useState<number | null>(null);
  const [openSummaryArticleId, setOpenSummaryArticleId] = useState<number | null>(null);
  const [summaryLoadingArticleId, setSummaryLoadingArticleId] = useState<number | null>(null);
  const [creatingSummaryArticleId, setCreatingSummaryArticleId] = useState<number | null>(null);
  const [applyingSummaryArticleId, setApplyingSummaryArticleId] = useState<number | null>(null);
  const [summariesByArticleId, setSummariesByArticleId] = useState<
    Record<number, AdminArticleSummaryResponse[]>
  >({});
  const [selectedSummaryByArticleId, setSelectedSummaryByArticleId] = useState<
    Record<number, number | null>
  >({});
  const [articleReportLoadingId, setArticleReportLoadingId] = useState<number | null>(null);
  const [openArticleReportsForId, setOpenArticleReportsForId] = useState<number | null>(null);
  const [articleReportsByArticleId, setArticleReportsByArticleId] = useState<
    Record<number, AdminArticleReportResponse[]>
  >({});

  const [receivedReports, setReceivedReports] = useState<AdminArticleReportResponse[]>([]);
  const [receivedReportNext, setReceivedReportNext] = useState<number | null>(null);
  const [receivedLoading, setReceivedLoading] = useState(false);
  const [receivedLoadingMore, setReceivedLoadingMore] = useState(false);
  const [receivedInitialized, setReceivedInitialized] = useState(false);
  const [updatingReportId, setUpdatingReportId] = useState<number | null>(null);
  const [pendingReportStatus, setPendingReportStatus] = useState<Record<number, AdminArticleReportStatus>>(
    {}
  );

  const [keywordOverview, setKeywordOverview] = useState<AdminKeywordOverviewResponse | null>(null);
  const [keywordLoading, setKeywordLoading] = useState(false);
  const [keywordUpdating, setKeywordUpdating] = useState(false);
  const [creatingKeyword, setCreatingKeyword] = useState(false);
  const [newKeywordValue, setNewKeywordValue] = useState("");
  const [newKeywordVisible, setNewKeywordVisible] = useState(true);
  const [draggingKeyword, setDraggingKeyword] = useState<DragKeywordItem | null>(null);
  const [droppingHeadId, setDroppingHeadId] = useState<number | null>(null);
  const touchDragKeywordRef = useRef<DragKeywordItem | null>(null);
  const touchDragActiveRef = useRef<boolean>(false);
  const touchDragStartRef = useRef<{ x: number; y: number } | null>(null);
  const suppressFollowerClickRef = useRef<boolean>(false);

  const [cssTestBlogUrl, setCssTestBlogUrl] = useState("");
  const [cssTestSelector, setCssTestSelector] = useState("");
  const [cssTestSampleSize, setCssTestSampleSize] = useState("5");
  const [cssTesting, setCssTesting] = useState(false);
  const [cssTestResult, setCssTestResult] = useState<AdminCssSelectorTestResponse | null>(null);

  const [cssSuggestBlogUrl, setCssSuggestBlogUrl] = useState("");
  const [cssSuggestLimit, setCssSuggestLimit] = useState("10");
  const [cssSuggestSampleSize, setCssSuggestSampleSize] = useState("5");
  const [cssSuggesting, setCssSuggesting] = useState(false);
  const [applyingSuggestedSelector, setApplyingSuggestedSelector] = useState<string | null>(null);
  const [cssSuggestResult, setCssSuggestResult] = useState<AdminCssSelectorSuggestResponse | null>(
    null
  );

  const [blogIdForSelector, setBlogIdForSelector] = useState("");
  const [selectorForSave, setSelectorForSave] = useState("");
  const [selectorSaveSampleSize, setSelectorSaveSampleSize] = useState("5");
  const [selectorForceSave, setSelectorForceSave] = useState(false);
  const [savingSelector, setSavingSelector] = useState(false);
  const [selectorSaveResult, setSelectorSaveResult] = useState<AdminBlogCssSelectorUpdateResponse | null>(
    null
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

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => {
    return () => {
      if (toastTimerRef.current !== null) {
        window.clearTimeout(toastTimerRef.current);
      }
    };
  }, []);

  async function loadRecentArticles({ reset, silent = false }: LoadOptions) {
    if (!token) return;
    if (reset) {
      setRecentLoading(true);
    } else {
      setRecentLoadingMore(true);
    }
    try {
      const response = await getAdminRecentArticles(token, {
        next: reset ? null : recentNext,
        size: 20,
      });
      setRecentArticles((prev) => {
        if (reset) {
          return response.items;
        }
        const merged = [...prev];
        const exists = new Set(prev.map((item) => item.id));
        response.items.forEach((item) => {
          if (!exists.has(item.id)) {
            merged.push(item);
          }
        });
        return merged;
      });
      setEditingDates((prev) => {
        const nextState: Record<number, string> = reset ? {} : { ...prev };
        response.items.forEach((item) => {
          if (!nextState[item.id]) {
            nextState[item.id] = item.createdDate;
          }
        });
        return nextState;
      });
      setRecentNext(response.next ?? null);
      if (!silent) {
        showToast("최근 게시글 목록을 불러왔습니다.", "success");
      }
    } catch (error) {
      showToast(normalizeError(error, "최근 게시글 조회에 실패했습니다."), "error");
    } finally {
      if (reset) {
        setRecentLoading(false);
        setRecentInitialized(true);
      } else {
        setRecentLoadingMore(false);
      }
    }
  }

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

  async function loadKeywordOverview(silent = false) {
    if (!token) return;
    setKeywordLoading(true);
    try {
      const result = await getAdminKeywordOverview(token);
      setKeywordOverview(result);
      if (!silent) {
        showToast("키워드 현황을 불러왔습니다.", "success");
      }
    } catch (error) {
      showToast(normalizeError(error, "키워드 현황 조회에 실패했습니다."), "error");
    } finally {
      setKeywordLoading(false);
    }
  }

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => {
    if (!isAuthenticated || !isAdmin || !token) {
      return;
    }
    if (activeTab === "articles") {
      if (!recentLoading && !recentInitialized) {
        void loadRecentArticles({ reset: true, silent: true });
      }
      if (!receivedLoading && !receivedInitialized) {
        void loadReceivedReports({ reset: true, silent: true });
      }
      return;
    }
    if (activeTab === "keywords" && !keywordLoading && !keywordOverview) {
      void loadKeywordOverview(true);
    }
  }, [
    activeTab,
    isAuthenticated,
    isAdmin,
    token,
    recentInitialized,
    recentLoading,
    receivedLoading,
    receivedInitialized,
    keywordLoading,
    keywordOverview,
  ]);

  async function ensureSummaries(articleId: number) {
    if (!token) return;
    setSummaryLoadingArticleId(articleId);
    try {
      const summaries = await getAdminArticleSummaries(token, articleId);
      setSummariesByArticleId((prev) => ({
        ...prev,
        [articleId]: summaries,
      }));
      const applied = summaries.find((summary) => summary.isApplied)?.id ?? summaries[0]?.id ?? null;
      setSelectedSummaryByArticleId((prev) => ({
        ...prev,
        [articleId]: prev[articleId] ?? applied,
      }));
    } catch (error) {
      showToast(normalizeError(error, "요약 목록 조회에 실패했습니다."), "error");
    } finally {
      setSummaryLoadingArticleId(null);
    }
  }

  async function handleToggleSummaries(articleId: number) {
    if (openSummaryArticleId === articleId) {
      setOpenSummaryArticleId(null);
      return;
    }
    setOpenSummaryArticleId(articleId);
    if (!summariesByArticleId[articleId]) {
      await ensureSummaries(articleId);
    }
  }

  async function handleResummarize(articleId: number) {
    if (!token) return;
    setCreatingSummaryArticleId(articleId);
    try {
      const preview = await previewAdminArticleResummary(token, [articleId]);
      const item = preview.items[0];
      if (!item || !item.success || item.candidateSummaryId === null) {
        showToast(item?.errorMessage ?? "재요약 후보 생성에 실패했습니다.", "error");
        return;
      }
      await ensureSummaries(articleId);
      setOpenSummaryArticleId(articleId);
      setSelectedSummaryByArticleId((prev) => ({
        ...prev,
        [articleId]: item.candidateSummaryId,
      }));
      showToast("새 요약 후보를 만들었습니다. 적용할 요약본을 선택해 주세요.", "success");
    } catch (error) {
      showToast(normalizeError(error, "재요약 실행에 실패했습니다."), "error");
    } finally {
      setCreatingSummaryArticleId(null);
    }
  }

  async function handleApplySummary(articleId: number) {
    if (!token) return;
    const summaryId = selectedSummaryByArticleId[articleId];
    if (!summaryId) {
      showToast("적용할 요약본을 선택해 주세요.", "error");
      return;
    }

    setApplyingSummaryArticleId(articleId);
    try {
      const applied = await applyAdminArticleSummary(token, summaryId);
      await ensureSummaries(articleId);
      setRecentArticles((prev) =>
        prev.map((item) =>
          item.id === articleId
            ? {
                ...item,
                summary: applied.summary,
                summarizedBy: applied.summarizedBy,
              }
            : item
        )
      );
      showToast("선택한 요약본을 적용했습니다.", "success");
    } catch (error) {
      showToast(normalizeError(error, "요약 적용에 실패했습니다."), "error");
    } finally {
      setApplyingSummaryArticleId(null);
    }
  }

  async function handleDateSave(articleId: number) {
    if (!token) return;
    const createdDate = editingDates[articleId];
    if (!createdDate) {
      showToast("날짜를 입력해 주세요.", "error");
      return;
    }
    setSavingDateArticleId(articleId);
    try {
      const result = await updateAdminArticleCreatedDate(token, articleId, createdDate);
      setRecentArticles((prev) =>
        prev.map((item) =>
          item.id === articleId
            ? {
                ...item,
                createdDate: result.createdDate,
              }
            : item
        )
      );
      setEditingDates((prev) => ({
        ...prev,
        [articleId]: result.createdDate,
      }));
      showToast("게시글 날짜를 수정했습니다.", "success");
    } catch (error) {
      showToast(normalizeError(error, "게시글 날짜 수정에 실패했습니다."), "error");
    } finally {
      setSavingDateArticleId(null);
    }
  }

  async function toggleArticleReports(articleId: number) {
    if (!token) return;
    if (openArticleReportsForId === articleId) {
      setOpenArticleReportsForId(null);
      return;
    }

    setOpenArticleReportsForId(articleId);
    if (articleReportsByArticleId[articleId]) {
      return;
    }

    setArticleReportLoadingId(articleId);
    try {
      const reports = await getAdminArticleReports(token, articleId);
      setArticleReportsByArticleId((prev) => ({
        ...prev,
        [articleId]: reports,
      }));
    } catch (error) {
      showToast(normalizeError(error, "게시글 신고 내역 조회에 실패했습니다."), "error");
    } finally {
      setArticleReportLoadingId(null);
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
      const beforeStatus = receivedReports.find((report) => report.id === reportId)?.status;
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
      setRecentArticles((prev) =>
        prev.map((article) => {
          if (article.id !== updatedReport.articleId) {
            return article;
          }
          if (beforeStatus === "RECEIVED" && updatedReport.status !== "RECEIVED") {
            return {
              ...article,
              receivedReportCount: Math.max(0, article.receivedReportCount - 1),
            };
          }
          if (beforeStatus !== "RECEIVED" && updatedReport.status === "RECEIVED") {
            return {
              ...article,
              receivedReportCount: article.receivedReportCount + 1,
            };
          }
          return article;
        })
      );
      if (openArticleReportsForId !== null) {
        const reports = await getAdminArticleReports(token, openArticleReportsForId);
        setArticleReportsByArticleId((prev) => ({
          ...prev,
          [openArticleReportsForId]: reports,
        }));
      }
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

  function recalculateKeywordOverviewCounts(
    headKeywords: AdminHeadKeywordOverviewResponse[]
  ): Pick<
    AdminKeywordOverviewResponse,
    "totalKeywordCount" | "headKeywordCount" | "followerKeywordCount" | "visibleKeywordCount" | "hiddenKeywordCount"
  > {
    const headKeywordCount = headKeywords.length;
    const followerKeywordCount = headKeywords.reduce(
      (count, headKeyword) => count + headKeyword.followers.length,
      0
    );
    const totalKeywordCount = headKeywordCount + followerKeywordCount;
    const visibleKeywordCount = headKeywords.reduce(
      (count, headKeyword) =>
        count +
        (headKeyword.isVisible ? 1 : 0) +
        headKeyword.followers.filter((follower) => follower.isVisible).length,
      0
    );
    const hiddenKeywordCount = totalKeywordCount - visibleKeywordCount;
    return {
      totalKeywordCount,
      headKeywordCount,
      followerKeywordCount,
      visibleKeywordCount,
      hiddenKeywordCount,
    };
  }

  function moveFollowerLocally(
    sourceFollowerId: number,
    targetHeadKeywordId: number | null
  ) {
    setKeywordOverview((prev) => {
      if (!prev) {
        return prev;
      }

      let movingFollower: AdminFollowerKeywordOverviewResponse | null = null;
      const removedHeadKeywords = prev.headKeywords.map((headKeyword) => {
        const nextFollowers = headKeyword.followers.filter((follower) => {
          if (follower.id === sourceFollowerId) {
            movingFollower = follower;
            return false;
          }
          return true;
        });
        if (nextFollowers.length === headKeyword.followers.length) {
          return headKeyword;
        }
        return {
          ...headKeyword,
          followers: nextFollowers,
        };
      });

      if (!movingFollower) {
        return prev;
      }
      const follower = movingFollower as AdminFollowerKeywordOverviewResponse;

      let nextHeadKeywords: AdminHeadKeywordOverviewResponse[];
      if (targetHeadKeywordId === null) {
        const newHead: AdminHeadKeywordOverviewResponse = {
          id: follower.id,
          value: follower.value,
          isVisible: follower.isVisible,
          articleCount: 0,
          createdAt: follower.createdAt,
          followers: [],
        };
        nextHeadKeywords = [...removedHeadKeywords, newHead];
      } else {
        nextHeadKeywords = removedHeadKeywords.map((headKeyword) => {
          if (headKeyword.id !== targetHeadKeywordId) {
            return headKeyword;
          }
          return {
            ...headKeyword,
            followers: [...headKeyword.followers, follower],
          };
        });
      }

      const nextCounts = recalculateKeywordOverviewCounts(nextHeadKeywords);
      return {
        ...prev,
        ...nextCounts,
        headKeywords: nextHeadKeywords,
      };
    });
  }

  async function handleToggleKeywordVisible(keywordId: number, value: string, isVisible: boolean) {
    if (!token) return;
    const nextVisible = !isVisible;
    setKeywordUpdating(true);
    try {
      const updatedOverview = await updateAdminKeywordById(token, keywordId, { isVisible: nextVisible });
      setKeywordOverview(updatedOverview);
      showToast(`키워드 "${value}"의 노출 상태를 바꿨습니다.`, "success");
    } catch (error) {
      showToast(normalizeError(error, "키워드 상태 변경에 실패했습니다."), "error");
    } finally {
      setKeywordUpdating(false);
    }
  }

  async function handleCreateKeyword(event: FormEvent) {
    event.preventDefault();
    if (!token) return;

    const value = newKeywordValue.trim();
    if (!value) {
      showToast("추가할 키워드를 입력해 주세요.", "error");
      return;
    }

    setCreatingKeyword(true);
    try {
      const created = await createAdminKeyword(token, {
        value,
        isVisible: newKeywordVisible,
      });
      await loadKeywordOverview(true);
      setNewKeywordValue("");
      setNewKeywordVisible(true);
      showToast(`키워드 "${created.value}"를 추가했습니다.`, "success");
    } catch (error) {
      showToast(normalizeError(error, "키워드 추가에 실패했습니다."), "error");
    } finally {
      setCreatingKeyword(false);
    }
  }

  function findHeadKeywordIdByPoint(clientX: number, clientY: number): number | null {
    const target = document.elementFromPoint(clientX, clientY) as HTMLElement | null;
    const lane = target?.closest<HTMLElement>("[data-head-keyword-id]");
    if (!lane) return null;
    const value = lane.dataset.headKeywordId;
    const parsed = value ? Number(value) : Number.NaN;
    return Number.isFinite(parsed) ? parsed : null;
  }

  function clearTouchDragState() {
    touchDragKeywordRef.current = null;
    touchDragActiveRef.current = false;
    touchDragStartRef.current = null;
    setDraggingKeyword(null);
    setDroppingHeadId(null);
  }

  function handleKeywordTouchStart(event: TouchEvent<HTMLElement>, keyword: DragKeywordItem) {
    const target = event.target as HTMLElement;
    if (target.closest(".keyword-visibility-btn")) {
      return;
    }
    const touch = event.touches.item(0);
    if (!touch) return;
    touchDragKeywordRef.current = keyword;
    touchDragActiveRef.current = false;
    touchDragStartRef.current = { x: touch.clientX, y: touch.clientY };
  }

  function handleKeywordTouchMove(event: TouchEvent<HTMLElement>) {
    if (!touchDragKeywordRef.current) return;
    const touch = event.touches.item(0);
    if (!touch) return;

    if (!touchDragActiveRef.current) {
      const start = touchDragStartRef.current;
      if (!start) return;
      const movedX = touch.clientX - start.x;
      const movedY = touch.clientY - start.y;
      const distance = Math.hypot(movedX, movedY);
      if (distance < 8) {
        return;
      }
      touchDragActiveRef.current = true;
      setDraggingKeyword(touchDragKeywordRef.current);
    }

    event.preventDefault();
    const dropHeadId = findHeadKeywordIdByPoint(touch.clientX, touch.clientY);
    setDroppingHeadId(dropHeadId);
  }

  function handleKeywordTouchEnd(event: TouchEvent<HTMLElement>) {
    const keyword = touchDragKeywordRef.current;
    if (!keyword) {
      clearTouchDragState();
      return;
    }

    const wasActive = touchDragActiveRef.current;
    const touch = event.changedTouches.item(0);
    if (wasActive) {
      event.preventDefault();
      const dropHeadId = touch
        ? findHeadKeywordIdByPoint(touch.clientX, touch.clientY)
        : null;
      if (dropHeadId !== null) {
        void handleDropToHead(dropHeadId, keyword);
      } else if (keyword.type === "follower") {
        void handleDropToRoot(keyword);
      }
      suppressFollowerClickRef.current = true;
      window.setTimeout(() => {
        suppressFollowerClickRef.current = false;
      }, 0);
    }

    clearTouchDragState();
  }

  async function handleDropToHead(
    targetHeadKeywordId: number,
    sourceKeyword: DragKeywordItem | null = draggingKeyword
  ) {
    if (!token || !sourceKeyword) return;
    if (sourceKeyword.type === "head") {
      if (sourceKeyword.id === targetHeadKeywordId) return;
      setKeywordUpdating(true);
      try {
        const updatedOverview = await mergeAdminKeywordsById(token, sourceKeyword.id, targetHeadKeywordId);
        setKeywordOverview(updatedOverview);
        showToast(`헤드 키워드 "${sourceKeyword.label}"를 병합했습니다.`, "success");
      } catch (error) {
        showToast(normalizeError(error, "키워드 병합에 실패했습니다."), "error");
      } finally {
        setKeywordUpdating(false);
      }
      return;
    }

    if (sourceKeyword.headId === targetHeadKeywordId) return;
    const snapshot = keywordOverview;
    moveFollowerLocally(sourceKeyword.id, targetHeadKeywordId);
    setKeywordUpdating(true);
    try {
      await updateAdminKeywordHead(token, sourceKeyword.id, targetHeadKeywordId);
      showToast(`팔로워 키워드를 이동했습니다.`, "success");
    } catch (error) {
      setKeywordOverview(snapshot);
      showToast(normalizeError(error, "팔로워 이동에 실패했습니다."), "error");
    } finally {
      setKeywordUpdating(false);
    }
  }

  async function handleDropToRoot(sourceKeyword: DragKeywordItem | null = draggingKeyword) {
    if (!token || !sourceKeyword) return;
    if (sourceKeyword.type !== "follower") {
      return;
    }
    const snapshot = keywordOverview;
    moveFollowerLocally(sourceKeyword.id, null);
    setKeywordUpdating(true);
    try {
      await updateAdminKeywordHead(token, sourceKeyword.id, null);
      showToast(`"${sourceKeyword.label}"를 헤드 키워드로 분리했습니다.`, "success");
    } catch (error) {
      setKeywordOverview(snapshot);
      showToast(normalizeError(error, "키워드 분리에 실패했습니다."), "error");
    } finally {
      setKeywordUpdating(false);
    }
  }

  async function handleCssTest(event: FormEvent) {
    event.preventDefault();
    if (!token) return;
    if (!cssTestBlogUrl.trim() || !cssTestSelector.trim()) {
      showToast("blogUrl과 cssSelector를 입력해 주세요.", "error");
      return;
    }
    setCssTesting(true);
    try {
      const result = await testAdminCssSelector(token, {
        blogUrl: cssTestBlogUrl.trim(),
        cssSelector: cssTestSelector.trim(),
        sampleSize: Number(cssTestSampleSize) || 5,
      });
      setCssTestResult(result);
      showToast(result.success ? "selector 테스트 성공" : "selector 테스트 완료", result.success ? "success" : "info");
    } catch (error) {
      showToast(normalizeError(error, "selector 테스트에 실패했습니다."), "error");
    } finally {
      setCssTesting(false);
    }
  }

  async function handleCssSuggest(event: FormEvent) {
    event.preventDefault();
    if (!token) return;
    if (!cssSuggestBlogUrl.trim()) {
      showToast("blogUrl을 입력해 주세요.", "error");
      return;
    }
    setCssSuggesting(true);
    try {
      const result = await suggestAdminCssSelector(token, {
        blogUrl: cssSuggestBlogUrl.trim(),
        candidateLimit: Number(cssSuggestLimit) || 10,
        sampleSize: Number(cssSuggestSampleSize) || 5,
      });
      setCssSuggestResult(result);
      showToast("selector 추천을 완료했습니다.", "success");
    } catch (error) {
      showToast(normalizeError(error, "selector 추천에 실패했습니다."), "error");
    } finally {
      setCssSuggesting(false);
    }
  }

  async function handleSaveSelector(event: FormEvent) {
    event.preventDefault();
    if (!token) return;
    const blogId = parseNumber(blogIdForSelector);
    if (!blogId || !selectorForSave.trim()) {
      showToast("blogId와 cssSelector를 입력해 주세요.", "error");
      return;
    }
    setSavingSelector(true);
    try {
      const result = await updateAdminBlogCssSelector(token, blogId, {
        cssSelector: selectorForSave.trim(),
        sampleSize: Number(selectorSaveSampleSize) || 5,
        force: selectorForceSave,
      });
      setSelectorSaveResult(result);
      showToast(result.saved ? "selector를 저장했습니다." : "테스트 실패로 저장하지 않았습니다.", result.saved ? "success" : "info");
    } catch (error) {
      showToast(normalizeError(error, "selector 저장에 실패했습니다."), "error");
    } finally {
      setSavingSelector(false);
    }
  }

  async function handleApplySuggestedSelector(selector: string) {
    if (!token) return;
    const blogUrl = cssSuggestResult?.blogUrl?.trim() || cssSuggestBlogUrl.trim();
    if (!blogUrl) {
      showToast("먼저 추천에 사용할 blogUrl을 입력해 주세요.", "error");
      return;
    }

    setApplyingSuggestedSelector(selector);
    try {
      const result = await updateAdminBlogCssSelectorByUrl(token, {
        blogUrl,
        cssSelector: selector,
        sampleSize: Number(cssSuggestSampleSize) || 5,
        force: false,
      });
      setSelectorSaveResult(result);
      if (result.saved) {
        setSelectorForSave(result.cssSelector);
      }
      showToast(
        result.saved
          ? "선택한 selector를 바로 적용했습니다."
          : result.message ?? "테스트 실패로 저장하지 않았습니다.",
        result.saved ? "success" : "info"
      );
    } catch (error) {
      showToast(normalizeError(error, "selector 적용에 실패했습니다."), "error");
    } finally {
      setApplyingSuggestedSelector(null);
    }
  }

  const keywordHeadList = useMemo(() => keywordOverview?.headKeywords ?? [], [keywordOverview]);

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
            <button
              type="button"
              className={`admin-tab-btn ${activeTab === "articles" ? "admin-tab-btn--active" : ""}`}
              onClick={() => setActiveTab("articles")}
            >
              게시글 관리
            </button>
            <button
              type="button"
              className={`admin-tab-btn ${activeTab === "keywords" ? "admin-tab-btn--active" : ""}`}
              onClick={() => setActiveTab("keywords")}
            >
              키워드 관리
            </button>
            <button
              type="button"
              className={`admin-tab-btn ${activeTab === "crawler" ? "admin-tab-btn--active" : ""}`}
              onClick={() => setActiveTab("crawler")}
            >
              크롤링 설정
            </button>
          </div>
        </aside>

        <div className="admin-main">
          {activeTab === "articles" ? (
            <section className="panel">
              <div className="panel-actions panel-actions--between">
                <div>
                  <h2>게시글 관리</h2>
                  <p className="panel__description">
                    최근 게시글을 확인하고 날짜 수정, 신고 처리, 요약본 재실행/적용을 진행할 수 있어요.
                  </p>
                </div>
                <button
                  type="button"
                  className="btn btn--ghost btn--small"
                  onClick={() => {
                    void Promise.all([
                      loadRecentArticles({ reset: true }),
                      loadReceivedReports({ reset: true }),
                    ]);
                  }}
                  disabled={recentLoading || receivedLoading}
                >
                  새로고침
                </button>
              </div>

              <div className="admin-article-layout">
                <div className="admin-section-stack">
                  <div className="admin-section-title">
                    <h3>최근 생성 게시글</h3>
                  </div>
                  {recentLoading ? (
                    <p className="status-text">최근 게시글을 불러오는 중...</p>
                  ) : recentArticles.length === 0 ? (
                    <p className="status-text">표시할 게시글이 없습니다.</p>
                  ) : (
                    <div className="admin-article-list">
                      {recentArticles.map((article) => {
                        const summaries = summariesByArticleId[article.id] ?? [];
                        const selectedSummaryId = selectedSummaryByArticleId[article.id] ?? null;
                        const articleReports = articleReportsByArticleId[article.id] ?? [];
                        return (
                          <article key={article.id} className="admin-article-card">
                            <header className="admin-article-card__head">
                              <div className="admin-article-card__blog">
                                <span className="badge badge--muted">{article.blogName}</span>
                                <span className="badge badge--soft">
                                  신고 {article.receivedReportCount}/{article.reportCount}
                                </span>
                              </div>
                              <a href={article.url} target="_blank" rel="noreferrer" className="admin-link">
                                원문
                              </a>
                            </header>
                            <h4>{article.title}</h4>
                            <p className="admin-article-card__summary">
                              {article.summary ?? "아직 적용된 요약이 없습니다."}
                            </p>
                            <p className="admin-article-card__meta">
                              by {article.summarizedBy ?? "-"} · 생성일 {article.createdDate}
                            </p>

                            <div className="admin-article-card__actions">
                              <div className="admin-inline-control">
                                <label>생성일</label>
                                <input
                                  type="date"
                                  value={editingDates[article.id] ?? article.createdDate}
                                  onChange={(event) =>
                                    setEditingDates((prev) => ({
                                      ...prev,
                                      [article.id]: event.target.value,
                                    }))
                                  }
                                />
                                <button
                                  type="button"
                                  className="btn btn--ghost btn--small"
                                  onClick={() => void handleDateSave(article.id)}
                                  disabled={savingDateArticleId === article.id}
                                >
                                  {savingDateArticleId === article.id ? "저장 중..." : "날짜 저장"}
                                </button>
                              </div>

                              <div className="admin-inline-control">
                                <button
                                  type="button"
                                  className="btn btn--ghost btn--small"
                                  onClick={() => void handleToggleSummaries(article.id)}
                                >
                                  {openSummaryArticleId === article.id ? "요약본 접기" : "요약본 선택"}
                                </button>
                                <button
                                  type="button"
                                  className="btn btn--primary btn--small"
                                  onClick={() => void handleResummarize(article.id)}
                                  disabled={creatingSummaryArticleId === article.id}
                                >
                                  {creatingSummaryArticleId === article.id ? "재실행 중..." : "요약 재실행"}
                                </button>
                                <button
                                  type="button"
                                  className="btn btn--ghost btn--small"
                                  onClick={() => void toggleArticleReports(article.id)}
                                >
                                  {openArticleReportsForId === article.id ? "신고 접기" : "신고 보기"}
                                </button>
                              </div>
                            </div>

                            {openSummaryArticleId === article.id ? (
                              <div className="admin-subpanel">
                                {summaryLoadingArticleId === article.id ? (
                                  <p className="status-text">요약 목록을 불러오는 중...</p>
                                ) : summaries.length === 0 ? (
                                  <p className="status-text">요약 이력이 없습니다.</p>
                                ) : (
                                  <>
                                    <div className="admin-summary-list">
                                      {summaries.map((summary) => (
                                        <label key={summary.id} className="admin-summary-item">
                                          <input
                                            type="radio"
                                            name={`summary-${article.id}`}
                                            checked={selectedSummaryId === summary.id}
                                            onChange={() =>
                                              setSelectedSummaryByArticleId((prev) => ({
                                                ...prev,
                                                [article.id]: summary.id,
                                              }))
                                            }
                                          />
                                          <div>
                                            <p className="admin-summary-item__meta">
                                              summaryId {summary.id} · {summary.summarizedBy} ·{" "}
                                              {summary.isApplied ? "현재 적용" : "미적용"}
                                            </p>
                                            <p>{summary.summary}</p>
                                          </div>
                                        </label>
                                      ))}
                                    </div>
                                    <button
                                      type="button"
                                      className="btn btn--primary btn--small"
                                      onClick={() => void handleApplySummary(article.id)}
                                      disabled={applyingSummaryArticleId === article.id}
                                    >
                                      {applyingSummaryArticleId === article.id
                                        ? "적용 중..."
                                        : "선택한 요약본 적용"}
                                    </button>
                                  </>
                                )}
                              </div>
                            ) : null}

                            {openArticleReportsForId === article.id ? (
                              <div className="admin-subpanel">
                                {articleReportLoadingId === article.id ? (
                                  <p className="status-text">신고 내역을 불러오는 중...</p>
                                ) : articleReports.length === 0 ? (
                                  <p className="status-text">신고 내역이 없습니다.</p>
                                ) : (
                                  <div className="admin-inline-report-list">
                                    {articleReports.map((report) => (
                                      <div key={report.id} className="admin-inline-report-item">
                                        <p>
                                          <strong>#{report.id}</strong> · {reportStatusLabel(report.status)} ·{" "}
                                          {formatDateTime(report.createdAt)}
                                        </p>
                                        <p>{report.reason}</p>
                                        {report.detail ? <p>{report.detail}</p> : null}
                                      </div>
                                    ))}
                                  </div>
                                )}
                              </div>
                            ) : null}
                          </article>
                        );
                      })}
                    </div>
                  )}
                  {recentNext !== null ? (
                    <div className="more-wrap">
                      <button
                        type="button"
                        className="btn btn--ghost btn--small"
                        onClick={() => void loadRecentArticles({ reset: false })}
                        disabled={recentLoadingMore}
                      >
                        {recentLoadingMore ? "불러오는 중..." : "게시글 더 보기"}
                      </button>
                    </div>
                  ) : null}
                </div>

                <div className="admin-section-stack">
                  <div className="admin-section-title">
                    <h3>접수된 신고</h3>
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
                          <a
                            href={report.articleUrl}
                            target="_blank"
                            rel="noreferrer"
                            className="admin-report-item__article-link"
                          >
                            {report.articleTitle}
                          </a>
                          <p className="admin-report-item__meta">
                            {report.blogName} · 사용자 {report.userId} · {formatDateTime(report.createdAt)}
                          </p>
                          <p className="admin-report-item__reason">신고 유형: {report.reason}</p>
                          {report.detail ? (
                            <p className="admin-report-item__detail">상세: {report.detail}</p>
                          ) : null}
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
                </div>
              </div>
            </section>
          ) : null}

          {activeTab === "keywords" ? (
            <section className="panel">
              <div className="panel-actions panel-actions--between">
                <div>
                  <h2>키워드 관리</h2>
                  <p className="panel__description">
                    헤드/팔로워, 노출/비노출 상태를 한 번에 보고 Drag & Drop으로 병합/분리할 수 있어요.
                  </p>
                </div>
                <button
                  type="button"
                  className="btn btn--ghost btn--small"
                  onClick={() => void loadKeywordOverview()}
                  disabled={keywordLoading}
                >
                  {keywordLoading ? "불러오는 중..." : "현황 새로고침"}
                </button>
              </div>

              {keywordOverview ? (
                <div className="admin-metric-grid">
                  <div className="admin-metric">
                    <strong>전체</strong>
                    <span>{keywordOverview.totalKeywordCount}</span>
                  </div>
                  <div className="admin-metric">
                    <strong>헤드</strong>
                    <span>{keywordOverview.headKeywordCount}</span>
                  </div>
                  <div className="admin-metric">
                    <strong>팔로워</strong>
                    <span>{keywordOverview.followerKeywordCount}</span>
                  </div>
                  <div className="admin-metric">
                    <strong>노출</strong>
                    <span>{keywordOverview.visibleKeywordCount}</span>
                  </div>
                  <div className="admin-metric">
                    <strong>비노출</strong>
                    <span>{keywordOverview.hiddenKeywordCount}</span>
                  </div>
                </div>
              ) : null}

              <form className="admin-form keyword-create-form" onSubmit={handleCreateKeyword}>
                <h3 className="subheading">키워드 직접 추가</h3>
                <div className="admin-form-grid">
                  <label className="admin-field">
                    <span>키워드</span>
                    <small>헤드 키워드로 추가되며, 필요 시 Drag & Drop으로 병합/분리할 수 있어요.</small>
                    <input
                      value={newKeywordValue}
                      onChange={(event) => setNewKeywordValue(event.target.value)}
                      placeholder="예: 생성형 AI"
                    />
                  </label>
                </div>
                <label className="admin-checkbox">
                  <input
                    type="checkbox"
                    checked={newKeywordVisible}
                    onChange={(event) => setNewKeywordVisible(event.target.checked)}
                  />
                  추가 즉시 노출 상태로 사용
                </label>
                <button type="submit" className="btn btn--primary btn--small" disabled={creatingKeyword}>
                  {creatingKeyword ? "추가 중..." : "키워드 추가"}
                </button>
              </form>

              <div
                className="keyword-lane-grid-droparea"
                onDragOver={(event) => {
                  if (draggingKeyword?.type === "follower") {
                    event.preventDefault();
                  }
                }}
                onDrop={(event) => {
                  const target = event.target as HTMLElement;
                  if (target.closest(".keyword-lane")) {
                    return;
                  }
                  event.preventDefault();
                  void handleDropToRoot();
                  setDraggingKeyword(null);
                  setDroppingHeadId(null);
                }}
              >
                <div className="keyword-lane-grid">
                {keywordHeadList.map((headKeyword: AdminHeadKeywordOverviewResponse) => (
                  <div
                    key={headKeyword.id}
                    data-head-keyword-id={headKeyword.id}
                    className={`keyword-lane ${droppingHeadId === headKeyword.id ? "is-drop-target" : ""}`}
                    onDragOver={(event) => {
                      if (draggingKeyword) {
                        event.preventDefault();
                        setDroppingHeadId(headKeyword.id);
                      }
                    }}
                    onDragLeave={() => {
                      setDroppingHeadId((prev) => (prev === headKeyword.id ? null : prev));
                    }}
                    onDrop={(event) => {
                      event.preventDefault();
                      void handleDropToHead(headKeyword.id);
                      setDraggingKeyword(null);
                      setDroppingHeadId(null);
                    }}
                  >
                    <div
                      className={`keyword-lane__head ${headKeyword.isVisible ? "" : "is-hidden"}`}
                      draggable
                      onDragStart={() =>
                        setDraggingKeyword({
                          id: headKeyword.id,
                          type: "head",
                          label: headKeyword.value,
                          headId: null,
                        })
                      }
                      onDragEnd={() => {
                        setDraggingKeyword(null);
                        setDroppingHeadId(null);
                      }}
                      onTouchStart={(event) =>
                        handleKeywordTouchStart(event, {
                          id: headKeyword.id,
                          type: "head",
                          label: headKeyword.value,
                          headId: null,
                        })
                      }
                      onTouchMove={handleKeywordTouchMove}
                      onTouchEnd={handleKeywordTouchEnd}
                      onTouchCancel={clearTouchDragState}
                    >
                      <div className="keyword-lane__head-main">
                        <p className={`keyword-lane__title ${headKeyword.isVisible ? "" : "is-hidden"}`}>
                          <strong className="keyword-head-prefix">HEAD</strong> {headKeyword.value}
                        </p>
                      </div>
                      <button
                        type="button"
                        className="btn btn--ghost btn--small keyword-visibility-btn"
                        onClick={() =>
                          void handleToggleKeywordVisible(
                            headKeyword.id,
                            headKeyword.value,
                            headKeyword.isVisible
                          )
                        }
                        disabled={keywordUpdating}
                      >
                        {headKeyword.isVisible ? "비노출" : "노출"}
                      </button>
                    </div>

                    {headKeyword.followers.length === 0 ? (
                      <p className="status-text">팔로워 없음</p>
                    ) : (
                      <div className="keyword-lane__followers">
                        {headKeyword.followers.map((follower) => (
                          <button
                            key={follower.id}
                            type="button"
                            className={`keyword-chip ${follower.isVisible ? "keyword-chip--visible" : "keyword-chip--hidden"} ${follower.isVisible ? "" : "is-hidden"}`}
                            draggable
                            onDragStart={() =>
                              setDraggingKeyword({
                                id: follower.id,
                                type: "follower",
                                label: follower.value,
                                headId: headKeyword.id,
                              })
                            }
                            onDragEnd={() => {
                              setDraggingKeyword(null);
                              setDroppingHeadId(null);
                            }}
                            onTouchStart={(event) =>
                              handleKeywordTouchStart(event, {
                                id: follower.id,
                                type: "follower",
                                label: follower.value,
                                headId: headKeyword.id,
                              })
                            }
                            onTouchMove={handleKeywordTouchMove}
                            onTouchEnd={handleKeywordTouchEnd}
                            onTouchCancel={clearTouchDragState}
                            onClick={() => {
                              if (suppressFollowerClickRef.current) {
                                suppressFollowerClickRef.current = false;
                                return;
                              }
                              void handleToggleKeywordVisible(
                                follower.id,
                                follower.value,
                                follower.isVisible
                              );
                            }}
                            disabled={keywordUpdating}
                          >
                            <span className="keyword-chip__title">{follower.value}</span>
                          </button>
                        ))}
                      </div>
                    )}
                  </div>
                ))}
                </div>
              </div>
            </section>
          ) : null}

          {activeTab === "crawler" ? (
            <section className="panel">
              <div className="panel-actions panel-actions--between">
                <div>
                  <h2>크롤링 selector 관리</h2>
                  <p className="panel__description">
                    blog URL CSS selector 테스트, 추천, 저장(검증 포함)을 수행합니다.
                  </p>
                </div>
              </div>

              <form className="admin-form" onSubmit={handleCssTest}>
                <h3 className="subheading">selector 테스트</h3>
                <div className="admin-form-grid">
                  <label className="admin-field">
                    <span>blogUrl</span>
                    <small>게시글 목록이 있는 블로그 페이지 URL</small>
                    <input
                      value={cssTestBlogUrl}
                      onChange={(event) => setCssTestBlogUrl(event.target.value)}
                      placeholder="https://example.com/blog"
                    />
                  </label>
                  <label className="admin-field">
                    <span>cssSelector</span>
                    <small>게시글 링크를 감싸는 HTML 요소 선택자</small>
                    <input
                      value={cssTestSelector}
                      onChange={(event) => setCssTestSelector(event.target.value)}
                      placeholder=".post-list a.post-link"
                    />
                  </label>
                  <label className="admin-field">
                    <span>sampleSize</span>
                    <small>검증 시 확인할 최대 샘플 개수</small>
                    <input
                      value={cssTestSampleSize}
                      onChange={(event) => setCssTestSampleSize(event.target.value)}
                      placeholder="5"
                    />
                  </label>
                </div>
                <button type="submit" className="btn btn--primary btn--small" disabled={cssTesting}>
                  {cssTesting ? "테스트 중..." : "테스트 실행"}
                </button>
                {cssTestResult ? (
                  <div className="admin-result-box">
                    <p>
                      성공: {cssTestResult.success ? "Y" : "N"} · match {cssTestResult.matchedElementCount} · url{" "}
                      {cssTestResult.extractableUrlCount}
                    </p>
                    {cssTestResult.message ? <p>{cssTestResult.message}</p> : null}
                  </div>
                ) : null}
              </form>

              <form className="admin-form" onSubmit={handleCssSuggest}>
                <h3 className="subheading">selector 추천</h3>
                <div className="admin-form-grid">
                  <label className="admin-field">
                    <span>blogUrl</span>
                    <small>추천할 CSS selector를 찾을 블로그 URL</small>
                    <input
                      value={cssSuggestBlogUrl}
                      onChange={(event) => setCssSuggestBlogUrl(event.target.value)}
                      placeholder="https://example.com/blog"
                    />
                  </label>
                  <label className="admin-field">
                    <span>candidateLimit</span>
                    <small>추천 후보 개수(클수록 느려짐)</small>
                    <input
                      value={cssSuggestLimit}
                      onChange={(event) => setCssSuggestLimit(event.target.value)}
                      placeholder="10"
                    />
                  </label>
                  <label className="admin-field">
                    <span>sampleSize</span>
                    <small>후보 검증에 사용할 샘플 개수</small>
                    <input
                      value={cssSuggestSampleSize}
                      onChange={(event) => setCssSuggestSampleSize(event.target.value)}
                      placeholder="5"
                    />
                  </label>
                </div>
                <button type="submit" className="btn btn--ghost btn--small" disabled={cssSuggesting}>
                  {cssSuggesting ? "추천 중..." : "추천 실행"}
                </button>
                {cssSuggestResult ? (
                  <div className="admin-scroll-list">
                    {cssSuggestResult.candidates.map((candidate) => (
                      <div key={candidate.selector} className="admin-list-item admin-list-item--stack">
                        <div>
                          <strong>{candidate.selector}</strong>
                          <p>
                            confidence {candidate.confidence.toFixed(2)} · match {candidate.matchedElementCount} · url{" "}
                            {candidate.extractableUrlCount}
                          </p>
                          {candidate.sampleMatches.length > 0 ? (
                            <ul className="admin-suggest-samples">
                              {candidate.sampleMatches.map((match) => (
                                <li key={`${candidate.selector}-${match.url}`}>
                                  <a href={match.url} target="_blank" rel="noreferrer">
                                    {match.title}
                                  </a>
                                </li>
                              ))}
                            </ul>
                          ) : null}
                        </div>
                        <button
                          type="button"
                          className="btn btn--primary btn--small"
                          onClick={() => void handleApplySuggestedSelector(candidate.selector)}
                          disabled={applyingSuggestedSelector === candidate.selector}
                        >
                          {applyingSuggestedSelector === candidate.selector
                            ? "적용 중..."
                            : "이 selector 적용"}
                        </button>
                      </div>
                    ))}
                  </div>
                ) : null}
              </form>

              <form className="admin-form" onSubmit={handleSaveSelector}>
                <h3 className="subheading">selector 저장</h3>
                <div className="admin-form-grid">
                  <label className="admin-field">
                    <span>blogId</span>
                    <small>selector를 저장할 블로그의 내부 ID</small>
                    <input
                      value={blogIdForSelector}
                      onChange={(event) => setBlogIdForSelector(event.target.value)}
                      placeholder="123"
                    />
                  </label>
                  <label className="admin-field">
                    <span>cssSelector</span>
                    <small>저장할 최종 CSS selector</small>
                    <input
                      value={selectorForSave}
                      onChange={(event) => setSelectorForSave(event.target.value)}
                      placeholder=".post-list a.post-link"
                    />
                  </label>
                  <label className="admin-field">
                    <span>sampleSize</span>
                    <small>저장 전 검증할 샘플 개수</small>
                    <input
                      value={selectorSaveSampleSize}
                      onChange={(event) => setSelectorSaveSampleSize(event.target.value)}
                      placeholder="5"
                    />
                  </label>
                </div>
                <label className="admin-checkbox">
                  <input
                    type="checkbox"
                    checked={selectorForceSave}
                    onChange={(event) => setSelectorForceSave(event.target.checked)}
                  />
                  테스트 실패여도 저장(force=true)
                </label>
                <button type="submit" className="btn btn--primary btn--small" disabled={savingSelector}>
                  {savingSelector ? "저장 중..." : "selector 저장"}
                </button>
                {selectorSaveResult ? (
                  <div className="admin-result-box">
                    <p>
                      blogId {selectorSaveResult.blogId} · 저장 {selectorSaveResult.saved ? "성공" : "실패"}
                    </p>
                    {selectorSaveResult.message ? <p>{selectorSaveResult.message}</p> : null}
                  </div>
                ) : null}
              </form>
            </section>
          ) : null}
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
