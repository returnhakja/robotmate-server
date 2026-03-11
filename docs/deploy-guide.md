# RobotMate 서버 배포 가이드

> **스택:** Render (Spring Boot) + Supabase (PostgreSQL)
> **목표:** `api.robotmate.kr` 에서 서버 운영, GitHub push 시 자동 배포

---

## 전체 순서 요약

1. Supabase — PostgreSQL 데이터베이스 생성
2. 프로젝트 — `application-prod.yml` 작성
3. GitHub — 코드 push
4. Render — 서버 배포 + 환경변수 설정
5. Cloudflare — `api.robotmate.kr` 도메인 연결
6. Render — 커스텀 도메인 등록
7. 확인

---

## 1단계. Supabase — 데이터베이스 생성

### 1-1. 회원가입

1. [https://supabase.com](https://supabase.com) 접속
2. 우측 상단 **Start your project** 클릭
3. GitHub 계정으로 로그인

### 1-2. 프로젝트 생성

1. 로그인 후 대시보드에서 **New project** 클릭
2. 아래 항목 입력:
   - **Organization**: 본인 계정 선택
   - **Project name**: `robotmate` 입력
   - **Database Password**: 강력한 비밀번호 입력 후 **반드시 어딘가에 저장** (나중에 다시 볼 수 없음)
   - **Region**: `Northeast Asia (Seoul)` 선택 (한국 서버)
3. **Create new project** 클릭
4. 프로젝트 생성까지 약 1~2분 대기

### 1-3. JDBC URL 복사

1. 좌측 사이드바에서 **Settings** (톱니바퀴 아이콘) 클릭
2. **Database** 메뉴 클릭
3. 페이지 중간 **Connection string** 섹션 찾기
4. 탭에서 **JDBC** 선택
5. 표시된 URL 복사 (예시):
   ```
   jdbc:postgresql://db.xxxxxxxxxxxx.supabase.co:5432/postgres
   ```
6. URL 안의 `[YOUR-PASSWORD]` 부분을 1-2단계에서 설정한 비밀번호로 교체

> **저장해둘 정보:**
> - JDBC URL (비밀번호 포함)
> - DB Username: `postgres`
> - DB Password: 설정한 비밀번호

---

## 2단계. 프로젝트 — prod 설정 파일 작성

### 2-1. `application-prod.yml` 생성

`src/main/resources/application-prod.yml` 파일 생성:

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

server:
  port: ${PORT:8080}
```

> 실제 값은 Render 환경변수에서 주입하므로 여기에 직접 쓰지 않습니다.

### 2-2. `.gitignore` 확인

`application-local.yml` 이 `.gitignore` 에 포함되어 있는지 확인:

```
application-local.yml
```

포함되어 있지 않으면 추가. (DB 비밀번호, Google Client ID 등이 GitHub에 올라가면 안 됨)

### 2-3. GitHub에 push

```bash
git add .
git commit -m "add prod profile"
git push origin main
```

---

## 3단계. Render — 서버 배포

### 3-1. 회원가입

1. [https://render.com](https://render.com) 접속
2. 우측 상단 **Get Started for Free** 클릭
3. **GitHub** 로 로그인 (GitHub 계정 연동 필수)

### 3-2. 새 Web Service 생성

1. 대시보드에서 **New +** 버튼 클릭
2. **Web Service** 선택
3. **Connect a repository** 화면에서:
   - 처음이면 **Configure account** 클릭 → GitHub 권한 허용
   - `robotmate-server` 레포지토리 옆 **Connect** 클릭

### 3-3. 서비스 설정

아래 항목들을 입력:

| 항목 | 값 |
|------|-----|
| **Name** | `robotmate-server` |
| **Region** | `Singapore` (한국에서 가장 가까운 무료 리전) |
| **Branch** | `main` |
| **Runtime** | `Java` |
| **Build Command** | `./mvnw clean package -DskipTests` |
| **Start Command** | `java -jar target/server-0.0.1-SNAPSHOT.jar` |
| **Instance Type** | `Free` |

> **Start Command JAR 이름 확인:**
> `pom.xml` 의 `<artifactId>server</artifactId>` + `<version>0.0.1-SNAPSHOT</version>` 기준.
> 다르면 `target/` 안의 실제 JAR 파일명으로 변경.

### 3-4. 환경변수 설정

같은 페이지 아래 **Environment Variables** 섹션에서 아래 항목 추가:

| Key | Value |
|-----|-------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DB_URL` | Supabase에서 복사한 JDBC URL |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | Supabase DB 비밀번호 |
| `JWT_SECRET` | 32자 이상 랜덤 문자열 (예: `my-super-secret-jwt-key-for-robotmate-2024`) |
| `GOOGLE_CLIENT_ID` | Google OAuth 클라이언트 ID |

> **JWT_SECRET 생성 방법 (터미널):**
> ```bash
> openssl rand -base64 48
> ```

### 3-5. 배포 시작

**Create Web Service** 클릭 → 자동으로 빌드 시작

- 첫 빌드는 **3~5분** 소요
- **Logs** 탭에서 빌드 과정 실시간 확인 가능
- 빌드 성공 시 상태가 `Live` 로 변경됨

### 3-6. 배포 확인

Render가 부여한 URL로 헬스체크:

```
GET https://robotmate-server.onrender.com/api/health
```

응답이 오면 서버 정상 동작.

Swagger UI 확인:
```
https://robotmate-server.onrender.com/swagger-ui/index.html
```

---

## 4단계. Cloudflare — DNS 설정

> `robotmate.kr` 도메인이 이미 Cloudflare에서 관리 중인 경우

### 4-1. Render 배포 URL 확인

Render 대시보드 → 서비스 선택 → 상단에 표시된 URL 확인:
```
robotmate-server.onrender.com
```

### 4-2. Cloudflare DNS 레코드 추가

1. [https://dash.cloudflare.com](https://dash.cloudflare.com) 접속
2. 좌측 도메인 목록에서 `robotmate.kr` 클릭
3. 좌측 메뉴 **DNS** → **Records** 클릭
4. **Add record** 클릭 후 아래 입력:

| 항목 | 값 |
|------|-----|
| **Type** | `CNAME` |
| **Name** | `api` |
| **Target** | `robotmate-server.onrender.com` |
| **Proxy status** | **DNS only** (회색 구름) ← 반드시 DNS only로 설정 |
| **TTL** | Auto |

5. **Save** 클릭

> **Proxy status를 DNS only로 설정하는 이유:**
> Cloudflare 프록시(주황 구름)를 켜면 SSL 인증서 충돌이 발생할 수 있습니다.
> Render가 자체적으로 SSL을 처리하므로 DNS only로 설정합니다.

---

## 5단계. Render — 커스텀 도메인 등록

### 5-1. 커스텀 도메인 추가

1. Render 대시보드 → `robotmate-server` 서비스 클릭
2. 좌측 메뉴 **Settings** 클릭
3. **Custom Domains** 섹션 찾기
4. **Add Custom Domain** 클릭
5. `api.robotmate.kr` 입력 후 **Save** 클릭

### 5-2. SSL 인증서 발급 대기

- Render가 자동으로 Let's Encrypt SSL 인증서 발급
- DNS 전파 + 인증서 발급까지 **최대 10분** 소요
- 상태가 `Verified` 로 바뀌면 완료

### 5-3. 최종 확인

```
GET https://api.robotmate.kr/api/health
GET https://api.robotmate.kr/swagger-ui/index.html
```

---

## 6단계. 이후 배포 방법 (CI/CD)

한 번 설정 후에는 코드 수정 → push 만 하면 자동 배포됩니다.

```bash
# 코드 수정 후
git add .
git commit -m "feat: 새 기능 추가"
git push origin main
# → Render가 자동으로 빌드 & 배포
```

Render 대시보드 → **Deploys** 탭에서 배포 이력과 로그 확인 가능.

---

## 환경변수 전체 목록

| Key | 설명 | 예시 |
|-----|------|------|
| `SPRING_PROFILES_ACTIVE` | 활성 프로필 | `prod` |
| `DB_URL` | Supabase JDBC URL | `jdbc:postgresql://db.xxx.supabase.co:5432/postgres` |
| `DB_USERNAME` | DB 유저명 | `postgres` |
| `DB_PASSWORD` | DB 비밀번호 | `your-password` |
| `JWT_SECRET` | JWT 서명 키 (32자+) | `random-string` |
| `GOOGLE_CLIENT_ID` | Google OAuth 클라이언트 ID | `xxx.apps.googleusercontent.com` |

---

## 자주 발생하는 문제

### 빌드 실패 — JAR 파일 못 찾음
```
Error: Unable to access jarfile target/server-0.0.1-SNAPSHOT.jar
```
→ `pom.xml` 의 `<artifactId>` 와 `<version>` 확인 후 Start Command 수정

### 서버 시작 실패 — DB 연결 오류
```
Connection refused: db.xxx.supabase.co:5432
```
→ Render 환경변수의 `DB_URL`, `DB_PASSWORD` 값 확인
→ Supabase 대시보드 **Settings → Database → Connection Pooling** 에서 URL 재확인

### 도메인 연결 안 됨
→ Cloudflare DNS 레코드 **Proxy status 가 DNS only(회색)** 인지 확인
→ DNS 전파 최대 24시간 소요될 수 있음 (보통 10분 내)

### Swagger UI 접근 안 됨
→ `SecurityConfig` 에서 `/swagger-ui/**`, `/v3/api-docs/**` permitAll 확인
