import {useEffect, useState} from "react";
import {Api} from "../utils/Api";
import {useNavigate} from "react-router-dom";

interface Blog {
  id: number;
  name: string;
  url: string;
}

interface Subscription {
  id: number;
  blog: Blog;
}

function MyPage() {

  const [accessToken, setAccessToken] = useState('');
  const [subscriptions, setSubscriptions] = useState<Subscription[]>([])
  const [blogs, setBlogs] = useState<Blog[]>([])
  const navigate = useNavigate();

  useEffect(() => {
    const accessToken = localStorage.getItem('accessToken');
    if (accessToken) {
      setAccessToken(accessToken)
    } else {
      navigate('/login')
      return
    }

    Api.get(`/api/v1/blog`)
    .onSuccess((response) => {
      setBlogs(response.data)
    })

    // Api.get(`/api/v1/subscription`, {
    //   headers: {
    //     Authorization: `Bearer ${accessToken}`,
    //   }
    // })
    // .onSuccess((response) => {
    //   setSubscriptions(response.data)
    // })
    // .on4XX((response) => {
    //   if (response.code === 'LOGIN_FAILED') {
    //     navigate('/login');
    //   } else {
    //
    //   }
    // })
    // .on5XX((response) => {
    //
    // })
  }, [navigate])

  return (
      <div>
        <h1>구독 목록</h1>
        {subscriptions.length > 0 ? (
            <ul>
              {subscriptions.map(sub => (
                  <li key={sub.id}>{sub.blog.name}</li>
              ))}
            </ul>
        ) : (
            <p>구독 정보가 없습니다.</p>
        )}

        <h1>블로그 목록</h1>
        <ul>
          {blogs.map(blog => (
              <li key={blog.id}>{blog.name}</li>
          ))}
        </ul>
      </div>
  )
}

export default MyPage;