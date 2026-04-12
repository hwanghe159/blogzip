import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { ApiError, updateReceiveDays, withdrawMe } from "../api";
import { DayOfWeek, SessionState, UserResponse } from "../types";
import { EmptyState } from "../components/EmptyState";
import { Toast } from "../components/Toast";

type AccountPageProps = {
  session: SessionState;
  loginUrl: string;
  onLogout: () => void;
  onUserUpdated: (user: UserResponse) => void;
};

type AccountToast = {
  id: number;
  message: string;
  tone: "success" | "error" | "info";
  durationMs?: number;
};

const DAY_OPTIONS: { value: DayOfWeek; label: string }[] = [
  { value: "MONDAY", label: "월" },
  { value: "TUESDAY", label: "화" },
  { value: "WEDNESDAY", label: "수" },
  { value: "THURSDAY", label: "목" },
  { value: "FRIDAY", label: "금" },
  { value: "SATURDAY", label: "토" },
  { value: "SUNDAY", label: "일" },
];

export function AccountPage({
  session,
  loginUrl,
  onLogout,
  onUserUpdated,
}: AccountPageProps) {
  const [withdrawing, setWithdrawing] = useState(false);
  const [savingReceiveDays, setSavingReceiveDays] = useState(false);
  const [receiveDays, setReceiveDays] = useState<DayOfWeek[]>([]);
  const [toast, setToast] = useState<AccountToast | null>(null);
  const toastSeqRef = useRef(0);
  const toastTimerRef = useRef<number | null>(null);

  const token = session.token;
  const isAuthenticated = session.status === "authenticated" && !!token;

  useEffect(() => {
    if (!session.user) return;
    setReceiveDays(session.user.receiveDays);
  }, [session.user]);

  useEffect(() => {
    return () => {
      if (toastTimerRef.current !== null) {
        window.clearTimeout(toastTimerRef.current);
      }
    };
  }, []);

  function showToast(
    message: string,
    tone: AccountToast["tone"],
    durationMs = 2800
  ) {
    if (toastTimerRef.current !== null) {
      window.clearTimeout(toastTimerRef.current);
    }
    toastSeqRef.current += 1;
    setToast({ id: toastSeqRef.current, message, tone, durationMs });
    toastTimerRef.current = window.setTimeout(() => {
      setToast(null);
      toastTimerRef.current = null;
    }, durationMs);
  }

  function toggleReceiveDay(targetDay: DayOfWeek) {
    setReceiveDays((prev) => {
      if (prev.includes(targetDay)) {
        return prev.filter((day) => day !== targetDay);
      }
      const merged = [...prev, targetDay];
      return DAY_OPTIONS.map((day) => day.value).filter((day) => merged.includes(day));
    });
  }

  async function handleSaveReceiveDays() {
    if (!token) return;
    if (receiveDays.length === 0) {
      showToast("최소 1개의 요일을 선택해 주세요.", "error");
      return;
    }

    setSavingReceiveDays(true);
    try {
      const updated = await updateReceiveDays(token, receiveDays);
      onUserUpdated(updated);
      showToast("이메일 수신 요일을 저장했어요.", "success");
    } catch (error) {
      const message =
        error instanceof ApiError ? error.message : "이메일 설정 저장에 실패했습니다.";
      showToast(message, "error");
    } finally {
      setSavingReceiveDays(false);
    }
  }

  async function handleWithdraw() {
    if (!token) return;
    const confirmed = window.confirm(
      "정말 탈퇴하시겠어요?\n탈퇴하면 계정이 비활성화되고, 나중에 읽기, 구독 데이터가 영구적으로 삭제됩니다."
    );
    if (!confirmed) return;

    setWithdrawing(true);
    try {
      await withdrawMe(token);
      onLogout();
      window.location.href = "/";
    } catch (error) {
      const message =
        error instanceof ApiError ? error.message : "회원 탈퇴에 실패했습니다.";
      showToast(message, "error");
      setWithdrawing(false);
    }
  }

  function handleAccountLogout() {
    onLogout();
    window.location.href = "/";
  }

  if (!isAuthenticated || !session.user) {
    return (
      <section className="page-section">
        <EmptyState
          title="로그인 후 이용 가능해요"
          description="계정 정보 확인과 이메일 설정, 회원 탈퇴는 로그인 후 이용할 수 있습니다."
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
          <p className="eyebrow">ACCOUNT</p>
          <h1>계정</h1>
        </div>
      </div>

      <section className="panel">
        <h2>내 계정 정보</h2>
        <p className="panel__description panel__description--email">
          수신 이메일: <strong>{session.user.email}</strong>
        </p>
        <p className="panel__description">선택한 요일 오전 9시에 새로운 글을 요약해서 보내드려요.</p>
        <div className="day-picker">
          {DAY_OPTIONS.map((day) => {
            const isActive = receiveDays.includes(day.value);
            return (
              <button
                key={day.value}
                type="button"
                className={isActive ? "day-chip day-chip--active" : "day-chip"}
                onClick={() => toggleReceiveDay(day.value)}
                disabled={savingReceiveDays}
              >
                {day.label}
              </button>
            );
          })}
        </div>
        <div className="day-save-row">
          <button
            type="button"
            className="btn btn--primary btn--compact"
            disabled={savingReceiveDays}
            onClick={handleSaveReceiveDays}
          >
            {savingReceiveDays ? "저장 중..." : "저장"}
          </button>
        </div>
        <div className="account-session-actions">
          <button type="button" className="btn btn--ghost btn--small" onClick={handleAccountLogout}>
            로그아웃
          </button>
        </div>
      </section>

      <section className="panel panel--danger account-withdraw-panel">
        <h2>계정 탈퇴</h2>
        <p className="panel__description">
          탈퇴하면 계정이 비활성화되고, 나중에 읽기, 구독 데이터가 영구적으로 삭제됩니다.
        </p>
        <div>
          <button
            type="button"
            className="btn btn--danger"
            disabled={withdrawing}
            onClick={handleWithdraw}
          >
            {withdrawing ? "탈퇴 처리 중..." : "회원 탈퇴"}
          </button>
        </div>
      </section>

      {session.user.isAdmin ? (
        <div className="account-admin-console">
          <Link to="/admin" className="btn btn--ghost">
            관리자 콘솔
          </Link>
        </div>
      ) : null}

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
