FROM mcr.microsoft.com/playwright:v1.52.0-noble

WORKDIR /app

COPY crawler/package.json /app/package.json

RUN npm install --omit=dev

COPY crawler/ /app/

ENV NODE_ENV=production
ENV PORT=8090
ENV HEADLESS=true

EXPOSE 8090

CMD ["npm", "start"]
