# Work Log

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
