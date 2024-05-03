import * as React from "react";
import {useEffect} from "react";
import {Api} from "../utils/Api";
import {useLocation, useNavigate} from "react-router-dom";

export function GoogleLoginPage() {

  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const code = searchParams.get('code');
  const navigate = useNavigate();

  useEffect(() => {
    Api.get(`/api/v1/login/google?code=${code}`)
    .onSuccess((response) => {
      localStorage.setItem('accessToken', response.data.accessToken);
      navigate('/my')
    })
  }, [code, navigate]);

  return (
      <div>
        리다이렉트 중..
      </div>
  )
}