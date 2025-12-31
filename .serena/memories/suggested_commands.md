# 개발 명령어

## 빌드 및 실행
```bash
./gradlew build                    # 전체 빌드
./gradlew bootRun                  # 애플리케이션 실행
./gradlew test                     # 전체 테스트 실행
./gradlew test --tests ClassName  # 특정 테스트 클래스 실행
./gradlew clean build              # 클린 빌드
```

## 데이터베이스
```bash
docker-compose up -d               # MySQL 시작 (port 13306)
docker-compose down                # MySQL 중지
```

## Windows 시스템 유틸리티
- `dir` - 파일 목록 조회
- `cd` - 디렉토리 이동
- `type` - 파일 내용 보기
- Git Bash 사용 권장 (ls, grep, find 등 Unix 명령어)
