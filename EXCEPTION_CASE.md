# 커스텀 에러별 예외 처리 케이스

이 문서는 v-log 프로젝트의 백엔드 커스텀 예외와 프론트엔드 에러 처리 방안을 정리합니다.

## 목차
1. [NotFoundException (404)](#1-notfoundexception-404-not-found)
2. [ForbiddenException (403)](#2-forbiddenexception-403-forbidden)
3. [BadCredentialsException (401)](#3-badcredentialsexception-401-unauthorized)
4. [DuplicateException (409)](#4-duplicateexception-409-conflict)
5. [IllegalArgumentException / Validation (400)](#5-illegalargumentexception--validation-400-bad-request)
6. [Exception (500)](#6-exception-500-internal-server-error)
7. [검색 결과 없음](#7-검색-결과-없음-일반-케이스)
8. [통합 에러 처리 가이드](#통합-에러-처리-가이드)

---

## 1. NotFoundException (404 Not Found)

**설명**: 요청한 리소스를 찾을 수 없을 때 발생하는 예외

**백엔드 Exception**: `com.likelion.vlog.exception.NotFoundException`

### 발생 상황

| 페이지 | 상황 | 메시지 | 프론트엔드 해결 방안 |
|--------|------|--------|---------------------|
| 메인 페이지 | 삭제된 게시글 클릭 | 게시글을 찾을 수 없습니다. | 팝업창 출력 후 메인 페이지로 이동 |
| 모든 페이지 | 존재하지 않는 사용자 조회 | 사용자를 찾을 수 없습니다. | 에러 메시지 팝업 표시 |
| 모든 페이지 | 존재하지 않는 블로그 접근 | 블로그를 찾을 수 없습니다. | 에러 메시지 팝업 표시 |
| 팔로우 기능 | 존재하지 않는 팔로우 관계 | 팔로우를 찾을 수 없습니다. | 에러 메시지 팝업 표시 |

### 프론트엔드 처리 패턴

```javascript
// HTTP 404 에러 처리
if (error.response?.status === 404) {
  alert(error.response.data.message);
  navigate('/');  // 메인 페이지로 리다이렉트
}
```

---

## 2. ForbiddenException (403 Forbidden)

**설명**: 리소스에 대한 접근 권한이 없을 때 발생하는 예외

**백엔드 Exception**: `com.likelion.vlog.exception.ForbiddenException`

### 발생 상황

| 페이지 | 상황 | 메시지 | 프론트엔드 해결 방안 |
|--------|------|--------|---------------------|
| 게시글 수정 | 타인의 게시글 수정 시도 | 게시글 수정 권한이 없습니다. | 팝업창 출력 후 게시글 상세 페이지로 이동 |
| 게시글 삭제 | 타인의 게시글 삭제 시도 | 게시글 삭제 권한이 없습니다. | 팝업창 출력 후 게시글 상세 페이지로 이동 |
| 회원 정보 수정 | 타인의 정보 수정 시도 | 사용자 정보 수정 권한이 없습니다. | 팝업창 출력 후 메인 페이지로 이동 |
| 회원 탈퇴 | 타인의 계정 탈퇴 시도 | 회원 탈퇴 권한이 없습니다. | 팝업창 출력 후 메인 페이지로 이동 |

### 프론트엔드 처리 패턴

```javascript
// HTTP 403 에러 처리
if (error.response?.status === 403) {
  alert(error.response.data.message);
  navigate(-1);  // 이전 페이지로 돌아가기
}
```

---

## 3. BadCredentialsException (401 Unauthorized)

**설명**: 인증에 실패했을 때 발생하는 예외 (로그인 실패, 비회원 접근)

**백엔드 Exception**: `org.springframework.security.authentication.BadCredentialsException` (Spring Security)

### 발생 상황

| 페이지 | 상황 | 메시지 | 프론트엔드 해결 방안 |
|--------|------|--------|---------------------|
| 로그인 페이지 | 이메일/비밀번호 불일치 | 이메일 또는 비밀번호가 일치하지 않습니다. | 팝업 출력 후 로그인 페이지에 머물기 |
| 회원 상세 정보 | 비회원 팔로우 요청 | 로그인 후 이용하세요. | 팝업 출력 후 로그인 페이지로 이동 |
| 게시글 상세 | 비회원 좋아요 요청 | 로그인 후 이용하세요. | 팝업 출력 후 로그인 페이지로 이동 |
| 게시글 상세 | 비회원 댓글/답글 작성 | 로그인 후 이용하세요. | 팝업 출력 후 로그인 페이지로 이동 |
| 회원 탈퇴 | 비밀번호 불일치 | 비밀번호가 일치하지 않습니다. | 입력 필드 아래 빨간 글씨로 표시 |

### 프론트엔드 처리 패턴

```javascript
// HTTP 401 에러 처리
if (error.response?.status === 401) {
  const message = error.response.data.message;
  alert(message);

  if (message.includes('로그인')) {
    navigate('/auth/login');  // 로그인 페이지로 리다이렉트
  } else {
    // 비밀번호 불일치는 현재 페이지에서 에러 표시
    setPasswordError(message);
  }
}
```

---

## 4. DuplicateException (409 Conflict)

**설명**: 중복된 데이터가 이미 존재할 때 발생하는 예외

**백엔드 Exception**: `com.likelion.vlog.exception.DuplicateException`

### 발생 상황

| 페이지 | 상황 | 메시지 | 프론트엔드 해결 방안 |
|--------|------|--------|---------------------|
| 회원가입 | 이메일 중복 | 이미 존재하는 이메일입니다. | 입력 필드 아래 빨간 글씨로 메시지 출력 |
| 회원가입 | 닉네임 중복 | 이미 존재하는 닉네임입니다. | 입력 필드 아래 빨간 글씨로 메시지 출력 |
| 팔로우 | 이미 팔로우 중 | 이미 팔로우 중입니다. | 팝업 출력 (선택사항) |

### 프론트엔드 처리 패턴

```javascript
// HTTP 409 에러 처리
if (error.response?.status === 409) {
  const message = error.response.data.message;

  // 필드 아래 에러 메시지 표시 (빨간 글씨)
  if (message.includes('이메일')) {
    setEmailError(message);
  } else if (message.includes('닉네임')) {
    setNicknameError(message);
  } else {
    alert(message);
  }
}
```

**CSS 스타일 예시**:
```css
.error-message {
  color: red;
  font-size: 0.875rem;
  margin-top: 0.25rem;
}
```

---

## 5. IllegalArgumentException / Validation (400 Bad Request)

**설명**: 잘못된 요청 파라미터 또는 Validation 실패

**백엔드 Exception**:
- `IllegalArgumentException`
- `MethodArgumentNotValidException` (Spring Validation)

### 발생 상황

| 페이지 | 상황 | 메시지 예시 | 프론트엔드 해결 방안 |
|--------|------|------------|---------------------|
| 모든 폼 | 필수 필드 누락 | {필드명}: 필수 항목입니다. | 필드 아래 빨간 글씨로 표시 |
| 회원가입 | 이메일 형식 오류 | email: 올바른 이메일 형식이 아닙니다. | 필드 아래 빨간 글씨로 표시 |
| 모든 폼 | 잘못된 데이터 형식 | {필드명}: {검증 실패 메시지} | 필드 아래 빨간 글씨로 표시 |
| 게시글 작성 | 제목/내용 누락 | title: 필수 항목입니다. | 필드 아래 빨간 글씨로 표시 |

### 프론트엔드 처리 패턴

```javascript
// HTTP 400 에러 처리
if (error.response?.status === 400) {
  const message = error.response.data.message;

  // Validation 에러 메시지 파싱하여 해당 필드에 표시
  if (message.includes(':')) {
    const [field, errorMsg] = message.split(':').map(s => s.trim());
    setFieldError(field, errorMsg);
  } else {
    alert(message);
  }
}
```

---

## 6. Exception (500 Internal Server Error)

**설명**: 예상치 못한 서버 내부 오류

**백엔드 Exception**: `Exception` (모든 예상치 못한 예외)

### 발생 상황

| 페이지 | 상황 | 메시지 | 프론트엔드 해결 방안 |
|--------|------|--------|---------------------|
| 모든 페이지 | 서버 내부 오류 | 서버 오류가 발생했습니다. | 에러 페이지 출력 또는 홈 버튼으로 메인 이동 |
| 모든 페이지 | 데이터 무결성 오류 | 데이터 처리 중 오류가 발생했습니다. | 팝업 출력 후 메인 페이지로 이동 |
| 모든 페이지 | 예상치 못한 예외 | 서버 오류가 발생했습니다. | 팝업 출력 후 메인 페이지로 이동 |

### 프론트엔드 처리 패턴

```javascript
// HTTP 500 에러 처리
if (error.response?.status === 500) {
  alert(error.response.data.message);
  navigate('/');  // 메인 페이지로 이동

  // 또는 에러 페이지로 이동
  // navigate('/error', { state: { message: error.response.data.message } });
}
```

---

## 7. 검색 결과 없음 (일반 케이스)

**설명**: 검색 조건에 맞는 결과가 없을 때 (HTTP 에러 아님, 빈 배열 반환)

### 발생 상황

| 페이지 | 상황 | 메시지 | 프론트엔드 해결 방안 |
|--------|------|--------|---------------------|
| 메인 페이지 | 제목 검색 결과 없음 | 검색 결과가 없습니다. | 화면에 메시지 출력 |
| 메인 페이지 | 태그 검색 결과 없음 | 검색 결과가 없습니다. | 화면에 메시지 출력 |
| 메인 페이지 | 작성자 검색 결과 없음 | 검색 결과가 없습니다. | 화면에 메시지 출력 |

### 프론트엔드 처리 패턴

```javascript
// 검색 결과가 빈 배열일 때
if (response.data.content?.length === 0 || response.data.length === 0) {
  displayEmptyState('검색 결과가 없습니다.');
}
```

**Empty State UI 예시**:
```jsx
function EmptyState({ message }) {
  return (
    <div className="empty-state">
      <p>{message}</p>
    </div>
  );
}
```

---

## 통합 에러 처리 가이드

### 공통 에러 응답 형식

백엔드의 `GlobalExceptionHandler`는 모든 예외를 다음 형식으로 반환합니다:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "게시글을 찾을 수 없습니다.",
  "timestamp": "2024-01-15T10:30:00"
}
```

### 프론트엔드 통합 에러 핸들러 예시

Axios interceptor를 사용한 통합 에러 처리:

```javascript
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

// Axios 인스턴스 생성
const apiClient = axios.create({
  baseURL: '/api/v1',
  withCredentials: true
});

// 응답 인터셉터 설정
apiClient.interceptors.response.use(
  response => response,
  error => {
    const status = error.response?.status;
    const message = error.response?.data?.message || '오류가 발생했습니다.';

    handleApiError(status, message);
    return Promise.reject(error);
  }
);

// 에러 핸들러 함수
function handleApiError(status, message) {
  switch(status) {
    case 400:
      // Validation 에러 처리
      if (message.includes(':')) {
        const [field, errorMsg] = message.split(':').map(s => s.trim());
        showFieldError(field, errorMsg);
      } else {
        alert(message);
      }
      break;

    case 401:
      // 인증 실패 처리
      alert(message);
      if (message.includes('로그인')) {
        window.location.href = '/auth/login';
      }
      break;

    case 403:
      // 권한 없음 처리
      alert(message);
      window.history.back();
      break;

    case 404:
      // 리소스 없음 처리
      alert(message);
      window.location.href = '/';
      break;

    case 409:
      // 중복 데이터 처리
      showFieldError(null, message);
      break;

    case 500:
      // 서버 에러 처리
      alert(message);
      window.location.href = '/';
      break;

    default:
      alert('오류가 발생했습니다.');
  }
}

// 필드 에러 표시 헬퍼 함수
function showFieldError(field, message) {
  // 구체적인 필드가 있으면 해당 필드에 표시
  if (field) {
    const errorElement = document.getElementById(`${field}-error`);
    if (errorElement) {
      errorElement.textContent = message;
      errorElement.style.color = 'red';
      errorElement.style.display = 'block';
    }
  } else {
    // 필드를 특정할 수 없으면 메시지에서 추론
    if (message.includes('이메일')) {
      showFieldError('email', message);
    } else if (message.includes('닉네임')) {
      showFieldError('nickname', message);
    } else {
      alert(message);
    }
  }
}

export default apiClient;
```

### React Hook 예시

React에서 사용할 수 있는 에러 처리 Hook:

```javascript
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

export function useApiError() {
  const navigate = useNavigate();
  const [fieldErrors, setFieldErrors] = useState({});

  const handleError = (error) => {
    const status = error.response?.status;
    const message = error.response?.data?.message || '오류가 발생했습니다.';

    switch(status) {
      case 400:
        if (message.includes(':')) {
          const [field, errorMsg] = message.split(':').map(s => s.trim());
          setFieldErrors(prev => ({ ...prev, [field]: errorMsg }));
        } else {
          alert(message);
        }
        break;

      case 401:
        alert(message);
        if (message.includes('로그인')) {
          navigate('/auth/login');
        }
        break;

      case 403:
        alert(message);
        navigate(-1);
        break;

      case 404:
        alert(message);
        navigate('/');
        break;

      case 409:
        if (message.includes('이메일')) {
          setFieldErrors(prev => ({ ...prev, email: message }));
        } else if (message.includes('닉네임')) {
          setFieldErrors(prev => ({ ...prev, nickname: message }));
        } else {
          alert(message);
        }
        break;

      case 500:
        alert(message);
        navigate('/');
        break;

      default:
        alert('오류가 발생했습니다.');
    }
  };

  const clearFieldError = (field) => {
    setFieldErrors(prev => {
      const newErrors = { ...prev };
      delete newErrors[field];
      return newErrors;
    });
  };

  return { fieldErrors, handleError, clearFieldError };
}
```

### 사용 예시

```javascript
function SignupForm() {
  const { fieldErrors, handleError, clearFieldError } = useApiError();
  const [formData, setFormData] = useState({ email: '', nickname: '', password: '' });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await apiClient.post('/auth/signup', formData);
      navigate('/auth/login');
    } catch (error) {
      handleError(error);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <div>
        <input
          type="email"
          value={formData.email}
          onChange={(e) => {
            setFormData({ ...formData, email: e.target.value });
            clearFieldError('email');
          }}
        />
        {fieldErrors.email && (
          <span className="error-message">{fieldErrors.email}</span>
        )}
      </div>
      {/* 닉네임, 비밀번호 필드도 동일한 패턴 */}
      <button type="submit">회원가입</button>
    </form>
  );
}
```

---

## 요약

### 에러 타입별 처리 방식

| HTTP 상태 | 예외 타입 | 프론트엔드 처리 방식 |
|-----------|----------|-------------------|
| 400 | IllegalArgumentException, Validation | 필드 아래 빨간 글씨로 에러 표시 |
| 401 | BadCredentialsException | 팝업 → 로그인 페이지 이동 (또는 현재 페이지) |
| 403 | ForbiddenException | 팝업 → 이전 페이지로 돌아가기 |
| 404 | NotFoundException | 팝업 → 메인 페이지로 이동 |
| 409 | DuplicateException | 필드 아래 빨간 글씨로 에러 표시 |
| 500 | Exception | 팝업 → 메인 페이지로 이동 |
| - | 검색 결과 없음 | Empty State UI 표시 |

### UI 패턴 요약

1. **팝업 (alert)**: 404, 403, 401 (로그인 필요), 500
2. **필드 인라인 에러**: 400 (Validation), 409 (중복)
3. **Empty State UI**: 검색 결과 없음
4. **에러 페이지**: 500 (선택사항)
