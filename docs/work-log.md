# Work Log

## 2026-04-02

### 뉴스 slug 잔재 제거

- `NewsController.java`: 상세 조회 Swagger 설명에서 "slug로 발행된" → "ID로 발행된" 수정
- `AdminController.java`: 뉴스 생성/수정 Swagger 주석에서 "slug 중복", "slug 변경도 가능합니다" 문구 제거
- `CLAUDE.md`: API 목록 `{slug}` → `{id}` 전체 수정 (뉴스 공개/어드민 모두)

### 내 반려로봇 기능 구현

- `UserRobot.java`: `profileImage`, `isPublic(default=true)` 필드 추가
- `Post.java`: `@ManyToOne UserRobot userRobot` 필드 추가 (nullable)
- `UserRobotRepository.java`: `findByUserId`, `findByUserIdAndIsPublicTrue`, `findByIdAndUserId` 메서드 추가
- `PostRepository.java`: `findByUserRobotIdOrderByCreatedAtDesc` 메서드 추가
- `UserRobotRequest.java`: 등록/수정 공용 DTO 신규 생성 (nickname, robotModelId, startDate, profileImage, isPublic)
- `UserRobotResponse.java`: 조회용 DTO 신규 생성 (robotModel 정보 포함)
- `UserRobotService.java`: 내 로봇 목록/등록/수정/삭제, 로봇별 게시글, 타인 공개 로봇 조회 구현
- `UserRobotController.java`: `GET/POST /api/users/me/robots`, `PUT/DELETE /api/users/me/robots/{id}`, `GET /api/users/me/robots/{id}/posts`, `GET /api/users/{userId}/robots` 엔드포인트 추가
- `CreatePostRequest.java`, `UpdatePostRequest.java`: `userRobotId` 필드 추가
- `UserRobotInfo.java`: Post 응답 내 반려로봇 정보용 내부 DTO 신규 생성
- `PostDetailResponse.java`, `PostSummaryResponse.java`: `userRobot` 필드 추가
- `PostService.java`: `createPost/updatePost`에 `userRobotId` 소유권 검증 및 연결 처리 추가, `UserRobotRepository` 의존성 추가
- `SecurityConfig.java`: `GET /api/users/*/robots` permitAll 추가



### 팔로우/언팔로우 및 피드 팔로잉 필터 구현

- `Follow.java`: `follows` 테이블 엔티티 신규 생성 (follower → following)
- `FollowRepository.java`: 팔로우 조회/존재 여부/팔로워 수/팔로잉 ID 목록 쿼리
- `FollowResponse.java`: `{ following, followerCount }` 응답 DTO
- `UserFollowController.java`: `POST /api/users/{id}/follow` 토글 엔드포인트
- `UserService.java`: `toggleFollow` 메서드 추가, `FollowRepository` 의존성 추가
- `PostSpecification.java`: `fromFollowing(List<String>)` 스펙 추가
- `PostService.java`: `getPosts`에 `feed` 파라미터 추가, `feed=following`이면 팔로잉 유저 게시글만 필터링
- `PostController.java`: `GET /api/posts?feed=following` 파라미터 추가



### 마켓 `tradeLocation` 필드 추가 및 판매완료 처리 API 구현

- `Post.java`: `tradeLocation` 필드 추가
- `CreatePostRequest.java`, `UpdatePostRequest.java`: `tradeLocation` 필드 추가
- `PostDetailResponse.java`: `tradeLocation` 응답에 포함, `PostService.createPost/updatePost`에 매핑 추가
- `PostService.java`: `markSold` 메서드 추가 (isSold 토글)
- `PostController.java`: `PATCH /api/posts/{id}/sold` 엔드포인트 추가

### 댓글 수정 API 구현

- `CommentService.java`: `updateComment` 메서드 추가 (본인 확인 후 content 수정)
- `CommentController.java`: `PUT /api/comments/{commentId}` 엔드포인트 추가

### 유저 `bio` 한 줄 소개 필드 추가

- `User.java`: `bio` 컬럼 추가 (`@Column(length = 100)`)
- `UpdateUserRequest.java`: `bio` 필드 추가 (`@Size(max = 100)` 검증)
- `UserResponse.java`: `bio` 필드 포함하여 응답에 노출
- `UserService.java`: `updateMe`에서 `bio` 업데이트 처리 추가

## 2026-03-31

### API 문서 전면 업데이트

- `docs/api-posts.md`: 공개 API 전체 재작성 — 인증/모델/게시글/댓글/마이페이지/뉴스/이미지업로드 포함, 페이지네이션 1-based 반영, PageResponse 구조(`list` 키) 반영
- `docs/api-admin.md`: 어드민 API 전면 재작성 — 뉴스 CRUD 섹션 추가, 회원 강제탈퇴 설명 정정(익명화 → PENDING_DELETION), 회원/게시글 목록 Spring Page 구조(0-based number) vs 뉴스 PageResponse 구조 차이 명시

### 뉴스 slug 제거 및 어드민 뉴스 robotModel 필터 추가

- `news/News.java`: slug 컬럼 제거
- `news/NewsRepository.java`: existsBySlug / findBySlug 제거
- `news/dto/NewsRequest.java`: slug 필드 제거
- `news/dto/NewsSummaryResponse.java`, `NewsDetailResponse.java`: slug 필드 제거
- `news/NewsService.java`: slug 관련 메서드 전부 제거, `getPublishedBySlug` → `getPublishedById`, `getAllNews`에 robotModelSlug 파라미터 추가
- `news/NewsController.java`: `GET /api/news/{slug}` → `GET /api/news/{id}`
- `admin/AdminService.java`: `getNews()`에 robotModelSlug 파라미터 추가
- `admin/AdminController.java`: `GET /api/admin/news`에 robotModelSlug 필터 파라미터 추가

### 어드민 뉴스 API slug → id 변경 및 페이지네이션 응답 1-based 수정

- `admin/AdminController.java`: `GET|PUT|DELETE /api/admin/news/{slug}`, `PATCH /api/admin/news/{slug}/pin` → `{id}` 기반으로 변경, 목록 반환 타입 `Page<>` → `PageResponse<>`
- `admin/AdminService.java`: `getNewsBySlug` → `getNewsById`, `updateNews/deleteNews/pinNews` 파라미터 slug → id
- `news/NewsService.java`: `getById`, `updateById`, `deleteById`, `setPinById`, `findById` 추가
- `news/NewsController.java`: 목록 반환 타입 `Page<>` → `PageResponse<>` (페이지 번호 1-based 응답)
- `common/PageResponse.java`: `page.getNumber()` → `page.getNumber() + 1` (응답 page 필드 0-based → 1-based)

### 뉴스/소식 관리 API 구현

**엔티티 확장**
- `news/NewsType.java`: NOTICE / NEWS / ARTICLE enum 신규 추가
- `news/News.java`: slug(unique), type(NewsType), content(TEXT), thumbnailUrl, isPinned, robotModel(FK), publishedAt 필드 추가

**레포지토리/스펙**
- `news/NewsRepository.java`: JpaSpecificationExecutor 추가, existsBySlug / findBySlug 쿼리
- `news/NewsSpecification.java`: isPublished / hasType / hasRobotModel / isPinned / hasKeyword / isPublishedFilter 스펙

**DTO**
- `news/dto/NewsRequest.java`: 생성/수정 공용 요청 DTO (slug, type, title, summary, content, thumbnailUrl, isPinned, robotModelSlug, sourceUrl, sourceName, publishedAt)
- `news/dto/RobotModelRef.java`: 연결 기종 경량 참조 (slug, name, emoji)
- `news/dto/NewsSummaryResponse.java`: 목록용 응답
- `news/dto/NewsDetailResponse.java`: 상세 응답 (content 포함)
- `news/dto/PinRequest.java`: 고정 토글 요청

**서비스/컨트롤러**
- `news/NewsService.java`: Public(발행 필터) / Admin(전체) CRUD + 고정 토글, summary 자동 생성(HTML 태그 제거 후 150자)
- `news/NewsController.java`: GET /api/news, GET /api/news/{slug} (공개 API)
- `admin/AdminController.java`: GET|POST|PUT|DELETE /api/admin/news, PATCH /api/admin/news/{slug}/pin
- `admin/AdminService.java`: 뉴스 관리 메서드 위임 추가, totalNews 실제 카운트 연결

## 2026-03-30

### 회원 탈퇴 로직 변경 (익명화 → 상태값 보존)

- `user/UserStatus.java`: `PENDING_DELETION` 상태 추가 (ACTIVE, SUSPENDED, PENDING_DELETION, DELETED)
- `admin/AdminService.java`: `deleteUser()` — 익명화 제거, `status = PENDING_DELETION` + 리프레시 토큰 무효화만 수행
- `auth/AuthService.java`: 로그인/토큰갱신 시 `PENDING_DELETION` 유저 차단 (SuspendedException 발생)
- `user/UserService.java`: `withdrawMe()` 추가 — `status = PENDING_DELETION` + 리프레시 토큰 무효화
- `user/UserController.java`: `DELETE /api/users/me` 자체 탈퇴 엔드포인트 추가
- `admin/AdminController.java`: Swagger 설명 업데이트

## 2026-03-23

### Phase 3 어드민 API 구현

**엔티티/enum 추가**
- `user/UserStatus.java`: ACTIVE, SUSPENDED, DELETED enum 추가
- `post/PostVisibility.java`: PUBLIC, PRIVATE enum 추가
- `user/User.java`: status(UserStatus), suspendReason 필드 추가
- `post/Post.java`: visibility(PostVisibility) 필드 추가 (기본값 PUBLIC)
- `robot/RobotModel.java`: emoji 필드 추가

**레포지토리**
- `user/UserRepository.java`: JpaSpecificationExecutor 추가, countByCreatedAtAfter 쿼리 추가
- `post/PostRepository.java`: countByType, countByCreatedAtAfter, countByRobotModelId 추가
- `user/UserRobotRepository.java`: 신규 — countByRobotModelId
- `common/exception/SuspendedException.java`: suspendReason 포함한 정지 유저 예외

**JWT / 인증 변경**
- `auth/JwtProvider.java`: generateToken에 role 클레임 추가, getRole() 메서드 추가
- `auth/JwtAuthenticationFilter.java`: DB 조회 제거, JWT 클레임에서 role 직접 추출
- `auth/AuthService.java`: SUSPENDED 유저 로그인/refresh 차단(SuspendedException), generateToken에 role 전달

**공통**
- `common/ApiResponse.java`: code 필드 추가, suspended() 팩토리 메서드 추가
- `common/GlobalExceptionHandler.java`: SuspendedException → 403 + USER_SUSPENDED 코드 처리
- `config/SecurityConfig.java`: /api/admin/** ADMIN 롤 체크 추가, CORS PATCH 메서드 추가

**어드민**
- `admin/UserSpecification.java`: keyword(email/nickname), role, status 필터
- `post/PostSpecification.java`: keyword(title), authorId, visibility 필터 추가
- `admin/dto/`: AdminStatsResponse, AdminUserResponse, AdminPostSummaryResponse, ChangeRoleRequest, ChangeStatusRequest, AdminModelRequest
- `admin/AdminService.java`: 통계, 회원 CRUD, 게시글 CRUD, 로봇모델 CRUD (삭제 시 연결 데이터 체크 409)
- `admin/AdminController.java`: GET /api/admin/stats, /users, /posts + PATCH /users/{id}/role,status + DELETE + POST/PUT/DELETE /models/{slug}

**기타**
- `robot/dto/RobotModelResponse.java`: emoji 필드 추가

## 2026-03-20

### 버그 수정: 좋아요 있는 댓글 삭제 시 500 에러
- `comment/Comment.java`: `CommentLike`에 대한 `@OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)` 추가 — 댓글(및 대댓글) 삭제 시 comment_likes FK 제약 위반 해소

### GET /api/users/me/comments — likeCount 필드 추가
- `comment/dto/MyCommentResponse.java`: `likeCount` 필드 추가, `from(Comment, long)` 시그니처 변경
- `user/UserService.java`: `CommentLikeRepository` 주입, `getMyComments`에서 댓글 ID 배치 조회 후 likeCount 매핑

## 2026-03-18

### Refresh Token 도입 (보안 강화)
- `application.yml` 수정: `expiration` 24시간 → 30분, `refresh-expiration` 기존 설정 활성화
- `auth/RefreshToken.java` 신규 생성: refresh token 엔티티 (`refresh_tokens` 테이블, UUID PK, userId/token/expiresAt)
- `auth/RefreshTokenRepository.java` 신규 생성: `findByToken`, `deleteByToken`, `deleteByUserId`
- `auth/JwtProvider.java` 수정: `refreshExpiration` 주입, `generateRefreshTokenValue()` (UUID), `getRefreshTokenExpiresAt()` 추가
- `auth/dto/AuthResponse.java` 수정: `refreshToken` 필드 추가
- `auth/AuthService.java` 수정: `googleLogin` 시 refresh token 발급+저장, `refresh()` (Rotation 방식), `logout()` 추가
- `auth/AuthController.java` 수정: `POST /api/auth/refresh`, `POST /api/auth/logout` 엔드포인트 추가

## 2026-03-17

### 댓글 좋아요 기능 추가
- `comment/CommentLike.java` 신규 생성: 댓글 좋아요 엔티티 (`comment_likes` 테이블, user+comment unique 제약)
- `comment/CommentLikeRepository.java` 신규 생성: `findByUserIdAndCommentId`, `countByCommentId`, `countsByCommentIds`, `findLikedCommentIds`
- `comment/dto/CommentLikeResponse.java` 신규 생성: `liked`, `likeCount`
- `comment/dto/CommentResponse.java` 수정: `likeCount`, `liked` 필드 추가; `from()` 시그니처 변경
- `comment/CommentService.java` 수정: `getComments`에 좋아요 수/여부 포함 (N+1 없이 IN 쿼리), `toggleCommentLike` 추가
- `comment/CommentController.java` 수정: `POST /api/comments/{commentId}/like` 추가, `getComments`에 userId 전달

### 내 댓글 목록 API 추가
- `comment/dto/MyCommentResponse.java` 신규 생성: `id`, `content`, `createdAt`, `postId`, `postTitle`, `isReply` 필드
- `comment/CommentRepository.java` 수정: `findByAuthorIdOrderByCreatedAtDesc` 추가 (post JOIN FETCH, 페이징)
- `user/UserService.java` 수정: `getMyComments` 추가
- `user/UserController.java` 수정: `GET /api/users/me/comments` 추가

### 마이페이지 프로필 수정 검증 및 닉네임 중복 처리 추가
- `user/dto/UpdateUserRequest.java` 수정: `nickname`에 `@Size(min=2, max=20)` 검증 추가
- `user/UserRepository.java` 수정: `existsByNicknameAndIdNot` 메서드 추가 (자기 자신 닉네임 허용)
- `user/UserService.java` 수정: 닉네임 변경 시 중복 사전 검사 → 중복이면 `ConflictException` (409)
- `common/exception/ConflictException.java` 신규 생성
- `common/GlobalExceptionHandler.java` 수정: `ConflictException` 핸들러 추가 (409)
- `user/UserController.java` 수정: `updateMe`에 `@Valid` 추가
- `CLAUDE.md` 수정: UserController ✅ 표시, 우선순위 업데이트

### Railway OOM 해결 및 Cloudflare R2 이미지 업로드 구현
- `railway.toml` 신규 생성: JVM 메모리 제한 (`-Xms128m -Xmx300m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC`), healthcheck 설정
- `application-prod.yml` 수정: HikariCP `maximum-pool-size: 3`, `minimum-idle: 1` 추가 (메모리 절약)
- `application.yml` 수정: multipart `max-file-size: 20MB`, `max-request-size: 60MB` 변경
- `config/R2Config.java` 신규 생성: Cloudflare R2용 S3Client 빈 (endpointOverride 사용)
- `upload/UploadService.java` 신규 생성: R2 파일 업로드 로직, 20MB 초과 시 예외
- `upload/UploadController.java` 신규 생성: `POST /api/upload/post`, `POST /api/upload/profile`

## 2026-03-16

### 토큰 유효성 검증 API 추가
- `auth/AuthController.java` 수정: `GET /api/auth/verify` 추가 — JWT 유효 시 유저 정보 반환, 무효 시 401
- `config/SecurityConfig.java` 수정: `/api/auth/**` permitAll → `/api/auth/google`만 permitAll로 변경 (verify는 인증 필요)

## 2026-03-15

### 마이페이지 API 구현
- `post/PostRepository.java` 수정: `findByAuthorIdOrderByCreatedAtDesc` 추가
- `post/BookmarkRepository.java` 수정: `findByUserIdWithPost` 추가 (JOIN FETCH + count 쿼리 분리)
- `user/dto/UpdateUserRequest.java` 신규 생성: `nickname`, `profileImage` 필드
- `user/UserService.java` 신규 생성: `getMe`, `updateMe`, `getMyPosts`, `getMyBookmarks`
- `user/UserController.java` 신규 생성: `GET /api/users/me`, `PUT /api/users/me`, `GET /api/users/me/posts`, `GET /api/users/me/bookmarks`

### CORS 허용 Origin 추가
- `src/main/resources/application.yml` 수정: `cors.allowed-origins`에 `https://robotmate.kr`, `https://www.robotmate.kr` 추가 (프로덕션 환경 CORS 차단 이슈 해결)

## 2026-03-13

### 대댓글 기능 추가
- `comment/Comment.java` 수정: `parent` (ManyToOne, nullable), `replies` (OneToMany cascade) 자기참조 관계 추가
- `comment/CommentRepository.java` 수정: `findByPostIdAndParentIsNullOrderByCreatedAtAsc` 추가 (최상위 댓글만 조회)
- `comment/dto/CommentResponse.java` 수정: `replies` 필드 추가, `from()`에서 대댓글 목록 포함
- `comment/CommentService.java` 수정: `createReply` 추가 (1단계 이상 대댓글 불가 검증 포함)
- `comment/CommentController.java` 수정: `POST /api/comments/{commentId}/replies` 추가

### 댓글 기능 구현
- `comment/CommentRepository.java` 신규 생성: `findByPostIdOrderByCreatedAtAsc`, `countByPostId`, `countsByPostIds`
- `comment/dto/CommentResponse.java` 신규 생성
- `comment/dto/CreateCommentRequest.java` 신규 생성
- `comment/CommentService.java` 신규 생성: `getComments`, `createComment`, `deleteComment`
- `comment/CommentController.java` 신규 생성: `GET /api/posts/{postId}/comments`, `POST /api/posts/{postId}/comments`, `DELETE /api/comments/{commentId}`
- `post/PostService.java` 수정: `getCommentCounts`/`getPost`/`updatePost`에서 `LikeRepository` 대신 `CommentRepository` 사용
- `post/LikeRepository.java` 수정: 잘못 위치해 있던 `commentCountsByPostIds` 쿼리 제거

## 2026-03-11

### API 문서 정비
- `docs/api-posts.md`: 전체 API 명세로 확장 — Auth, RobotModel, Post(좋아요/북마크 포함) 반영
  - 공통 응답/에러 구조 추가
  - 좋아요·북마크 토글 구현 완료 상태로 업데이트 (미구현 목록에서 제거)

### Swagger (springdoc-openapi) 적용
- `pom.xml`: `springdoc-openapi-starter-webmvc-ui 2.8.6` 의존성 추가
- `SecurityConfig.java`: `/swagger-ui/**`, `/v3/api-docs/**`, `/swagger-ui.html` permitAll 추가
- `config/OpenApiConfig.java` 신규 생성: OpenAPI 빈 설정 (JWT Bearer 인증 스킴 포함)
  - Swagger UI: http://localhost:8080/swagger-ui.html
  - OpenAPI JSON: http://localhost:8080/v3/api-docs
