# CLAUDE.md

## 빌드 및 실행

```bash
./gradlew build        # 빌드
./gradlew bootRun      # 실행
docker-compose up -d   # MySQL (port 13306)
```

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

## 코딩 컨벤션

- **Entity**: `BaseEntity` 상속, `@Setter` 금지, 정적 팩토리 메서드 사용
- **Service**: 커스텀 예외만 사용 (`NotFoundException`, `ForbiddenException`)
- **Controller**: `@AuthenticationPrincipal UserDetails`, `userDetails.getUsername()` = email
- **Transaction**: 클래스에 `@Transactional(readOnly=true)`, 쓰기 메서드만 `@Transactional`

## 구현 현황

### 완료
- 회원가입/로그인/로그아웃, 게시글 CRUD, 댓글 CRUD, 사용자 CRUD, 해시태그

### 미구현 (Sprint 2)
- 좋아요, 팔로우 (Entity만 존재)

## TODO (Critical)

- [ ] AuthService/UserService: `IllegalArgumentException` → 커스텀 예외
- [ ] UserController: 권한 검증 추가 (본인만 수정/삭제)
- [ ] User.java: `BaseEntity` 상속, `@Setter` 제거
- [ ] UserService vs UserServiceV2 중복 정리

## 테스트 현황

총 85개 테스트 (12개 파일)

```bash
./gradlew test  # 전체 테스트 실행
```