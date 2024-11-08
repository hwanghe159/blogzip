import * as React from 'react';
import styled, {keyframes} from 'styled-components';
import {useNavigate} from "react-router-dom";
import GoogleLoginButton, {handleLogin} from "./GoogleLoginButton";
import {useEffect} from "react";
import {getLoginUser} from "../utils/LoginUserHelper";
import Profile from "./Profile";
import Button from "@mui/material/Button";
import Box from "@mui/material/Box";

const NavBarInner = styled.div`
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  margin-left: auto;
  margin-right: auto;
  width: 100%;
  max-width: 1000px;

  @media screen and (max-width: 1000px) {
    width: calc(100% - 20px);
    max-width: none;
  }
`;

const floatAnimation = keyframes`
  0% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
  100% {
    transform: translateY(0);
  }
`;

const Tooltip = styled.div`
  position: absolute;
  top: 150%;
  left: 10%;
  transform: translate(-50%, 8px);
  background-color: #1976D2;
  color: #ffffff;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 15px;
  font-weight: bold;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  white-space: nowrap;
  animation: ${floatAnimation} 2s ease-in-out infinite;
  z-index: 10;

  &::after {
    content: '';
    position: absolute;
    top: -11px; /* 말풍선 바로 위에 삼각형 위치 */
    left: 65%;
    transform: translateX(-50%);
    border-width: 6px;
    border-style: solid;
    border-color: transparent transparent #1976D2 transparent;
  }
`;

export interface LoginUser {
  id: number;
  accessToken: string;
  email: string;
  image: string;
}

function NavBar() {
  const navigate = useNavigate();
  const [loginUser, setLoginUser] = React.useState<LoginUser | null>(null);

  useEffect(() => {
    const storedUser = getLoginUser();
    if (storedUser) {
      setLoginUser(storedUser);
    }
  }, []);

  return (
      <Box sx={{
        display: "flex",
        height: "60px",
        borderBottom: "1px solid #dfdfdf",
        position: "relative"
      }}>
        <NavBarInner>
          <Box>
            <Box sx={{
              fontSize: "24px",
              fontWeight: "bold",
              cursor: "pointer",
            }} onClick={() => navigate('/')}>
              블로그zip
            </Box>
          </Box>
          <Box sx={{position: "relative"}}>
            {loginUser == null ? (
                <>
                  <GoogleLoginButton/>
                  <Button
                      disableRipple={true}
                      disableFocusRipple={true}
                      disableElevation={true}
                      variant={"contained"}
                      sx={{ml: 2}}
                      onClick={handleLogin}
                  >
                    구독하기
                  </Button>
                  <Tooltip>매일 아침 이메일 받기</Tooltip>
                </>
            ) : (<Profile/>)}
          </Box>
        </NavBarInner>
      </Box>
  );
}

export default NavBar;