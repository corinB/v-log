# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 빌드 및 실행

```bash
# 빌드 및 실행
./gradlew build                    # 전체 빌드
./gradlew bootRun                  # 애플리케이션 실행
./gradlew test                     # 전체 테스트 실행
./gradlew test --tests ClassName  # 특정 테스트 클래스 실행
./gradlew clean build              # 클린 빌드

# 데이터베이스
docker-compose up -d               # MySQL 시작 (port 13306)
docker-compose down                # MySQL 중지
```

## 환경 설정

**application.yaml** 기본 설정:
- DB URL: `jdbc:mysql://localhost:3306/vlog`
- DB User: `root` / Password: `1111`
- Hibernate DDL: `create` (애플리케이션 시작 시 테이블 재생성)
- SQL 로깅 활성화 (format_sql, bind parameter trace)

**데이터베이스 설정**:
- 개발 환경: **로컬 MySQL 사용** (port 3306, database: vlog)
- docker-compose.yml은 참고용 (사용 시 application.yaml 포트를 13306으로 변경 필요)

## 기술 스택

Spring Boot 3.5.9 / Java 21 / JPA + MySQL / Spring Security (세션 기반)

## 패키지 구조

```
com.likelion.vlog
├── config/          # SecurityConfig
├── controller/      # PostController, CommentController, AuthController, UserController
├── service/         # PostService, CommentService, AuthService, UserService, UserServiceV2
├── repository/      # JPA Repositories
├── dto/             # Request/Response DTOs
├── exception/       # NotFoundException, ForbiddenException, DuplicateException
└── entity/          # User, Blog, Post, Comment, Tag, TagMap, Like, Follow
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

### 댓글 (`/api/v1/posts/{postId}/comments`)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/v1/posts/{postId}/comments` | 목록 조회 | X |
| POST | `/api/v1/posts/{postId}/comments` | 작성 | O |
| PUT | `/api/v1/posts/{postId}/comments/{id}` | 수정 | O (작성자) |
| DELETE | `/api/v1/posts/{postId}/comments/{id}` | 삭제 | O (작성자) |

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
  - `NotFoundException`: 리소스 없음 (404)
  - `ForbiddenException`: 권한 없음 (403)
  - `DuplicateException`: 중복 리소스 (409)
- **GlobalExceptionHandler**: 전역 예외 처리로 일관된 에러 응답

### DTO 구조
DTOs는 도메인별로 하위 패키지 구성:
- `dto/auth/`: 인증 관련 (SignupRequest, LoginRequest, etc.)
- `dto/posts/`: 게시글 관련
- `dto/comments/`: 댓글 관련
- `dto/users/`: 사용자 관련
- `dto/tags/`: 태그 관련
- `dto/common/`: 공통 응답 (ErrorResponse 등)

## 구현 현황

### 완료
- 회원가입/로그인/로그아웃, 게시글 CRUD, 댓글 CRUD, 사용자 CRUD, 해시태그

### 미구현 (Sprint 2)
- 좋아요, 팔로우 (Entity만 존재)

## 구현 가이드

### 새 엔티티 추가 시
1. `BaseEntity` 상속
2. `@Getter` 사용, `@Setter` 금지
3. 정적 팩토리 메서드로 생성 (`of()`, `create()` 등)
4. 연관 관계 설정 시 양방향이면 편의 메서드 추가

### 새 API 엔드포인트 추가 시
1. Controller: `@AuthenticationPrincipal UserDetails`로 인증 사용자 받기
2. Service: `@Transactional(readOnly=true)` 클래스 레벨, 쓰기 메서드에만 `@Transactional`
3. 예외 처리: `NotFoundException`, `ForbiddenException`, `DuplicateException` 사용
4. 권한 검증: Service 레이어에서 작성자 검증 후 `ForbiddenException` 발생
5. SecurityConfig: `ProjectSecurityConfig`에 엔드포인트 인증 규칙 추가

### 테스트 작성
- **Repository 테스트**: `@DataJpaTest` 사용
- **Service 테스트**: Mockito로 Repository mocking
- **Controller 테스트**: `@WebMvcTest` + MockMvc 사용
- 총 85개 테스트 존재, 새 기능 추가 시 테스트 작성 필수

## 알려진 이슈 및 TODO

### Critical
- [ ] **AuthService/UserService**: `IllegalArgumentException` → 커스텀 예외로 변경
- [ ] **UserController**: 권한 검증 추가 (본인만 수정/삭제)
- [ ] **User.java**: `BaseEntity` 상속, `@Setter` 제거
- [ ] **UserService vs UserServiceV2**: 중복 정리 필요

### Enhancement
- [ ] **CORS 설정**: 프론트엔드 연결 시 `ProjectSecurityConfig`에서 allowedOrigins 등 설정
- [ ] **TagController**: 현재 비어있음, 태그 조회 API 추가 가능
- [ ] **Hibernate DDL**: `create` → `update` 또는 `validate`로 변경 (프로덕션 준비 시)