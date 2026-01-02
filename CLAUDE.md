# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 빌드 및 실행

```bash
# 빌드 및 실행
./gradlew build                    # 전체 빌드
./gradlew bootRun                  # 애플리케이션 실행 (http://localhost:8080)
./gradlew test                     # 전체 테스트 실행
./gradlew test --tests ClassName  # 특정 테스트 클래스 실행
./gradlew clean build              # 클린 빌드

# QueryDSL Q-class 생성 (build/generated/sources/annotationProcessor/java/main)
./gradlew clean compileJava        # Q-class 재생성이 필요한 경우

# 데이터베이스
docker-compose up -d               # MySQL 시작 (port 13306)
docker-compose down                # MySQL 중지

# API 문서 (애플리케이션 실행 후)
# Swagger UI: http://localhost:8080/swagger-ui/index.html
```

## 환경 설정

### 사전 요구사항
- Java 21
- MySQL 8.0 (또는 Docker)

### 데이터베이스 설정
**옵션 A: 로컬 MySQL 사용**
- MySQL에서 `vlog` 데이터베이스 생성
- `application.yaml` 설정 확인

**옵션 B: Docker 사용**
```bash
docker-compose up -d  # MySQL 시작 (port 13306)
# application.yaml 포트를 13306으로 변경 필요
```

**application.yaml** 기본 설정:
- DB URL: `jdbc:mysql://localhost:3306/vlog`
- DB User: `root` / Password: `1111`
- Hibernate DDL: `update` (스키마 자동 업데이트, 데이터 유지)
- SQL 로깅 활성화 (format_sql, bind parameter trace)

**데이터베이스 설정**:
- 개발 환경: **로컬 MySQL 사용** (port 3306, database: vlog)
- docker-compose.yml은 참고용 (사용 시 application.yaml 포트를 13306으로 변경 필요)

## 기술 스택

Spring Boot 3.5.9 / Java 21 / JPA + QueryDSL + MySQL / Spring Security (세션 기반)

## 프로젝트 구조

```
v-log/
├── backend/vlog/                    # Spring Boot 백엔드
│   └── src/main/java/com/likelion/vlog/
│       ├── config/                  # ProjectSecurityConfig, SwaggerConfig, JpaConfig
│       ├── controller/              # REST API 컨트롤러
│       ├── service/                 # 비즈니스 로직
│       ├── repository/
│       │   ├── querydsl/
│       │   │   ├── custom/          # Custom interface & implementations
│       │   │   └── expresion/       # QueryDSL expression helpers (PostExpression, TagMapExpression)
│       │   └── *.java               # Standard JPA repositories
│       ├── dto/                     # Request/Response DTOs (도메인별 패키지)
│       ├── enums/                   # SearchField, SortField, SortOrder, TagMode
│       ├── exception/               # 커스텀 예외 및 GlobalExceptionHandler
│       └── entity/                  # JPA 엔티티 (User, Blog, Post, Comment, Tag, TagMap, Like, Follow)
└── front-end/v-log-front/           # React 프론트엔드 (별도 관리)
```

## API 엔드포인트

### 게시글 (`/api/v1/posts`)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/v1/posts` | 목록 조회 | X |
| GET | `/api/v1/posts/{id}` | 상세 조회 | X |
| POST | `/api/v1/posts` | 작성 | O |
| PUT | `/api/v1/posts/{id}` | 수정 | O (작성자) |
| DELETE | `/api/v1/posts/{id}` | 삭제 | O (작성자) |

### 인증 (`/auth`)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/auth/signup` | 회원가입 | X |
| POST | `/auth/login` | 로그인 | X |
| POST | `/auth/logout` | 로그아웃 | O |

### 사용자 (`/users`)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/users/{id}` | 조회 | X |
| PUT | `/users/{id}` | 수정 | O |
| DELETE | `/users/{id}` | 탈퇴 | O |

### 좋아요 (`/api/v1/posts/{postId}/like`)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/v1/posts/{postId}/like` | 좋아요 정보 조회 | X (선택적) |
| POST | `/api/v1/posts/{postId}/like` | 좋아요 추가 | O |
| DELETE | `/api/v1/posts/{postId}/like` | 좋아요 취소 | O |

### 댓글 (`/api/v1/posts/{postId}/comments`)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/v1/posts/{postId}/comments` | 댓글 목록 조회 (답글 포함) | X |
| POST | `/api/v1/posts/{postId}/comments` | 댓글 작성 | O |
| PUT | `/api/v1/posts/{postId}/comments/{commentId}` | 댓글 수정 | O (작성자) |
| DELETE | `/api/v1/posts/{postId}/comments/{commentId}` | 댓글 삭제 | O (작성자) |
| POST | `/api/v1/posts/{postId}/comments/{commentId}/replies` | 답글 작성 | O |
| PUT | `/api/v1/posts/{postId}/comments/{commentId}/replies/{replyId}` | 답글 수정 | O (작성자) |
| DELETE | `/api/v1/posts/{postId}/comments/{commentId}/replies/{replyId}` | 답글 삭제 | O (작성자) |

### 팔로우 (`/api/v1/users/{userId}`)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/v1/users/{userId}/follows` | 팔로우 | O |
| DELETE | `/api/v1/users/{userId}/follows` | 언팔로우 | O |
| GET | `/api/v1/users/{userId}/followers` | 팔로워 목록 조회 (페이징) | X |
| GET | `/api/v1/users/{userId}/followings` | 팔로잉 목록 조회 (페이징) | X |

### 태그 (`/api/v1/tags`)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/v1/tags/{title}` | 태그 조회 | X |

## Entity 관계

```
User (1) ── (1) Blog (1) ── (*) Post ── (*) TagMap ── (1) Tag
                              ├── (*) Comment (self-ref)
                              └── (*) Like ── (*) User
```

## 아키텍처 핵심 패턴

### 인증 시스템 (Session-based)
- **SecurityFilterChain**: `ProjectSecurityConfig`에서 세션 기반 인증 설정
- **인증 방식**: `DaoAuthenticationProvider` + `UserDetailsService` (AuthService 구현)
- **세션 저장소**: `HttpSessionSecurityContextRepository`
- **비밀번호 인코딩**: `DelegatingPasswordEncoder` (bcrypt 기본)
- **인증 실패 처리**: `AuthEntryPoint` (커스텀 EntryPoint)

**중요**:
- `userDetails.getUsername()`은 **email**을 반환합니다 (User entity의 email이 인증 식별자)
- 컨트롤러에서 인증된 사용자는 `@AuthenticationPrincipal UserDetails`로 주입받습니다

### Entity 설계 원칙
- **BaseEntity 상속**: 모든 엔티티는 `createdAt`, `updatedAt` 자동 관리를 위해 상속 필요
- **@Setter 금지**: 불변성 보장, 명시적 메서드로만 상태 변경
- **정적 팩토리 메서드**: Entity 생성은 정적 팩토리 메서드 사용 (`of()`, `create()` 등)
- **JPA Auditing**: `@CreatedDate`, `@LastModifiedDate`로 시간 자동 기록

### Service 레이어
- **트랜잭션 전략**: 클래스 레벨에 `@Transactional(readOnly=true)`, 쓰기 메서드만 `@Transactional` 오버라이드
- **예외 처리**: 커스텀 예외만 사용
  - `NotFoundException`: 리소스 없음 (404) - 정적 팩토리: `post()`, `user()`, `blog()`, `follow()`, `like()`
  - `ForbiddenException`: 권한 없음 (403) - 정적 팩토리: `postUpdate()`, `postDelete()`, `userUpdate()`, `userDelete()`
  - `DuplicateException`: 중복 리소스 (409) - 정적 팩토리: `email()`, `nickname()`, `following()`, `like()`
  - `InvalidCredentialsException`: 인증 정보 불일치 (401) - 정적 팩토리: `login()`, `password()`
  - `UnauthorizedException`: 로그인 필요 (401) - 정적 팩토리: `loginRequired()`
  - `BadRequestException`: 잘못된 요청 (400) - 정적 팩토리: `requiredField()`, `invalidFormat()`, `selfFollow()`
- **GlobalExceptionHandler**: 전역 예외 처리로 일관된 에러 응답

### QueryDSL 동적 쿼리 패턴
- **Custom Repository**: `PostRepositoryCustom` 인터페이스 + `PostRepositoryImpl` 구현체
- **Expression Helper**: `PostExpression`, `TagMapExpression`으로 재사용 가능한 조건 추상화
- **복합 검색**: `PostGetRequest`로 keyword, tag, blogId, 정렬, 페이징을 한 번에 처리
- **Enum 기반 설정**:
  - `SearchField`: BLOG, NICKNAME, TITLE (검색 대상 필드)
  - `SortField`: CREATED_AT, LIKE_COUNT (정렬 기준)
  - `SortOrder`: ASC, DESC (정렬 방향)
  - `TagMode`: OR, AND (태그 필터 모드)

### DTO 구조
DTOs는 도메인별로 하위 패키지 구성:
- `dto/auth/`: 인증 관련 (SignupRequest, LoginRequest)
- `dto/posts/`: 게시글 관련 (PostGetRequest, PostCreatePostRequest, PostUpdatePutRequest, PostListGetResponse, PostGetResponse, PageResponse)
- `dto/comments/`: 댓글 관련 (CommentCreatePostRequest, CommentUpdatePutRequest, CommentPostResponse, CommentPutResponse, CommentWithRepliesGetResponse, ReplyCreatePostRequest, ReplyUpdatePutRequest, ReplyPostResponse, ReplyPutResponse, ReplyGetResponse)
- `dto/like/`: 좋아요 관련 (LikeResponse)
- `dto/follows/`: 팔로우 관련 (FollowPostResponse, FollowDeleteResponse, FollowerGetResponse, FollowingGetResponse, PageResponse)
- `dto/users/`: 사용자 관련 (UserGetResponse, UserUpdateRequest, UserDeleteRequest)
- `dto/tags/`: 태그 관련 (TagGetResponse)
- `dto/common/`: 공통 응답 (ApiResponse)

**주의**: PageResponse가 `dto/posts/`와 `dto/follows/`에 중복 정의되어 있음 → `dto/common/`으로 통합 필요

## 구현 현황

### 완료
- 회원가입/로그인/로그아웃 (AuthController, AuthService)
- 게시글 CRUD (QueryDSL 동적 쿼리 포함)
- 사용자 CRUD (UserController, UserService)
- 해시태그 (TagMap을 통한 다대다 관계)
- 좋아요 CRUD (LikeController, LikeService)
- 댓글/답글 CRUD (CommentController, CommentService)
- 팔로우/언팔로우 (FollowController, FollowService)
- 태그 조회 (TagController, TagService)
- Swagger UI (springdoc-openapi)

## 구현 가이드

### 새 엔티티 추가 시
1. `BaseEntity` 상속
2. `@Getter` 사용, `@Setter` 금지
3. 정적 팩토리 메서드로 생성 (`of()`, `create()` 등)
4. 연관 관계 설정 시 양방향이면 편의 메서드 추가

### 새 API 엔드포인트 추가 시
1. Controller: `@AuthenticationPrincipal UserDetails`로 인증 사용자 받기
2. Service: `@Transactional(readOnly=true)` 클래스 레벨, 쓰기 메서드에만 `@Transactional`
3. 예외 처리: 커스텀 예외의 정적 팩토리 메서드 사용
   - 리소스 없음: `NotFoundException.post(id)`, `NotFoundException.user(email)` 등
   - 중복 데이터: `DuplicateException.email(email)`, `DuplicateException.like()` 등
   - 권한 없음: `ForbiddenException.postUpdate()`, `ForbiddenException.userDelete()` 등
   - 인증 실패: `InvalidCredentialsException.password()`, `UnauthorizedException.loginRequired()` 등
   - 잘못된 요청: `BadRequestException.requiredField(name)`, `BadRequestException.selfFollow()` 등
4. 권한 검증: Service 레이어에서 작성자 검증 후 `ForbiddenException` 발생
5. SecurityConfig: `ProjectSecurityConfig`에 엔드포인트 인증 규칙 추가

### QueryDSL Custom Repository 추가 시
1. `repository/querydsl/custom/` 에 `XxxRepositoryCustom` 인터페이스 생성
2. 같은 패키지에 `XxxRepositoryImpl` 구현체 생성 (이름 규칙 필수)
3. 기본 JPA Repository가 Custom 인터페이스 상속: `interface XxxRepository extends JpaRepository, XxxRepositoryCustom`
4. `repository/querydsl/expresion/` 에 재사용 가능한 BooleanExpression 메서드 작성
5. 복잡한 동적 쿼리는 Expression Helper 활용하여 가독성 향상

### 좋아요 구현 패턴 (참고)
- **중복 체크**: `existsByUserIdAndPostId`로 추가 전 검증, 중복 시 `IllegalStateException`
- **원자적 연산**: `@Modifying @Query`로 좋아요 수 증가/감소 (동시성 안전)
- **반환값**: 최신 좋아요 수와 사용자의 좋아요 상태를 함께 반환
- **프론트엔드 처리**: POST/DELETE 구분, 현재 상태 기반 호출 (LikeController 주석 참조)

### 테스트 작성
- **Repository 테스트**: `@DataJpaTest` 사용
- **Service 테스트**: Mockito로 Repository mocking
- **Controller 테스트**: `@WebMvcTest` + MockMvc 사용
- 총 85개 테스트 존재, 새 기능 추가 시 테스트 작성 필수

## 예외 처리 전략

### HTTP 상태 코드 및 메시지
프론트엔드 에러 케이스는 `EXCEPTION_CASE.md` 참조. 주요 에러:

- **401 Unauthorized**: 인증 실패 (로그인 불일치, 비회원 접근)
- **403 Forbidden**: 권한 없음 (타인의 리소스 수정/삭제)
- **404 Not Found**: 리소스 없음 (삭제된 게시글 등)
- **409 Conflict**: 중복 리소스 (이메일/닉네임 중복)
- **500 Internal Server Error**: 서버 내부 오류

### GlobalExceptionHandler 처리 방식
모든 커스텀 예외는 `GlobalExceptionHandler`에서 일관된 `ErrorResponse` 형식으로 변환되어 반환됩니다.

## 알려진 이슈 및 TODO

### Critical
- [x] **LikeService**: `IllegalArgumentException`, `IllegalStateException` → 커스텀 예외로 변경 완료
- [x] **AuthService/UserService**: `IllegalArgumentException` → 커스텀 예외로 변경 완료
- [x] **FollowService**: `IllegalArgumentException` → 커스텀 예외로 변경 완료
- [ ] **UserController 보안**: `@AuthenticationPrincipal` 누락 - 아무나 다른 사용자 수정/삭제 가능
- [ ] **CORS 미설정**: `ProjectSecurityConfig`에 CORS 설정 추가 필요 (허용 도메인: `localhost:3000`)
- [ ] **Blog.java**: `@Setter` 사용 중 - 제거 필요 (CLAUDE.md 원칙 위반)
- [ ] **User.java**: Hibernate 시간 어노테이션(`@CurrentTimestamp`, `@UpdateTimestamp`)과 JPA Auditing 충돌 - Hibernate 어노테이션 제거 필요

### High Priority
- [ ] **LikeService @Transactional**: 클래스 레벨 `@Transactional` → `@Transactional(readOnly = true)`로 변경
- [ ] **LikeController 인증**: GET 메서드에 `@AuthenticationPrincipal(required = false)` 명시 필요
- [ ] **SecurityConfig 중복**: 동일 엔드포인트 중복 정의 (라인 40-44, 51-59)
- [ ] **PathVariable 네이밍**: `user_id` (snake_case) vs `postId` (camelCase) 혼용 → camelCase 통일
- [ ] **FollowService N+1**: getFollowers에서 각 팔로워마다 추가 쿼리 발생 → QueryDSL로 개선

### Medium Priority
- [ ] **CommentService 예외**: NotFoundException, ForbiddenException 정적 팩토리 미사용
- [ ] **PageResponse 중복**: `dto/posts/`와 `dto/follows/`에 중복 → `dto/common/`으로 통합
- [ ] **PostController 응답 불일치**: getPosts만 ApiResponse 래핑 없이 반환 → 통일 필요
- [ ] **User.upDateInfo() 오타**: `upDateInfo` → `updateInfo`로 수정
- [ ] **TagService null 처리**: `orElse(null)` → `NotFoundException.tag(tagName)` 발생

### Low Priority
- [ ] **코드 중복 제거**: User/Post 조회 로직, FollowController PageResponse 구성 중복
- [ ] **Swagger @Parameter 누락**: 컨트롤러 파라미터에 설명 추가 필요
- [ ] **삭제 응답 통일**: PostController(204) vs CommentController(200) → 204로 통일
- [ ] **DDL 운영 모드**: 프로덕션에서 `validate`로 변경 (현재 `update`)
#### 15. API 버전 관리 전략
- **해결**: 헤더 기반 버전 관리 또는 URL 분리

#### 16. 프로파일 전략 개선
- **해결**: `application-dev.yaml`, `application-prod.yaml` 분리

### Enhancement (기능 추가)
- [ ] **팔로우 기능**: FollowController, FollowService 구현
- [ ] **CORS 설정**: 프론트엔드 연결 시 `ProjectSecurityConfig`에서 allowedOrigins 등 설정
- [ ] **TagController**: 현재 비어있음, 태그 조회 API 추가 가능
- [ ] **좋아요 토글 API**: 단일 엔드포인트로 POST/DELETE 통합 고려

## 우선순위 매트릭스

| 우선순위 | 개선 항목 | 영향도 | 난이도 |
|---------|----------|-------|-------|
| Critical | CSRF 보호 활성화 | ⚠️⚠️⚠️ | 낮음 |
| Critical | DB 비밀번호 환경변수화 | ⚠️⚠️⚠️ | 낮음 |
| Critical | DDL Auto 환경별 분리 | ⚠️⚠️⚠️ | 낮음 |
| High | N+1 쿼리 해결 | ⚡⚡⚡ | 중간 |
| High | 데이터베이스 인덱스 추가 | ⚡⚡⚡ | 낮음 |
| High | 동시성 제어 추가 | ⚡⚡ | 중간 |
| High | 트랜잭션 범위 최적화 | ⚡⚡ | 중간 |