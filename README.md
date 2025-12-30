# V-Log

Spring Boot 기반 블로그 플랫폼 REST API 서버

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-green)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)

## 기술 스택

| 분류 | 기술 |
|------|------|
| Backend | Spring Boot 3.5.9, Java 21 |
| Database | MySQL 8.0 |
| ORM | Spring Data JPA |
| Security | Spring Security (세션 기반 인증) |
| Build | Gradle |
| Test | JUnit 5, Mockito |

## 주요 기능

- **회원 관리**: 회원가입, 로그인/로그아웃, 프로필 수정
- **게시글**: CRUD, 해시태그, 페이징/필터링
- **댓글/대댓글**: 계층형 댓글 (1-depth)
- **좋아요**: 게시글 좋아요/취소
- **팔로우**: 사용자 팔로우/언팔로우 (예정)

## 시작하기

### 사전 요구사항

- Java 21
- MySQL 8.0 (또는 Docker)

### 설치 및 실행

```bash
# 1. 저장소 클론
git clone https://github.com/your-username/v-log.git
cd v-log

# 2. 데이터베이스 설정 (택1)

# Option A: 로컬 MySQL 사용
# MySQL에서 'vlog' 데이터베이스 생성 후 application.yaml 설정 확인

# Option B: Docker 사용
docker-compose up -d

# 3. 빌드 및 실행
./gradlew build
./gradlew bootRun
```

### 환경 설정

`application.yaml` 기본 설정:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/vlog
    username: root
    password: 1111
```

## 프로젝트 구조

```
com.likelion.vlog
├── config/              # Security 설정
├── controller/          # REST API 컨트롤러
├── service/             # 비즈니스 로직
├── repository/          # 데이터 접근 계층
├── entity/              # JPA 엔티티
├── dto/                 # 요청/응답 DTO
│   ├── auth/
│   ├── posts/
│   ├── comments/
│   ├── like/
│   └── common/          # ApiResponse
└── exception/           # 커스텀 예외
```

### Entity 관계

```
User (1) ─── Blog (1) ─── Post (*) ─── TagMap (*) ─── Tag (*)
  │                         │
  │                         ├── Comment (*) [self-reference]
  │                         │
  │                         └── Like (*) ←─ User (*)
  │
  └── Follow (*) [self-reference]
```

## API 엔드포인트

### 인증

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/v1/auth/signup` | 회원가입 | X |
| POST | `/api/v1/auth/login` | 로그인 | X |
| POST | `/api/v1/auth/logout` | 로그아웃 | O |

### 게시글

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/v1/posts` | 목록 조회 | X |
| GET | `/api/v1/posts/{id}` | 상세 조회 | X |
| POST | `/api/v1/posts` | 작성 | O |
| PUT | `/api/v1/posts/{id}` | 수정 | O (작성자) |
| DELETE | `/api/v1/posts/{id}` | 삭제 | O (작성자) |

### 댓글

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/v1/posts/{postId}/comments` | 목록 조회 | X |
| POST | `/api/v1/posts/{postId}/comments` | 작성 | O |
| PUT | `/api/v1/posts/{postId}/comments/{id}` | 수정 | O (작성자) |
| DELETE | `/api/v1/posts/{postId}/comments/{id}` | 삭제 | O (작성자) |
| POST | `/api/v1/posts/{postId}/comments/{id}/replies` | 답글 작성 | O |
| PUT | `/api/v1/posts/{postId}/comments/{id}/replies/{replyId}` | 답글 수정 | O (작성자) |
| DELETE | `/api/v1/posts/{postId}/comments/{id}/replies/{replyId}` | 답글 삭제 | O (작성자) |

### 좋아요

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/v1/posts/{postId}/like` | 조회 | O |
| POST | `/api/v1/posts/{postId}/like` | 좋아요 | O |
| DELETE | `/api/v1/posts/{postId}/like` | 취소 | O |

> 상세 API 문서: [docs/API.md](docs/API.md)

## 개발 가이드

### 코딩 컨벤션

**Entity**
- `BaseEntity` 상속 (createdAt, updatedAt 자동 관리)
- `@Setter` 금지, 정적 팩토리 메서드 사용

```java
Post post = Post.create(title, content, blog);
Comment reply = Comment.createReply(user, post, parent, content);
```

**Service**
- 클래스: `@Transactional(readOnly = true)`
- 쓰기 메서드만: `@Transactional`
- 커스텀 예외 사용: `NotFoundException`, `ForbiddenException`, `DuplicateException`

**DTO**
- Request: `{Action}{HttpMethod}Request` (예: `CommentCreatePostRequest`)
- Response: `{Resource}{HttpMethod}Response` (예: `PostGetResponse`)

**API 응답**
```java
// 성공
return ResponseEntity.ok(ApiResponse.success("메시지", data));

// 생성
return ResponseEntity.status(HttpStatus.CREATED)
    .body(ApiResponse.success("생성 성공", data));

// 삭제
return ResponseEntity.noContent().build();
```

> 상세 컨벤션: [docs/v-log-dto-convention.md](docs/v-log-dto-convention.md)

### 테스트 실행

```bash
# 전체 테스트
./gradlew test

# 특정 테스트 클래스
./gradlew test --tests PostControllerTest

# 테스트 리포트
open build/reports/tests/test/index.html
```

## 라이선스

MIT License
