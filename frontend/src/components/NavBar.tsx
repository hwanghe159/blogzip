import * as React from 'react';
import styled from 'styled-components';
import {useNavigate} from "react-router-dom";
import GoogleLoginButton from "./GoogleLoginButton";
import {useEffect} from "react";
import {getLoginUser} from "../utils/LoginUserHelper";
import Profile from "./Profile";

const Container = styled.nav`
  //justify-content: center; // 좌우 정렬
  display: flex;
  height: 60px;
  border-bottom: 1px solid #dfdfdf;
`;

const NavBarInner = styled.div`
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center; // 상하 정렬
  margin-left: auto;
  margin-right: auto;
  width: 100%;
  max-width: 1000px;

  @media screen and (max-width: 1000px) {
    width: calc(100% - 20px);
    max-width: none;
  }
`;

const Logo = styled.div`
  font-size: 24px;
  font-weight: bold;
  cursor: pointer;
`;

export interface LoginUser {
  id: number;
  accessToken: string,
  email: string,
  image: string,
}

function NavBar() {
  const navigate = useNavigate();
  const [loginUser, setLoginUser] = React.useState<LoginUser | null>(null);

  useEffect(() => {
    const storedUser = getLoginUser()
    if (storedUser) {
      setLoginUser(storedUser);
    }
  }, []);

  return (
      <Container>
        <NavBarInner>
          <Logo onClick={() => navigate('/')}>블로그zip</Logo>
          {loginUser == null ? (<GoogleLoginButton/>) : (<Profile/>)}
        </NavBarInner>
      </Container>
  );
}

export default NavBar;
