# 엔티티 구조 및 연관관계 코드 리뷰

**작성일**: 2025-12-26
**검토 범위**: Entity, Repository, Service 레이어

---

## 1. 분석 결과 요약

### 발견된 문제 심각도별 분류기존 

| 심각도 | 개수 | 설명 |
|--------|------|------|
| CRITICAL | 5개 | 즉시 수정 필요 (보안, 데이터 무결성) |
| MAJOR | 6개 | 이번 Sprint 내 수정 권장 |
| MINOR | 4개 | 개선 권장 |

---

## 2. CRITICAL 문제 (5개)

### 2.1 User.java - @Setter 사용

**위치**: `src/main/java/com/likelion/vlog/entity/User.java:17`

```java
@Getter @Setter  // @Setter 제거 필요
public class User {
```

**문제점**
- 캡슐화 위반, 어디서든 필드 변경 가능
- 데이터 무결성 위험

**해결 방안**
- @Setter 제거
- 비즈니스 메서드로 대체 (`updateInfo()`, `changePassword()` 등)

---

### 2.2 User.java - BaseEntity 미상속

**위치**: `src/main/java/com/likelion/vlog/entity/User.java`

```java
public class User {  // extends BaseEntity 누락
    @CurrentTimestamp  // 다른 엔티티와 불일관
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
```

**문제점**
- 다른 7개 엔티티(Blog, Post, Comment, Tag, TagMap, Like, Follow)는 BaseEntity 상속
- User만 예외적으로 @CurrentTimestamp/@UpdateTimestamp 직접 사용
- 일관성 부재

**해결 방안**
- `extends BaseEntity` 추가
- @CurrentTimestamp → @CreatedDate (BaseEntity에서 상속)
- @UpdateTimestamp → @LastModifiedDate (BaseEntity에서 상속)

---

### 2.3 Blog.java - @Setter 사용

**위치**: `src/main/java/com/likelion/vlog/entity/Blog.java:10`

```java
@Getter @Setter  // User.@PrePersist에서 setter 호출됨
public class Blog extends BaseEntity {
```

**문제점**
- User.java의 @PrePersist에서 `blog.setUser()`, `blog.setTitle()` 호출
- 외부에서 setter를 통한 필드 변경 가능

**해결 방안**
- @Setter 제거
- Blog.createForUser() 팩토리 메서드 추가
- User.@PrePersist에서 팩토리 메서드 사용

---

### 2.4 N+1 문제 - PostService.getTagNames()

**위치**: `src/main/java/com/likelion/vlog/service/PostService.java:149-152`

```java
private List<String> getTagNames(Post post) {
    return post.getTagMapList().stream()
            .map(tagMap -> tagMap.getTag().getTitle())  // N+1 발생!
            .toList();
}
```

**문제점**
- Post를 조회하면 tagMapList는 LAZY 로딩
- tagMapList 순회 시 각 TagMap의 Tag를 개별 쿼리로 조회
- 게시글 10개 조회 시 → 최대 41번 쿼리 발생
  - Post 목록: 1 쿼리
  - TagMap (Post마다): 10 쿼리
  - Tag (TagMap마다): 평균 30 쿼리

**해결 방안**
- PostRepository에 Fetch Join 쿼리 추가:

```java
@Query("SELECT DISTINCT p FROM Post p " +
       "LEFT JOIN FETCH p.tagMapList tm " +
       "LEFT JOIN FETCH tm.tag")
List<Post> findAllWithTags(Pageable pageable);
```

---

### 2.5 Post 삭제 시 Comment/Like 미처리

**위치**: `src/main/java/com/likelion/vlog/service/PostService.java:133-143`

```java
@Transactional
public void deletePost(Long postId, String email) {
    Post post = postRepository.findById(postId)
            .orElseThrow(() -> NotFoundException.post(postId));

    if (!post.getBlog().getUser().getEmail().equals(email)) {
        throw ForbiddenException.postDelete();
    }

    tagMapRepository.deleteAllByPost(post);  // TagMap만 삭제
    postRepository.delete(post);              // Comment, Like는?
}
```

**문제점**
- TagMap은 명시적으로 삭제하지만 Comment, Like는 처리되지 않음
- FK 제약조건 위반 가능 (Comment.post_id, Like.post_id가 삭제된 Post 참조)
- 고아 데이터 발생 가능

**해결 방안**

Option A: 명시적 삭제 (현재 패턴 유지)
```java
likeRepository.deleteAllByPost(post);      // Like 삭제
commentRepository.deleteAllByPost(post);   // Comment 삭제
tagMapRepository.deleteAllByPost(post);    // TagMap 삭제
postRepository.delete(post);               // Post 삭제
```

Option B: Cascade 설정 (권장)
```java
// Post.java
@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Comment> comments = new ArrayList<>();

@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Like> likes = new ArrayList<>();
```

---

## 3. MAJOR 문제 (6개)

### 3.1 Post.java - Comment/Like 역관계 미정의

**현재 상태**
```java
// Post.java
@OneToMany(mappedBy = "post")
private List<TagMap> tagMapList = new ArrayList<>();
// comments, likes 필드 없음
```

**문제점**
- Post에서 Comment, Like 목록 조회 불가
- Cascade 삭제 불가능
- Post 입장에서 연관 데이터 관리 어려움

**해결 방안**
```java
@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Comment> comments = new ArrayList<>();

@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Like> likes = new ArrayList<>();
```

---

### 3.2 Blog.java - Post 역관계 미정의

**현재 상태**
```java
// Blog.java
// posts 필드 없음 (Post → Blog만 단방향)
```

**문제점**
- Blog에서 Post 목록 조회 불가
- User 삭제 → Blog cascade 삭제 시 Post 처리 불명확

**해결 방안**
```java
@OneToMany(mappedBy = "blog", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Post> posts = new ArrayList<>();
```

---

### 3.3 예외 처리 불일관성

**현재 상태**
```java
// AuthService.java
throw new IllegalArgumentException("이미 존재하는 이메일");

// UserService.java
throw new IllegalArgumentException("존재하지 않는 유저입니다.");
throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
```

**문제점**
- 프로젝트 컨벤션: 커스텀 예외 사용 (NotFoundException, ForbiddenException, DuplicateException)
- IllegalArgumentException은 GlobalExceptionHandler에서 400 Bad Request로 처리됨
- 예외 종류에 따른 명확한 HTTP 상태 코드 구분 어려움

**해결 방안**
```java
// AuthService.java
throw DuplicateException.email(dto.getEmail());

// UserService.java
throw NotFoundException.user(userId);
```

---

### 3.4 Like/Follow 중복 제약 미설정

**현재 상태**
```java
// Like.java
@Table(name = "likes")  // UniqueConstraint 없음
public class Like extends BaseEntity {
    @ManyToOne
    private User user;
    @ManyToOne
    private Post post;
}
```

**문제점**
- 같은 User-Post 조합으로 여러 Like 생성 가능
- 좋아요 기능 구현 시 중복 체크 로직 필수

**해결 방안**
```java
@Table(name = "likes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "post_id"})
})
public class Like extends BaseEntity {
```

Follow도 동일하게 적용:
```java
@Table(name = "follows", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"follower_id", "following_id"})
})
```

---

### 3.5 UserService vs UserServiceV2 중복

**현재 상태**
- `UserService.java`: 권한 검증 없음, IllegalArgumentException 사용
- `UserServiceV2.java`: 권한 검증 있음, 커스텀 예외 사용

**문제점**
- 동일 기능을 하는 두 개의 Service 존재
- UserController에서 어떤 것을 사용해야 하는지 혼란
- 코드 중복, 유지보수 어려움

**해결 방안**
- UserService.java 삭제
- UserServiceV2.java → UserService.java로 이름 변경
- 또는 UserController에서 UserServiceV2만 사용

---

### 3.6 User 삭제 시 Post 처리 불명확

**현재 상태**
```java
// User.java
@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
private Blog blog;  // User 삭제 시 Blog cascade 삭제

// Blog.java
// posts 필드 없음 → Post는 어떻게 되는가?
```

**문제점**
- User 삭제 → Blog cascade 삭제
- Blog에 posts 역관계가 없어서 Post cascade 삭제 불가
- Post.blog_id가 NULL 또는 고아 상태 가능

**해결 방안**
1. Blog.java에 posts 역관계 추가 + cascade 설정
2. 또는 UserService.deleteUser()에서 Post 명시적 삭제

---

## 4. MINOR 문제 (4개)

### 4.1 @OneToMany FetchType 미명시

```java
// Post.java
@OneToMany(mappedBy = "post")  // 기본값 LAZY지만 명시 권장
private List<TagMap> tagMapList;

// Comment.java
@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
private List<Comment> children;  // fetch 미명시
```

**권장**
```java
@OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
```

---

### 4.2 Follow 자기참조 방지 없음

```java
// 현재: follower == following 허용됨
Follow.create(user, user);  // 자기 자신을 팔로우 가능
```

**해결 방안**
```java
public static Follow create(User follower, User following) {
    if (follower.equals(following)) {
        throw new IllegalArgumentException("자기 자신을 팔로우할 수 없습니다.");
    }
    // ...
}
```

---

### 4.3 Post.tagMapList cascade 미설정

```java
@OneToMany(mappedBy = "post")  // cascade 없음
private List<TagMap> tagMapList;
```

**현재 처리 방식**
- PostService.deletePost()에서 `tagMapRepository.deleteAllByPost()` 수동 호출

**권장**
```java
@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
private List<TagMap> tagMapList = new ArrayList<>();
```

---

### 4.4 권한 검증 시 N+1

```java
// PostService.java
post.getBlog().getUser().getEmail()  // Blog, User 추가 쿼리
```

**해결 방안**
```java
// PostRepository.java
@Query("SELECT p FROM Post p " +
       "LEFT JOIN FETCH p.blog b " +
       "LEFT JOIN FETCH b.user " +
       "WHERE p.id = :id")
Optional<Post> findByIdWithBlogAndUser(@Param("id") Long id);
```

---

## 5. 수정 대상 파일 요약

| 파일 | 수정 내용 | 심각도 |
|------|----------|--------|
| `entity/User.java` | @Setter 제거, BaseEntity 상속 | CRITICAL |
| `entity/Blog.java` | @Setter 제거, posts 역관계 추가 | CRITICAL/MAJOR |
| `entity/Post.java` | comments, likes 역관계 + cascade | MAJOR |
| `entity/Like.java` | @UniqueConstraint 추가 | MAJOR |
| `entity/Follow.java` | @UniqueConstraint 추가 | MAJOR |
| `repository/PostRepository.java` | Fetch Join 쿼리 추가 | CRITICAL |
| `repository/CommentRepository.java` | deleteAllByPost() 추가 | CRITICAL |
| `repository/LikeRepository.java` | deleteAllByPost() 추가 | CRITICAL |
| `service/PostService.java` | N+1 해결, 삭제 로직 보완 | CRITICAL |
| `service/AuthService.java` | 예외 처리 개선 | MAJOR |
| `service/UserService.java` | 삭제 (UserServiceV2로 통합) | MAJOR |
| `service/UserServiceV2.java` | Post 삭제 로직 추가 | MAJOR |

---

## 6. 예상 쿼리 개선 효과

| 작업 | Before | After | 개선율 |
|------|--------|-------|--------|
| 게시글 목록 (10개) | 41 쿼리 | 1 쿼리 | 97% 감소 |
| 게시글 수정 | 3 쿼리 | 1 쿼리 | 67% 감소 |
| 게시글 삭제 | 3 쿼리 | 1 쿼리 | 67% 감소 |

---

## 7. 엔티티 관계도

### 현재 상태

```
User (1) ─────── (1) Blog
  │                   │
  │                   └── Post (역관계 없음)
  │
  ├── Comment ←── Post (역관계 없음)
  │
  ├── Like ←── Post (역관계 없음)
  │
  ├── Follow.follower (역관계 없음)
  │
  └── Follow.following (역관계 없음)
```

### 개선 후

```
User (1) ─────── (1) Blog (1) ─────── (*) Post
  │                                      │
  │                                      ├── (*) Comment (cascade)
  │                                      │
  │                                      ├── (*) Like (cascade)
  │                                      │
  │                                      └── (*) TagMap (cascade)
  │
  ├── (*) Comment
  │
  ├── (*) Like
  │
  └── (*) Follow (follower/following)
```

---

## 8. 주의사항

### 8.1 DB 스키마 변경

- @UniqueConstraint 추가 시 기존 중복 데이터 확인 필요
- 개발 환경: `ddl-auto: update`로 자동 반영
- 운영 환경: Flyway/Liquibase 마이그레이션 필요

### 8.2 Cascade 삭제 주의

- Post 삭제 시 Comment, Like, TagMap 함께 삭제됨
- 의도치 않은 데이터 삭제 방지를 위해 충분한 테스트 필요

### 8.3 테스트 코드 영향

- User 생성 시 @Setter 제거로 ReflectionTestUtils 사용 부분 확인
- Blog 자동 생성 로직 변경 시 관련 테스트 수정 필요

---

## 9. 우선순위별 수정 체크리스트

### 즉시 수정 (CRITICAL)
- [ ] User.java @Setter 제거
- [ ] User.java BaseEntity 상속
- [ ] Blog.java @Setter 제거
- [ ] PostRepository Fetch Join 쿼리 추가
- [ ] PostService N+1 문제 해결
- [ ] Post 삭제 시 Comment/Like 처리

### 이번 Sprint (MAJOR)
- [ ] Post.java comments/likes 역관계 추가
- [ ] Blog.java posts 역관계 추가
- [ ] AuthService 예외 처리 개선
- [ ] Like/Follow @UniqueConstraint 추가
- [ ] UserService 통합

### 개선 권장 (MINOR)
- [ ] @OneToMany FetchType 명시
- [ ] Follow 자기참조 방지
- [ ] Post.tagMapList cascade 설정
