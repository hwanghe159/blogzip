FROM node:20-bookworm AS build

WORKDIR /workspace/frontend

COPY frontend/package*.json ./
RUN npm ci

COPY frontend/ ./

ENV NODE_OPTIONS=--max_old_space_size=700
RUN npm run build

FROM nginx:1.27-alpine

COPY deploy/nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/frontend/build /usr/share/nginx/html

EXPOSE 80
