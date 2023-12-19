## 등록/수정/조회 API 만들기

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

```java
@Transactional
public Order cancelOrder(int orderId){
    // 1
    ordersDto order = ordersDao.selectOrders(orderId);
    BillingDto billing = billingDao.selectBilling(orderId);
    DeliveryDto delivery = deliveryDao.selectDelivery(orderid);
    
    // 2
    String deliveryStatus = delivery.getStatuse();
    
    // 3
    if("IN_PROGRESS".equals(deliveryStatus)){
        delivery.setStatus("CANCEL");
        deliveryDao.update(delivery);
    }
    
    // 4
    order.setStatus("CANCEL");
    ordersDao.update(order);
    
    billing.setStatus("CANCEL");
    deliveryDao.update(billing);
    
    return order;
}

```

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
&rarr; WebEnvironment.RANDOM_PORT 로 인한 랜덤 포트 실행과 insert 쿼리가 실행된 것 모두 확인

### JPA Auditing 으로 생성시간/수정시간 자동화

보통 `Entity`에 해당 데이터의 생성시간과 수정시간을 포함합니다. <br>
&rarr; 언제 만들어졌는지, 언제 수정되었는지 등은 차후 유지보수에 있어 굉장히 중요한 정보 <br>

따라서 매번 DB insert/update 전에 날짜 데이터를 등록/수정하는 코드가 다음과 같이 반복하여 들어가게 됩니다.

```java
public void savePosts() {
  ...
  posts.setCreateDate(new LocalDate());
  postsRepository.save(posts);
  ...
}
```

이와 같은 보일러 플레이트 코드를 `JPA Auditing`을 통해 해결할 수 있습니다.

### LocalDate 사용

**다음의 domain 패키지에 생성한 BaseTimeEntity 클래스는 모든 Entity의 상위 클래스로 <br> 
Entity들의 createDate, modifiedDate를 자동으로 관리하는 역할입니다.**

```java
@Getter
@MappedSuperclass                               // createDate, ModifiedDate 필드들도 칼럼으로 인식
@EntityListeners(AuditingEntityListener.class)  // Auditing 기능을 포함
public class BaseTimeEntity {
    @CreatedDate                                // Entity 생성 후 저장될 떄 시간이 자동 저장
    private LocalDateTime createdDate;

    @LastModifiedDate                           // 조회한 Entity 값을 변경할 때 시간 자동 저장
    private LocalDateTime modifiedDate;
}
```

## 게시글 등록하기

```js
var main = {
init: function () {
var _this = this;
$('#btn-save').on('click', function () {
_this.save();
});
},
save : function () {
var data = {
title: $('#title').val(),
author: $('#author').val(),
content: $('#content').val()
};

        $.ajax({
            type: 'POST',
            url: '/api/v1/posts',
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(data)
        }).done(function () {
            alert('글이 등록되었습니다.');
            window.location.href= '/';
        }).fail(function (error) {
            alert(JSON.stringify(error));
        });
    }
};

main.init();
```

다음은 등록 버튼에서 API 호출하는 JS 입니다. <br>

브라우저의 스코프는 **공용 공간**으로 쓰이기 때문에 나중에 로딩된 js의 `init`, `save`가 먼저 로딩된 js의 function 을 덮어쓰게 됩니다. <br>

여러 사람이 참여하는 프로젝트에서 `중복된 함수 이름`은 자주 발생할 수 있습니다. <br>
모든 function 이름을 확인하면서 만들 수는 없으므로 index.js 만의 유효범위를 만들어 사용해야 합니다. <br>

방법은 `var index`란 객체를 만들어 해당 객체에 필요한 모든 function 을 선언하는 것입니다. <br>
이렇게 하면 **index 객체 안에서만 function이 유효하므로 다른 js와 겹칠 위험이 사라집니다.**

> footer.mustache 에 index.js 호출 코드를 보면 `절대 경로(/)`로 바로 시작합니다.

* src/main/resources/static/js/...
* src/main/resources/static/css/...
* src/main/resources/static/image/...

> 스프링 부트는 기본적으로 위와 같이 `src/main/resources/static`에 위치한 정적 파일들은 URL에서 `/`로 설정됩니다.

<br>

```md
규모가 있는 프로젝트에서의 데이터 조회는 FK의 조인, 복잡한 조건 등으로 인해 이런 Entity 만으로 처리하기 어려워
조회용 프레임워크를 추가로 사용합니다. 대표적 예로 querydsl, jooq, Mybatis 등이 있습니다.
이 중 querydsl을 추천합니다.
1. 타입 안정성이 보장됩니다.
2. 국내 많은 기업이 사용중입니다.
3. 레퍼런스가 다양합니다.
```

#### PostsService

```java
@Service
public class PostsService {
    private final PostsRepository postsRepository;
    
    ...
    
    @Transactional(readOnly=true)
    public List<PostsListResponseDto> findAllDesc() {
        return postsRepository.findAllDesc().stream()
                .map(PostsListResponseDto::new)
                // .map(posts -> new PostsListResponseDto(posts))
                .collect(Collectors.toList());
    }
}
```
`findAllDesc`메소드의 트랜잭션 어노테이션(@Transactional)에 옵션이 하나 추가되었습니다. <br>

`readOnly=true` 를 주면 **트랜잭션 범위는 유지**하되, 조회 기능만 남겨두어 **조회 속도가 개선**되기 때문에 <br>
등록, 수정, 삭제 기능이 전혀 없는 서비스 메소드에서 사용하는 것을 추천합니다.

findAllDesc 메소드의 람다식을 해석하면 다음과 같습니다.

> postsRepository 결과로 넘어온 Posts의 Stream을 map을 통해 <br>
> PostsListResponseDto 변환 &rarr; List로 반환하는 메소드 입니다.




