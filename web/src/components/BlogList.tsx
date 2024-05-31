import React from "react";
import { BlogResponse } from "../pages/MainPage";
import { Card, CardActionArea, CardActions, CardContent, CardMedia, Grid } from "@mui/material";
import Typography from "@mui/material/Typography";
import Button from "@mui/material/Button";
import Box from "@mui/material/Box";

interface BlogsProps {
  blogs: BlogResponse[];
}

export function BlogList({ blogs }: BlogsProps) {
  return (
      <Box sx={{ width: '100%', maxWidth: 1000, margin: 'auto', mt: 4 }}>
        <Grid container spacing={2}>
          {blogs.map(blog =>
              <Grid item key={blog.id} xs={12} sm={6} md={4} lg={3}>
                <Card sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
                  <CardActionArea disableRipple={true} sx={{ flexGrow: 1 }}>
                    <CardMedia
                        component="img"
                        height="200"
                        image={blog.image || "/default_blog_image.png"}
                        alt={blog.name}
                    />
                    <CardContent>
                      <Typography gutterBottom variant="h5" component="div">
                        {blog.name}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        {blog.url}
                      </Typography>
                    </CardContent>
                  </CardActionArea>
                  <CardActions>
                    <Button size="small" color="primary">
                      구독중
                    </Button>
                  </CardActions>
                </Card>
              </Grid>
          )}
        </Grid>
      </Box>
  );
}
