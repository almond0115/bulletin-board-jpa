## 로그인 기능 구현

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
&rarr; 이유는 User 클래스가 `Entity` 이기 때문이기 떄문입니다. <br>
엔티티 클래스는 언제 다른 엔티티와 관계가 형성될지 모릅니다. <br>
예를 들어 @OneToMany, @ManyToMany 등 자식 엔티티를 갖고 있다면 직렬화 대상에 자식까지 포함되니 <br>
**성능 저하, 부수 효과**가 발생할 확률이 높습니다. <br>
&rarr; 그래서 `직렬화 기능을 가진 세션 DTO`를 하나 추가로 만드는 것이 이후 운영 및 유지보수에 많은 도움이 됩니다. <br>

</details>