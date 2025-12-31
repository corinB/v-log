# 작업 완료 시 체크리스트

## 필수 단계
1. **빌드 확인**: `./gradlew build`
   - 컴파일 오류 없는지 확인
   
2. **코드 스타일 검증**:
   - Entity: BaseEntity 상속, @Setter 제거, 정적 팩토리 메서드 사용
   - Service: 트랜잭션 전략 준수, 커스텀 예외 사용
   - Controller: @AuthenticationPrincipal 사용, 권한 검증
   
3. **예외 처리 확인**:
   - 모든 예외는 커스텀 예외의 정적 팩토리 메서드 사용
   - GlobalExceptionHandler에서 처리되는지 확인
   
4. **보안 검증**:
   - 권한 검증 로직 (본인 확인)
   - SQL Injection 방지 (QueryDSL/JPA 사용)
   - XSS 방지 (입력 검증)

5. **Git 커밋 전**:
   - 민감 정보 제거 (비밀번호, API 키 등)
   - 불필요한 주석/로그 제거
