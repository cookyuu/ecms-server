# ECMS Server Project Context

## 프로젝트 개요
E-Commerce Management System (ECMS) - Spring Boot 기반 이커머스 백엔드 시스템

## 기술 스택
- **Framework**: Spring Boot 3.x, Java 17+
- **Database**: PostgreSQL, Redis
- **ORM**: JPA/Hibernate, QueryDSL
- **Concurrency**: Pessimistic Lock, Optimistic Lock (@Version), Distributed Lock (Redisson)
- **Cache**: Redis Cluster
- **Build Tool**: Gradle
- **Test**: JUnit 5, Mockito, AssertJ

## 도메인 구조
- **Member**: 회원 관리
- **Auth**: 인증/인가
- **Product**: 상품 관리 (Seller 포함)
- **Order**: 주문 관리
- **Payment**: 결제 처리
- **Coupon**: 쿠폰 발급/관리 (Facade 패턴 적용)
- **Cart**: 장바구니
- **Shipment**: 배송 관리
- **Alert**: 알림

## 아키텍처 특징
- **동시성 제어**: 비관적 락(상품 재고), 낙관적 락(@Version), 분산 락(쿠폰 발급)
- **트랜잭션**: @Transactional 기반 관리
- **Facade 패턴**: Coupon 도메인에 적용 (복잡한 동시성 제어)
- **N+1 방지**: Bulk fetch (findByIdInWithLock 등)

## 코딩 컨벤션
- **매직 넘버**: 상수로 추출 (ORDER_NUMBER_EXPIRATION_SECONDS 등)
- **로깅**: Slf4j 사용, 주요 비즈니스 로직 로깅
- **예외 처리**: BusinessException + ResultCode
- **네이밍**: 명확한 도메인 용어 사용

---

# 테스트 코드 작성 규칙

## 필수 규칙

### 1. Controller 테스트
- **@WebMvcTest** 기반 테스트
- **MockMvc** 사용
- **JSON Path 검증** 포함
- HTTP 상태 코드, 응답 본문 검증

```java
@WebMvcTest(OrderController.class)
class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    @DisplayName("주문 생성 성공")
    void createOrder_Success() throws Exception {
        // given
        CreateOrderDto.Request request = ...;
        CreateOrderDto.Response response = ...;
        given(orderService.createOrder(anyLong(), any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value("ORD123"));

        verify(orderService, times(1)).createOrder(anyLong(), any());
    }
}
```

### 2. Service 테스트
- **Mockito** 기반 단위 테스트
- **@Mock**, **@InjectMocks** 사용
- **ArgumentCaptor** 사용하여 전달된 인자 검증
- **ReflectionTestUtils** 사용하여 Entity ID 세팅
- **Validator 호출 검증** 필수
- **Repository 호출 검증** 필수

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductService productService;
    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("주문 생성 시 재고가 충분하면 성공")
    void createOrder_WithSufficientStock_Success() {
        // given
        Member buyer = Member.builder().email("test@test.com").build();
        ReflectionTestUtils.setField(buyer, "id", 1L);

        Product product = Product.of("상품명", "설명", 10000, 100, category, seller);
        ReflectionTestUtils.setField(product, "id", 1L);

        given(memberService.findMemberById(1L)).willReturn(buyer);
        given(productService.findProductByIdWithLock(1L)).willReturn(product);
        given(orderRepository.save(any(Order.class))).willReturn(order);

        // when
        CreateOrderDto.Response result = orderService.createOrder(1L, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOrderNumber()).isNotBlank();

        // ArgumentCaptor로 검증
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getBuyer()).isEqualTo(buyer);

        // 호출 검증
        verify(memberService, times(1)).findMemberById(1L);
        verify(productService, times(1)).findProductByIdWithLock(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
    }
}
```

### 3. 테스트 구조
- **Given / When / Then** 구조 엄격히 유지
- 각 섹션을 주석으로 명확히 구분

```java
@Test
@DisplayName("명확한 테스트 설명")
void testMethod() {
    // given
    // 테스트 데이터 준비

    // when
    // 실제 테스트 실행

    // then
    // 결과 검증
}
```

### 4. 성공/실패 케이스
- **성공 케이스**: 정상적인 시나리오
- **실패 케이스**:
  - 예외 발생 검증
  - **BusinessException**의 **ResultCode**, **message** 검증 필수

```java
@Test
@DisplayName("재고 부족 시 주문 생성 실패")
void createOrder_WithInsufficientStock_ThrowsException() {
    // given
    Product product = Product.of("상품명", "설명", 10000, 5, category, seller);
    CreateOrderItemInfo item = new CreateOrderItemInfo(1L, 10, 10000); // 재고보다 많은 수량

    given(productService.findProductByIdWithLock(1L)).willReturn(product);

    // when & then
    BusinessException exception = assertThrows(BusinessException.class, () -> {
        orderService.createOrder(1L, request);
    });

    assertThat(exception.getResultCode()).isEqualTo(ResultCode.PRODUCT_SOLD_OUT);
    assertThat(exception.getMessage()).contains("재고가 부족합니다");

    verify(productService, times(1)).findProductByIdWithLock(1L);
    verify(orderRepository, never()).save(any());
}
```

### 5. 필수 검증 항목
- **Validator 호출 여부**: `verify(validator).validate(...)`
- **Repository 호출 횟수**: `verify(repository, times(n))...`
- **ArgumentCaptor**: 저장된 엔티티의 필드값 검증
- **예외 검증**: ResultCode, message 확인

### 6. MDC 사용 시 테스트 격리
```java
@AfterEach
void tearDown() {
    MDC.clear();
}
```

### 7. AssertJ 사용
- `assertThat()` 사용
- 명확한 에러 메시지 제공

```java
assertThat(result.getTotalPrice()).isEqualTo(10000);
assertThat(result.getOrderLines()).hasSize(2);
assertThat(result.getStatus()).isEqualTo(OrderStatus.ORDER_COMPLETE);
```

### 8. @DisplayName 필수
- 한글로 명확한 테스트 의도 작성
- "~할 때 ~하면 ~한다" 형식 권장

```java
@DisplayName("쿠폰이 만료되었을 때 발급 시도하면 예외가 발생한다")
@DisplayName("결제 금액이 주문 금액과 일치하지 않으면 결제 실패 처리한다")
@DisplayName("주문 취소 가능한 상태에서 취소 요청하면 재고가 복구된다")
```

### 9. JUnit 5 사용
- **@ExtendWith(MockitoExtension.class)** 사용
- **@Test**, **@BeforeEach**, **@AfterEach**
- **assertThrows**, **assertAll** 활용

### 10. Repository 테스트 (필요 시)
- **@DataJpaTest** 사용
- 실제 DB 쿼리 검증
- **@Transactional** 자동 롤백

```java
@DataJpaTest
class OrderRepositoryTest {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("주문 번호로 주문 조회")
    void findByOrderNumber_Success() {
        // given
        Order order = Order.builder().orderNumber("ORD123").build();
        em.persist(order);
        em.flush();

        // when
        Optional<Order> result = orderRepository.findByOrderNumber("ORD123");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getOrderNumber()).isEqualTo("ORD123");
    }
}
```

## 테스트 커버리지 목표
- **Line Coverage**: 80% 이상
- **Branch Coverage**: 70% 이상
- **핵심 비즈니스 로직**: 100%

## 테스트 실행
```bash
# 전체 테스트
./gradlew test

# 특정 테스트
./gradlew test --tests OrderServiceTest

# 커버리지 리포트
./gradlew test jacocoTestReport
```

---

## 주의사항
- 테스트는 **독립적**이어야 함 (다른 테스트에 영향 X)
- 테스트는 **반복 가능**해야 함 (항상 같은 결과)
- 테스트는 **빠르게** 실행되어야 함
- **실제 외부 API 호출 금지** (Mock 사용)
- **실제 Redis/DB는 테스트 전용 환경** 사용