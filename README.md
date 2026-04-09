<h1 align="middle">블로그 구독/요약 서비스 Blogzip</h1>
<br>

## 기능
- 비회원은 운영자가 선정한 블로그들의 요약본을 볼 수 있다.
- 회원은 블로그를 검색하여 구독할 수 있고, 없다면 URL로 직접 추가가 가능하다.
- 회원은 블로그에 올라온 새 글의 요약본을 매일 아침 이메일로 받아볼 수 있다.
- 회원은 게시글을 나중에 읽을 수 있도록 저장할 수 있다.
- 서비스 주소 : https://blogzip.co.kr

## 기술 스택
### 백엔드
- Kotlin, Java, Gradle
- Spring Boot, JPA, MySQL
- Spring Batch
- Selenium, Jsoup
- OpenAI API
- Spring Mail, Thymeleaf
### 프론트엔드
- React, TypeScript, Material-ui
### 인프라
- OCI Compute, OCIR, OCI Vault
### ETC
- Google OAuth2, JWT

## CI/CD
- `master` 브랜치에 머지되면 GitHub Actions가 `api`, `nginx`, `batch` Docker 이미지를 빌드해서 레지스트리에 업로드합니다.
- 같은 워크플로우에서 운영 VM에 SSH로 접속해 최신 `api`, `nginx` 이미지를 `docker compose`로 재배포합니다.
- `batch`는 파라미터가 필요한 일회성 잡이라 자동 상시 배포 대신, `Run Batch Job` 워크플로우에서 최신 이미지를 받아 실행합니다.

### 한 번만 해두면 되는 서버 준비
1. 운영 VM에 Docker Engine + Docker Compose plugin을 설치합니다.
2. 운영 VM에 OCI CLI를 설치합니다.
3. 운영 VM이 OCI Vault를 읽을 수 있도록 Instance Principal IAM 정책을 설정합니다.
4. 80 포트를 외부에 열고, 필요하면 443은 로드밸런서나 리버스 프록시에서 종료합니다.
5. `nginx` 컨테이너가 TLS 종료를 수행하므로 VM에 Certbot 인증서가 있어야 합니다.
6. 아래 파일이 VM에 존재해야 합니다.
- `/etc/letsencrypt/live/blogzip.co.kr/fullchain.pem`
- `/etc/letsencrypt/live/blogzip.co.kr/privkey.pem`
- `/etc/letsencrypt/options-ssl-nginx.conf`
- `/etc/letsencrypt/ssl-dhparams.pem`

### OCI Vault 연동 (prod, local-prod)
- 앱이 직접 Vault를 import 하지 않고, 배포 스크립트(`deploy/scripts/fetch-vault-env.sh`)가 Vault Secret을 읽어 `deploy/compose/.env.runtime`을 생성한 뒤 컨테이너에 주입합니다.
- VM에 별도 `.env.api` 파일을 두지 않아도 됩니다.
- `OCI_REGION`, `OCI_VAULT_ID`는 GitHub Actions Variables에서 SSH 실행 환경변수로 전달합니다.
- `MYSQL_HOST`는 Vault Secret으로 관리하고, `MYSQL_DATABASE`는 `blogzip`으로 고정됩니다.
- `local` 프로파일은 Vault를 사용하지 않고, 필요한 값을 로컬 환경변수로 직접 주입해서 실행합니다.
- 기본 Vault 인증: `instance_principal` (필요 시 GitHub Variables의 `OCI_CLI_AUTH`로 변경 가능)
- 필수 환경변수: `OCI_REGION`, `OCI_VAULT_ID` (`MYSQL_PORT`는 기본값 `3306`)
- Vault Secret 이름은 아래 환경변수 키와 동일하게 생성해야 합니다.
- `JWT_SECRET_KEY`
- `GOOGLE_CLIENT_SECRET`
- `ADMIN_TOKEN`
- `MYSQL_HOST`
- `MYSQL_USERNAME`
- `MYSQL_PASSWORD`
- `OCI_EMAIL_SMTP_USERNAME`
- `OCI_EMAIL_SMTP_PASSWORD`
- `OPEN_AI_API_KEY`
- `SLACK_WEBHOOK_URL`

### GitHub 설정
- Repository Variables
- `REGISTRY_HOST`: 예) `ap-chuncheon-1.ocir.io`
- `IMAGE_PREFIX`: 예) `ap-chuncheon-1.ocir.io/<tenancy-namespace>/blogzip`
- `OCI_REGION`: 예) `ap-chuncheon-1`
- `OCI_VAULT_ID`
- Repository Secrets
- `REGISTRY_USERNAME`
- `REGISTRY_PASSWORD`
- `DEPLOY_HOST`
- `DEPLOY_USER`
- `DEPLOY_SSH_KEY`

## 인프라 아키텍쳐
```mermaid
flowchart TB
  Client["🖥️ Client"]
  DNS["🌐 DNS<br/>Route53 / Gabia"]
  GH["⚙️ GitHub Actions"]
  OCIR["📦 OCIR"]
  Vault["🔐 OCI Vault"]

  subgraph OCI["OCI Cloud (ap-chuncheon-1)"]
    IGW["🛜 Internet Gateway"]
    subgraph VCN["VCN"]
      subgraph PublicSubnet["Public Subnet"]
        subgraph VM["VM.Standard.A1.Flex (Docker Compose)"]
          Nginx["🟩 Nginx"]
          Web["⚛️ Web Static"]
          API["☕ API"]
          Batch["🕒 Batch"]
          Crawler["🕷️ Crawler"]
        end
      end
      subgraph PrivateSubnet["Private DB Subnet"]
        MySQL["🛢️ MySQL"]
      end
    end
  end

  subgraph Integrations["외부 연동 서비스"]
    direction LR
    OpenAI["🤖 OpenAI API"]
    Email["✉️ OCI Email Delivery"]
    Slack["💬 Slack"]
  end

  Client -->|"80/443"| DNS --> IGW --> Nginx
  Nginx -->|"/"| Web
  Nginx -->|"/api"| API

  API -->|"HTTP 8090"| Crawler
  API -->|"3306"| MySQL
  API --> OpenAI
  API --> Email
  API --> Slack

  Batch -->|"HTTP 8090"| Crawler
  Batch -->|"3306"| MySQL
  Batch --> OpenAI
  Batch --> Email
  Batch --> Slack

  GH -->|"Build & Push"| OCIR
  OCIR -->|"Pull image"| VM
  GH -->|"SSH Deploy / Run Batch"| VM
  VM -->|"Secret 조회"| Vault

  classDef external fill:#f7f7f7,stroke:#7a7a7a,color:#111,stroke-width:1.2px;
  classDef infra fill:#f0f7ff,stroke:#2f6feb,color:#111,stroke-width:1.2px;
  classDef app fill:#eefaf0,stroke:#2da44e,color:#111,stroke-width:1.2px;
  classDef db fill:#fff8e6,stroke:#b08800,color:#111,stroke-width:1.2px;

  class Client,DNS,GH,OCIR,OpenAI,Slack,Email,Vault external;
  class IGW infra;
  class Nginx,Web,API,Batch,Crawler app;
  class MySQL db;
```

## 백엔드 모듈 구조
```
backend
├── api : API 서버 애플리케이션 모듈
├── batch : 배치 애플리케이션 모듈
├── domain : 도메인 로직과 DB과의 연결 담당
├── crawler : 크롤링과 ChatGPT API 호출 담당
├── notification : 이메일 발송 담당
└── logging : slack으로 로그 메시지 발송 담당
```
