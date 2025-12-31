# 코딩 컨벤션 및 스타일

## Entity 설계 원칙
- **BaseEntity 상속 필수**: createdAt, updatedAt 자동 관리
- **@Setter 금지**: 불변성 보장, 명시적 메서드로만 상태 변경
- **정적 팩토리 메서드**: `of()`, `create()` 등으로 Entity 생성
- **JPA Auditing**: `@CreatedDate`, `@LastModifiedDate` 사용

## Service 레이어
- 클래스 레벨: `@Transactional(readOnly=true)`
- 쓰기 메서드만: `@Transactional` 오버라이드
- 커스텀 예외만 사용 (GlobalExceptionHandler 처리)
  - NotFoundException (404)
  - ForbiddenException (403)
  - DuplicateException (409)
  - InvalidCredentialsException (401)
  - UnauthorizedException (401)
  - BadRequestException (400)

## Controller
- 인증 사용자: `@AuthenticationPrincipal UserDetails`
- email이 인증 식별자: `userDetails.getUsername()` = email
- 권한 검증은 Service 레이어에서 처리

## DTO 네이밍
- Request: `{Action}{HttpMethod}Request` (예: CommentCreatePostRequest)
- Response: `{Resource}{HttpMethod}Response` (예: PostGetResponse)
- 공통 응답: `ApiResponse.success()`, `ApiResponse.error()`

## 패키지 구조
- 도메인별 DTO 하위 패키지 구성 (auth, posts, users, comments, tags, like, common)
