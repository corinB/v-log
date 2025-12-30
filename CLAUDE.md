# CLAUDE.md

This file provides guidance to Claude Code when working with code in this repository.

## 빌드 및 실행

```bash
# 빌드 및 실행
./gradlew build                    # 전체 빌드
./gradlew bootRun                  # 애플리케이션 실행
./gradlew test                     # 전체 테스트 실행
./gradlew test --tests ClassName   # 특정 테스트 클래스 실행
./gradlew clean build              # 클린 빌드

# 데이터베이스
docker-compose up -d               # MySQL 시작 (port 13306)
docker-compose down                # MySQL 중지
```

## 환경 설정

**application.yaml** 기본 설정:
- DB URL: `jdbc:mysql://localhost:3306/vlog`
- DB User: `root` / Password: `1111`
- Hibernate DDL: `update` (스키마 자동 업데이트)
- SQL 로깅 활성화 (format_sql, bind parameter trace)

**데이터베이스 설정**:
- 개발 환경: **로컬 MySQL 사용** (port 3306, database: vlog)
- docker-compose.yml은 참고용 (사용 시 application.yaml 포트를 13306으로 변경 필요)

## 기술 스택

Spring Boot 3.5.9 / Java 21 / JPA + QueryDSL + MySQL / Spring Security (세션 기반)

## 패키지 구조

```
com.likelion.vlog
├── VlogApplication.java           # 메인 진입점 (@EnableJpaAuditing)
├── config/
│   └── ProjectSecurityConfig.java # Spring Security 설정
├── controller/
│   ├── AuthController.java        # 인증 API (/api/v1/auth)
│   ├── PostController.java        # 게시글 API (/api/v1/posts)
│   ├── CommentController.java     # 댓글 API (/api/v1/posts/{postId}/comments)
│   ├── LikeController.java        # 좋아요 API (/api/v1/posts/{postId}/like)
│   ├── FollowController.java      # 팔로우 API (/api/v1/users/{userId}/follows)
│   ├── UserController.java        # 사용자 API (/api/v1/tags/users) [경로 수정 필요]
│   └── TagController.java         # 태그 API (/api/v1/tags)
├── service/
│   ├── AuthService.java           # 인증 + UserDetailsService 구현
│   ├── PostService.java           # 게시글 CRUD + 태그 관리
│   ├── CommentService.java        # 댓글/대댓글 CRUD
│   ├── LikeService.java           # 좋아요 추가/삭제/조회
│   ├── FollowService.java         # 팔로우/언팔로우
│   ├── UserService.java           # 사용자 CRUD
│   └── TagService.java            # 태그 조회
├── repository/
│   ├── UserRepository.java
│   ├── BlogRepository.java
│   ├── PostRepository.java
│   ├── CommentRepository.java
│   ├── TagRepository.java
│   ├── TagMapRepository.java
│   ├── LikeRepository.java
│   └── FollowRepository.java
├── entity/
│   ├── BaseEntity.java            # 공통 (createdAt, updatedAt)
│   ├── User.java
│   ├── Blog.java
│   ├── Post.java
│   ├── Comment.java               # self-reference (대댓글 지원)
│   ├── Tag.java
│   ├── TagMap.java                # Post-Tag 중간 테이블
│   ├── Like.java                  # User-Post 좋아요 (유니크 제약)
│   └── Follow.java                # User-User 팔로우
├── dto/
│   ├── auth/                      # LoginRequest, SignupRequest
│   ├── posts/                     # PostCreatePostRequest, PostUpdatePutRequest
│   │                              # PostGetResponse, PostListGetResponse, PageResponse
│   ├── users/                     # UserGetResponse, UserUpdateRequest 등
│   ├── comments/                  # 댓글 DTO 10개
│   │   ├── CommentCreatePostRequest.java
│   │   ├── CommentUpdatePutRequest.java
│   │   ├── CommentPostResponse.java
│   │   ├── CommentPutResponse.java
│   │   ├── CommentWithRepliesResponse.java
│   │   ├── ReplyCreatePostRequest.java
│   │   ├── ReplyUpdatePutRequest.java
│   │   ├── ReplyPostResponse.java
│   │   ├── ReplyPutResponse.java
│   │   └── ReplyResponse.java
│   ├── like/                      # LikeResponse
│   ├── follows/                   # FollowPostResponse, FollowDeleteResponse
│   ├── tags/                      # TagGetResponse
│   └── common/                    # ApiResponse
└── exception/
    ├── NotFoundException.java     # 404
    ├── ForbiddenException.java    # 403
    ├── DuplicateException.java    # 409
    ├── AuthEntryPoint.java        # 인증 실패 처리
    └── GlobalExceptionHandler.java # 전역 예외 처리
```

## Entity 관계

```
User (1) ─── Blog (1) ─── Post (*) ─── TagMap (*) ─── Tag (*)
  │                         │
  │                         ├── Comment (*) [self-reference 대댓글]
  │                         │
  │                         └── Like (*) ←─ User (*)
  │
  └── Follow (*) [self-reference 팔로워/팔로잉]
```

## 아키텍처 핵심 패턴

### 인증 시스템 (Session-based)

**Security 설정** (`ProjectSecurityConfig.java`):
- CSRF 비활성화
- HttpSessionSecurityContextRepository로 세션 관리
- DaoAuthenticationProvider + AuthService (UserDetailsService 구현)
- PasswordEncoder: DelegatingPasswordEncoder (bcrypt 기본)
- 인증 실패: AuthEntryPoint (401 JSON 응답)

**인증 흐름**:
```
로그인 요청 → AuthController.login()
           → AuthenticationManager.authenticate()
           → AuthService.loadUserByUsername(email)
           → SecurityContextRepository.saveContext() [HttpSession 저장]

API 요청   → SecurityContextRepository [HttpSession에서 Context 복원]
           → @AuthenticationPrincipal UserDetails
           → userDetails.getUsername() = email
```

**중요**:
- `userDetails.getUsername()`은 **email**을 반환 (User.email이 인증 식별자)
- Controller에서 `@AuthenticationPrincipal UserDetails userDetails`로 주입

### Entity 설계 원칙

- **BaseEntity 상속**: `createdAt`, `updatedAt` 자동 관리 (JPA Auditing)
- **@Setter 금지**: 불변성 보장, 명시적 메서드로만 상태 변경
- **정적 팩토리 메서드**: `of()`, `from()`, `ofReply()` 사용

```java
// 예시
Post post = Post.of(title, content, blog);
Comment comment = Comment.of(user, post, content);
Comment reply = Comment.ofReply(user, post, parentComment, content);
Tag tag = Tag.of(tagName);
TagMap tagMap = TagMap.of(post, tag);
Like like = Like.from(user, post);
```

### Service 레이어

- **트랜잭션 전략**: 클래스에 `@Transactional(readOnly=true)`, 쓰기 메서드만 `@Transactional`
- **예외 처리**: 커스텀 예외만 사용
  - `NotFoundException`: 리소스 없음 (404) - 정적 메서드: `post()`, `user()`, `blog()`
  - `ForbiddenException`: 권한 없음 (403) - 정적 메서드: `postUpdate()`, `postDelete()`
  - `DuplicateException`: 중복 리소스 (409) - 정적 메서드: `email()`, `following()`
- **권한 검증**: Service에서 작성자 확인 후 ForbiddenException 발생

```java
// 권한 검증 패턴
if (!post.getBlog().getUser().getEmail().equals(email)) {
    throw ForbiddenException.postUpdate();
}
```

### DTO 구조

**네이밍 규칙**:
- Request: `{Action}{HttpMethod}Request` (예: `CommentCreatePostRequest`)
- Response: `{Resource}{HttpMethod}Response` (예: `CommentPostResponse`)
- 도메인별 하위 패키지 구성: `dto/auth/`, `dto/posts/`, `dto/comments/`, `dto/like/`

**정적 팩토리 메서드**:
- `from(Entity)`: 타입 변환/매핑
- `of(값들)`: 값 조립

**API 응답 래핑**:
```java
// 성공 (데이터 있음)
return ResponseEntity.ok(ApiResponse.success("메시지", data));

// 성공 (데이터 없음)
return ResponseEntity.ok(ApiResponse.success("메시지"));

// 생성 성공 (201)
return ResponseEntity.status(HttpStatus.CREATED)
    .body(ApiResponse.success("생성 성공", data));

// 삭제 성공 (204)
return ResponseEntity.noContent().build();
```

## API 엔드포인트

### 인증 (`/api/v1/auth`)
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/signup` | 회원가입 (블로그 자동생성) | X |
| POST | `/login` | 로그인 (세션 생성) | X |
| POST | `/logout` | 로그아웃 (세션 무효화) | O |

### 게시글 (`/api/v1/posts`)
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/` | 목록 조회 (페이징, 태그/블로그 필터) | X |
| GET | `/{postId}` | 상세 조회 (댓글 포함) | X |
| POST | `/` | 작성 | O |
| PUT | `/{postId}` | 수정 | O (작성자) |
| DELETE | `/{postId}` | 삭제 | O (작성자) |

### 댓글 (`/api/v1/posts/{postId}/comments`)
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/` | 댓글 목록 조회 (대댓글 포함) | X |
| POST | `/` | 댓글 작성 | O |
| PUT | `/{commentId}` | 댓글 수정 | O (작성자) |
| DELETE | `/{commentId}` | 댓글 삭제 | O (작성자) |
| POST | `/{commentId}/replies` | 답글 작성 | O |
| PUT | `/{commentId}/replies/{replyId}` | 답글 수정 | O (작성자) |
| DELETE | `/{commentId}/replies/{replyId}` | 답글 삭제 | O (작성자) |

### 좋아요 (`/api/v1/posts/{postId}/like`)
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/` | 좋아요 정보 조회 (개수 + 현재 사용자 여부) | O |
| POST | `/` | 좋아요 추가 | O |
| DELETE | `/` | 좋아요 삭제 | O |

### 팔로우 (`/api/v1/users/{userId}/follows`)
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/` | 팔로우 | O |
| DELETE | `/` | 언팔로우 | O |

### 사용자 (`/api/v1/tags/users`) ⚠️ 경로 수정 필요
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/{userId}` | 조회 | X |
| PUT | `/{userId}` | 수정 | O |
| DELETE | `/{userId}` | 탈퇴 | O |

### 좋아요 (`/api/v1/posts/{postId}/like`)

### 태그 (`/api/v1/tags`)
| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/v1/posts/{postId}/like` | 좋아요 정보 조회 | O |
| POST | `/api/v1/posts/{postId}/like` | 좋아요 추가 | O |
| DELETE | `/api/v1/posts/{postId}/like` | 좋아요 취소 | O |

**참고**: 댓글은 PostResponse에 포함되어 반환됩니다 (별도 엔드포인트 없음)

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
- `dto/auth/`: 인증 관련 (SignupRequest, LoginRequest, etc.)
- `dto/posts/`: 게시글 관련 (PostGetRequest, PostCreateRequest, PostUpdateRequest, PostListResponse, PostResponse)
- `dto/like/`: 좋아요 관련 (LikeResponse)
- `dto/users/`: 사용자 관련
- `dto/tags/`: 태그 관련
- `dto/common/`: 공통 응답 (ApiResponse, ErrorResponse, PageResponse 등)

## 구현 현황

### 완료
- 회원가입/로그인/로그아웃
- 게시글 CRUD (QueryDSL 동적 쿼리 포함)
- 사용자 CRUD
- 해시태그 (TagMap을 통한 다대다 관계)
- 좋아요 CRUD (LikeController, LikeService)

### 미구현
- 팔로우 (Follow entity만 존재, 기능 미구현)
- 댓글 별도 엔드포인트 (현재 PostResponse에만 포함)
| GET | `/{title}` | 태그명으로 조회 | X |

## 구현 가이드

### 새 엔티티 추가 시
1. `BaseEntity` 상속
2. `@Getter` 사용, `@Setter` 금지
3. 정적 팩토리 메서드로 생성 (`of()`, `from()`)
4. 연관 관계 설정 시 양방향이면 편의 메서드 추가

### 새 API 엔드포인트 추가 시
1. **Controller**: `@AuthenticationPrincipal UserDetails`로 인증 사용자 받기
2. **Service**: 클래스 `@Transactional(readOnly=true)`, 쓰기 메서드만 `@Transactional`
3. **예외 처리**: `NotFoundException`, `ForbiddenException`, `DuplicateException` 사용
4. **권한 검증**: Service에서 작성자 검증 후 `ForbiddenException` 발생
5. **SecurityConfig**: `ProjectSecurityConfig`에 엔드포인트 인증 규칙 추가

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
- **Repository 테스트**: `@DataJpaTest`
- **Service 테스트**: Mockito로 Repository mocking (`@ExtendWith(MockitoExtension.class)`)
- **Controller 테스트**: `@WebMvcTest` + MockMvc + `@WithMockUser`

## 구현 현황

### 완료
- 회원가입/로그인/로그아웃
- 게시글 CRUD (태그 포함, ApiResponse 래핑 완료)
- 사용자 CRUD
- 태그 조회
- 댓글/대댓글 CRUD (self-reference 계층 구조)
- 좋아요 추가/삭제/조회
- 팔로우/언팔로우

### 미구현
- 팔로워/팔로잉 목록 조회 API

## 알려진 이슈 및 TODO

### Critical
- [ ] **LikeService**: `IllegalArgumentException`, `IllegalStateException` → 커스텀 예외로 변경
- [ ] **AuthService/UserService**: `IllegalArgumentException` → 커스텀 예외로 변경
- [ ] **UserController**: 경로 수정 필요 (`/api/v1/tags/users` → `/api/v1/users`)
- [ ] **AuthService**: `IllegalArgumentException` → `DuplicateException.email()` 변경
- [ ] **UserService**: `IllegalArgumentException` → `NotFoundException.user()` 변경
- [ ] **LikeService**: `IllegalArgumentException`, `IllegalStateException` → 커스텀 예외 변경
- [ ] **UserController**: 권한 검증 추가 (본인만 수정/삭제)
- [ ] **User.java**: `BaseEntity` 상속, `@Setter` 제거

### Enhancement
- [ ] **댓글 API**: 별도 엔드포인트 추가 (현재 PostResponse에만 포함)
- [ ] **팔로우 기능**: FollowController, FollowService 구현
- [ ] **CORS 설정**: 프론트엔드 연결 시 `ProjectSecurityConfig`에서 allowedOrigins 등 설정
- [ ] **TagController**: 현재 비어있음, 태그 조회 API 추가 가능
- [ ] **좋아요 토글 API**: 단일 엔드포인트로 POST/DELETE 통합 고려
- [ ] **DDL 운영 모드**: 프로덕션에서 `validate`로 변경 (현재 `update`)
- [ ] **PostGetResponse**: `likeCount`, `isLiked` 필드 추가 (현재 별도 API 호출 필요)
- [ ] **CORS 설정**: 프론트엔드 연결 시 allowedOrigins 등 설정
- [ ] **Hibernate DDL**: 프로덕션 시 `validate`로 변경

## 테스트 현황

| 테스트 클래스 | 설명 | 상태 |
|--------------|------|------|
| PostControllerTest | 게시글 컨트롤러 테스트 | 통과 |
| PostServiceTest | 게시글 서비스 테스트 | 통과 |
| CommentControllerTest | 댓글 컨트롤러 테스트 | 통과 |
| CommentServiceTest | 댓글 서비스 테스트 | 통과 |
| CommentRepositoryTest | 댓글 저장소 테스트 | 일부 실패 (DB 설정) |
| AuthControllerTest | 인증 컨트롤러 테스트 | 일부 실패 |
| UserControllerTest | 사용자 컨트롤러 테스트 | 일부 실패 |
| TagServiceTest | 태그 서비스 테스트 | 실패 (NullPointer) |
| VlogApplicationTests | 애플리케이션 기본 테스트 | 통과 |

```bash
# 테스트 실행
./gradlew test

# Post/Comment 관련 테스트만 실행 (안정적)
./gradlew test --tests "PostControllerTest" --tests "PostServiceTest" --tests "CommentControllerTest" --tests "CommentServiceTest"
```
