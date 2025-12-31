# v-log 프로젝트 개요

## 목적
블로그 플랫폼 백엔드 API 서버 (멋쟁이사자처럼 프로젝트)
- 사용자 인증/권한 관리
- 게시글, 댓글, 좋아요, 태그 기능
- 팔로우 기능 (Entity만 존재, 미구현)

## 기술 스택
- **Framework**: Spring Boot 3.5.9
- **Language**: Java 21
- **Database**: MySQL (로컬 port 3306)
- **ORM**: JPA + QueryDSL
- **Security**: Spring Security (세션 기반 인증)
- **Build**: Gradle

## 주요 특징
- 세션 기반 인증 (JWT 아님)
- QueryDSL 동적 쿼리 지원
- 커스텀 예외 처리 (GlobalExceptionHandler)
- BaseEntity 상속 패턴 (createdAt, updatedAt 자동 관리)
