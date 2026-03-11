# RobotMate API 명세

Base URL: `https://api.robotmate.kr`

---

## 공통 응답 구조

### 성공

```json
{
  "success": true,
  "data": { ... }
}
```

> `data`가 없는 경우(예: 삭제) `"data"` 필드 자체가 생략됩니다.

### 실패

```json
{
  "success": false,
  "message": "오류 메시지"
}
```

### 공통 에러 코드

| 상태코드 | 설명 |
|---------|------|
| `400` | 잘못된 요청 (유효성 검사 실패, 잘못된 파라미터) |
| `401` | 인증 필요 (토큰 없음 또는 만료) |
| `403` | 권한 없음 (본인 리소스 아님) |
| `404` | 리소스 없음 |
| `500` | 서버 내부 오류 |

### 인증 헤더

인증이 필요한 API는 모든 요청에 아래 헤더를 포함해야 합니다.

```
Authorization: Bearer {accessToken}
```

---

## 공통 타입

### PostType

| 값 | 설명 |
|----|------|
| `SHOW` | 자랑하기 |
| `REVIEW` | 사용기 |
| `QUESTION` | 질문 |
| `CONCEPT` | 컨셉 |
| `SALE` | 판매/나눔 |

### SaleType (`type=SALE`일 때만 사용)

| 값 | 설명 |
|----|------|
| `SELL` | 판매 |
| `FREE` | 나눔 |

---

## 인증

### Google 로그인

```
POST /api/auth/google
```

**인증:** 불필요

**Request Body**

```json
{
  "idToken": "구글에서 발급받은 idToken"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `idToken` | string | ✅ | Google Sign-In으로 취득한 ID 토큰 |

**Response** `200 OK`

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "user": {
      "id": "uuid",
      "email": "user@example.com",
      "nickname": "닉네임",
      "profileImage": "https://...",
      "role": "USER"
    }
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `accessToken` | string | JWT 액세스 토큰 |
| `tokenType` | string | 항상 `"Bearer"` |
| `user.id` | string (UUID) | 유저 고유 ID |
| `user.email` | string | 이메일 |
| `user.nickname` | string | 닉네임 |
| `user.profileImage` | string | 프로필 이미지 URL |
| `user.role` | string | 권한 (`USER` \| `ADMIN`) |

> 신규 유저는 자동으로 계정이 생성됩니다.

**Error**

| 상태코드 | 설명 |
|---------|------|
| `400` | 유효하지 않은 idToken |
| `500` | Google 토큰 검증 실패 |

---

## 로봇 모델

### 전체 목록 조회

```
GET /api/models
```

**인증:** 불필요

**Response** `200 OK`

```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "slug": "unitree-go2",
      "name": "Unitree Go2",
      "maker": "Unitree",
      "price": "1,600,000원",
      "description": "4족 보행 로봇",
      "imageUrl": "https://...",
      "officialSite": "https://www.unitree.com/go2",
      "keywords": ["4족보행", "야외용"]
    }
  ]
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | string (UUID) | 모델 고유 ID |
| `slug` | string | URL용 식별자 (예: `unitree-go2`) |
| `name` | string | 모델명 |
| `maker` | string | 제조사 |
| `price` | string | 가격 (표시용 문자열) |
| `description` | string | 설명 |
| `imageUrl` | string | 대표 이미지 URL |
| `officialSite` | string | 공식 사이트 URL |
| `keywords` | string[] | 검색 키워드 |

---

### 단일 모델 조회

```
GET /api/models/{slug}
```

**인증:** 불필요

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `slug` | string | 로봇 모델 slug (예: `unitree-go2`) |

**Response** `200 OK` — 목록 조회의 단일 객체와 동일한 구조

**Error**

| 상태코드 | 설명 |
|---------|------|
| `404` | 존재하지 않는 모델 |

---

## 게시글

### 목록 조회

```
GET /api/posts
```

**인증:** 불필요

**Query Parameters**

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| `type` | PostType | ❌ | - | 게시글 타입 필터 (`all` 또는 생략 시 전체) |
| `model` | string | ❌ | - | 로봇 모델 slug 필터 |
| `tag` | string | ❌ | - | 태그 필터 |
| `sort` | string | ❌ | `latest` | 정렬 (`latest` \| `popular`) |
| `page` | number | ❌ | `0` | 페이지 번호 (0-based) |
| `size` | number | ❌ | `12` | 페이지 크기 |

**Response** `200 OK`

```json
{
  "success": true,
  "data": {
    "list": [
      {
        "id": "uuid",
        "type": "SHOW",
        "title": "게시글 제목",
        "content": "본문 앞 150자...",
        "author": {
          "id": "uuid",
          "nickname": "닉네임",
          "profileImage": "https://..."
        },
        "robotModelSlug": "unitree-go2",
        "robotModelName": "Unitree Go2",
        "tags": ["태그1", "태그2"],
        "thumbnail": "https://...",
        "viewCount": 42,
        "likeCount": 10,
        "commentCount": 3,
        "sold": false,
        "createdAt": "2026-03-11T12:00:00"
      }
    ],
    "page": 0,
    "size": 12,
    "totalElements": 100,
    "totalPages": 9
  }
}
```

> **참고:** 목록에서 `content`는 본문 앞 150자만 반환됩니다. 판매 전용 필드(`saleType`, `salePrice` 등)는 포함되지 않습니다.

---

### 상세 조회

```
GET /api/posts/{id}
```

**인증:** 선택 (로그인 시 `liked`, `bookmarked` 필드 정확히 반영)

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `id` | string (UUID) | 게시글 ID |

**Response** `200 OK`

```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "type": "SALE",
    "title": "게시글 제목",
    "content": "본문 전체 내용",
    "author": {
      "id": "uuid",
      "nickname": "닉네임",
      "profileImage": "https://..."
    },
    "robotModelSlug": "unitree-go2",
    "robotModelName": "Unitree Go2",
    "tags": ["태그1", "태그2"],
    "images": ["https://...", "https://..."],
    "viewCount": 42,
    "likeCount": 10,
    "commentCount": 3,
    "liked": false,
    "bookmarked": false,
    "createdAt": "2026-03-11T12:00:00",

    "saleType": "SELL",
    "salePrice": 150000,
    "condition": "거의 새것",
    "usagePeriod": "6개월",
    "tradeMethod": "직거래 또는 택배",
    "contactInfo": "오픈채팅 링크",
    "sold": false
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | string (UUID) | 게시글 ID |
| `type` | PostType | 게시글 타입 |
| `title` | string | 제목 |
| `content` | string | 본문 전체 |
| `author.id` | string (UUID) | 작성자 ID |
| `author.nickname` | string | 작성자 닉네임 |
| `author.profileImage` | string | 작성자 프로필 이미지 URL |
| `robotModelSlug` | string \| null | 연관 로봇 모델 slug |
| `robotModelName` | string \| null | 연관 로봇 모델명 |
| `tags` | string[] | 태그 목록 |
| `images` | string[] | 이미지 URL 목록 |
| `viewCount` | number | 조회수 |
| `likeCount` | number | 좋아요 수 |
| `commentCount` | number | 댓글 수 |
| `liked` | boolean | 현재 유저 좋아요 여부 (비로그인 시 `false`) |
| `bookmarked` | boolean | 현재 유저 북마크 여부 (비로그인 시 `false`) |
| `createdAt` | string (ISO 8601) | 작성일시 |
| `saleType` | SaleType \| null | 판매/나눔 구분 (`type=SALE`만 유효) |
| `salePrice` | number \| null | 판매 가격 |
| `condition` | string \| null | 상품 상태 |
| `usagePeriod` | string \| null | 사용 기간 |
| `tradeMethod` | string \| null | 거래 방법 |
| `contactInfo` | string \| null | 연락처 |
| `sold` | boolean | 판매 완료 여부 |

**Error**

| 상태코드 | 설명 |
|---------|------|
| `404` | 존재하지 않는 게시글 |

---

### 게시글 작성

```
POST /api/posts
```

**인증:** 필요

**Request Body**

```json
{
  "type": "SHOW",
  "title": "게시글 제목",
  "content": "본문 내용",
  "robotModelId": "uuid",
  "tags": ["태그1", "태그2"],
  "images": ["https://...", "https://..."],

  "saleType": "SELL",
  "salePrice": 150000,
  "condition": "거의 새것",
  "usagePeriod": "6개월",
  "tradeMethod": "직거래 또는 택배",
  "contactInfo": "오픈채팅 링크"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `type` | PostType | ✅ | 게시글 타입 |
| `title` | string | ✅ | 제목 |
| `content` | string | ✅ | 본문 |
| `robotModelId` | string (UUID) | ❌ | 로봇 모델 ID |
| `tags` | string[] | ❌ | 태그 목록 |
| `images` | string[] | ❌ | 이미지 URL 목록 (최대 5장) |
| `saleType` | SaleType | ❌ | 판매/나눔 구분 (`type=SALE`일 때 사용) |
| `salePrice` | number | ❌ | 판매 가격 |
| `condition` | string | ❌ | 상품 상태 |
| `usagePeriod` | string | ❌ | 사용 기간 |
| `tradeMethod` | string | ❌ | 거래 방법 |
| `contactInfo` | string | ❌ | 연락처 |

**Response** `201 Created` — 상세 조회 응답과 동일한 구조

**Error**

| 상태코드 | 설명 |
|---------|------|
| `400` | 유효성 검사 실패 (`type`, `title`, `content` 누락 / 이미지 5장 초과) |
| `401` | 인증 필요 |

---

### 게시글 수정

```
PUT /api/posts/{id}
```

**인증:** 필요 (작성자 본인만 가능)

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `id` | string (UUID) | 게시글 ID |

**Request Body** — 수정할 필드만 포함 (Partial Update)

```json
{
  "title": "수정된 제목",
  "content": "수정된 본문",
  "robotModelId": "uuid",
  "tags": ["태그1"],
  "images": ["https://..."],

  "saleType": "FREE",
  "salePrice": null,
  "condition": "사용감 있음",
  "usagePeriod": "1년",
  "tradeMethod": "택배만",
  "contactInfo": "010-xxxx-xxxx",
  "sold": true
}
```

**Response** `200 OK` — 상세 조회 응답과 동일한 구조

**Error**

| 상태코드 | 설명 |
|---------|------|
| `401` | 인증 필요 |
| `403` | 본인 게시글이 아님 |
| `404` | 존재하지 않는 게시글 |

---

### 게시글 삭제

```
DELETE /api/posts/{id}
```

**인증:** 필요 (작성자 본인만 가능)

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `id` | string (UUID) | 게시글 ID |

**Response** `200 OK`

```json
{
  "success": true
}
```

**Error**

| 상태코드 | 설명 |
|---------|------|
| `401` | 인증 필요 |
| `403` | 본인 게시글이 아님 |
| `404` | 존재하지 않는 게시글 |

---

### 좋아요 토글

```
POST /api/posts/{id}/like
```

**인증:** 필요

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `id` | string (UUID) | 게시글 ID |

**Response** `200 OK`

```json
{
  "success": true,
  "data": {
    "liked": true,
    "likeCount": 11
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `liked` | boolean | 토글 후 현재 상태 (`true` = 좋아요 됨) |
| `likeCount` | number | 토글 후 총 좋아요 수 |

**Error**

| 상태코드 | 설명 |
|---------|------|
| `401` | 인증 필요 |
| `404` | 존재하지 않는 게시글 |

---

### 북마크 토글

```
POST /api/posts/{id}/bookmark
```

**인증:** 필요

**Path Parameters**

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| `id` | string (UUID) | 게시글 ID |

**Response** `200 OK`

```json
{
  "success": true,
  "data": {
    "bookmarked": true
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `bookmarked` | boolean | 토글 후 현재 상태 (`true` = 북마크 됨) |

**Error**

| 상태코드 | 설명 |
|---------|------|
| `401` | 인증 필요 |
| `404` | 존재하지 않는 게시글 |

---

## 미구현 API (예정)

| 메서드 | 경로 | 설명 |
|--------|------|------|
| `GET` | `/api/posts/{id}/comments` | 댓글 목록 |
| `POST` | `/api/posts/{id}/comments` | 댓글 작성 |
| `DELETE` | `/api/comments/{id}` | 댓글 삭제 |
| `GET` | `/api/users/me` | 내 프로필 조회 |
| `PUT` | `/api/users/me` | 내 프로필 수정 |
| `GET` | `/api/news` | 뉴스 목록 |
| `POST` | `/api/news` | 뉴스 등록 (ADMIN) |
| `GET` | `/api/market` | 중고 마켓 (`/api/posts?type=SALE` 와 동일) |
| `POST` | `/api/upload/image` | 이미지 업로드 |
