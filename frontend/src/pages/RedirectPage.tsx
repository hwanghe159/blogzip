import React, {useEffect} from 'react';
import {useLocation, useNavigate} from 'react-router-dom';
import Box from "@mui/material/Box";
import {Api} from "../utils/Api";

function RedirectPage() {
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const url = searchParams.get('url');
  const userId = searchParams.get('userId');
  const navigate = useNavigate();

  useEffect(() => {
    if (url === null || userId === null) {
      alert("잘못된 요청입니다.")
      navigate('/')
      return
    }
    Api.post(`/api/v1/user/${userId}/read`, {articleUrl: url}, {})
    .onSuccess(response => {
    })
    .on4XX((response) => {
    })
    .on5XX((response) => {
    })
    window.location.href = url;
    // window.open(url, '_blank');
  }, [url, navigate, location]);

  return (
      <Box></Box>
  );
}

export default RedirectPage;