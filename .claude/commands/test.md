---
description: 테스트 코드 생성 (프로젝트 규칙 준수)
---

# 테스트 코드 작성 요청

사용자가 지정한 클래스나 메서드에 대해 **프로젝트 테스트 규칙**을 엄격히 준수하여 테스트 코드를 작성합니다.

## 필수 준수 규칙

### 1. Controller 테스트
- **@WebMvcTest** 기반
- **MockMvc** 사용
- **JSON Path 검증** 포함
- HTTP 상태 코드 및 응답 본문 검증

### 2. Service 테스트
- **Mockito** 기반 (@Mock, @InjectMocks)
- **ArgumentCaptor**로 전달된 인자 검증
- **ReflectionTestUtils**로 Entity ID 세팅
- **Validator 호출 검증** 필수
- **Repository 호출 검증** 필수

### 3. 테스트 구조
- **Given / When / Then** 구조 엄격히 유지
- 각 섹션 주석으로 명확히 구분

### 4. 성공/실패 케이스
- **성공 케이스**: 정상 시나리오
- **실패 케이스**:
  - BusinessException의 **ResultCode** 검증
  - BusinessException의 **message** 검증

### 5. 필수 검증
- Validator 호출 여부
- Repository 호출 횟수
- ArgumentCaptor로 저장 데이터 검증
- 예외의 ResultCode/message 확인

### 6. MDC 사용 시
```java
@AfterEach
void tearDown() {
    MDC.clear();
}
```

### 7. AssertJ 사용
- `assertThat()` 필수
- 명확한 검증 메시지

### 8. @DisplayName 필수
- 한글로 명확한 의도 작성
- "~할 때 ~하면 ~한다" 형식

### 9. JUnit 5
- @ExtendWith(MockitoExtension.class)
- @Test, @BeforeEach, @AfterEach
- assertThrows, assertAll 활용

### 10. 테스트 격리
- 각 테스트는 독립적
- 다른 테스트에 영향 없음
- 반복 실행 시 동일한 결과

---

## 작업 순서

1. 사용자가 테스트할 클래스/메서드를 지정하면 해당 파일을 읽습니다
2. 의존성 파악 (Service, Repository, Validator 등)
3. 성공 케이스 테스트 작성
4. 실패 케이스 테스트 작성 (예외 시나리오별)
5. 모든 검증 항목 포함 확인
6. 테스트 파일 생성 또는 업데이트

---

## 예시

**사용자 입력**: "OrderService의 createOrder 메서드에 대한 테스트 작성해줘"

**작성할 테스트**:
- ✅ 주문 생성 성공 (정상 케이스)
- ✅ 재고 부족 시 예외 발생
- ✅ 상품을 찾을 수 없을 때 예외 발생
- ✅ 가격 불일치 시 예외 발생
- ✅ 삭제된 상품일 때 예외 발생

각 테스트마다:
- Repository 호출 검증
- ArgumentCaptor로 저장 데이터 검증
- 예외 발생 시 ResultCode/message 검증

---

이제 테스트하고 싶은 클래스나 메서드를 알려주세요!