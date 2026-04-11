import React, { FormEvent, useMemo, useState } from 'react';
import { Api } from '../utils/Api';
import { getLoginUser } from '../utils/LoginUserHelper';
import { handleLogin } from './GoogleLoginButton';

interface Blog {
  id: number;
  name: string;
  url: string;
  rss: string | null;
  image: string | null;
}

interface BlogAddDialogProps {
  onClose: () => void;
}

type DialogStatus =
  | 'INITIAL'
  | 'SUCCESS'
  | 'MANUAL_ADD_REQUIRED'
  | 'BLOG_DUPLICATED'
  | 'BLOG_URL_NOT_VALID'
  | 'UNEXPECTED_ERROR';

function isValidUrl(value: string): boolean {
  try {
    const parsed = new URL(value);
    return parsed.protocol === 'http:' || parsed.protocol === 'https:';
  } catch (_error) {
    return false;
  }
}

export default function BlogAddDialog({ onClose }: BlogAddDialogProps) {
  const [url, setUrl] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [status, setStatus] = useState<DialogStatus>('INITIAL');
  const [blog, setBlog] = useState<Blog | null>(null);

  const isUrlValid = useMemo(() => isValidUrl(url.trim()), [url]);

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const loginUser = getLoginUser();
    if (!loginUser) {
      alert('로그인이 필요한 서비스입니다.');
      handleLogin();
      return;
    }

    if (!isUrlValid) {
      alert('URL 형식에 맞게 정확히 입력해주세요.');
      return;
    }

    setIsLoading(true);
    setStatus('INITIAL');
    setBlog(null);

    Api.post(
      '/api/v1/blog',
      {
        url: url.trim(),
      },
      {
        headers: {
          Authorization: `Bearer ${loginUser.accessToken}`,
        },
      }
    )
      .onSuccess((response) => {
        const createdBlog = response.data as Blog;

        setBlog(createdBlog);
        setStatus(createdBlog.rss ? 'SUCCESS' : 'MANUAL_ADD_REQUIRED');
        setIsLoading(false);
      })
      .on4XX((errorResponse) => {
        if (errorResponse.code === 'LOGIN_FAILED') {
          setIsLoading(false);
          alert('로그인이 필요한 서비스입니다.');
          handleLogin();
          return;
        }

        if (errorResponse.code === 'BLOG_URL_DUPLICATED') {
          setStatus('BLOG_DUPLICATED');
        } else if (errorResponse.code === 'BLOG_URL_NOT_VALID') {
          setStatus('BLOG_URL_NOT_VALID');
        } else {
          setStatus('UNEXPECTED_ERROR');
        }

        setIsLoading(false);
      })
      .on5XX(() => {
        setStatus('UNEXPECTED_ERROR');
        setIsLoading(false);
      });
  };

  const handleSubscribe = () => {
    const loginUser = getLoginUser();
    if (!loginUser || !blog) {
      onClose();
      return;
    }

    Api.post(
      '/api/v1/subscription',
      {
        blogId: blog.id,
      },
      {
        headers: {
          Authorization: `Bearer ${loginUser.accessToken}`,
        },
      }
    )
      .onSuccess(() => {
        onClose();
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
        <h2 className="modal-title">URL로 블로그 직접 추가</h2>
      </div>
      <div className="modal-body stack">
        {status === 'INITIAL' ? (
          <form className="stack" onSubmit={handleSubmit}>
            <p className="page-subtitle" style={{ marginTop: 0 }}>
              구독하고 싶은 블로그 주소를 입력하면 자동으로 RSS를 확인해 추가해요.
            </p>
            <input
              className="input"
              value={url}
              onChange={(event) => setUrl(event.target.value)}
              placeholder="https://example.com"
            />
            <div className="modal-footer" style={{ padding: 0 }}>
              <button type="button" className="btn btn-text" onClick={onClose}>
                취소
              </button>
              <button type="submit" className="btn btn-primary" disabled={!isUrlValid || isLoading}>
                {isLoading ? '추가 중...' : '블로그 추가'}
              </button>
            </div>
          </form>
        ) : null}

        {status === 'SUCCESS' && blog ? (
          <div className="stack">
            <div className="banner banner-success">블로그가 정상적으로 추가되었습니다. 바로 구독하시겠어요?</div>
            <div className="surface panel stack" style={{ padding: 16 }}>
              <strong>{blog.name}</strong>
              <span>{blog.url}</span>
              {blog.rss ? <span>RSS: {blog.rss}</span> : null}
              {blog.image ? (
                <img
                  src={blog.image}
                  alt={blog.name}
                  style={{ width: '100%', maxHeight: 180, objectFit: 'cover', borderRadius: 12 }}
                />
              ) : null}
            </div>
            <div className="modal-footer" style={{ padding: 0 }}>
              <button type="button" className="btn btn-text" onClick={onClose}>
                닫기
              </button>
              <button type="button" className="btn btn-primary" onClick={handleSubscribe}>
                추가한 블로그 구독
              </button>
            </div>
          </div>
        ) : null}

        {status === 'MANUAL_ADD_REQUIRED' ? (
          <div className="stack">
            <div className="banner banner-warning">
              RSS 정보를 자동으로 찾지 못해 수동 등록 요청이 접수되었습니다. 확인 후 추가될 예정입니다.
            </div>
            <div className="modal-footer" style={{ padding: 0 }}>
              <button type="button" className="btn btn-text" onClick={onClose}>
                닫기
              </button>
              <button type="button" className="btn btn-primary" onClick={handleSubscribe}>
                확인하고 구독
              </button>
            </div>
          </div>
        ) : null}

        {status === 'BLOG_DUPLICATED' ? (
          <div className="stack">
            <div className="banner banner-warning">이미 등록되어 있는 블로그 주소입니다.</div>
            <div className="modal-footer" style={{ padding: 0 }}>
              <button type="button" className="btn btn-primary" onClick={onClose}>
                확인
              </button>
            </div>
          </div>
        ) : null}

        {status === 'BLOG_URL_NOT_VALID' ? (
          <div className="stack">
            <div className="banner banner-warning">URL 형식이 올바르지 않습니다.</div>
            <div className="modal-footer" style={{ padding: 0 }}>
              <button type="button" className="btn btn-primary" onClick={onClose}>
                확인
              </button>
            </div>
          </div>
        ) : null}

        {status === 'UNEXPECTED_ERROR' ? (
          <div className="stack">
            <div className="banner banner-error">예상치 못한 오류가 발생했습니다. 잠시 후 다시 시도해주세요.</div>
            <div className="modal-footer" style={{ padding: 0 }}>
              <button type="button" className="btn btn-primary" onClick={onClose}>
                닫기
              </button>
            </div>
          </div>
        ) : null}
      </div>
    </>
  );
}
