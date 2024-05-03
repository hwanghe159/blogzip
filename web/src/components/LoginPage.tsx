import GoogleLoginButton from "./GoogleLoginButton";
import * as React from "react";

export function LoginPage() {
  return (
      <div>
        <h1>로그인 페이지</h1>
        <GoogleLoginButton buttonProps={{
          color: "primary",
          variant: "contained",
          size: "small",
          component: "a",
          target: "_blank",
          sx: {width: '100%'},
        }} text='구글로 로그인'/>
      </div>
  )
}