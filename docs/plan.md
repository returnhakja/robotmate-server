# RobotMate 백엔드 구현 플랜

## 진행 상태 범례
- ⬜ 미시작
- 🔄 진행 중
- ✅ 완료

---

## Phase 1 — 공통 인프라 + RobotModel + DataLoader
> 다른 기능의 기반. 먼저 해야 이후 개발 시 테스트 가능.

| 상태 | 파일 | 역할 |
|------|------|------|
| ✅ | `common/ApiResponse.java` | 공통 응답 래퍼 `{success, data, message}` |
| ✅ | `common/GlobalExceptionHandler.java` | 404 / 400 / 500 예외 처리 |
| ✅ | `common/SecurityUtil.java` | SecurityContext에서 현재 userId 추출 헬퍼 |
| ✅ | `common/exception/NotFoundException.java` | 404 커스텀 예외 |
| ✅ | `common/exception/ForbiddenException.java` | 403 커스텀 예외 |
| ✅ | `robot/RobotModelRepository.java` | `findBySlug()` |
| ✅ | `robot/dto/RobotModelResponse.java` | 응답 DTO |
| ✅ | `robot/RobotModelService.java` | |
| ✅ | `robot/RobotModelController.java` | `GET /api/models`, `GET /api/models/{slug}` |
| ✅ | `seed/DataLoader.java` | RobotModel 6개 시드 (멱등성 보장) |

---

## Phase 2 — User 프로필 API

| 상태 | 파일 | 역할 |
|------|------|------|
| ⬜ | `user/UserRobotRepository.java` | |
| ⬜ | `user/dto/UserProfileResponse.java` | id, email, nickname, profileImage, role, ownedRobots |
| ⬜ | `user/dto/UpdateProfileRequest.java` | nickname, profileImage |
| ⬜ | `user/dto/UserRobotResponse.java` | id, robotModel, nickname, startDate |
| ⬜ | `user/dto/AddUserRobotRequest.java` | robotModelId, nickname, startDate |
| ⬜ | `user/UserService.java` | |
| ⬜ | `user/UserController.java` | `GET /api/users/me`, `PUT /api/users/me` |

---

## Phase 3 — Post CRUD + 복합 필터

| 상태 | 파일 | 역할 |
|------|------|------|
| ✅ | `post/PostRepository.java` | JpaSpecificationExecutor 포함 |
| ✅ | `post/PostSpecification.java` | type / model / tag / sort 복합 필터 |
| ✅ | `post/dto/PostSummaryResponse.java` | 목록용 (likeCount, commentCount 포함) |
| ✅ | `post/dto/PostDetailResponse.java` | 상세용 (isLiked, isBookmarked 포함) |
| ✅ | `post/dto/CreatePostRequest.java` | type, title, content, robotModelId, tags, images, 판매필드 |
| ✅ | `post/dto/UpdatePostRequest.java` | |
| ✅ | `post/dto/AuthorInfo.java` | 작성자 정보 (id, nickname, profileImage) |
| ✅ | `post/PostService.java` | |
| ✅ | `post/PostController.java` | `GET /api/posts`, `GET /api/posts/{id}`, `POST`, `PUT`, `DELETE` |
| ✅ | `post/LikeRepository.java` | countByPostId, countsByPostIds |
| ✅ | `post/BookmarkRepository.java` | existsByUserIdAndPostId |
| ⬜ | `seed/DataLoader.java` 업데이트 | Post 8개, News 5개 시드 추가 |

---

## Phase 4 — Comment + Like + Bookmark

| 상태 | 파일 | 역할 |
|------|------|------|
| ⬜ | `comment/CommentRepository.java` | `findByPostIdOrderByCreatedAtAsc()` |
| ⬜ | `comment/dto/CommentResponse.java` | id, content, author, createdAt |
| ⬜ | `comment/dto/CreateCommentRequest.java` | content |
| ⬜ | `comment/CommentService.java` | 본인 댓글만 삭제 권한 체크 |
| ⬜ | `comment/CommentController.java` | `GET/POST /api/posts/{id}/comments`, `DELETE /api/comments/{id}` |
| ⬜ | `post/LikeRepository.java` | `findByUserIdAndPostId()`, `countByPostId()` |
| ⬜ | `post/BookmarkRepository.java` | `findByUserIdAndPostId()` |
| ⬜ | `PostService` 메서드 추가 | 좋아요 / 북마크 토글 (`{liked, likeCount}` 반환) |

---

## Phase 5 — News + Market + 이미지 업로드

| 상태 | 파일 | 역할 |
|------|------|------|
| ⬜ | `news/NewsRepository.java` | `findAllByOrderByPublishedAtDesc()` |
| ⬜ | `news/dto/NewsResponse.java` | |
| ⬜ | `news/dto/CreateNewsRequest.java` | |
| ⬜ | `news/NewsService.java` | |
| ⬜ | `news/NewsController.java` | `GET /api/news`, `POST /api/news` (ADMIN) |
| ⬜ | `upload/R2Config.java` | S3Client Bean (R2 endpoint 설정) |
| ⬜ | `upload/UploadService.java` | R2 업로드 처리, UUID 파일명 생성 |
| ⬜ | `upload/UploadController.java` | `POST /api/upload/image` |
| ⬜ | `config/SecurityConfig.java` 수정 | ADMIN 전용 엔드포인트 `hasRole("ADMIN")` 추가 |

---

## 완료된 작업

### 인증 시스템 ✅
- `auth/JwtProvider.java` — JWT 생성 / 검증
- `auth/JwtAuthenticationFilter.java` — Bearer 토큰 필터
- `auth/CustomUserDetailsService.java` — userId 기반 유저 로드
- `auth/AuthService.java` — Google idToken 검증 → 유저 생성/조회 → JWT 발급
- `auth/AuthController.java` — `POST /api/auth/google`
- `auth/dto/` — GoogleLoginRequest, AuthResponse, UserResponse, GoogleTokenInfo
- `user/UserRepository.java` — findByEmail, findByGoogleId, existsByNickname
- `config/SecurityConfig.java` — JWT 필터 등록, 경로별 인증 규칙
- `config/AppConfig.java` — RestTemplate Bean
