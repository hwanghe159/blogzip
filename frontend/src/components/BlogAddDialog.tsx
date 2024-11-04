import React, {useState, useEffect} from 'react';
import {
  Button,
  TextField,
  Card,
  CardContent,
  CardActions,
  Typography,
  LinearProgress,
  Box,
  Alert,
  AlertTitle
} from '@mui/material';
import {Api} from "../utils/Api";
import {getLoginUser} from "../utils/LoginUserHelper";
import {handleLogin} from "./GoogleLoginButton";

interface Blog {
  id: number;
  name: string;
  url: string;
  rss: string | null;
  imageUrl: string | null;
}

interface BlogAddDialogProps {
  onClose: () => void; // 다이얼로그를 닫는 함수 prop
}

enum Status {
  INITIAL,

  MANUAL_ADD_REQUIRED,

  BLOG_DUPLICATED,
  UNEXPECTED_ERROR,

  SUCCESS,
}

export default function BlogAddDialog({onClose}: BlogAddDialogProps) {
  const [url, setUrl] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [blog, setBlog] = useState<Blog | null>(null);
  const [status, setStatus] = useState<Status>(Status.INITIAL)

  useEffect(() => {
    if (isLoading) {
      const timer = setInterval(() => {
        setProgress((oldProgress) => {
          if (oldProgress === 100) {
            clearInterval(timer);
            return 100;
          }
          return Math.min(oldProgress + 10, 100);
        });
      }, 500);

      return () => {
        clearInterval(timer);
      };
    }
  }, [isLoading]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setProgress(0);
    setBlog(null);

    Api.post(`/api/v1/blog`, {
          url: url
        },
        {
          headers: {
            Authorization: `Bearer ${getLoginUser()?.accessToken}`,
          }
        })
    .onSuccess((response) => {
      if (response.data.rss === null) {
        setStatus(Status.MANUAL_ADD_REQUIRED)
      } else {
        setStatus(Status.SUCCESS)
      }
      setBlog({
        id: response.data.id,
        name: response.data.name,
        url: response.data.url,
        rss: response.data.rss,
        imageUrl: response.data.image
      })
      setIsLoading(false);
    })
    .on4XX((response) => {
      if (response.code === 'LOGIN_FAILED') {
        alert("로그인이 필요한 서비스입니다.")
        handleLogin()
      }
      if (response.code === 'BLOG_URL_DUPLICATED') {
        setStatus(Status.BLOG_DUPLICATED)
      }
      setIsLoading(false);
    })
    .on5XX((response) => {
      setIsLoading(false);
      setStatus(Status.UNEXPECTED_ERROR)
    })
  };

  const handleSubscribe = async (e: React.FormEvent) => {
    e.preventDefault();

    Api.post(`/api/v1/subscription`, {
          blogId: blog!!.id
        },
        {
          headers: {
            Authorization: `Bearer ${getLoginUser()?.accessToken}`,
          }
        })
    .onSuccess((response) => {
      onClose();
    })
    .on4XX((response) => {

    })
    .on5XX((response) => {

    })
  };


  return (
      <Box sx={{width: '90%', maxWidth: 500, mx: 'auto', my: 8}}>
        <Typography variant="h5" component="h1" gutterBottom>
          URL로 직접 추가하세요.
        </Typography>
        <Card>
          <CardContent>
            {status === Status.INITIAL && (
                <form onSubmit={handleSubmit}>
                  <Box sx={{display: 'flex', flexDirection: 'column', gap: 2}}>
                    <TextField
                        label="URL"
                        error={url === ""}
                        helperText={url === "" ? "URL 형식에 맞게 정확하게 입력해주세요." : ""}
                        placeholder="https://example.com"
                        value={url}
                        onChange={(e) => setUrl(e.target.value)}
                        required
                        fullWidth
                        disabled={isLoading}
                    />
                    <Button type="submit" variant="contained" disabled={isLoading || !url}>
                      {isLoading ? '추가 중...' : 'URL 추가'}
                    </Button>
                  </Box>
                </form>
            )}
            {status === Status.SUCCESS && blog && (
                <Box sx={{mt: 2}}>
                  <Alert severity="success">
                    <AlertTitle>블로그가 추가되었습니다!</AlertTitle>
                  </Alert>
                  <Box sx={{mt: 2}}>
                    <Typography><strong>제목:</strong> {blog.name}</Typography>
                    <Typography><strong>URL:</strong> {blog.url}</Typography>
                    <Typography><strong>RSS 링크:</strong> {blog.rss}</Typography>
                    {blog.imageUrl && (
                        <>
                          <Typography><strong>대표 이미지:</strong></Typography>
                          <img src={blog.imageUrl} alt="사이트 대표 이미지"
                               style={{marginTop: '8px', maxWidth: '100%', height: 'auto'}}/>
                        </>
                    )}
                  </Box>
                  <CardActions>
                    <Button onClick={handleSubscribe} variant="contained" fullWidth>
                      추가한 블로그 구독하기
                    </Button>
                  </CardActions>
                </Box>
            )}
            {status === Status.MANUAL_ADD_REQUIRED && (
                <Box>
                  <Alert severity="warning" sx={{my: 2}}>
                    <AlertTitle>
                      RSS 정보가 없어 수동으로 등록이 필요합니다.
                      운영자에게 요청이 전송되었으니 조금만 기다려주시면 추가해 드릴게요!
                    </AlertTitle>
                  </Alert>
                  <CardActions>
                    <Button onClick={handleSubscribe} variant="contained" fullWidth>
                      확인
                    </Button>
                  </CardActions>
                </Box>
            )}
            {status === Status.BLOG_DUPLICATED && (
                <Box>
                  <Alert severity="warning" sx={{my: 2}}>
                    <AlertTitle>
                      해당하는 블로그가 이미 등록되어 있어요!
                    </AlertTitle>
                  </Alert>
                  <CardActions>
                    <Button onClick={() => {
                      onClose()
                    }} variant="contained" fullWidth>
                      닫기
                    </Button>
                  </CardActions>
                </Box>
            )}
            {status === Status.UNEXPECTED_ERROR && (
                <Box>
                  <Alert severity="error" sx={{my: 2}}>
                    <AlertTitle>
                      예상치 못한 오류가 발생했습니다. 다시 시도해주세요.
                    </AlertTitle>
                  </Alert>
                  <CardActions>
                    <Button onClick={() => {
                      onClose()
                    }} variant="contained" fullWidth>
                      닫기
                    </Button>
                  </CardActions>
                </Box>
            )}

            {isLoading && (
                <Box sx={{mt: 2}}>
                  <LinearProgress variant="determinate" value={progress}/>
                </Box>
            )}

          </CardContent>
        </Card>
      </Box>
  );
}
