import React, { useEffect, useMemo, useState } from 'react';
import { Api } from '../utils/Api';
import { getLoginUser } from '../utils/LoginUserHelper';
import { UserResponse } from '../types';
import { handleLogin } from '../components/GoogleLoginButton';

interface DayOption {
  kor: string;
  eng: string;
  isChecked: boolean;
}

const initialDays: DayOption[] = [
  { kor: '월', eng: 'MONDAY', isChecked: false },
  { kor: '화', eng: 'TUESDAY', isChecked: false },
  { kor: '수', eng: 'WEDNESDAY', isChecked: false },
  { kor: '목', eng: 'THURSDAY', isChecked: false },
  { kor: '금', eng: 'FRIDAY', isChecked: false },
  { kor: '토', eng: 'SATURDAY', isChecked: false },
  { kor: '일', eng: 'SUNDAY', isChecked: false },
];

function EmailPage() {
  const [user, setUser] = useState<UserResponse | null>(null);
  const [daysOfWeek, setDaysOfWeek] = useState<DayOption[]>(initialDays);

  useEffect(() => {
    const loginUser = getLoginUser();
    if (!loginUser) {
      alert('로그인이 필요한 서비스입니다.');
      handleLogin();
      return;
    }

    Api.get('/api/v1/me', {
      headers: {
        Authorization: `Bearer ${loginUser.accessToken}`,
      },
    })
      .onSuccess((response) => {
        const currentUser = response.data as UserResponse;
        setUser(currentUser);
        setDaysOfWeek(
          initialDays.map((day) => ({
            ...day,
            isChecked: currentUser.receiveDays.includes(day.eng),
          }))
        );
      })
      .on4XX(() => {})
      .on5XX(() => {});
  }, []);

  const selectedCount = useMemo(
    () => daysOfWeek.filter((day) => day.isChecked).length,
    [daysOfWeek]
  );

  const handleDayChange = (eng: string) => {
    setDaysOfWeek((prevDays) =>
      prevDays.map((day) => (day.eng === eng ? { ...day, isChecked: !day.isChecked } : day))
    );
  };

  const handleSubmit = () => {
    const loginUser = getLoginUser();
    if (!loginUser) {
      alert('로그인이 필요한 서비스입니다.');
      handleLogin();
      return;
    }

    const receiveDays = daysOfWeek.filter((day) => day.isChecked).map((day) => day.eng);

    Api.put(
      '/api/v1/me',
      {
        receiveDays,
      },
      {
        headers: {
          Authorization: `Bearer ${loginUser.accessToken}`,
        },
      }
    )
      .onSuccess(() => {
        if (receiveDays.length === 0) {
          alert('수신 거부 처리되었습니다.');
          return;
        }

        if (receiveDays.length === 7) {
          alert('수정되었습니다. 매일 아침 보내드릴게요!');
          return;
        }

        alert('수정되었습니다. 선택한 요일 아침에만 메일을 보내드릴게요!');
      })
      .on4XX(() => {})
      .on5XX(() => {});
  };

  return (
    <div className="stack">
      <section className="surface panel">
        <h1 className="page-title">이메일 설정</h1>
        <p className="page-subtitle">아침 9시에 받아볼 요약 메일의 수신 요일을 선택할 수 있어요.</p>
      </section>

      <section className="surface-strong panel stack">
        <div className="stack" style={{ gap: 8 }}>
          <strong style={{ color: 'var(--text-strong)' }}>수신 이메일</strong>
          <input className="input" value={user?.email ?? ''} readOnly />
        </div>

        <div className="stack" style={{ gap: 8 }}>
          <strong style={{ color: 'var(--text-strong)' }}>수신 요일</strong>
          <div className="day-grid">
            {daysOfWeek.map((day) => (
              <button
                key={day.eng}
                type="button"
                className={`day-button ${day.isChecked ? 'active' : ''}`}
                onClick={() => handleDayChange(day.eng)}
              >
                {day.kor}
              </button>
            ))}
          </div>
          <span className="page-subtitle" style={{ marginTop: 2 }}>
            현재 {selectedCount}일 선택됨
          </span>
        </div>

        <div className="modal-footer" style={{ padding: 0 }}>
          <button type="button" className="btn btn-primary" onClick={handleSubmit}>
            설정 저장
          </button>
        </div>
      </section>
    </div>
  );
}

export default EmailPage;
