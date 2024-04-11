import React, {useEffect} from 'react';
import {useLocation, useNavigate} from 'react-router-dom';
import {Api} from "../utils/Api";

function EmailVerifyPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const searchParams = new URLSearchParams(location.search);
  const address = searchParams.get('address');
  const code = searchParams.get('code');

  useEffect(() => {
    if (address && code) {
      Api.get(`/api/v1/email/${address}/verify/${code}`)
      .onSuccess((response) => {
        alert("인증이 완료되었습니다. 내일부터 이메일을 보내드릴게요!");
        navigate("/");
      })
      .on4XX((response) => {
        if (response.code === "ALREADY_VERIFIED") {
          alert("이미 인증이 완료된 이메일입니다.");
        } else {
          alert("인증에 실패했습니다.");
        }
        navigate("/");
      })
      .on5XX((response) => {
        alert("인증에 실패했습니다.");
        navigate("/");
      })
    }
  }, [address, code, navigate]);

  return (
      <div>
        <h2>이메일 인증 중...</h2>
      </div>
  );
}

export default EmailVerifyPage;