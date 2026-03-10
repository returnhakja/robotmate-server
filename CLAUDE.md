# 로봇메이트(RobotMate) — 풀스택 개발 프롬프트

## 프로젝트 개요

**서비스명:** 로봇메이트 (RobotMate)
**도메인:** robotmate.kr (메인), robotmate.org (글로벌 예비)
**한 줄 정의:** 반려 로봇에 관심 있는 사람과 실제 사용자가 모여 정보·사진·꾸미기 아이디어를 공유하고, 중고 반려 로봇과 악세서리까지 가볍게 사고팔 수 있는 커뮤니티 플랫폼

**서비스 목표:**
- 반려 로봇 시장 초기 단계에서 유저·정보·이미지·거래가 모이는 첫 번째 허브
- 향후 로봇용 악세서리/테마 스토어로 확장할 수 있는 기반(유저·데이터·트래픽) 확보

---

## 타겟 유저

**주요 타겟:**
- AI 반려 로봇을 이미 구매했거나 관심 있는 20–40대 1인 가구, 젊은 부모, 시니어 가족
- 새 디바이스·로봇에 관심 있는 얼리어답터

**부가 타겟:**
- 로봇/캐릭터 디자인, 악세서리 제작에 관심 있는 메이커·디자이너
- 반려 로봇을 선물로 고민하는 사람

---

## 기술 스택

### 프론트엔드
- **Next.js 14+ (App Router)** — SSR/SSG로 SEO 최적화, 한국 시장에서 네이버/구글 검색 유입 중요
- **TypeScript**
- **Tailwind CSS** — 아래 디자인 토큰을 Tailwind config로 변환하여 사용
- **Lucide React** — 아이콘

### 백엔드
- **Spring Boot 3.x (Java 17+)** / **빌드: Maven**
- **Spring Data JPA** + **Hibernate**
- **Spring Security** + **JWT** — 이메일/비밀번호 인증 (추후 카카오 OAuth2 추가)
- **PostgreSQL** — AWS RDS Free Tier (db.t3.micro, 20GB)
- **Cloudflare R2** — 이미지 업로드 스토리지 (S3 SDK 호환)
- **Redis** (선택) — 인기글 랭킹 캐싱. 초기에는 없이 시작해도 됨

### 백엔드 프로젝트 구조
```
src/main/java/kr/robotmate/
├── config/           — SecurityConfig, CorsConfig, S3Config
├── auth/             — JWT 토큰, 로그인/회원가입 API
├── user/             — User 엔티티, Repository, Service, Controller
├── robot/            — RobotModel 엔티티
├── post/             — Post 엔티티 (게시글 + 판매/나눔 통합)
├── comment/          — Comment 엔티티
├── news/             — News 엔티티
├── common/           — BaseEntity(createdAt, updatedAt), 예외 처리, 응답 DTO
└── seed/             — 초기 시드 데이터 DataLoader
```

### API 설계 (REST)
```
POST   /api/auth/register       — 회원가입
POST   /api/auth/login           — 로그인 (JWT 발급)
GET    /api/users/me             — 내 정보
PUT    /api/users/me             — 프로필 수정

GET    /api/posts                — 피드 (쿼리: type, model, tag, sort, page)
GET    /api/posts/{id}           — 게시글 상세
POST   /api/posts                — 글쓰기 (multipart: JSON + 이미지)
PUT    /api/posts/{id}           — 수정
DELETE /api/posts/{id}           — 삭제

POST   /api/posts/{id}/like      — 좋아요 토글
POST   /api/posts/{id}/bookmark  — 북마크 토글

GET    /api/posts/{id}/comments  — 댓글 목록
POST   /api/posts/{id}/comments  — 댓글 작성
DELETE /api/comments/{id}        — 댓글 삭제

GET    /api/models               — 로봇 기종 목록
GET    /api/models/{slug}        — 기종 상세 + 관련 게시글
POST   /api/models               — 기종 추가 (ADMIN)

GET    /api/news                 — 소식 목록
POST   /api/news                 — 소식 추가 (ADMIN)

GET    /api/market               — 중고/나눔 목록 (= posts?type=SALE 필터)

POST   /api/upload/image         — 이미지 업로드 → S3 URL 반환
```

### 배포

| 역할 | 서비스 |
|------|--------|
| **백엔드 서버** | AWS EC2 t2.micro (Spring Boot JAR) |
| **DB** | AWS RDS PostgreSQL db.t3.micro 또는 EC2 내 PostgreSQL |
| **이미지 스토리지** | **Cloudflare R2** — S3 호환, 이그레스 무료 |
| **프론트엔드** | **Cloudflare Pages** — Next.js App Router 지원 |
| **DNS** | **Cloudflare DNS** — robotmate.kr 연결 |
| **HTTPS / CDN** | **Cloudflare 자동 SSL** + CDN 캐싱 |
| **터널** | **Cloudflare Tunnel** — EC2 포트 개방 없이 백엔드 연결 |

### Cloudflare 설정 흐름
1. `robotmate.kr` 네임서버 → Cloudflare로 변경
2. **Cloudflare Pages** — GitHub 연동 후 Next.js 자동 배포
3. **Cloudflare R2** — 버킷 생성 후 S3 SDK로 연동 (endpoint만 R2 URL로 변경)
4. **Cloudflare Tunnel** — EC2에 `cloudflared` 설치 → `api.robotmate.kr` → Spring Boot 8080 포트로 라우팅
5. Cloudflare 대시보드에서 SSL/TLS → Full(strict) 설정

### R2 연동 (Spring Boot)
```yaml
# application.yml
cloud:
  aws:
    s3:
      endpoint: https://<ACCOUNT_ID>.r2.cloudflarestorage.com
      bucket: robotmate-images
    credentials:
      access-key: ${R2_ACCESS_KEY}
      secret-key: ${R2_SECRET_KEY}
    region:
      static: auto
```

### 비용 예상
- EC2 t2.micro: 무료 (12개월) → 이후 ~$8/월
- Cloudflare Pages/R2/Tunnel: **무료 플랜으로 충분**
- RDS 대신 EC2 내 PostgreSQL 사용 시 추가 비용 없음

---

## 정보 구조 (IA) 및 라우팅

```
/ (홈 = 피드)
├── /feed                    — 전체 피드 (메인 페이지)
├── /models                  — 기종별 목록
│   └── /models/[slug]       — 기종 상세 (예: /models/loona)
├── /market                  — 중고/나눔 목록
│   └── /market/[id]         — 중고 상세
├── /news                    — 소식/큐레이션
├── /post/[id]               — 게시글 상세
├── /write                   — 글쓰기
├── /mypage                  — 마이페이지
│   ├── /mypage/posts        — 내가 쓴 글
│   ├── /mypage/comments     — 내 댓글
│   ├── /mypage/likes        — 좋아요한 글
│   └── /mypage/sales        — 내 판매글
├── /login                   — 로그인
├── /register                — 회원가입
└── /admin                   — 운영자 콘텐츠 관리
```

**상단 공통 네비게이션:**
로고 / [피드] / [기종별] / [중고/나눔] / [소식] / 검색 / 다크모드 토글 / [마이페이지] / 로그인

---

## 데이터베이스 스키마 (JPA 엔티티)

모든 엔티티는 `BaseEntity`를 상속받아 `createdAt`, `updatedAt`을 자동 관리.

### User (회원)
```java
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;  // BCrypt 해시

    @Column(unique = true, nullable = false)
    private String nickname;

    private String profileImage;  // S3 URL

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;  // USER | ADMIN

    @OneToMany(mappedBy = "user")
    private List<UserRobot> ownedRobots;
}
```

### UserRobot (내 반려 로봇)
```java
@Entity
public class UserRobot extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private RobotModel robotModel;

    private String nickname;      // 로봇 별명
    private LocalDate startDate;  // 함께한 날짜
}
```

### RobotModel (로봇 기종)
```java
@Entity
public class RobotModel extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String slug;  // URL용: "loona", "moflin"

    @Column(nullable = false)
    private String name;

    private String maker;
    private String price;  // 출시 가격 텍스트

    @Column(columnDefinition = "TEXT")
    private String description;

    private String imageUrl;
    private String officialSite;

    @ElementCollection
    private List<String> keywords;  // 특징 키워드
}
```

### Post (게시글)
```java
@Entity
public class Post extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    private PostType type;  // SHOW, REVIEW, QUESTION, CONCEPT, SALE

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    private RobotModel robotModel;  // 선택

    @ElementCollection
    private List<String> tags;

    @ElementCollection
    private List<String> images;  // S3 URL, 최대 5장

    private int viewCount = 0;

    // 판매/나눔 전용 (type=SALE)
    @Enumerated(EnumType.STRING)
    private SaleType saleType;    // SELL | FREE
    private Integer salePrice;
    private String condition;     // like-new, good, used, repair
    private String usagePeriod;
    private String tradeMethod;   // direct, delivery, negotiable
    private String contactInfo;
    private boolean isSold = false;
}
```

### Comment (댓글)
```java
@Entity
public class Comment extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;
}
```

### Like (좋아요)
```java
@Entity
@Table(name = "likes", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "post_id"}))
public class Like extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;
}
```

### Bookmark (북마크)
```java
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "post_id"}))
public class Bookmark extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;
}
```

### News (소식 — 운영자가 직접 입력)
```java
@Entity
public class News extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

    private String sourceUrl;
    private String sourceName;
    private LocalDateTime publishedAt;
}
```

---

## 핵심 기능 상세

### 1. 회원 시스템
- **회원가입:** 이메일 + 비밀번호 (+ 카카오 소셜 로그인)
    - 필수: 이메일, 비밀번호(8자 이상), 닉네임
    - 선택: 프로필 이미지, 보유/관심 로봇 기종
- **로그인:** 이메일+비밀번호 또는 카카오
- **마이페이지:**
    - 내 프로필 정보 수정
    - 내 반려 로봇 카드 등록/관리 (기종 선택 + 별명 + 함께한 날짜)
    - 탭별: 내가 쓴 글 / 내 댓글 / 좋아요한 글 / 내 판매글

### 2. 게시글/피드
- **글 종류 (PostType):** 자랑/사진, 리뷰/사용기, 질문/정보, 콘셉트/상상, 판매/나눔
- **작성 필드:**
    - 제목, 내용(텍스트), 기종 선택(선택), 태그(해시태그), 이미지 1~5장 업로드
    - 판매/나눔 선택 시 추가 필드: 거래유형, 가격, 제품상태, 사용기간, 거래방식, 연락방법
- **인터랙션:** 좋아요(하트), 댓글(단일 깊이), 북마크
- **피드 화면:**
    - 카드형 그리드 (대표이미지 + 타입 배지 + 제목 + 일부 내용 + 좋아요/댓글 수)
    - 상단 필터 탭: [전체 / 자랑 / 리뷰 / 질문 / 콘셉트 / 판매/나눔]
    - "이번 주 인기" 트렌딩 섹션 (좋아요 수 기준 정렬, 가로 스크롤)
    - 태그/키워드 검색

### 3. 기종별 페이지
- `/models` — 전체 기종 카드 그리드 (이미지 + 이름 + 제조사 + 키워드 + 가격)
- `/models/[slug]` — 상세:
    - 상단: 기종 대표 이미지, 기본 정보(스펙, 출시 가격, 특징 키워드, 공식 사이트 링크)
    - 하단: 해당 기종 게시글 전용 피드 (필터: 자랑/리뷰/질문/콘셉트/판매)

### 4. 중고/나눔 (/market)
- **전용 페이지** — 판매/나눔 타입 게시글만 모아서 표시
- **필터:** 기종별, 가격대, 제품 상태, 거래 유형(판매/나눔)
- **리스트:** 썸네일 + 가격 + 기종 + 상태 + 업로드 일자
- **거래 완료 체크:** 작성자가 "거래 완료" 버튼으로 상태 변경
- **안전 거래 안내:** 고정 공지 배너 (선입금 주의, 안전거래 권장)
- **결제/배송/에스크로는 제공하지 않음** — 연락처 노출 + 댓글 문의만

### 5. 소식 (/news)
- 운영자가 직접 입력하는 큐레이션 콘텐츠
- 반려 로봇 관련 뉴스/기사/영상 링크 + 짧은 요약
- 리스트 형태 (아이콘 + 제목 + 요약 + 출처 + 날짜)

### 6. 운영자 관리 (/admin)
- role이 ADMIN인 사용자만 접근 가능
- **소식 추가:** 제목, URL, 요약, 출처 입력
- **기종 추가:** 모델명, 제조사, 가격, 설명, 키워드, 공식 사이트 URL
- **샘플 데이터 초기화** (개발/테스트용)

---

## 비로그인/로그인 유저 권한

| 기능 | 비로그인 | 로그인 |
|---|---|---|
| 피드/기종별/소식 열람 | ✅ | ✅ |
| 게시글 상세 보기 | ✅ | ✅ |
| 중고/나눔 목록 보기 | ✅ | ✅ |
| 중고 상세(연락처) 보기 | ❌ → 로그인 유도 | ✅ |
| 좋아요/댓글/북마크 | ❌ → 로그인 유도 | ✅ |
| 글쓰기 | ❌ → 로그인 유도 | ✅ |
| 마이페이지 | ❌ | ✅ |
| 운영자 관리 | ❌ | ✅ (ADMIN만) |

---

## 디자인 시스템

### 톤 & 무드
- **따뜻하면서 약간 테크한** — 반려의 따뜻함 + 로봇의 기술감
- warm neutral 베이지 배경 + soft teal 포인트 컬러

### 컬러 토큰
```css
/* Light Mode */
--color-bg:             #f5f3ef;
--color-surface:        #faf9f6;
--color-surface-2:      #ffffff;
--color-text:           #1e1d1a;
--color-text-muted:     #6b6a65;
--color-text-faint:     #a3a29d;
--color-primary:        #1a8a8a;  /* Warm Teal */
--color-primary-hover:  #147070;
--color-primary-light:  #e0f4f4;
--color-coral:          #e87461;  /* 좋아요, 하트, 포인트 배지 */
--color-coral-light:    #fde8e4;

/* Dark Mode */
--color-bg:             #141312;
--color-surface:        #1c1b19;
--color-surface-2:      #232220;
--color-text:           #e5e4e0;
--color-text-muted:     #9a9994;
--color-primary:        #4ac5c5;
--color-coral:          #ff9080;
```

### 폰트
- **Display (제목):** Satoshi (Fontshare)
- **Body (본문):** General Sans (Fontshare)
- 둘 다 `https://api.fontshare.com/v2/css` 에서 로드

### 간격
- 4px 기반 스페이싱 시스템: 4, 8, 12, 16, 20, 24, 32, 40, 48, 64, 80, 96px

### 라운딩
- sm: 6px, md: 8px, lg: 12px, xl: 16px, full: 9999px

### 필수 UI 컴포넌트
- 카드 (피드, 기종, 마켓, 소식)
- 배지 (글 타입별 색상 분류)
- 필터 탭 (태그 형태)
- 버튼 (Primary, Secondary, Ghost, Coral, Icon)
- 인풋/셀렉트 필드
- 토스트 알림
- 모바일 햄버거 메뉴
- FAB (플로팅 글쓰기 버튼)
- 다크모드 토글

---

## 초기 시드 데이터

서비스 초기에는 시장에 데이터가 거의 없으므로, 다음 시드 데이터를 DB에 넣어줘:

### 로봇 기종 (6개)
1. **LOONA** — KEYi Tech, ₩990,000, 고양이형 AI 반려 로봇 (얼굴인식, 감정표현, 자율이동, 앱연동)
2. **Moflin** — Vanguard Industries, ₩550,000, 털복숭이 감정 반려 로봇 (감정진화, 촉각센서, 힐링)
3. **LOVOT** — GROOVE X, ₩3,500,000, 체온이 느껴지는 교감 로봇 (체온감지, 자율주행, 프리미엄)
4. **Ebo Air** — Enabot, ₩250,000, 펫 모니터링 구형 로봇 (원격카메라, 자동놀이, 컴팩트)
5. **Eilik** — Energize Lab, ₩190,000, 데스크탑 감성 로봇 (감성반응, 멀티교감, 가성비)
6. **Vector 2.0** — Digital Dream Labs, ₩350,000, 소형 음성비서 로봇 (음성비서, 스마트홈, 클래식)

### 샘플 게시글 (8개 — 다양한 타입)
### 샘플 중고/나눔 (4개)
### 샘플 소식 (5개)

(구체적인 샘플 데이터는 MVP 프론트엔드 코드의 `app.js`에 있는 `SAMPLE_POSTS`, `SAMPLE_MARKET_POSTS`, `SAMPLE_NEWS` 배열을 그대로 seed 스크립트에 활용해줘)

---

## SEO 요구사항

- 모든 페이지에 적절한 `<title>`, `<meta description>`, OG 태그
- 기종별 상세 페이지는 SSG/ISR로 정적 생성
- sitemap.xml 자동 생성
- robots.txt 설정
- 네이버 서치어드바이저, 구글 서치콘솔 메타 태그 지원

---

## 반응형 브레이크포인트

- Mobile: < 768px (카드 1열, 햄버거 메뉴, FAB 글쓰기)
- Tablet: 768px – 1024px (카드 2열)
- Desktop: > 1024px (카드 3~4열, 전체 네비게이션)

---

## 개발 우선순위

### Phase 1 (MVP — 지금 구현)
1. 프로젝트 세팅 (프론트: Next.js / 백엔드: Spring Boot + JPA + PostgreSQL)
2. 회원 시스템 (가입/로그인/세션)
3. 게시글 CRUD + 이미지 업로드
4. 피드 (필터, 검색, 인기글)
5. 기종별 페이지
6. 중고/나눔 기능
7. 소식 페이지
8. 마이페이지
9. 운영자 관리
10. 다크모드 + 반응형

### Phase 2 (추후)
- 카카오 소셜 로그인
- 알림 시스템 (댓글, 좋아요 알림)
- 기종 비교 기능
- 이미지 최적화 (리사이징, WebP 변환)
- 무한 스크롤 / 페이지네이션
- 쇼핑몰 기능

---

## 참고: MVP 프론트엔드 코드

이미 정적 HTML/CSS/JS로 만든 MVP 프론트엔드가 있어. 디자인과 UX 흐름은 이걸 참고해서 전환해줘.

**프론트엔드 (Next.js):** MVP의 디자인/레이아웃을 최대한 유지하면서 React 컴포넌트로 전환하고, fetch/axios로 Spring Boot API를 호출하도록 변경.

**백엔드 (Spring Boot):** MVP의 `app.js`에 있는 인메모리 샘플 데이터를 JPA 엔티티 + seed DataLoader로 변환. REST API로 프론트와 통신.

MVP 코드 구조:
- `index.html` — 전체 페이지 구조 (해시 라우팅)
- `style.css` — 디자인 토큰 + 색상 체계
- `layout.css` — 레이아웃 + 컴포넌트 스타일
- `app.js` — 라우팅, 렌더링, 이벤트 핸들링, 샘플 데이터
- `base.css` — CSS 리셋 + 기본 스타일
