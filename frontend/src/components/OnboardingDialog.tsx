import React, { ChangeEvent, useEffect, useMemo, useState } from 'react';
import { Api } from '../utils/Api';
import { getLoginUser } from '../utils/LoginUserHelper';

interface OnboardingDialogProps {
  onClose: () => void;
}

interface BlogOption {
  id: number;
  name: string;
  url: string;
  image: string | null;
  isShowOnMain: boolean;
  isChecked: boolean;
}

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

export default function OnboardingDialog({ onClose }: OnboardingDialogProps) {
  const [step, setStep] = useState<'SUBSCRIPTION' | 'EMAIL'>('SUBSCRIPTION');
  const [blogs, setBlogs] = useState<BlogOption[]>([]);
  const [daysOfWeek, setDaysOfWeek] = useState<DayOption[]>(initialDays);

  useEffect(() => {
    Api.get('/api/v1/blog')
      .onSuccess((response) => {
        const data = response.data as BlogOption[];
        setBlogs(
          data
            .filter((blog) => blog.isShowOnMain)
            .map((blog) => ({
              id: blog.id,
              name: blog.name,
              url: blog.url,
              image: blog.image,
              isShowOnMain: blog.isShowOnMain,
              isChecked: false,
            }))
        );
      })
      .on4XX(() => {})
      .on5XX(() => {});
  }, []);

  const selectedBlogCount = useMemo(
    () => blogs.filter((blog) => blog.isChecked).length,
    [blogs]
  );

  const selectedDayCount = useMemo(
    () => daysOfWeek.filter((day) => day.isChecked).length,
    [daysOfWeek]
  );

  const handleSelectAll = (event: ChangeEvent<HTMLInputElement>) => {
    setBlogs((prevBlogs) => prevBlogs.map((blog) => ({ ...blog, isChecked: event.target.checked })));
  };

  const handleSelectBlog = (blogId: number) => {
    setBlogs((prevBlogs) =>
      prevBlogs.map((blog) => (blog.id === blogId ? { ...blog, isChecked: !blog.isChecked } : blog))
    );
  };

  const handleSubscribe = () => {
    const loginUser = getLoginUser();
    if (!loginUser) {
      window.location.reload();
      return;
    }

    const selectedBlogIds = blogs.filter((blog) => blog.isChecked).map((blog) => blog.id);

    selectedBlogIds.forEach((blogId) => {
      Api.post(
        '/api/v1/subscription',
        {
          blogId,
        },
        {
          headers: {
            Authorization: `Bearer ${loginUser.accessToken}`,
          },
        }
      )
        .onSuccess(() => {})
        .on4XX(() => {})
        .on5XX(() => {});
    });

    setStep('EMAIL');
  };

  const handleDayChange = (eng: string) => {
    setDaysOfWeek((prevDays) =>
      prevDays.map((day) => (day.eng === eng ? { ...day, isChecked: !day.isChecked } : day))
    );
  };

  const handleEmailSetting = (receiveDays: string[]) => {
    const loginUser = getLoginUser();
    if (!loginUser) {
      onClose();
      return;
    }

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
        onClose();
        window.location.reload();
      })
      .on4XX(() => {
        onClose();
      })
      .on5XX(() => {
        onClose();
      });
  };

  return (
    <>
      <div className="modal-header">
        <h2 className="modal-title">처음 오셨네요. 맞춤 설정을 시작해볼까요?</h2>
      </div>

      {step === 'SUBSCRIPTION' ? (
        <div className="modal-body stack">
          <p className="page-subtitle" style={{ marginTop: 0 }}>
            메인에 추천된 블로그 중 관심 있는 곳을 선택해 주세요.
          </p>

          <label style={{ display: 'inline-flex', alignItems: 'center', gap: 8, fontWeight: 700 }}>
            <input
              className="checkbox"
              type="checkbox"
              checked={blogs.length > 0 && blogs.every((blog) => blog.isChecked)}
              onChange={handleSelectAll}
            />
            전체 선택
          </label>

          {blogs.length > 0 ? (
            <div className="onboarding-list">
              {blogs.map((blog) => (
                <article key={blog.id} className="onboarding-blog" onClick={() => handleSelectBlog(blog.id)}>
                  <input
                    className="checkbox"
                    type="checkbox"
                    checked={blog.isChecked}
                    onClick={(event) => event.stopPropagation()}
                    onChange={() => handleSelectBlog(blog.id)}
                  />
                  <img src={blog.image ?? '/default_blog_image.png'} alt={blog.name} />
                  <div className="stack" style={{ gap: 4 }}>
                    <strong>{blog.name}</strong>
                    <span>{blog.url}</span>
                  </div>
                </article>
              ))}
            </div>
          ) : (
            <div className="list-empty">추천 블로그를 불러오는 중입니다.</div>
          )}

          <div className="modal-footer" style={{ padding: 0 }}>
            <button type="button" className="btn btn-text" onClick={onClose}>
              닫기
            </button>
            <button
              type="button"
              className="btn btn-primary"
              disabled={selectedBlogCount === 0}
              onClick={handleSubscribe}
            >
              다음
            </button>
          </div>
        </div>
      ) : null}

      {step === 'EMAIL' ? (
        <div className="modal-body stack">
          <p className="page-subtitle" style={{ marginTop: 0 }}>
            요약 이메일은 오전 9시에 발송됩니다. 받고 싶은 요일을 골라 주세요.
          </p>

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

          <span className="page-subtitle">현재 {selectedDayCount}일 선택됨</span>

          <div className="modal-footer" style={{ padding: 0 }}>
            <button type="button" className="btn btn-text" onClick={() => handleEmailSetting([])}>
              안 받을래요
            </button>
            <button
              type="button"
              className="btn btn-primary"
              disabled={selectedDayCount === 0}
              onClick={() => handleEmailSetting(daysOfWeek.filter((day) => day.isChecked).map((day) => day.eng))}
            >
              완료
            </button>
          </div>
        </div>
      ) : null}
    </>
  );
}
