import * as React from 'react';
import AppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import Button from '@mui/material/Button';
import Typography from "@mui/material/Typography";
import styled from 'styled-components';

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
`;

const LoginButton = styled.button`
  color: black; 
  border: none; 
  cursor: pointer; 
  font-size: 20px;
`;

function NavBar() {
  return (
      <Container>
        <NavBarInner>
          <Logo>블로그zip</Logo>
          <LoginButton>로그인</LoginButton>
        </NavBarInner>
      </Container>
  );
}

export default NavBar;
