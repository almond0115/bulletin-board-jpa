### 프로젝트에 Spring Data Jpa 적용하기

어노테이션 순서를 **주요 어노테이션을 클래스에 가깝게** 둡니다. <br>
&rarr; `@Entity`는 JPA의 어노테이션이며 `@Getter`과 `@NoArgsConstructor`는 롬복의 어노테이션입니다.<br>

```
롬복은 코드를 단순화 시켜주지만 필수 어노테이션은 아닙니다.
그러다 보니 주요 어노테이션인 @Entity를 클래스에 가까에 두고, 롬복 어노테이션을 위로 두었습니다.
이렇게 하면 이후에 코틀린 등 새 언어 전환으로 롬복이 더 이상 필요 없을 경우 쉽게 삭제할 수 있습니다.
```

<br>

```angular2html
웬만하면 Entity의 PK는 Long 타입의 Auto_increment를 추천합니다. (MySQL 기준 이렇게 하면 bigint 타입)
```

(1) FK를 맺을 때 다른 테이블에서 복합키를 전부 갖고 있거나, 중간 테이블을 하나 더 두어야 하는 상황 발생 <br>
(2) 인덱스에 좋은 영향 끼치지 못함 <br>
(3) 유니크한 조건이 변경될 경우 PK 전체를 수정해야 하는 일 발생 <br>

&rarr; 주민등록번호나 복합키 등은 유니크 키로 별도 추가하는 것을 추천합니다.

<br>

```angular2html
Java Bean 규약을 생각하면서 getter/setter 생성을 무작정 하는 경우가 있습니다.
이렇게 되면 해당 클래스 인스턴스 값들이 언제 어디서 변해야 하는지 코드상으로 명확하게 구분할 수 없어,
차후 기능 변경 시 정말 복잡해 집니다.
```
&rarr; 그래서 Entity 클래스에서는 절대 Setter 메소드를 만들 지 않습니다.

<details>
<summary> &nbsp; <b>Setter 가 없는 이 상황에서 어떻게 값을 채워 DB에 삽입해야 할까요? </b> </summary>
기본적인 구조는 생성자를 통해 최종 값을 채운 후 DB 삽입하는 것입니다. <br>
값 변경이 필요한 경우 해당 이벤트에 맞는 public 메소드를 호출하여 변경하는 것을 전제로 합니다.
</details>

### Builder

이 책에서는 생성자 대신 `@Builder`를 통해 제공되는 빌더 클래스를 사용합니다. <br>

예로 다음 같은 생성자가 있다면 개발자가 new Example(b, a)처럼 매개변수 위치를 변경하여도 코드 실행 전까지 문제를 찾을 수 없습니다. <br>

```java
public Example(String a, String b){
    this.a = a;
    this.b = b;
}
```

하지만 빌더를 사용하게 되면 다음과 같이 어느 필드에 어떤 값을 채워야 할 지 명확하게 인지할 수 있습니다. <br>
```java
Example.builder()
    .a(a)
    .b(b)
    .build()
```

<details>
<summary> &nbsp; <b> 실제로 실행된 쿼리는 어떤 형태일까? </b> </summary>
실행된 쿼리 로그를 ON/OFF 할 수 있는 설정이 있습니다. <br>
다만, 이런 설정들을 Java 클래스로 구현할 수 있으나 스프링 부트에서는 application.properties, application.yml 등의 <br>
파일로 한 줄의 코드로 설정할 수 있도록 지원하고 권장합니다.
</details>
