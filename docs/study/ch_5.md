## OAuth 2.0 로그인 기능 구현하기

### 구글 서비스 등록
```java
spring.security.oauth2.client.registration.google.client-id={Client ID}
spring.security.oauth2.client.registration.google.client-secret={Client Secret}
spring.security.oauth2.client.registration.google.scope=profile,email
```

구글 로그인 기능에서 `scope`은 기본 값이 `openid, profile, email` 입니다. <br>
강제로 `profile, email`로 등록한 이유는 openid 라는 scope가 있으면 Open Id Provider로 인식하기 때문입니다. <br>
이렇게 되면 OpenId Provider인 서비스(구글)와 그렇지 않은 서비스(네이버/카카오 etc)로 나누어 OAuth2Service를 만들어야 합니다. <br>
&rarr; `하나의 OAuth2Service로 사용하기 위해 일부러 openid scope를 빼고 등록합니다.`

```java
@Getter
public class SessionUser implements Serializable {
private String name;
private String email;
private String picture;

    public SessionUser(User user){
        this.name = user.getName();
        this.email = user.getEmail();
        this.picture = user.getPicture();
    }
}
```
<details>
<summary>User 클래스를 쓰지 않고 새로 만들어서 사용하는 이유는?</summary>

만약 User 클래스를 그래도 사용하면 다음의 에러가 발생합니다.

```
Failed to convert from type [java.lang.Object] to type [byte[]] for value
'com.nerocoding.springboot.domain.user.User@4a43d6'
```
이는 세션에 저장하기 위해 User 클래스를 세션에 저장하려고 하니, `User 클래스에 직렬화를 구현하지 않았다`는 의미 입니다. <br>
그렇다면 User 클래스에 직렬화 코드를 넣으면 될까요? 그것에 대하여 생각할 것이 많습니다. <br>
&rarr; 이유는 User 클래스가 `Entity` 이기 때문이기 떄문입니다. <br>하
엔티티 클래스는 언제 다른 엔티티와 관계가 형성될지 모릅니다. <br>
예를 들어 @OneToMany, @ManyToMany 등 자식 엔티티를 갖고 있다면 직렬화 대상에 자식까지 포함되니 <br>
**성능 저하, 부수 효과**가 발생할 확률이 높습니다. <br>
&rarr; 그래서 `직렬화 기능을 가진 세션 DTO`를 하나 추가로 만드는 것이 이후 운영 및 유지보수에 많은 도움이 됩니다. <br>

</details>

### 어노테이션 기반 개선하기

보일러 플레이트 코드를 최소화하는 방법 중 어노테이션을 사용한 방법이 있습니다. <br>
여기서는 `IndexController`의 **세션 값을 가져오는 부분**을 개선해볼 수 있습니다.
```java
@GetMapping("/")
public String index(Model model, @LoginUser SessionUser user) {
    model.addAttribute("posts", postsService.findAllDesc());
    // CustomOAuth2UserService 로그인 성공 시 세션에 SessionUser 를 저장하도록 구성 -> @LoginUser Refactoring
    // SessionUser user = (SessionUser) httpSession.getAttribute("user");
    // 세션 저장 값이 있을 때에만 model 에 userName 으로 등록
    if(user != null) {
        model.addAttribute("userName", user.getName());
    }
    return "index";
}
```
만약 index 메소드 외 다른 컨트롤러와 메소드에서 세션 값이 필요하면 그때마다 직접 세션에서 값을
가져와야 합니다.<br> 
&rarr; 이 부분을 **메소드 인자로 세션 값을 바로 받을 수 있도록** 변경할 수 있습니다.

### 세션 저장소로 데이터베이스 사용하기

기본적으로 세션은 실행되는 `WAS (Web Application Server)`의 메모리에 저장되고 호출됩니다. <br>

메모리에 저장되다 보니 내장 톰캣처럼 애플리케이션 실행 시 실행되는 구조에서는 항상 초기화가 됩니다. 즉, 배포할 때마다 톰캣이 재시작 되는 것 입니다. <br>
2대 이상의 서버에서 서비스하고 있다면 **톰캣마다 세션 동기화**설정을 해야만 합니다. 그래서 기본적으로 현업에서는 세션 저장소에 대해 다음 세가지 중 한가지를 선택합니다. <br>

1. 톰캣 세션을 사용한다. <br>
톰캣 (WAS)에 세션이 저장되기 때문에 2대 이상의 WAS가 구동되는 환경에서는 톰캣들 간의 세션 공유를 위한 추가 설정이 필요 <br><br>

2. MySQL과 같은 데이터베이스를 세션 저장소로 사용한다. <br>
여러 WAS 간 공용 세션을 사용할 수 있는 가장 쉬운 방법 <br>
`많은 설정이 필요 없지만 결국 로그인 요청마다 DB IO 가 발생하여 성능상 이슈가 발생할 수 있습니다.` <br><br>
 
3. Redis, Memcached와 같은 메모리 DB를 세션 저장소로 사용한다.
B2C 서비스에서 가장 많이 사용하는 방식입니다. <br>
실제 서비스로 사용하기 위해 Embedded Redis 같은 방식이 아닌 외부 메모리 서버가 필요 <br><br>

> Redis 같은 서비스(엘라스틱 캐시)는 별도 사용료를 지불해야 하므로 현재 상황에서 적절한 기술이 아닙니다.

