import React from 'react';
import Button from "@mui/material/Button";
import {Link} from 'react-router-dom';
import Box from "@mui/material/Box";
import Typography from "@mui/material/Typography";

function NotFoundPage() {
  return (
      <Box
          display="flex"
          flexDirection="column"
          justifyContent="center"
          alignItems="center"
          minHeight="100vh"
      >
        <Typography variant="h4" component="h1" gutterBottom>
          404 Not Found
        </Typography>
        <Typography variant="subtitle1" gutterBottom>
          죄송합니다. 요청하신 페이지를 찾을 수 없습니다.
        </Typography>
        <Button
            component={Link}
            to="/"
            variant="contained"
            color="primary"
        >
          홈으로 돌아가기
        </Button>
      </Box>
  );
}

export default NotFoundPage;