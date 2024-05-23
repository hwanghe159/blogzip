import * as React from 'react';
import Button from '@mui/material/Button';
import styled from 'styled-components';
import {Link, useNavigate} from "react-router-dom";

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

function NavBar() {
  const navigate = useNavigate();

  return (
      <Container>
        <NavBarInner>
          <Logo onClick={() => navigate('/')}>블로그zip</Logo>
          <Button
              to="/login"
              component={Link}
              size="large"
              disableRipple={true}>
            로그인
          </Button>
        </NavBarInner>
      </Container>
  );
}

export default NavBar;
