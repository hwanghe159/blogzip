import * as React from "react";
import Box from "@mui/material/Box";
import {Avatar, IconButton, Menu, MenuItem} from "@mui/material";
import Typography from "@mui/material/Typography";
import {useNavigate} from "react-router-dom";
import {getLoginUser, removeLoginUser} from "../utils/LoginUserHelper";

function Profile() {

  const navigate = useNavigate();

  const menus = [
    {
      name: '구독 설정', onClick: () => {
        navigate('/subscription')
      }
    },
    {
      name: '이메일 설정', onClick: () => {
        navigate('/email')
      }
    },
    {
      name: '로그아웃', onClick: () => {
        window.location.href = '/';
        removeLoginUser()
      }
    }
  ];

  const [anchorElUser, setAnchorElUser] = React.useState<null | HTMLElement>(null);

  const handleOpenUserMenu = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorElUser(event.currentTarget);
  };

  const handleCloseUserMenu = () => {
    setAnchorElUser(null);
  };

  return (
      <Box sx={{flexGrow: 0}}>
        <IconButton disableRipple={true} onClick={handleOpenUserMenu} sx={{p: 0}}>
          <Avatar src={getLoginUser()?.image}/>
        </IconButton>
        <Menu
            sx={{mt: '45px'}}
            id="menu-appbar"
            anchorEl={anchorElUser}
            anchorOrigin={{
              vertical: 'top',
              horizontal: 'right',
            }}
            keepMounted
            transformOrigin={{
              vertical: 'top',
              horizontal: 'right',
            }}
            open={Boolean(anchorElUser)}
            onClose={handleCloseUserMenu}
            transitionDuration={0}
        >
          {menus.map((menu) => (
              <MenuItem disableRipple={true} key={menu.name} onClick={() => {
                handleCloseUserMenu();
                menu.onClick();
              }}>
                <Typography textAlign="center">{menu.name}</Typography>
              </MenuItem>
          ))}
        </Menu>
      </Box>
  );
}

export default Profile;