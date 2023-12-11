### 등록/수정/조회 API 만들기

* Request 데이터를 받을 Dto
* API 요청을 받을 Controller
* 트랜잭션, 도메인 기능 간의 순서를 보장하는 Service

### Service에서 비즈니스 로직을 처리해야 하는 걸까?

```
Service는 트랜잭션, 도메인 간 순서 보장의 역할만 합니다.
```

&rarr; &nbsp; 그렇다면 비즈니스 로직 처리는 어디서 처리할까?

* Web Layer
  * 흔히 사용하는 컨트롤러 (@Controller) 와 JSP/Freemarker 등 뷰 템플릿 영역입니다.
  * 이외에도 필터 (@Filter), 인터셉터, 컨트롤러 어드바이스(@ControllerAdvice) 등 외부 요청과 응답에 대한 전반적인 영역을 이야기 합니다.


* Service Layer
  * @Service에 사용되는 서비스 영역입니다.
  * 일반적으로 Controller와 DAO 중간 영역에서 사용됩니다.
  * @Transaction이 사용되어야 하는 영역이기도 합니다.


* Repository Layer
  * Database와 같이 데이터 저장소에 접근하는 영역입니다.
  * DAO 영역으로 이해하시면 쉬울 것입니다.


* Dtos
  * Dto는 계층 간에 데이터 교환을 위한 객체를 이야기 하며 Dtos는 이들의 영역을 이야기합니다.
  * 예로 뷰 템플릿 엔진에서 사용될 객체나 Repository Layer에서 결과로 넘겨준 객체 등입니다.


* Domain Model
  * 도메인이라 불리는 개발 대상을 모든 사람이 동일한 관점에서 이해할 수 있고 공유할 수 있도록 <br> 단순화 시킨 것을 도메인 모델이라고 합니다.
  * 이를테면 택시 앱이라고 하면 배차,탑승,요금 등이 모두 도메인이 될 수 있습니다.
  * @Entity를 사용해보신 분들은 @Entity가 사용된 영역 역시 도메인 모델이라고 이해해주시면 됩니다.
  * 다만, 무조건 데이터베이스의 테이블과 관계가 있어야만 하는 것은 아닙니다.
  * VO처럼 값 객체들도 이 영역에 해당하기 때문입니다.

### 이 5가지 레이어에서 비즈니스에서 비즈니스 처리를 담당해야 할 곳은 어디일까요?

```
비즈니스 로직 처리를 담당해야 할 곳은 Domain 입니다. 
```

기존 서비스로 처리하던 방식을 `트랜잭션 스크립트` 라고 합니다. <br>
모든 로직이 **서비스 클래스 내부에서 처리됩니다.** <br>
그러다 보니 **서비스 계층이 무의미하며, 객체란 단순히 데이터 덩어리** 역할만 하게 됩니다. <br>

#### 반면 도메인 모델에서 처리할 경우 다음과 같은 코드가 될 수 있습니다.

```java
@Transactional
public Order cancelOrder(int orderId){
    
    Orders order = orderRepository.findById(orderId);
    Billing billing = billingRepository.findByOrderId(orderId);
    Delivery delivery = deliveryRepository.findByOrderId(orderId);
    
    delivery.cancel();
    order.cancel();
    billing.cancel();
    
    return order;
}
```

트랜잭션과 도메인 간의 순서만 보장해 줍니다. <br>


**Controller와 Service에서 @Autowired가 없는 것이 어색하게 느껴집니다.** <br>

> 스프링에서는 Bean을 주입받는 방식들이 다음과 같습니다.

* Autowired
* setter
* 생성자

```
이 중 가장 권장하는 방식이 생성자로 주입받는 방식입니다.
즉, 생성자로 Bean 객체를 받도록 하면 @Autowired 와 동일한 효과를 볼 수 있다는 것입니다.
그렇다면 앞에서 생성자는 어디 있을까요?
```

### `@RequiredArgsConstructor` 에서 해결해 줍니다.

롬복 어노테이션이 있으면 해당 컨트롤러에 새로운 서비스를 추가하거나, 기존 컴포넌트를 제거하는 등의
상황이 발생해도 생성자 코드는 전혀 손대지 않아도 됩니다.

```java
@Getter
@NoArgsConstructor
public class PostsSaveRequestDto {
  private String title;
  private String content;
  private String author;

  @Builder
  public PostsSaveRequestDto(String title, String content, String author){
    this.title = title;
    this.content = content;
    this.author = author;
  }

  public Posts toEntity() {
    return Posts.builder()
            .title(title)
            .content(content)
            .author(author)
            .build();
  }
}
```

**절대로 Entity 클래스를 Request/Response 클래스로 사용해서는 안 됩니다.**

**Entity 클래스는 데이터베이스와 맞닿은 핵심 클래스 입니다.**

**수많은 서비스 클래스나 비즈니스 로직들이 Entity 클래스 기준으로 동작합니다.**

**Entity 클래스가 변경되면 여러 클래스에 영향을 끼치지만, <br> 
Request와 Response용 Dto는 View를 위한 클래스라 정말 자주 변경이 필요합니다.**

`View Layer와 DB Layer 역할 분리를 철저하게 하는 게 좋습니다.`

실제 Controller 에서 결괏값으로 여러 테이블을 조인해서 줘야 할 경우가 빈번하므로 Entity 클래스만으로 표현하기 어려운 경우가 많습니다.

```
꼭 Entity 클래스와 Controller에서 쓸 Dto는 분리해서 사용해야 합니다.
```

### API Controller 테스트 

API Controller 테스트에 `@WebMvbTest` 를 사용하지 않는 이유는 JPA 기능이 작동하지 않기 때문입니다. <br>

```
Controller와 ControllerAdvice 등 외부 연동 관련된 부분만 활성화되니 <br>
지금 같이 JPA 기능까지 한번에 테스트 할 때는 @SpringBootTest와 TestRestTemplate을 사용하면 됩니다.
```
&rarr; WebEnvironment.RANDOM_PORT 로 인한 랜덤 포트 실행과 insert 쿼리가 실행된 것 모두 확인했습니다.




