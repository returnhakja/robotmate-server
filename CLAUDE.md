# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## 프로젝트 개요

반려 로봇 커뮤니티 플랫폼 **RobotMate** 백엔드 서버.
- 도메인: `robotmate.kr`
- 스택: Spring Boot 3.4.3 / Java 17 / Maven / PostgreSQL / Spring Security + JWT

---

## 빌드 및 실행 명령어

```bash
# 빌드
./mvnw clean package -DskipTests

# 로컬 실행 (local 프로필 — PostgreSQL 사용)
./mvnw spring-boot:run

# 테스트 실행
./mvnw test

# 단일 테스트 실행
./mvnw test -Dtest=ClassName#methodName
```

서버 기본 포트: `8080`
Health check: `GET http://localhost:8080/api/health`

---

## 프로젝트 구조

```
src/main/java/kr/robotmate/server/
├── config/         — SecurityConfig (JWT 필터 등록), JpaConfig, AppConfig (RestTemplate Bean)
├── auth/           — JwtProvider, JwtAuthenticationFilter, CustomUserDetailsService
│   ├── dto/        — GoogleLoginRequest, AuthResponse, UserResponse, GoogleTokenInfo
│   ├── AuthService.java      — Google idToken 검증 → 유저 생성/조회 → JWT 발급
│   └── AuthController.java   — POST /api/auth/google
├── user/           — User, UserRobot 엔티티 + UserRepository
├── robot/          — RobotModel 엔티티
├── post/           — Post, Like, Bookmark 엔티티 + PostType, SaleType enum
├── comment/        — Comment 엔티티
├── news/           — News 엔티티
└── common/         — BaseEntity (createdAt, updatedAt — JPA Auditing)
```

**현재 구현된 계층:**
- Entity: 전체 완성
- Repository: UserRepository만 존재
- Service/Controller: AuthService/AuthController만 존재
- 나머지 도메인(Post, RobotModel, News 등): Repository/Service/Controller 미구현

---

## 설정 파일 구조

| 파일 | 용도 |
|------|------|
| `application.yml` | 공통 설정 (profiles.active=local, JWT, R2, CORS) |
| `application-local.yml` | 로컬 개발용 (PostgreSQL localhost, Google client-id) |

환경변수 대신 local 프로필에서 직접 값 사용 중.
**`application-local.yml`은 `.gitignore`에 추가 필요** (DB 비밀번호, Google Client ID 포함).

---

## 인증 아키텍처

**Google OAuth2 (idToken 방식):**
1. 프론트엔드가 Google Sign-In으로 `idToken` 취득
2. `POST /api/auth/google { idToken }` 요청
3. 백엔드가 `https://oauth2.googleapis.com/tokeninfo?id_token=` 으로 검증
4. User 없으면 자동 생성(구글 sub, email, nickname, profileImage 저장), 있으면 googleId 연결
5. JWT 발급 후 반환 (`{ accessToken, tokenType, user }`)

**JWT 필터 (`JwtAuthenticationFilter`):**
- `Authorization: Bearer {token}` 헤더에서 토큰 추출
- 유효하면 `SecurityContextHolder`에 인증 정보 세팅 (userId = JWT subject)

**현재 Security 규칙:**
- `permitAll`: `/api/auth/**`, `/api/health`, GET `/api/posts/**`, `/api/models/**`, `/api/news/**`, `/api/market/**`
- `authenticated`: 그 외 모든 요청

---

## 핵심 엔티티 관계

- `User` ↔ `UserRobot` (1:N) — 보유 로봇
- `User` ↔ `Post` (1:N) — 작성 게시글
- `Post` ↔ `Comment`, `Like`, `Bookmark` (1:N)
- `Post` → `RobotModel` (N:1, nullable)
- `Post.type` = `SHOW | REVIEW | QUESTION | CONCEPT | SALE`
- `Post.saleType` = `SELL | FREE` (type=SALE일 때만 사용)

모든 엔티티 ID: `@GeneratedValue(strategy = GenerationType.UUID)` (String 타입)

---

## 다음 구현 우선순위

1. `RobotModelRepository/Service/Controller` — `GET /api/models`, `GET /api/models/{slug}`
2. `PostRepository/Service/Controller` — `GET /api/posts` (필터/페이징), `POST /api/posts`
3. `CommentService/Controller`, `LikeService`, `BookmarkService`
4. `NewsRepository/Service/Controller`
5. `UserController` — `GET /api/users/me`, `PUT /api/users/me`
6. `DataLoader` — 시드 데이터 (RobotModel 6개, Post 8개, News 5개)
7. Cloudflare R2 이미지 업로드 — `POST /api/upload/image`

---

## 주요 의존성

- `jjwt` 0.12.6 — JWT 생성/검증
- `spring-boot-starter-security` — Spring Security
- `software.amazon.awssdk:s3` 2.25.27 — Cloudflare R2 연동 (미구현)
- `postgresql` — 런타임 드라이버
- `lombok` — 보일러플레이트 제거

---

## API 전체 목록 (설계 기준)

```
POST   /api/auth/google          ✅ 구현됨

GET    /api/models               ❌
GET    /api/models/{slug}        ❌

GET    /api/posts                ❌
GET    /api/posts/{id}           ❌
POST   /api/posts                ❌
PUT    /api/posts/{id}           ❌
DELETE /api/posts/{id}           ❌
POST   /api/posts/{id}/like      ❌
POST   /api/posts/{id}/bookmark  ❌

GET    /api/posts/{id}/comments  ❌
POST   /api/posts/{id}/comments  ❌
DELETE /api/comments/{id}        ❌

GET    /api/users/me             ❌
PUT    /api/users/me             ❌

GET    /api/news                 ❌
POST   /api/news                 ❌ (ADMIN)

GET    /api/market               ❌ (= posts?type=SALE 필터)

POST   /api/upload/image         ❌
```


## 작업 기록 규칙

작업을 완료할 때마다 `docs/work-log.md`에 기록을 추가한다.
- 날짜 (`## YYYY-MM-DD`) 헤더 아래에 작성
- 같은 날짜 항목이 있으면 새 항목을 추가하지 않고 기존 항목에 이어 씀
- 변경한 파일명과 무엇을 바꿨는지 간결하게 기록